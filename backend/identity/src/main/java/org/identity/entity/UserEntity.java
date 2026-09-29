package org.identity.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

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
    @Column(nullable = false, length = 5)
    String role;
    @Column(nullable = false, length = 60)
    String password;
    @Column(nullable = false, length = 20)
    String userStatus;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<WebhookEntity> webhooks = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    List<VerificationToken> verificationTokens = new ArrayList<>();

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

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }

    public List<VerificationToken> getVerificationTokens() {
        return verificationTokens;
    }

    public void setVerificationTokens(List<VerificationToken> verificationTokens) {
        this.verificationTokens = verificationTokens;
    }

    public void removeVerificationToken(VerificationToken token) {
        verificationTokens.remove(token);
        token.setUser(null);
    }

    public Set<WebhookEntity> getWebhooks() {
        return webhooks;
    }

    public void setWebhooks(Set<WebhookEntity> webhooks) {
        this.webhooks = webhooks;
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
