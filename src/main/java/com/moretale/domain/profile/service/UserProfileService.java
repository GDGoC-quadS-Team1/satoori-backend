package com.moretale.domain.profile.service;

import com.moretale.domain.profile.dto.*;
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

    // 온보딩용 프로필 생성
    @Transactional
    public OnboardingProfileResponse createOnboardingProfile(Long userId, OnboardingProfileRequest request) {
        log.info("온보딩 프로필 생성 시작 - userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 동일한 이름의 아이가 이미 등록되어 있는지 체크
        if (userProfileRepository.existsByUser_UserIdAndChildName(userId, request.getChildName())) {
            throw new CustomException(ErrorCode.PROFILE_ALREADY_EXISTS);
        }

        UserProfile profile = UserProfile.builder()
                .user(user)
                .childName(request.getChildName())
                .ageGroup(request.getAgeGroup())
                .firstLanguage(request.getFirstLanguage())
                .firstLanguageProficiency(request.getFirstLanguageProficiency())
                .secondLanguage(request.getSecondLanguage())
                .secondLanguageProficiency(request.getSecondLanguageProficiency())
                .firstLanguageListening(request.getFirstLanguageListening())
                .firstLanguageSpeaking(request.getFirstLanguageSpeaking())
                .secondLanguageListening(request.getSecondLanguageListening())
                .secondLanguageSpeaking(request.getSecondLanguageSpeaking())
                .familyStructure(request.getFamilyStructure())
                .customFamilyStructure(request.getCustomFamilyStructure())
                .storyPreference(request.getStoryPreference())
                .customStoryPreference(request.getCustomStoryPreference())
                .childNationality(request.getChildNationality())
                .parentCountry(request.getParentCountry())
                .build();

        // 하위 호환성 유지 (primary/secondaryLanguage 세팅)
        profile.syncLegacyLanguages();

        UserProfile savedProfile = userProfileRepository.save(profile);
        log.info("온보딩 프로필 생성 완료 - profileId: {}, childName: {}",
                savedProfile.getProfileId(), savedProfile.getChildName());

        return OnboardingProfileResponse.fromEntity(savedProfile);
    }

    // 기본 프로필 생성
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
                .ageGroup(request.getAgeGroup())
                .firstLanguage(request.getFirstLanguage())
                .firstLanguageProficiency(request.getFirstLanguageProficiency())
                .secondLanguage(request.getSecondLanguage())
                .secondLanguageProficiency(request.getSecondLanguageProficiency())
                .firstLanguageListening(request.getFirstLanguageListening())
                .firstLanguageSpeaking(request.getFirstLanguageSpeaking())
                .secondLanguageListening(request.getSecondLanguageListening())
                .secondLanguageSpeaking(request.getSecondLanguageSpeaking())
                .familyStructure(request.getFamilyStructure())
                .customFamilyStructure(request.getCustomFamilyStructure())
                .storyPreference(request.getStoryPreference())
                .customStoryPreference(request.getCustomStoryPreference())
                .childNationality(request.getChildNationality())
                .parentCountry(request.getParentCountry())
                .build();

        profile.syncLegacyLanguages();

        UserProfile savedProfile = userProfileRepository.save(profile);
        log.info("프로필 생성 완료 - profileId: {}", savedProfile.getProfileId());

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
    public UserProfileResponse updateProfile(Long userId, Long profileId, UserProfileRequest request) {
        log.info("프로필 수정 시작 - userId: {}, profileId: {}", userId, profileId);

        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 프로필 존재 확인
        UserProfile profile = userProfileRepository.findById(profileId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROFILE_NOT_FOUND));

        // 본인의 프로필인지 권한 확인
        if (!profile.getUser().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // 프로필 업데이트 메서드 사용
        profile.updateProfile(
                request.getChildName(),
                request.getAgeGroup(),
                request.getFirstLanguage(),
                request.getFirstLanguageProficiency(),
                request.getSecondLanguage(),
                request.getSecondLanguageProficiency(),
                request.getFirstLanguageListening(),
                request.getFirstLanguageSpeaking(),
                request.getSecondLanguageListening(),
                request.getSecondLanguageSpeaking(),
                request.getFamilyStructure(),
                request.getCustomFamilyStructure(),
                request.getStoryPreference(),
                request.getCustomStoryPreference(),
                request.getChildNationality(),
                request.getParentCountry()
        );

        log.info("프로필 수정 완료 - profileId: {}, childName: {}", profile.getProfileId(), profile.getChildName());

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
