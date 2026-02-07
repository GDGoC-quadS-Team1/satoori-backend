package com.MoreTale.domain.translate.entity;

import com.MoreTale.global.config.Region;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

// 최초 변환 및 재변환(다시 물어보기, 다른 사투리 보기)을 포함한 사투리 변환 이력 Entity
@Entity
@Table(name = "translate_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TranslateHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "translate_history_id")
    private Long id;

    // 원본 입력 문장
    @Column(name = "original_text", nullable = false, columnDefinition = "TEXT")
    private String originalText;

    // 변환된 사투리 문장
    @Column(name = "converted_text", nullable = false, columnDefinition = "TEXT")
    private String convertedText;

    // 변환 대상 지역 코드 - Enum 이름 문자열로 저장
    @Enumerated(EnumType.STRING)
    @Column(name = "region", nullable = false, length = 30)
    private Region region;

    // TTS 생성된 음성 파일 URL
    @Column(name = "audio_url", length = 512)
    private String audioUrl;

    // 최초 변환이면 null, 재변환이면 원본 이력 ID를 가리킴
    @Column(name = "parent_history_id")
    private Long parentHistoryId;

    // 요청한 사용자 ID (users 테이블 연동)
    @Column(name = "user_id")
    private Long userId;

    //생성 시각
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 최초 변환 기록 생성
    public static TranslateHistory create(String originalText, String convertedText,
                                          Region region, String audioUrl, Long userId) {
        return TranslateHistory.builder()
                .originalText(originalText)
                .convertedText(convertedText)
                .region(region)
                .audioUrl(audioUrl)
                .userId(userId)
                .parentHistoryId(null)
                .build();
    }

    // 재변환 기록 생성 (다시 물어보기 / 다른 사투리 보기)
    public static TranslateHistory createReconversion(String originalText, String convertedText,
                                                      Region region, String audioUrl,
                                                      Long parentHistoryId, Long userId) {
        return TranslateHistory.builder()
                .originalText(originalText)
                .convertedText(convertedText)
                .region(region)
                .audioUrl(audioUrl)
                .parentHistoryId(parentHistoryId)
                .userId(userId)
                .build();
    }
}
