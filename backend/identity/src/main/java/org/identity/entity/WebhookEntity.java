package org.identity.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@Table(name = "webhooks")
public class WebhookEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private String webhookUrl;

// uncomment when UserEntity is on master
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "user_id", nullable = false)
//    private UserEntity user;

    @Column(nullable = false)
    private String webhookType;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

}
