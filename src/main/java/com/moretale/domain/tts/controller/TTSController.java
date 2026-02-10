package com.moretale.domain.tts.controller;

import com.moretale.domain.tts.dto.TTSRequest;
import com.moretale.domain.tts.dto.TTSResponse;
import com.moretale.domain.tts.service.TTSGenerationService;
import com.moretale.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/tts")
@RequiredArgsConstructor
public class TTSController {

    private final TTSGenerationService ttsGenerationService;

    // TTS 생성 API
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<TTSResponse>> generateTTS(
            @Valid @RequestBody TTSRequest request
    ) {
        log.info("TTS 생성 요청 - 언어: {}, 텍스트 길이: {}",
                request.getLanguage(), request.getText().length());

        TTSResponse response = ttsGenerationService.generateSingleTTS(request);

        return ResponseEntity.ok(
                ApiResponse.success(response, "TTS 생성 완료")
        );
    }

    // 슬라이드 단위 TTS 재생성 (관리자/개발용)
    @PostMapping("/regenerate/slide/{slideId}")
    public ResponseEntity<ApiResponse<String>> regenerateSlideTS(
            @PathVariable Long slideId,
            @RequestParam String primaryLanguage,
            @RequestParam String secondaryLanguage
    ) {
        log.info("슬라이드 {} TTS 재생성 요청", slideId);

        ttsGenerationService.generateTTSForSlide(slideId, primaryLanguage, secondaryLanguage);

        return ResponseEntity.ok(
                ApiResponse.success("슬라이드 TTS 재생성 완료")
        );
    }

    // 동화 전체 TTS 재생성 (관리자/개발용)
    @PostMapping("/regenerate/story/{storyId}")
    public ResponseEntity<ApiResponse<String>> regenerateStoryTTS(
            @PathVariable Long storyId
    ) {
        log.info("동화 {} 전체 TTS 재생성 요청", storyId);

        ttsGenerationService.generateTTSForStory(storyId);

        return ResponseEntity.ok(
                ApiResponse.success("동화 전체 TTS 재생성 완료")
        );
    }

    // 누락된 TTS만 재생성
    @PostMapping("/regenerate/missing/{storyId}")
    public ResponseEntity<ApiResponse<String>> regenerateMissingTTS(
            @PathVariable Long storyId
    ) {
        log.info("동화 {} 누락된 TTS 재생성 요청", storyId);

        ttsGenerationService.regenerateMissingTTS(storyId);

        return ResponseEntity.ok(
                ApiResponse.success("누락된 TTS 재생성 완료")
        );
    }
}
