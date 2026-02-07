package com.moretale.domain.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LanguageUpdateRequest {

    @NotBlank(message = "기본 언어는 필수입니다.")
    @Pattern(regexp = "^[a-z]{2}$", message = "언어 코드는 2자리 소문자여야 합니다 (예: ko, en)")
    private String primaryLanguage;

    @NotBlank(message = "부모 언어는 필수입니다.")
    @Pattern(regexp = "^[a-z]{2}$", message = "언어 코드는 2자리 소문자여야 합니다 (예: vi, en)")
    private String secondaryLanguage;
}
