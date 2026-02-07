package com.moretale.domain.user.controller;

import com.moretale.domain.user.dto.UserResponse;
import com.moretale.domain.user.service.UserService;
import com.moretale.global.common.ApiResponse;
import com.moretale.global.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 현재 로그인한 사용자 정보 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        log.info("사용자 정보 조회 요청 - userId: {}", userPrincipal.getUserId());

        UserResponse response = userService.getUserInfo(userPrincipal.getUserId());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 사용자 지역 설정
    @PatchMapping("/me/region")
    public ResponseEntity<ApiResponse<UserResponse>> updateRegion(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam("region") String region) {

        log.info("사용자 지역 설정 요청 - userId: {}, region: {}", userPrincipal.getUserId(), region);

        UserResponse response = userService.updateRegion(userPrincipal.getUserId(), region);

        return ResponseEntity.ok(ApiResponse.success(response, "지역이 설정되었습니다."));
    }
}
