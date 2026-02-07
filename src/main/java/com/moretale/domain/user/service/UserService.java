package com.moretale.domain.user.service;

import com.moretale.domain.user.dto.UserResponse;
import com.moretale.domain.user.entity.User;
import com.moretale.domain.user.repository.UserRepository;
import com.moretale.global.exception.CustomException;
import com.moretale.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    // 사용자 정보 조회
    public UserResponse getUserInfo(Long userId) {
        log.info("사용자 정보 조회 - userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return UserResponse.fromEntity(user);
    }

    // 사용자 지역 설정
    @Transactional
    public UserResponse updateRegion(Long userId, String region) {
        log.info("사용자 지역 설정 - userId: {}, region: {}", userId, region);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        user.setRegion(region);

        log.info("사용자 지역 설정 완료 - userId: {}", userId);

        return UserResponse.fromEntity(user);
    }
}
