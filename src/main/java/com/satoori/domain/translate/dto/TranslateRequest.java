package com.satoori.domain.translate.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 사투리 변환 요청 DTO (입력 문장 + 지역 코드)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TranslateRequest {

    private String text;
    private String region;
}
