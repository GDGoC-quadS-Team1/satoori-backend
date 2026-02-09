package com.moretale.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // User 관련
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "U002", "이미 존재하는 사용자입니다."),

    // Profile 관련
    PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "프로필을 찾을 수 없습니다."),
    PROFILE_ALREADY_EXISTS(HttpStatus.CONFLICT, "P002", "이미 프로필이 존재합니다."),

    // Story 관련
    STORY_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "동화를 찾을 수 없습니다."),
    STORY_ACCESS_DENIED(HttpStatus.FORBIDDEN, "S002", "동화에 접근할 권한이 없습니다."),
    STORY_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S003", "동화 생성에 실패했습니다."),
    STORY_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S004", "동화 저장에 실패했습니다."),

    // Slide 관련
    SLIDE_NOT_FOUND(HttpStatus.NOT_FOUND, "SL001", "슬라이드를 찾을 수 없습니다."),
    INVALID_SLIDE_ORDER(HttpStatus.BAD_REQUEST, "SL002", "슬라이드 순서가 올바르지 않습니다."),

    // TTS 관련
    TTS_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "T001", "음성 생성에 실패했습니다."),
    INVALID_LANGUAGE_CODE(HttpStatus.BAD_REQUEST, "T002", "지원하지 않는 언어 코드입니다."),

    // AI 관련
    AI_SERVICE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "A001", "AI 서비스 오류가 발생했습니다."),
    AI_RESPONSE_INVALID(HttpStatus.INTERNAL_SERVER_ERROR, "A002", "AI 응답이 올바르지 않습니다."),

    // 공통
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "C002", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "C003", "권한이 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C004", "서버 오류가 발생했습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C005", "허용되지 않은 메서드입니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "C006", "요청한 리소스를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
