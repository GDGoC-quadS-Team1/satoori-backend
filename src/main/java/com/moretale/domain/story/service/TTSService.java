package com.moretale.domain.story.service;

public interface TTSService {

    // 텍스트를 음성(TTS)으로 변환하여 오디오 URL 반환
    String generateTTS(
            String text,      // 변환할 텍스트
            String language   // 언어 코드 (ex. ko-KR, vi-VN)
    );
}
