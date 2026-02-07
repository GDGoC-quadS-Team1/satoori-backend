package com.moretale.domain.profile.repository;

import com.moretale.domain.profile.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    // 특정 사용자의 모든 자녀 프로필 목록을 조회
    List<UserProfile> findAllByUser_UserId(Long userId);

    // 특정 사용자의 프로필 존재 여부를 확인
    boolean existsByUser_UserId(Long userId);

    // 특정 사용자가 동일한 이름의 자녀를 이미 등록했는지 확인
    boolean existsByUser_UserIdAndChildName(Long userId, String childName);

    // 특정 프로필 ID와 사용자 ID가 일치하는 프로필을 조회 (보안 강화용)
    Optional<UserProfile> findByProfileIdAndUser_UserId(Long profileId, Long userId);
}
