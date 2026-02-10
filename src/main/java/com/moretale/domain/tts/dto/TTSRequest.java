package com.moretale.domain.tts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TTSRequest {

    @NotBlank(message = "텍스트는 필수입니다")
    private String text;

    @NotBlank(message = "언어 코드는 필수입니다")
    // ko-KR, vi-VN, en-US 형식만 허용하도록 정규식 추가
    @Pattern(regexp = "^(ko-KR|vi-VN|en-US)$", message = "지원하지 않는 언어 코드입니다. (ko-KR, vi-VN, en-US 중 하나여야 합니다.)")
    private String language;

    private String style;
}
