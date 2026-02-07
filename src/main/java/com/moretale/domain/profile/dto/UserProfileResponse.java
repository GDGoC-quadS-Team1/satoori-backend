package com.moretale.domain.profile.dto;

import com.moretale.domain.profile.entity.UserProfile;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {

    private Long profileId;      // 프로필 고유 ID
    private Long userId;         // 부모(User) ID
    private String nickname;
    private String childName;
    private Integer childAge;
    private String childNationality;
    private String parentCountry;
    private String primaryLanguage;
    private String secondaryLanguage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserProfileResponse fromEntity(UserProfile profile) {
        return UserProfileResponse.builder()
                .profileId(profile.getProfileId())
                .userId(profile.getUser().getUserId())
                .nickname(profile.getUser().getNickname())
                .childName(profile.getChildName())
                .childAge(profile.getChildAge())
                .childNationality(profile.getChildNationality())
                .parentCountry(profile.getParentCountry())
                .primaryLanguage(profile.getPrimaryLanguage())
                .secondaryLanguage(profile.getSecondaryLanguage())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
