package org.identity.security;

import java.util.Objects;
import java.util.UUID;
import org.audit.AuditUserIdProvider;
import org.identity.entity.UserEntity;
import org.identity.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuditUserIdProviderImpl implements AuditUserIdProvider {

    private final UserRepository userRepository;

    public AuditUserIdProviderImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()
                && !Objects.equals(authentication.getPrincipal(), "anonymousUser")) {

            return userRepository.findByEmail(authentication.getName())
                    .map(UserEntity::getId)
                    .orElse(null);
        }
        return null;
    }
}
