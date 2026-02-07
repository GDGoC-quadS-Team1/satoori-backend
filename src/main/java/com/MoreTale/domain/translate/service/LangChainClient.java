package com.MoreTale.domain.translate.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

// LangChain 변환 서버 호출 클라이언트
// POST /convert 요청으로 표준어 -> 사투리 변환 수행
// 호출 실패 시 LangChainCallException을 발생시키고, GlobalExceptionHandler가 502로 응답
@Component
public class LangChainClient {

    private final RestTemplate restTemplate;
    private final String       langchainBaseUrl;

    public LangChainClient(RestTemplate restTemplate,
                           @Value("${satoori.langchain.url}") String langchainBaseUrl) {
        this.restTemplate     = restTemplate;
        this.langchainBaseUrl = langchainBaseUrl;
    }

     // 표준어 → 지역 사투리 변환 요청
    public String convertToDialect(String originalText, String region) {

        String url = langchainBaseUrl + "/convert";

        Map<String, String> requestBody = Map.of(
                "text",   originalText,
                "region", region
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            Map<String, String> body = response.getBody();
            if (body == null || !body.containsKey("converted_text")) {
                throw new LangChainCallException("LangChain 응답에 converted_text 필드가 없습니다.");
            }
            return body.get("converted_text");

        } catch (RestClientException e) {
            throw new LangChainCallException("LangChain 변환 서버 호출 실패: " + e.getMessage(), e);
        }
    }

    public static class LangChainCallException extends RuntimeException {
        public LangChainCallException(String message)                  { super(message); }
        public LangChainCallException(String message, Throwable cause) { super(message, cause); }
    }
}
