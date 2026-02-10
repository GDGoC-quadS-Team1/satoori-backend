package com.moretale.domain.tts.service;

import com.moretale.domain.tts.dto.TTSRequest;
import com.moretale.domain.tts.dto.TTSResponse;
import com.moretale.global.exception.BusinessException;
import com.moretale.global.exception.ErrorCode;

import java.util.Set;

public interface TTSService {

    // TTS 생성
    TTSResponse generateTTS(TTSRequest request);

    // 텍스트 + 언어 코드로 오디오 URL 생성 (내부 사용)
    String generateAudioUrl(String text, String language);

    // 언어 코드 유효성 검증 (ko-KR, vi-VN, en-US)
    default void validateLanguage(String language) {
        Set<String> supportedLanguages = Set.of("ko-KR", "vi-VN", "en-US");
        if (!supportedLanguages.contains(language)) {
            throw new BusinessException(ErrorCode.TTS_INVALID_LANGUAGE);
        }
    }
}
