package com.moretale.domain.story.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoryGenerateRequest {

    @NotBlank(message = "프롬프트를 입력해주세요.")
    private String prompt;

    // 프로필 ID 추가 (선택적)
    private Long profileId;

    // 선택적 필드 (프로필 정보를 오버라이드)
    private String childName;
    private String primaryLanguage;
    private String secondaryLanguage;
}
