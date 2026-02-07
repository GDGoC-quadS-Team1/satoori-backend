package com.MoreTale.domain.user.controller;

import com.MoreTale.domain.user.dto.UserResponse;
import com.MoreTale.domain.user.entity.User;
import com.MoreTale.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// 사용자 관련 API 컨트롤러 (로그인한 사용자 기준으로 내 정보 조회/수정 기능 제공)
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 현재 로그인한 사용자 정보 조회
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal User user) {
        UserResponse response = UserResponse.from(user);
        return ResponseEntity.ok(response);
    }

    // 현재 로그인한 사용자의 지역 정보 수정
    @PatchMapping("/me/region")
    public ResponseEntity<UserResponse> updateRegion(
            @AuthenticationPrincipal User user,
            @RequestParam("region") String region) { // 쿼리 파라미터 'region'을 문자열로 받음
        User updatedUser = userService.updateRegion(user.getUserId(), region);
        return ResponseEntity.ok(UserResponse.from(updatedUser));
    }
}
