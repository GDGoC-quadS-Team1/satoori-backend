package com.MoreTale.global.exception;

import com.MoreTale.domain.translate.service.LangChainClient.LangChainCallException;
import com.MoreTale.domain.translate.service.TtsClient.TtsCallException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Dialect 관련 예외
    @ExceptionHandler(DuplicateDialectException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateDialect(DuplicateDialectException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    // Translate 기능 관련 예외
    @ExceptionHandler(EmptyInputException.class)
    public ResponseEntity<Map<String, Object>> handleEmptyInput(EmptyInputException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(InvalidRegionException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidRegion(InvalidRegionException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(LangChainCallException.class)
    public ResponseEntity<Map<String, Object>> handleLangChainFail(LangChainCallException ex) {
        return buildResponse(
                HttpStatus.BAD_GATEWAY,
                "사투리 변환 서버에 문제가 발생했습니다. 잠시 후 다시 시도해주세요."
        );
    }

    @ExceptionHandler(TtsCallException.class)
    public ResponseEntity<Map<String, Object>> handleTtsFail(TtsCallException ex) {
        return buildResponse(
                HttpStatus.BAD_GATEWAY,
                "음성 생성 서버에 문제가 발생했습니다. 텍스트 변환 결과는 정상입니다."
        );
    }

    // 기타 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(Exception ex) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "서버 내부 오류가 발생했습니다."
        );
    }

    // 공통 응답 포맷
    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);

        return new ResponseEntity<>(body, status);
    }
}
