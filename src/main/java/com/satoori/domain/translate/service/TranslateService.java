package com.satoori.domain.translate.service;

import com.satoori.domain.translate.dto.ReconvertRequest;
import com.satoori.domain.translate.dto.TranslateRequest;
import com.satoori.domain.translate.dto.TranslateResponse;
import com.satoori.domain.translate.entity.TranslateHistory;
import com.satoori.domain.translate.repository.TranslateHistoryRepository;
import com.satoori.global.config.Region;
import com.satoori.global.exception.EmptyInputException;
import com.satoori.global.exception.InvalidRegionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// AI 사투리 도사 (사투리 변환 기능) 서비스
@Service
@RequiredArgsConstructor
public class TranslateService {

    private final LangChainClient            langChainClient;
    private final TtsClient                  ttsClient;
    private final TranslateHistoryRepository historyRepo;

    // 최초 변환 (POST /api/translate)
    @Transactional
    public TranslateResponse translate(TranslateRequest request, Long userId) {

        // 입력 검증
        validateInput(request.getText(), request.getRegion());

        Region region       = Region.fromString(request.getRegion());
        String originalText = request.getText().trim();

        String convertedText = "[임시 변환] " + originalText;
        String audioUrl = "https://mock-audio-url.com/temp.mp3";

        // LangChain 변환 호출
        // String convertedText = langChainClient.convertToDialect(originalText, region.name());

        // TTS 음성 생성 호출
        // String audioUrl = ttsClient.generateAudio(convertedText, region.name());

        // 변환 이력 저장
        TranslateHistory history = TranslateHistory.create(
                originalText, convertedText, region, audioUrl, userId
        );
        historyRepo.save(history);

        // 응답 조립 및 반환
        return TranslateResponse.builder()
                .historyId(history.getId())
                .original(originalText)
                .converted(convertedText)
                .region(region.name())
                .regionDisplayName(region.getDisplayName())
                .audioUrl(audioUrl)
                .build();
    }

    // 재변환 (POST /api/translate/reconvert)
    @Transactional
    public TranslateResponse reconvert(ReconvertRequest request, Long userId) {

        // historyId 검증 및 원본 기록 조회
        if (request.getHistoryId() == null) {
            throw new IllegalArgumentException("historyId 값이 필요합니다.");
        }

        TranslateHistory parentHistory = historyRepo.findById(request.getHistoryId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "해당 변환 기록을 찾을 수 없습니다. historyId=" + request.getHistoryId()));

        // targetRegion 검증
        validateInput(parentHistory.getOriginalText(), request.getTargetRegion());
        Region targetRegion = Region.fromString(request.getTargetRegion());

        String originalText = parentHistory.getOriginalText();

        // LangChain 재변환 호출 (원본 텍스트 + 새 지역)
        String convertedText = langChainClient.convertToDialect(originalText, targetRegion.name());

        // TTS 음성 생성
        String audioUrl = ttsClient.generateAudio(convertedText, targetRegion.name());

        // 재변환 이력 저장 (parentHistoryId 연결)
        TranslateHistory newHistory = TranslateHistory.createReconversion(
                originalText, convertedText, targetRegion, audioUrl,
                parentHistory.getId(), userId
        );
        historyRepo.save(newHistory);

        // 응답 조립 및 반환
        return TranslateResponse.builder()
                .historyId(newHistory.getId())
                .original(originalText)
                .converted(convertedText)
                .region(targetRegion.name())
                .regionDisplayName(targetRegion.getDisplayName())
                .audioUrl(audioUrl)
                .build();
    }

    // 입력값 공통 검증
    // 변환 입력값(text, regionCode) 유효성 확인
    private void validateInput(String text, String regionCode) {
        if (text == null || text.isBlank()) {
            throw new EmptyInputException("변환할 문장을 입력해주세요.");
        }
        if (Region.fromString(regionCode) == null) {
            throw new InvalidRegionException(
                    "지원하지 않는 지역 코드입니다: " + regionCode +
                            " | 지원 목록: GYEONGSANG_BUKDO, GYEONGSANG_NAMDO, CHUNGCHEONGDO, JEONRA, JEJU"
            );
        }
    }
}
