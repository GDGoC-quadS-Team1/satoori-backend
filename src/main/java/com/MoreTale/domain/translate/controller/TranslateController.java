package com.MoreTale.domain.translate.controller;

import com.MoreTale.domain.translate.dto.ReconvertRequest;
import com.MoreTale.domain.translate.dto.TranslateRequest;
import com.MoreTale.domain.translate.dto.TranslateResponse;
import com.MoreTale.domain.translate.service.TranslateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

// 사투리 변환 및 재변환 API 컨트롤러
// 현재는 로그인 없이 사용 가능 (추후 확인 필요 / 2026.02.04)
@RestController
@RequestMapping("/api/translate")
@RequiredArgsConstructor
public class TranslateController {

    private final TranslateService translateService;

    // 사투리 최초 변환 또는 다시 물어보기 처리
    @PostMapping
    public ResponseEntity<TranslateResponse> translate(
            @RequestBody TranslateRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = extractUserId(jwt);
        TranslateResponse response = translateService.translate(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 기존 이력 기반 다른 사투리 보기 처리
    @PostMapping("/reconvert")
    public ResponseEntity<TranslateResponse> reconvert(
            @RequestBody ReconvertRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = extractUserId(jwt);
        TranslateResponse response = translateService.reconvert(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // JWT에서 userId 추출 (없으면 null 반환)
    // 추후 로그인 필수 단계에서는 여기서 예외를 throw하면 됨
    private Long extractUserId(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null) {
            return null;
        }
        try {
            return Long.parseLong(jwt.getSubject());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
