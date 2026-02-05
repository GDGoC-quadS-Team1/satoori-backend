package com.satoori.domain.translate.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 재변환 요청 DTO (원본 이력 ID + 새 지역 코드)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReconvertRequest {

    private Long   historyId; // 원본 변환 기록에서 originalText를 조회
    private String targetRegion; // 새로 변환할 지역 코드
}
