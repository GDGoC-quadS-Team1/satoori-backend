package com.moretale.domain.profile.controller;

import com.moretale.domain.profile.dto.*;
import com.moretale.domain.profile.service.UserProfileService;
import com.moretale.global.common.ApiResponse;
import com.moretale.global.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users/profile")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "사용자 프로필 관리 API")
public class UserProfileController {

    private final UserProfileService userProfileService;

    // 온보딩용 프로필 생성
    //최초 로그인 후 단계별 질문에 따라 상세 프로필을 생성
    @PostMapping("/onboarding")
    @Operation(summary = "온보딩 프로필 생성", description = "최초 로그인 후 단계별 질문에 따라 프로필을 생성합니다.")
    public ResponseEntity<ApiResponse<OnboardingProfileResponse>> createOnboardingProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody OnboardingProfileRequest request) {

        log.info("온보딩 프로필 생성 요청 - userId: {}", userPrincipal.getUserId());

        OnboardingProfileResponse response = userProfileService.createOnboardingProfile(
                userPrincipal.getUserId(), request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "프로필 설정이 완료되었습니다!"));
    }

    // 프로필 생성 (기본/추가)
    //사용자 자녀 프로필을 생성합니다. (1:N 지원)
    @PostMapping
    @Operation(summary = "프로필 생성", description = "사용자 자녀 프로필을 생성합니다. (1:N 지원)")
    public ResponseEntity<ApiResponse<UserProfileResponse>> createProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody UserProfileRequest request) {

        log.info("프로필 생성 요청 - userId: {}", userPrincipal.getUserId());

        UserProfileResponse response = userProfileService.createProfile(
                userPrincipal.getUserId(), request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "프로필이 생성되었습니다."));
    }

    // 전체 프로필 목록 조회
    @GetMapping("/list")
    @Operation(summary = "전체 프로필 목록 조회", description = "현재 로그인한 사용자의 모든 자녀 프로필 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<UserProfileResponse>>> getAllProfiles(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        log.info("전체 프로필 목록 조회 요청 - userId: {}", userPrincipal.getUserId());

        List<UserProfileResponse> responses = userProfileService.getAllProfiles(userPrincipal.getUserId());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // 특정 프로필 상세 조회
    @GetMapping("/{profileId}")
    @Operation(summary = "특정 프로필 상세 조회", description = "프로필 고유 ID(profileId)를 기준으로 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable("profileId") Long profileId) {

        log.info("프로필 상세 조회 요청 - userId: {}, profileId: {}", userPrincipal.getUserId(), profileId);

        // 보안을 위해 서비스단에서 해당 사용자의 프로필인지 확인하는 로직이 권장됩니다.
        UserProfileResponse response = userProfileService.getProfile(profileId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 프로필 수정 (전체 정보 수정 - PUT)
    // 마이페이지 등에서 자녀의 정보를 수정할 때 사용
    @PutMapping("/{profileId}")
    @Operation(summary = "프로필 수정", description = "특정 자녀 프로필 정보 전체를 수정합니다.")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @PathVariable("profileId") Long profileId,
            @Valid @RequestBody UserProfileRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Long userId = userPrincipal.getUserId();
        log.info("프로필 수정 요청 - userId: {}, profileId: {}", userId, profileId);

        // 유저 ID와 프로필 ID를 매칭하여 보안 검증 후 수정
        UserProfileResponse response = userProfileService.updateProfile(userId, profileId, request);

        return ResponseEntity.ok(
                ApiResponse.success(response, "프로필이 수정되었습니다.")
        );
    }

    // 언어 설정만 수정 (부분 수정 - PATCH)
    @PatchMapping("/{profileId}/language")
    @Operation(summary = "언어 설정 수정", description = "특정 프로필의 이중언어 설정만 변경합니다.")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateLanguage(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable("profileId") Long profileId,
            @Valid @RequestBody LanguageUpdateRequest request) {

        log.info("언어 설정 수정 요청 - userId: {}, profileId: {}", userPrincipal.getUserId(), profileId);

        UserProfileResponse response = userProfileService.updateLanguage(profileId, request);

        return ResponseEntity.ok(ApiResponse.success(response, "언어 설정이 변경되었습니다."));
    }

    // 프로필 존재 여부 확인 (최초 온보딩 리다이렉트 판단용)
    @GetMapping("/exists")
    @Operation(summary = "프로필 존재 여부", description = "최소 하나 이상의 자녀 프로필이 설정되어 있는지 확인합니다.")
    public ResponseEntity<ApiResponse<Boolean>> hasProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        boolean exists = userProfileService.hasProfile(userPrincipal.getUserId());

        return ResponseEntity.ok(ApiResponse.success(exists));
    }

    // 프로필 삭제
    @DeleteMapping("/{profileId}")
    @Operation(summary = "프로필 삭제", description = "특정 자녀 프로필을 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable("profileId") Long profileId) {

        log.info("프로필 삭제 요청 - userId: {}, profileId: {}", userPrincipal.getUserId(), profileId);

        userProfileService.deleteProfile(profileId);
        return ResponseEntity.ok(ApiResponse.success(null, "프로필이 삭제되었습니다."));
    }
}
