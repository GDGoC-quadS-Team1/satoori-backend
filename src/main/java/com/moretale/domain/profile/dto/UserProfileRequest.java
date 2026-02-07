package com.moretale.domain.profile.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileRequest {

    @NotBlank(message = "아이 이름은 필수입니다.")
    @Size(max = 50, message = "아이 이름은 50자 이하여야 합니다.")
    private String childName;

    @NotNull(message = "아이 나이는 필수입니다.")
    @Min(value = 1, message = "아이 나이는 1세 이상이어야 합니다.")
    @Max(value = 18, message = "아이 나이는 18세 이하여야 합니다.")
    private Integer childAge;

    @Size(max = 50, message = "자녀 국적은 50자 이하여야 합니다.")
    private String childNationality;

    @NotBlank(message = "부모 출신 국가는 필수입니다.")
    @Size(max = 50, message = "부모 출신 국가는 50자 이하여야 합니다.")
    private String parentCountry;

    @NotBlank(message = "기본 언어는 필수입니다.")
    @Pattern(regexp = "^[a-z]{2}$", message = "언어 코드는 2자리 소문자여야 합니다 (예: ko, en)")
    private String primaryLanguage;

    @NotBlank(message = "부모 언어는 필수입니다.")
    @Pattern(regexp = "^[a-z]{2}$", message = "언어 코드는 2자리 소문자여야 합니다 (예: vi, en)")
    private String secondaryLanguage;
}
