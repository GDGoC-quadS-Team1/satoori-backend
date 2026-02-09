package com.moretale.domain.story.service.impl;

import com.moretale.domain.story.service.TTSService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TTSServiceImpl implements TTSService {

    // TODO: 실제 TTS API 연동 (Google TTS, AWS Polly 등)

    @Override
    public String generateTTS(String text, String language) {
        log.info("TTS 생성 요청 - text: {}, language: {}", text, language);

        // 임시 더미 URL (실제로는 TTS API 호출 후 저장된 파일 URL 반환)
        return "https://storage.moretale.ai/tts/" + System.currentTimeMillis() + "-" + language + ".mp3";
    }
}
