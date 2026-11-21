package org.identity.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;
    @Column(nullable = false, unique = true, length = 254)
    String email;
    @Column(nullable = false, length = 13)
    String phoneNumber;
    @Column(nullable = false, length = 50)
    String role;
    @Column(nullable = false)
    String password;
    @Column(nullable = false, length = 50)
    String userStatus;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<WebhookEntity> webhooks = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    List<VerificationToken> verificationTokens = new ArrayList<>();

    @Column(nullable = false)
    boolean verificationReminderSent = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt = LocalDateTime.now(ZoneId.of("Europe/Kyiv"));

    protected UserEntity() {
    }

    public UserEntity(String email,
                      String phoneNumber,
                      String role,
                      String password,
                      String userStatus) {
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.password = password;
        this.userStatus = userStatus;
    }

    public UserEntity(UUID id,
                      String email,
                      String phoneNumber,
                      String role,
                      String password,
                      String userStatus) {
        this.id = id;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.password = password;
        this.userStatus = userStatus;
    }

    public void removeVerificationToken(VerificationToken token) {
        verificationTokens.remove(token);
        token.setUser(null);
    }

    public void addWebhook(WebhookEntity webhook) {
        webhooks.add(webhook);
        webhook.setUser(this);
    }

    public void removeWebhook(WebhookEntity webhook) {
        webhooks.remove(webhook);
        webhook.setUser(null);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UserEntity that = (UserEntity) o;
        return Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
}
