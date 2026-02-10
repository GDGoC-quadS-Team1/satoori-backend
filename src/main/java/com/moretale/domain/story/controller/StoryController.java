package com.moretale.domain.story.controller;

import com.moretale.domain.story.dto.*;
import com.moretale.domain.story.service.StoryService;
import com.moretale.global.exception.BusinessException;
import com.moretale.global.exception.ErrorCode;
import com.moretale.global.response.ApiResponse;
import com.moretale.global.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stories")
@RequiredArgsConstructor
@Slf4j
public class StoryController {

    private final StoryService storyService;

    // 동화 생성 (AI 연동)
    @PostMapping("/generate")
    public ApiResponse<StoryGenerateResponse> generateStory(
            @AuthenticationPrincipal Object principal,
            @Valid @RequestBody StoryGenerateRequest request) {

        String email = getEmailFromPrincipal(principal);
        log.info("동화 생성 요청 - email: {}, prompt: {}", email, request.getPrompt());

        StoryGenerateResponse response = storyService.generateStory(email, request);
        return ApiResponse.success(response, "동화가 생성되었습니다.");
    }

    // 동화 저장
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<StoryResponse> saveStory(
            @AuthenticationPrincipal Object principal,
            @Valid @RequestBody StorySaveRequest request) {

        String email = getEmailFromPrincipal(principal);
        log.info("동화 저장 요청 - email: {}, title: {}", email, request.getTitle());

        StoryResponse response = storyService.saveStory(email, request);
        return ApiResponse.success(response, "동화가 저장되었습니다.");
    }

    // 내 동화 목록 조회
    @GetMapping("/my")
    public ApiResponse<List<StoryListResponse>> getMyStories(
            @AuthenticationPrincipal Object principal) {

        String email = getEmailFromPrincipal(principal);
        log.info("내 동화 목록 조회 - email: {}", email);

        List<StoryListResponse> response = storyService.getMyStories(email);
        return ApiResponse.success(response);
    }

    // 공개 동화 목록 조회
    @GetMapping("/public")
    public ApiResponse<List<StoryListResponse>> getPublicStories() {
        log.info("공개 동화 목록 조회");

        List<StoryListResponse> response = storyService.getPublicStories();
        return ApiResponse.success(response);
    }

    // 특정 동화 상세 조회
    @GetMapping("/{storyId}")
    public ApiResponse<StoryResponse> getStoryDetail(
            @AuthenticationPrincipal Object principal,
            @PathVariable("storyId") Long storyId) {

        String email = getEmailFromPrincipal(principal);
        log.info("동화 상세 조회 - email: {}, storyId: {}", email, storyId);

        StoryResponse response = storyService.getStoryDetail(email, storyId);
        return ApiResponse.success(response);
    }

    // 동화 공유 여부 변경
    @PatchMapping("/{storyId}/share")
    public ApiResponse<Void> updateStoryShareStatus(
            @AuthenticationPrincipal Object principal,
            @PathVariable("storyId") Long storyId,
            @Valid @RequestBody StoryShareRequest request) {

        String email = getEmailFromPrincipal(principal);
        log.info("동화 공유 설정 변경 - email: {}, storyId: {}, isPublic: {}",
                email, storyId, request.getIsPublic());

        storyService.updateStoryShareStatus(email, storyId, request);
        return ApiResponse.success(null, "공유 설정이 변경되었습니다.");
    }

    // 동화 삭제
    @DeleteMapping("/{storyId}")
    public ApiResponse<Void> deleteStory(
            @AuthenticationPrincipal Object principal,
            @PathVariable("storyId") Long storyId) {

        String email = getEmailFromPrincipal(principal);
        log.info("동화 삭제 요청 - email: {}, storyId: {}", email, storyId);

        storyService.deleteStory(email, storyId);
        return ApiResponse.success(null, "동화가 삭제되었습니다.");
    }

    // Principal에서 email 추출 (JWT UserPrincipal / OAuth2User / 테스트용 @WithMockUser)
    private String getEmailFromPrincipal(Object principal) {
        if (principal == null) {
            log.warn("Principal이 null입니다.");
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 1. JWT 필터를 통한 인증인 경우 (UserPrincipal)
        if (principal instanceof UserPrincipal) {
            return ((UserPrincipal) principal).getEmail();
        }

        // 2. OAuth2 로그인 직후 세션 인증인 경우 (OAuth2User)
        if (principal instanceof OAuth2User) {
            OAuth2User oAuth2User = (OAuth2User) principal;
            String email = oAuth2User.getAttribute("email");
            if (email == null) {
                log.warn("OAuth2User의 email 속성이 null입니다.");
                throw new BusinessException(ErrorCode.USER_NOT_FOUND);
            }
            return email;
        }

        // 3. 테스트 코드 (@WithMockUser) 대응
        if (principal instanceof User) {
            return ((User) principal).getUsername();
        }

        log.error("지원하지 않는 Principal 타입: {}", principal.getClass().getName());
        throw new BusinessException(ErrorCode.USER_NOT_FOUND);
    }
}
