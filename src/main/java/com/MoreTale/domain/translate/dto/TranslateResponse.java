package com.MoreTale.domain.translate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 사투리 변환 및 재변환 API 공통 응답 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TranslateResponse {

    private Long   historyId; // '다른 사투리 보기' 버튼 클릭 시 이 값을 reconvert API에 다시 보냄
    private String original;
    private String converted;
    private String region;
    private String regionDisplayName; // 프론트 태그 UI에 바로 표시
    private String audioUrl; // '듣기' 버튼 & '음원 저장 (MP3)' 버튼에 연결
}
