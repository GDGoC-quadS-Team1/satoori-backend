package com.satoori.domain.translate.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

// 변환된 사투리 문장을 음성으로 생성하기 위한 TTS 서버 호출 클라이언트
// POST /generate 호출
// 성공 시 audio_url 반환, 실패 시 TtsCallException 발생
@Component
public class TtsClient {

    private final RestTemplate restTemplate;
    private final String       ttsBaseUrl;

    public TtsClient(RestTemplate restTemplate,
                     @Value("${satoori.tts.url}") String ttsBaseUrl) {
        this.restTemplate = restTemplate;
        this.ttsBaseUrl   = ttsBaseUrl;
    }

    // TTS 음성 생성 요청
    public String generateAudio(String text, String region) {

        String url = ttsBaseUrl + "/generate";

        Map<String, String> requestBody = Map.of(
                "text",   text,
                "region", region
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            Map<String, String> body = response.getBody();
            if (body == null || !body.containsKey("audio_url")) {
                throw new TtsCallException("TTS 응답에 audio_url 필드가 없습니다.");
            }
            return body.get("audio_url");

        } catch (RestClientException e) {
            throw new TtsCallException("TTS 음성 생성 서버 호출 실패: " + e.getMessage(), e);
        }
    }

    public static class TtsCallException extends RuntimeException {
        public TtsCallException(String message)                  { super(message); }
        public TtsCallException(String message, Throwable cause) { super(message, cause); }
    }
}
