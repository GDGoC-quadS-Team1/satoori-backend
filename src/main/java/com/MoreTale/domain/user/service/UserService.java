package com.MoreTale.domain.user.service;

import com.MoreTale.domain.user.dto.UserResponse;
import com.MoreTale.domain.user.entity.User;
import com.MoreTale.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // 사용자 지역(region) 정보 업데이트
    @Transactional
    public User updateRegion(Long userId, String region) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRegion(region); // 지역 정보 수정
        return userRepository.save(user);
    }

    // 사용자 조회
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return UserResponse.from(user);
    }
}
