package com.satoori.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

// 사용자 정보 저장 User 엔티티
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    // 사용자 고유 ID (PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    // 사용자 이메일 (OAuth 로그인 기준, 중복X)
    @Column(nullable = false, unique = true)
    private String email;

    // 사용자 닉네임
    @Column(nullable = false)
    private String nickname;

    // 사용자 지역 정보 (방언 추천, 통계 등에 사용)
    private String region;

    // 사용자 권한
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // 계정 생성 시각
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // OAuth2 제공자 정보
    @Column(name = "provider")
    private String provider;

    // OAuth2 사용자 고유 ID
    @Column(name = "provider_id")
    private String providerId;

    // 사용자 권한 enum
    public enum Role {
        USER, ADMIN
    }
}