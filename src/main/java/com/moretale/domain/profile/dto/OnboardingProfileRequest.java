package com.moretale.domain.profile.dto;

import com.moretale.domain.profile.entity.AgeGroup;
import com.moretale.domain.profile.entity.FamilyStructure;
import com.moretale.domain.profile.entity.LanguageProficiency;
import com.moretale.domain.profile.entity.StoryPreference;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnboardingProfileRequest {

    // 1단계: 주인공(아이) 소개
    @NotBlank(message = "아이 이름은 필수입니다.")
    @Size(max = 50, message = "아이 이름은 50자 이하여야 합니다.")
    private String childName;

    @NotNull(message = "아이 나이는 필수입니다.")
    private AgeGroup ageGroup; // 0-2세, 3-4세, 5-6세, 7-8세, 9-10세, 10세이상

    // 2단계: 주인공이 쓰는 말이에요
    @NotBlank(message = "첫 번째 언어는 필수입니다.")
    @Pattern(regexp = "^[a-z]{2}$", message = "언어 코드는 2자리 소문자여야 합니다 (ex. ko, en)")
    private String firstLanguage; // 첫 번째 말 (주 사용 언어)

    @NotNull(message = "첫 번째 언어 숙련도는 필수입니다.")
    private LanguageProficiency firstLanguageProficiency; // 알, 애벌레, 번데기, 꿀벌

    @NotBlank(message = "두 번째 언어는 필수입니다.")
    @Pattern(regexp = "^[a-z]{2}$", message = "언어 코드는 2자리 소문자여야 합니다")
    private String secondLanguage; // 두 번째 말

    @NotNull(message = "두 번째 언어 숙련도는 필수입니다.")
    private LanguageProficiency secondLanguageProficiency;

    // 3단계: 이 언어로 어느 정도 말할 수 있나요?
    @NotNull(message = "첫 번째 언어 듣기 숙련도는 필수입니다.")
    private LanguageProficiency firstLanguageListening;

    @NotNull(message = "첫 번째 언어 말하기 숙련도는 필수입니다.")
    private LanguageProficiency firstLanguageSpeaking;

    @NotNull(message = "두 번째 언어 듣기 숙련도는 필수입니다.")
    private LanguageProficiency secondLanguageListening;

    @NotNull(message = "두 번째 언어 말하기 숙련도는 필수입니다.")
    private LanguageProficiency secondLanguageSpeaking;

    // 4단계: 함께 사는 사람들
    @NotNull(message = "함께 사는 사람 정보는 필수입니다.")
    private FamilyStructure familyStructure; // 한 분과 살아요, 두 분과 살아요, 다른 가족과 살아요, 비밀이에요, 직접 작성

    private String customFamilyStructure; // 직접 작성 시 사용

    // 5단계: 어떤 이야기가 좋아요
    @NotNull(message = "선호하는 이야기 유형은 필수입니다.")
    private StoryPreference storyPreference; // 포근포근한 이야기, 신나는 모험 이야기, 오늘 하루를 담은 이야기, 직접 작성

    private String customStoryPreference; // 직접 작성 시 사용

    // 부가 정보 (선택)
    private String childNationality;
    private String parentCountry;
}
