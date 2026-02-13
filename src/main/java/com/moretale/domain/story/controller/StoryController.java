package com.moretale.domain.story.controller;

import com.moretale.domain.story.dto.*;
import com.moretale.domain.story.service.StoryService;
import com.moretale.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Story", description = "동화 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/stories")
@RequiredArgsConstructor
public class StoryController {

    private final StoryService storyService;

    // 온보딩 기반 동화 생성 초기값 조회
    // GET /api/stories/init?profileId=1
    @Operation(summary = "동화 생성 초기값 조회", description = "온보딩 데이터를 기반으로 동화 생성 폼의 초기값을 반환합니다.")
    @GetMapping("/init")
    public ApiResponse<StoryInitResponse> getStoryInitData(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(name = "profileId", required = false) Long profileId  // 🔧 수정: name 명시
    ) {
        log.info("동화 초기값 조회 요청 - email={}, profileId={}",
                userDetails.getUsername(), profileId);

        StoryInitResponse response = storyService.getStoryInitData(
                userDetails.getUsername(),
                profileId
        );

        return ApiResponse.success(response, "동화 생성 초기값 조회 성공");
    }

    // 온보딩 직후 자동 동화 생성 (추천 전래동화 기반)
    // POST /api/stories/auto-generate
    @Operation(summary = "자동 동화 생성", description = "온보딩 데이터를 기반으로 추천 전래동화를 자동 생성합니다.")
    @PostMapping("/auto-generate")
    public ApiResponse<StoryGenerateResponse> autoGenerateStory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(name = "profileId", required = false) Long profileId  // 🔧 수정: name 명시
    ) {
        log.info("자동 동화 생성 요청 - email={}, profileId={}",
                userDetails.getUsername(), profileId);

        StoryGenerateResponse response = storyService.autoGenerateStory(
                userDetails.getUsername(),
                profileId
        );

        return ApiResponse.success(response, "동화 자동 생성 완료");
    }

    // 동화 생성 (사용자 입력 기반)
    // POST /api/stories/generate
    @Operation(summary = "동화 생성", description = "사용자 프롬프트를 기반으로 이중언어 동화를 생성합니다.")
    @PostMapping("/generate")
    public ApiResponse<StoryGenerateResponse> generateStory(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody StoryGenerateRequest request
    ) {
        log.info("동화 생성 요청 - email={}, prompt={}",
                userDetails.getUsername(), request.getPrompt());

        StoryGenerateResponse response = storyService.generateStory(
                userDetails.getUsername(),
                request
        );

        return ApiResponse.success(response, "동화 생성 완료");
    }

    // 동화 저장
    // POST /api/stories
    @Operation(summary = "동화 저장", description = "생성된 동화를 데이터베이스에 저장합니다.")
    @PostMapping
    public ApiResponse<StoryResponse> saveStory(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody StorySaveRequest request
    ) {
        log.info("동화 저장 요청 - email={}, title={}",
                userDetails.getUsername(), request.getTitle());

        StoryResponse response = storyService.saveStory(
                userDetails.getUsername(),
                request
        );

        return ApiResponse.success(response, "동화 저장 완료");
    }

    // 동화 상세 조회
    // GET /api/stories/{storyId}
    @Operation(summary = "동화 상세 조회", description = "특정 동화의 상세 정보를 조회합니다.")
    @GetMapping("/{storyId}")
    public ApiResponse<StoryResponse> getStoryDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable(name = "storyId") Long storyId  // 🔧 수정: name 명시
    ) {
        StoryResponse response = storyService.getStoryDetail(
                userDetails.getUsername(),
                storyId
        );

        return ApiResponse.success(response);
    }

    // 내 동화 목록 조회
    // GET /api/stories/my
    @Operation(summary = "내 동화 목록 조회", description = "현재 사용자가 생성한 모든 동화를 조회합니다.")
    @GetMapping("/my")
    public ApiResponse<List<StoryListResponse>> getMyStories(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<StoryListResponse> response = storyService.getMyStories(
                userDetails.getUsername()
        );

        return ApiResponse.success(response);
    }

    // 공개 동화 목록 조회
    // GET /api/stories/public
    @Operation(summary = "공개 동화 목록 조회", description = "공개 설정된 모든 동화를 조회합니다.")
    @GetMapping("/public")
    public ApiResponse<List<StoryListResponse>> getPublicStories() {
        List<StoryListResponse> response = storyService.getPublicStories();
        return ApiResponse.success(response);
    }

    // 동화 공유 설정 변경
    // PATCH /api/stories/{storyId}/share
    @Operation(summary = "동화 공유 설정", description = "동화의 공개/비공개 설정을 변경합니다.")
    @PatchMapping("/{storyId}/share")
    public ApiResponse<Void> updateStoryShareStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable(name = "storyId") Long storyId,  // 🔧 수정: name 명시
            @Valid @RequestBody StoryShareRequest request
    ) {
        storyService.updateStoryShareStatus(
                userDetails.getUsername(),
                storyId,
                request
        );

        return ApiResponse.success(null, "공유 설정 변경 완료");
    }

    // 동화 삭제
    // DELETE /api/stories/{storyId}
    @Operation(summary = "동화 삭제", description = "특정 동화를 삭제합니다.")
    @DeleteMapping("/{storyId}")
    public ApiResponse<Void> deleteStory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable(name = "storyId") Long storyId  // 🔧 수정: name 명시
    ) {
        storyService.deleteStory(userDetails.getUsername(), storyId);
        return ApiResponse.success(null, "동화 삭제 완료");
    }
}
