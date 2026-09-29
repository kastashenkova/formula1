package org.identity.service.internal;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.identity.command.UpdateUserStatusCommand;
import org.identity.dto.UserResponseDto;
import org.identity.dto.UserRegistrationRequestDto;
import org.identity.entity.UserEntity;
import org.identity.entity.VerificationToken;
import org.identity.enums.TokenType;
import org.identity.enums.UserStatus;
import org.identity.dto.UserRegisteredEvent;
import org.identity.exception.DuplicateUserException;
import org.identity.exception.InvalidTokenException;
import org.identity.exception.InvalidUserStateException;
import org.identity.exception.InvalidVerificationStrategyException;
import org.identity.repository.TokenRepository;
import org.identity.repository.UserRepository;
import org.identity.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
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
                           UserRepository userRepository,
                           TokenRepository tokenRepository,
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
        if (userRepository.existsByEmail(requestDto.email())) {
            String message = String.format("User with email %s already exists", requestDto.email());
            throw new DuplicateUserException(message);
        }

        if (userRepository.existsByPhoneNumber(requestDto.phoneNumber())) {
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
                UserStatus.PENDING_VERIFICATION.toString()
        );

        UserEntity savedUser = userRepository.save(newUser);

        VerificationStrategy emailStrategy = strategyMap.get(TokenType.EMAIL_VERIFICATION.name());
        VerificationStrategy phoneStrategy = strategyMap.get(TokenType.PHONE_VERIFICATION.name());

        if (emailStrategy == null || phoneStrategy == null) {
            throw new InvalidVerificationStrategyException(
                    "Verification strategies are not properly configured");
        }

        VerificationToken emailToken = emailStrategy.createVerificationToken(savedUser);
        VerificationToken phoneToken = phoneStrategy.createVerificationToken(savedUser);

        VerificationToken savedEmailToken = tokenRepository.save(emailToken);
        VerificationToken savedPhoneToken = tokenRepository.save(phoneToken);

        eventPublisher.publishEvent(new UserRegisteredEvent(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getPhoneNumber(),
                savedUser.getRole(),
                savedUser.getUserStatus(),
                savedEmailToken.getToken(),
                savedPhoneToken.getToken()
        ));

        log.info("Registered user {}", savedUser.getId());

        return UserResponseDto.fromEntity(savedUser);
    }

    @Override
    @Transactional
    public UserResponseDto updateStatus(UUID id, UpdateUserStatusCommand command) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !Objects.equals(authentication.getPrincipal(), "anonymousUser")) {
            String currentEmail = authentication.getName();
            UserEntity currentUser = userRepository.findByEmail(currentEmail).orElse(null);
            if (currentUser != null && currentUser.getId().equals(id)) {
                throw new AccessDeniedException("You cannot change your own status");
            }
        }

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "User with ID '" + id + "' not found"));

        UserStatus currentStatus = UserStatus.valueOf(user.getUserStatus());
        UserStatus targetStatus = command.userStatus();

        if (!currentStatus.canTransitionTo(targetStatus)) {
            String message = String.format("Illegal state transition for user '%s' from %s to %s",
                    id,  currentStatus, targetStatus);
            throw new InvalidUserStateException(message);
        }

        user.setUserStatus(targetStatus.toString());

        UserEntity updatedUser = userRepository.save(user);

        log.info("Updated status for user {} to {}", id, targetStatus);

        return UserResponseDto.fromEntity(updatedUser);
    }

    @Override
    @Transactional
    public void confirmByToken(String token) {
        VerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid token"));

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            String message = String.format("Token %s has expired", token);
            throw new InvalidTokenException(message);
        }

        UserEntity user = userRepository.findById(verificationToken.getUser().getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        VerificationStrategy strategy = strategyMap.get(verificationToken.getTokenType());
        if (strategy == null) {
            String message = String.format("Verification strategy for token %s not found",
                    verificationToken.getTokenType());
            throw new InvalidVerificationStrategyException(message);
        }

        UserStatus currentStatus = UserStatus.valueOf(verificationToken.getUser().getUserStatus());
        UserStatus nextStatus = strategy.getNextStatus(currentStatus);

        if (!currentStatus.canTransitionTo(nextStatus)) {
            String message = String.format("Illegal transition from %s to %s",
                    user.getUserStatus(), nextStatus);
            throw new InvalidUserStateException(message);
        }

        user.setUserStatus(nextStatus.toString());
        userRepository.save(user);
        tokenRepository.delete(verificationToken);

        log.info("Token confirmed. User status {} transitioned to {}", user.getId(), nextStatus);
    }
}
