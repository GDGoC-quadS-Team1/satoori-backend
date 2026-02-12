package com.moretale.domain.profile.dto;

import com.moretale.domain.profile.dto.OnboardingProfileRequest.*;
import com.moretale.domain.profile.entity.UserProfile;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {

    private Long profileId;
    private Long userId;
    private String nickname;
    private String childName;
    private Integer childAge;

    // === 온보딩 상세 필드 추가 ===
    private AgeGroup ageGroup;

    // 언어 설정
    private String firstLanguage;
    private LanguageProficiency firstLanguageProficiency;
    private String secondLanguage;
    private LanguageProficiency secondLanguageProficiency;

    // 언어 능력 (듣기/말하기)
    private LanguageProficiency firstLanguageListening;
    private LanguageProficiency firstLanguageSpeaking;
    private LanguageProficiency secondLanguageListening;
    private LanguageProficiency secondLanguageSpeaking;

    // 가족 구조
    private FamilyStructure familyStructure;
    private String customFamilyStructure;

    // 이야기 선호도
    private StoryPreference storyPreference;
    private String customStoryPreference;

    private String childNationality;
    private String parentCountry;

    // 하위 호환성 필드 (Deprecated)
    @Deprecated
    private String primaryLanguage;

    @Deprecated
    private String secondaryLanguage;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserProfileResponse fromEntity(UserProfile profile) {
        if (profile == null || profile.getUser() == null) {
            throw new IllegalArgumentException("Profile or User cannot be null");
        }

        return UserProfileResponse.builder()
                .profileId(profile.getProfileId())
                .userId(profile.getUser().getUserId())
                .nickname(profile.getUser().getNickname())
                .childName(profile.getChildName())
                .childAge(profile.getChildAge())
                .ageGroup(profile.getAgeGroup())
                .firstLanguage(profile.getFirstLanguage())
                .firstLanguageProficiency(profile.getFirstLanguageProficiency())
                .secondLanguage(profile.getSecondLanguage())
                .secondLanguageProficiency(profile.getSecondLanguageProficiency())
                .firstLanguageListening(profile.getFirstLanguageListening())
                .firstLanguageSpeaking(profile.getFirstLanguageSpeaking())
                .secondLanguageListening(profile.getSecondLanguageListening())
                .secondLanguageSpeaking(profile.getSecondLanguageSpeaking())
                .familyStructure(profile.getFamilyStructure())
                .customFamilyStructure(profile.getCustomFamilyStructure())
                .storyPreference(profile.getStoryPreference())
                .customStoryPreference(profile.getCustomStoryPreference())
                .childNationality(profile.getChildNationality())
                .parentCountry(profile.getParentCountry())
                .primaryLanguage(profile.getPrimaryLanguage())
                .secondaryLanguage(profile.getSecondaryLanguage())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
