package com.satoori.domain.user.service;

import com.satoori.domain.user.entity.User;
import com.satoori.domain.user.repository.UserRepository;
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
        // userId로 사용자 조회, 없으면 예외 발생
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRegion(region); // 지역 정보 수정
        return userRepository.save(user);
    }
}
