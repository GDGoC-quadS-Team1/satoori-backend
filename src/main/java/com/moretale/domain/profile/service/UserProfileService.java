package com.moretale.domain.profile.service;

import com.moretale.domain.profile.dto.LanguageUpdateRequest;
import com.moretale.domain.profile.dto.UserProfileRequest;
import com.moretale.domain.profile.dto.UserProfileResponse;
import com.moretale.domain.profile.entity.UserProfile;
import com.moretale.domain.profile.repository.UserProfileRepository;
import com.moretale.domain.user.entity.User;
import com.moretale.domain.user.repository.UserRepository;
import com.moretale.global.exception.CustomException;
import com.moretale.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    // 프로필 추가
    @Transactional
    public UserProfileResponse createProfile(Long userId, UserProfileRequest request) {
        log.info("프로필 생성 시작 - userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 동일한 이름의 아이가 이미 등록되어 있는지 체크
        if (userProfileRepository.existsByUser_UserIdAndChildName(userId, request.getChildName())) {
            throw new CustomException(ErrorCode.PROFILE_ALREADY_EXISTS);
        }

        UserProfile profile = UserProfile.builder()
                .user(user)
                .childName(request.getChildName())
                .childAge(request.getChildAge())
                .childNationality(request.getChildNationality())
                .parentCountry(request.getParentCountry())
                .primaryLanguage(request.getPrimaryLanguage())
                .secondaryLanguage(request.getSecondaryLanguage())
                .build();

        UserProfile savedProfile = userProfileRepository.save(profile);
        log.info("프로필 생성 완료 - userId: {}, profileId: {}, childName: {}",
                userId, savedProfile.getProfileId(), savedProfile.getChildName());

        return UserProfileResponse.fromEntity(savedProfile);
    }

    // 특정 사용자의 모든 자녀 프로필 목록 조회
    public List<UserProfileResponse> getAllProfiles(Long userId) {
        log.info("전체 프로필 조회 - userId: {}", userId);

        return userProfileRepository.findAllByUser_UserId(userId).stream()
                .map(UserProfileResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // 특정 프로필 상세 조회 (profileId 기준)
    public UserProfileResponse getProfile(Long profileId) {
        log.info("프로필 상세 조회 - profileId: {}", profileId);

        UserProfile profile = userProfileRepository.findById(profileId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROFILE_NOT_FOUND));

        return UserProfileResponse.fromEntity(profile);
    }

    // 프로필 정보 수정 (profileId 기준)
    @Transactional
    public UserProfileResponse updateProfile(Long profileId, UserProfileRequest request) {
        log.info("프로필 수정 시작 - profileId: {}", profileId);

        UserProfile profile = userProfileRepository.findById(profileId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROFILE_NOT_FOUND));

        profile.setChildName(request.getChildName());
        profile.setChildAge(request.getChildAge());
        profile.setChildNationality(request.getChildNationality());
        profile.setParentCountry(request.getParentCountry());
        profile.setPrimaryLanguage(request.getPrimaryLanguage());
        profile.setSecondaryLanguage(request.getSecondaryLanguage());

        return UserProfileResponse.fromEntity(profile);
    }

    // 언어 설정만 수정 (profileId 기준)
    @Transactional
    public UserProfileResponse updateLanguage(Long profileId, LanguageUpdateRequest request) {
        log.info("언어 설정 수정 - profileId: {}", profileId);

        UserProfile profile = userProfileRepository.findById(profileId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROFILE_NOT_FOUND));

        profile.setPrimaryLanguage(request.getPrimaryLanguage());
        profile.setSecondaryLanguage(request.getSecondaryLanguage());

        return UserProfileResponse.fromEntity(profile);
    }

    // 프로필 삭제
    @Transactional
    public void deleteProfile(Long profileId) {
        log.info("프로필 삭제 - profileId: {}", profileId);
        UserProfile profile = userProfileRepository.findById(profileId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROFILE_NOT_FOUND));
        userProfileRepository.delete(profile);
    }

    // 프로필 존재 여부 확인 (최소 하나 이상의 프로필이 있는지)
    public boolean hasProfile(Long userId) {
        return userProfileRepository.existsByUser_UserId(userId);
    }
}
