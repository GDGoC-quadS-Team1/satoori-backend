package com.MoreTale.domain.user.dto;

import com.MoreTale.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class UserResponse {
    private Long userId; // 사용자 고유 ID
    private String email; // 이메일 (OAuth 계정)
    private String nickname; // 사용자 닉네임
    private String region; // 사용자 지역 정보
    private String role; // 사용자 권한 (USER / ADMIN)
    private LocalDateTime createdAt; // 가입 시각

    // User 엔티티를 UserResponse DTO로 변환
    public static UserResponse from(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .region(user.getRegion())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .build();
    }
}