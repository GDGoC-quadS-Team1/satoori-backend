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
public class OnboardingProfileResponse {

    private Long profileId;
    private Long userId;
    private String nickname;
    private String childName;
    private AgeGroup ageGroup;
    private Integer childAge; // 실제 나이 (대표값)

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

    // 부가 정보
    private String childNationality;
    private String parentCountry;

    // 생성/수정 시각
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static OnboardingProfileResponse fromEntity(UserProfile profile) {
        if (profile == null || profile.getUser() == null) {
            throw new IllegalArgumentException("Profile or User cannot be null");
        }

        return OnboardingProfileResponse.builder()
                .profileId(profile.getProfileId())
                .userId(profile.getUser().getUserId())
                .nickname(profile.getUser().getNickname())
                .childName(profile.getChildName())
                .ageGroup(profile.getAgeGroup())
                .childAge(profile.getChildAge())
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
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
