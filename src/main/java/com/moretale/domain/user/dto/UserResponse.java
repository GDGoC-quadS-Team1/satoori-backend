package com.moretale.domain.user.dto;

import com.moretale.domain.user.entity.User;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long userId;
    private String email;
    private String nickname;
    private String region;
    private String role;
    private LocalDateTime createdAt;

    public static UserResponse fromEntity(User user) {
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
