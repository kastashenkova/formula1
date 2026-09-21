package org.example.service.user.management;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.example.command.UpdateUserStatusCommand;
import org.example.dto.user.UserResponseDto;
import org.example.dto.user.UserRegistrationRequestDto;
import org.example.entity.UserEntity;
import org.example.entity.VerificationToken;
import org.example.enums.TokenType;
import org.example.enums.UserStatus;
import org.example.event.UserRegisteredEvent;
import org.example.exception.DuplicateUserException;
import org.example.exception.InvalidUserStateException;
import org.example.repository.token.TokenRepository;
import org.example.repository.user.UserRepository;
import org.example.service.user.verification_token.EmailVerificationTokenStrategy;
import org.example.service.user.verification_token.PhoneVerificationTokenStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
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

    private final Map<String, EmailVerificationTokenStrategy> emailTokenMap;
    private final Map<String, PhoneVerificationTokenStrategy> phoneTokenMap;

    public UserServiceImpl(PasswordEncoder passwordEncoder,
                           UserRepository userRepository, TokenRepository tokenRepository,
                           ApplicationEventPublisher eventPublisher,
                           List<EmailVerificationTokenStrategy> emailTokenStrategyList,
                           List <PhoneVerificationTokenStrategy> phoneTokenStrategyList) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.eventPublisher = eventPublisher;
        this.emailTokenMap = emailTokenStrategyList.stream()
                .collect(Collectors.toMap(
                        s -> s.getVerificationTokenType().toString(),
                        Function.identity()
                ));
        this.phoneTokenMap = phoneTokenStrategyList.stream()
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

        Long id = (requestDto.id() != null)
                ? requestDto.id()
                : Math.abs(new java.util.Random().nextLong());

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

        EmailVerificationTokenStrategy emailTokenStrategy
                = emailTokenMap.get(TokenType.EMAIL_VERIFICATION.toString());
        if (emailTokenStrategy == null) {
            String message = String.format("Email verification token for %s not created", requestDto.email());
            throw new InvalidUserStateException(message);
        }

        PhoneVerificationTokenStrategy phoneTokenStrategy
                = phoneTokenMap.get(TokenType.PHONE_VERIFICATION.toString());
        if (phoneTokenStrategy == null) {
            String message = String.format("Phone number verification token for %s not created", requestDto.phoneNumber());
            throw new InvalidUserStateException(message);
        }

        VerificationToken emailToken = emailTokenStrategy.createVerificationToken(id);
        VerificationToken phoneToken = phoneTokenStrategy.createVerificationToken(id);

        VerificationToken savedEmailToken = tokenRepository.save(emailToken);
        VerificationToken savedPhoneToken = tokenRepository.save(phoneToken);

        emailTokenStrategy.sendMessage(savedUser.email(), savedEmailToken);
        phoneTokenStrategy.sendMessage(savedUser.phoneNumber(), savedPhoneToken);

        eventPublisher.publishEvent(new UserRegisteredEvent(
                savedUser.id(),
                savedUser.email(),
                savedUser.phoneNumber(),
                savedUser.role(),
                savedUser.userStatus()
        ));

        log.info("Registered user {}", savedUser.id());

        return mapToResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponseDto updateStatus(Long id, UpdateUserStatusCommand command) {
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
