package com.moretale.domain.profile.dto;

import com.moretale.domain.profile.entity.AgeGroup;
import com.moretale.domain.profile.entity.FamilyStructure;
import com.moretale.domain.profile.entity.LanguageProficiency;
import com.moretale.domain.profile.entity.StoryPreference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "사용자 프로필 요청 DTO (생성/수정)")
public class UserProfileRequest {

    @NotBlank(message = "아이 이름은 필수입니다.")
    @Size(max = 50, message = "아이 이름은 50자 이하로 입력해주세요.")
    @Schema(description = "아이 이름", example = "민준")
    private String childName;

    @NotNull(message = "나이 그룹은 필수입니다.")
    @Schema(description = "나이 그룹 (AGE_0_2, AGE_3_4, AGE_5_6, AGE_7_8, AGE_9_10, AGE_10_PLUS)", example = "AGE_5_6")
    private AgeGroup ageGroup;

    @NotBlank(message = "제1언어는 필수입니다.")
    @Schema(description = "제1언어 (ISO 639-1 코드)", example = "ko")
    private String firstLanguage;

    @NotNull(message = "제1언어 숙련도는 필수입니다.")
    @Schema(description = "제1언어 전체 숙련도 (EGG, LARVA, PUPA, BEE)", example = "BEE")
    private LanguageProficiency firstLanguageProficiency;

    @NotBlank(message = "제2언어는 필수입니다.")
    @Schema(description = "제2언어 (ISO 639-1 코드)", example = "vi")
    private String secondLanguage;

    @NotNull(message = "제2언어 숙련도는 필수입니다.")
    @Schema(description = "제2언어 전체 숙련도 (EGG, LARVA, PUPA, BEE)", example = "LARVA")
    private LanguageProficiency secondLanguageProficiency;

    @NotNull(message = "제1언어 듣기 능력은 필수입니다.")
    @Schema(description = "제1언어 듣기 능력", example = "BEE")
    private LanguageProficiency firstLanguageListening;

    @NotNull(message = "제1언어 말하기 능력은 필수입니다.")
    @Schema(description = "제1언어 말하기 능력", example = "BEE")
    private LanguageProficiency firstLanguageSpeaking;

    @NotNull(message = "제2언어 듣기 능력은 필수입니다.")
    @Schema(description = "제2언어 듣기 능력", example = "PUPA")
    private LanguageProficiency secondLanguageListening;

    @NotNull(message = "제2언어 말하기 능력은 필수입니다.")
    @Schema(description = "제2언어 말하기 능력", example = "LARVA")
    private LanguageProficiency secondLanguageSpeaking;

    @NotNull(message = "가족 구조는 필수입니다.")
    @Schema(description = "가족 구조 (ONE_PARENT, TWO_PARENTS, EXTENDED_FAMILY, SECRET, CUSTOM)", example = "TWO_PARENTS")
    private FamilyStructure familyStructure;

    @Size(max = 200, message = "커스텀 가족 구조는 200자 이하로 입력해주세요.")
    @Schema(description = "커스텀 가족 구조 (CUSTOM 선택 시)")
    private String customFamilyStructure;

    @NotNull(message = "이야기 선호도는 필수입니다.")
    @Schema(description = "이야기 선호도 (WARM_HUG, FUN_ADVENTURE, DAILY_LIFE, CUSTOM)", example = "FUN_ADVENTURE")
    private StoryPreference storyPreference;

    @Size(max = 200, message = "커스텀 이야기 선호도는 200자 이하로 입력해주세요.")
    @Schema(description = "커스텀 이야기 선호도 (CUSTOM 선택 시)")
    private String customStoryPreference;

    @Size(max = 50, message = "아이 국적은 50자 이하로 입력해주세요.")
    @Schema(description = "아이 국적 (ISO 3166-1 alpha-2)", example = "KR")
    private String childNationality;

    @Size(max = 50, message = "부모 거주 국가는 50자 이하로 입력해주세요.")
    @Schema(description = "부모 거주 국가 (ISO 3166-1 alpha-2)", example = "VN")
    private String parentCountry;

    @Deprecated
    @Schema(description = "[Deprecated] 기본 언어", example = "ko")
    private String primaryLanguage;

    @Deprecated
    @Schema(description = "[Deprecated] 보조 언어", example = "vi")
    private String secondaryLanguage;

    @Deprecated
    @Min(value = 1, message = "아이 나이는 1세 이상이어야 합니다.")
    @Max(value = 12, message = "아이 나이는 12세 이하여야 합니다.")
    @Schema(description = "[Deprecated] 아이 나이", example = "6")
    private Integer childAge;
}
