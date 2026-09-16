package org.example.service.user;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.example.dto.user.registration.UserRegistrationRequestDto;
import org.example.dto.user.registration.UserRegistrationResponseDto;
import org.example.entity.UserEntity;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    private final List<UserEntity> userEntities = new CopyOnWriteArrayList<>();

    @Override
    public UserRegistrationResponseDto addUser(UserRegistrationRequestDto requestDto) {
        Long id = (requestDto.id() != null)
                ? requestDto.id()
                : Math.abs(new java.util.Random().nextLong());

        if (id.describeConstable().isEmpty()) {
            throw new IllegalArgumentException("User ID must not be empty");
        }

        UserEntity newUser = new UserEntity(
                id,
                requestDto.email(),
                requestDto.role(),
                requestDto.password()
        );

        userEntities.add(newUser);
        return mapToResponse(newUser);
    }

    private UserRegistrationResponseDto mapToResponse(UserEntity entity) {
        return new UserRegistrationResponseDto(
                entity.id(),
                entity.email(),
                entity.role()
        );
    }
}
