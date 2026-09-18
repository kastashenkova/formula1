package org.example.service.user;

import org.example.dto.user.registration.UserRegistrationRequestDto;
import org.example.dto.user.registration.UserRegistrationResponseDto;
import org.example.entity.UserEntity;
import org.example.event.UserRegisteredEvent;
import org.example.exception.DuplicateUserException;
import org.example.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public UserServiceImpl(UserRepository userRepository, ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public UserRegistrationResponseDto addUser(UserRegistrationRequestDto requestDto) {
        if (userRepository.findByEmail(requestDto.email()).isPresent()) {
            throw new DuplicateUserException("User with email "
                    + requestDto.email() + " already exists");
        }

        if (userRepository.findByPhoneNumber(requestDto.phoneNumber()).isPresent()) {
            throw new DuplicateUserException("User with phone number "
                    + requestDto.phoneNumber() + " already exists");
        }

        Long id = (requestDto.id() != null)
                ? requestDto.id()
                : Math.abs(new java.util.Random().nextLong());

        if (userRepository.findById(id).isPresent()) {
            throw new DuplicateUserException("User with id " + id + " already exists");
        }

        UserEntity newUser = new UserEntity(
                id,
                requestDto.email(),
                requestDto.phoneNumber(),
                requestDto.role(),
                requestDto.password()
        );

        UserEntity savedUser = userRepository.save(newUser);

        eventPublisher.publishEvent(new UserRegisteredEvent(
                savedUser.id(),
                savedUser.email(),
                savedUser.phoneNumber(),
                savedUser.role()
        ));

        log.info("Registered user {}", savedUser.id());

        return mapToResponse(savedUser);
    }

    private UserRegistrationResponseDto mapToResponse(UserEntity entity) {
        return new UserRegistrationResponseDto(
                entity.id(),
                entity.email(),
                entity.phoneNumber(),
                entity.role()
        );
    }
}
