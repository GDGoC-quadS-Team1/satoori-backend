package com.moretale.domain.profile.controller;

import com.moretale.domain.profile.dto.LanguageUpdateRequest;
import com.moretale.domain.profile.dto.UserProfileRequest;
import com.moretale.domain.profile.dto.UserProfileResponse;
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

    @GetMapping("/list")
    @Operation(summary = "전체 프로필 목록 조회", description = "현재 로그인한 사용자의 모든 자녀 프로필 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<UserProfileResponse>>> getAllProfiles(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        log.info("전체 프로필 목록 조회 요청 - userId: {}", userPrincipal.getUserId());

        List<UserProfileResponse> responses = userProfileService.getAllProfiles(userPrincipal.getUserId());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{profileId}")
    @Operation(summary = "특정 프로필 상세 조회", description = "프로필 고유 ID(profileId)를 기준으로 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            @PathVariable("profileId") Long profileId) { // 명시적 이름 지정

        log.info("프로필 상세 조회 요청 - profileId: {}", profileId);

        UserProfileResponse response = userProfileService.getProfile(profileId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{profileId}")
    @Operation(summary = "프로필 수정", description = "특정 자녀 프로필 정보를 수정합니다.")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @PathVariable("profileId") Long profileId, // 명시적 이름 지정
            @Valid @RequestBody UserProfileRequest request) {

        log.info("프로필 수정 요청 - profileId: {}", profileId);

        UserProfileResponse response = userProfileService.updateProfile(profileId, request);

        return ResponseEntity.ok(ApiResponse.success(response, "프로필이 수정되었습니다."));
    }

    @PatchMapping("/{profileId}/language")
    @Operation(summary = "언어 설정 수정", description = "특정 프로필의 이중언어 설정만 변경합니다.")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateLanguage(
            @PathVariable("profileId") Long profileId, // 명시적 이름 지정
            @Valid @RequestBody LanguageUpdateRequest request) {

        log.info("언어 설정 수정 요청 - profileId: {}", profileId);

        UserProfileResponse response = userProfileService.updateLanguage(profileId, request);

        return ResponseEntity.ok(ApiResponse.success(response, "언어 설정이 변경되었습니다."));
    }

    @GetMapping("/exists")
    @Operation(summary = "프로필 존재 여부", description = "최소 하나 이상의 자녀 프로필이 설정되어 있는지 확인합니다.")
    public ResponseEntity<ApiResponse<Boolean>> hasProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        boolean exists = userProfileService.hasProfile(userPrincipal.getUserId());

        return ResponseEntity.ok(ApiResponse.success(exists));
    }

    @DeleteMapping("/{profileId}")
    @Operation(summary = "프로필 삭제", description = "특정 자녀 프로필을 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteProfile(
            @PathVariable("profileId") Long profileId) { // 명시적 이름 지정

        log.info("프로필 삭제 요청 - profileId: {}", profileId);
        userProfileService.deleteProfile(profileId);
        return ResponseEntity.ok(ApiResponse.success(null, "프로필이 삭제되었습니다."));
    }
}
