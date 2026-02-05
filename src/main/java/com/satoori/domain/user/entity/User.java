package com.satoori.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

// 사용자 정보 저장 User 엔티티
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

// Spring Security UserDetails 구현
public class User implements UserDetails {

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

    // UserDetails 구현 메서드
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 권한 정보를 Spring Security가 인식할 수 있는 SimpleGrantedAuthority로 변환 ('ROLE_USER', 'ROLE_ADMIN' 형태)
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // 사용자 권한 enum
    public enum Role {
        USER, ADMIN
    }
}
