package org.example.service.user;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.example.command.UpdateUserStatusCommand;
import org.example.dto.user.UserResponseDto;
import org.example.dto.user.UserRegistrationRequestDto;
import org.example.entity.UserEntity;
import org.example.entity.VerificationToken;
import org.example.enums.TokenType;
import org.example.enums.UserStatus;
import org.example.dto.event.UserRegisteredEvent;
import org.example.exception.DuplicateUserException;
import org.example.exception.InvalidTokenException;
import org.example.exception.InvalidUserStateException;
import org.example.repository.token.TokenRepository;
import org.example.repository.user.UserRepository;
import org.example.service.verification.VerificationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final Map<String, VerificationStrategy> strategyMap;

    public UserServiceImpl(PasswordEncoder passwordEncoder,
                           UserRepository userRepository, TokenRepository tokenRepository,
                           ApplicationEventPublisher eventPublisher,
                           List<VerificationStrategy> strategyList) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.eventPublisher = eventPublisher;
        this.strategyMap = strategyList.stream()
                .collect(Collectors.toMap(
                        s -> s.getVerificationTokenType().toString(),
                        Function.identity()
                ));
    }

    @Override
    @Transactional
    public UserResponseDto addUser(UserRegistrationRequestDto requestDto) {
        if (userRepository.findByEmail(requestDto.email()).isPresent()) {
            String message = String.format("User with email %s already exists", requestDto.email());
            throw new DuplicateUserException(message);
        }

        if (userRepository.findByPhoneNumber(requestDto.phoneNumber()).isPresent()) {
            String message = String.format("User with phone number %s already exists", requestDto.phoneNumber());
            throw new DuplicateUserException(message);
        }

        UUID id = (requestDto.id() != null)
                ? requestDto.id()
                : UUID.randomUUID();

        if (userRepository.findById(id).isPresent()) {
            String message = String.format("User with id %s already exists", id);
            throw new DuplicateUserException(message);
        }

        UserEntity newUser = new UserEntity(
                id,
                requestDto.email(),
                requestDto.phoneNumber(),
                requestDto.role(),
                passwordEncoder.encode(requestDto.password()),
                UserStatus.PENDING_VERIFICATION
        );

        UserEntity savedUser = userRepository.save(newUser);

        VerificationStrategy emailStrategy = strategyMap.get(TokenType.EMAIL_VERIFICATION.name());
        VerificationStrategy phoneStrategy = strategyMap.get(TokenType.PHONE_VERIFICATION.name());

        if (emailStrategy == null || phoneStrategy == null) {
            throw new InvalidUserStateException("Verification strategies are not properly configured");
        }

        VerificationToken emailToken = emailStrategy.createVerificationToken(savedUser.id());
        VerificationToken phoneToken = phoneStrategy.createVerificationToken(savedUser.id());

        VerificationToken savedEmailToken = tokenRepository.save(emailToken);
        VerificationToken savedPhoneToken = tokenRepository.save(phoneToken);

        eventPublisher.publishEvent(new UserRegisteredEvent(
                savedUser.id(),
                savedUser.email(),
                savedUser.phoneNumber(),
                savedUser.role(),
                savedUser.userStatus(),
                savedEmailToken.token(),
                savedPhoneToken.token()
        ));

        log.info("Registered user {}", savedUser.id());

        return mapToResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponseDto updateStatus(UUID id, UpdateUserStatusCommand command) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !Objects.equals(authentication.getPrincipal(), "anonymousUser")) {
            String currentEmail = authentication.getName();
            UserEntity currentUser = userRepository.findByEmail(currentEmail).orElse(null);
            if (currentUser != null && currentUser.id().equals(id)) {
                throw new InvalidUserStateException("You cannot change your own status");
            }
        }

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "User with ID '" + id + "' not found"));

        UserStatus currentStatus = user.userStatus();
        UserStatus targetStatus = command.userStatus();

        if (!currentStatus.canTransitionTo(targetStatus)) {
            String message = String.format("Illegal state transition for user '%s' from %s to %s",
                    id,  currentStatus, targetStatus);
            throw new InvalidUserStateException(message);
        }

        UserEntity updatedData = new UserEntity(
                user.id(),
                user.email(),
                user.phoneNumber(),
                user.role(),
                user.password(),
                targetStatus
        );

        UserEntity updatedUser = userRepository.updateById(id, updatedData);

        log.info("Updated status for user {} to {}", id, targetStatus);

        return mapToResponse(updatedUser);
    }

    @Override
    @Transactional
    public void confirmByToken(String token) {
        VerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid token: " + token));

        if (verificationToken.expiryDate().isBefore(LocalDateTime.now())) {
            String message = String.format("Token %s has expired", token);
            throw new InvalidTokenException(message);
        }

        UserEntity user = userRepository.findById(verificationToken.userId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        VerificationStrategy strategy = strategyMap.get(verificationToken.tokenType().name());
        if (strategy == null) {
            String message = String.format("Verification token for %s not found", verificationToken.tokenType());
            throw new InvalidUserStateException(message);
        }

        UserStatus nextStatus = strategy.getNextStatus(user.userStatus());

        if (!user.userStatus().canTransitionTo(nextStatus)) {
            String message = String.format("Illegal transition from %s to %s", user.userStatus(), nextStatus);
            throw new InvalidUserStateException(message);
        }

        UserEntity updatedUser = new UserEntity(
                user.id(),
                user.email(),
                user.phoneNumber(),
                user.role(),
                user.password(),
                nextStatus
        );
        userRepository.updateById(user.id(), updatedUser);

        tokenRepository.delete(verificationToken);

        log.info("Token {} confirmed. User status {} transitioned to {}", token, user.id(), nextStatus);
    }

    private UserResponseDto mapToResponse(UserEntity entity) {
        return new UserResponseDto(
                entity.id(),
                entity.email(),
                entity.phoneNumber(),
                entity.role(),
                entity.userStatus()
        );
    }
}
