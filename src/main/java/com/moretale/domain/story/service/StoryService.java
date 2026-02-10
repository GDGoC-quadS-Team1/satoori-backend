package com.moretale.domain.story.service;

import com.moretale.domain.story.dto.*;
import com.moretale.domain.story.entity.Slide;
import com.moretale.domain.story.entity.Story;
import com.moretale.domain.story.repository.SlideRepository;
import com.moretale.domain.story.repository.StoryRepository;
import com.moretale.domain.user.entity.User;
import com.moretale.domain.profile.entity.UserProfile;
import com.moretale.domain.profile.repository.UserProfileRepository;
import com.moretale.domain.user.repository.UserRepository;
import com.moretale.global.exception.BusinessException;
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
public class StoryService {

    private final StoryRepository storyRepository;
    private final SlideRepository slideRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final AIStoryService aiStoryService;
    private final TTSService ttsService;

    // 동화 생성 (AI 연동)
    @Transactional
    public StoryGenerateResponse generateStory(String email, StoryGenerateRequest request) {
        User user = getUserByEmail(email);
        UserProfile profile = getUserProfile(user, request.getProfileId());

        String childName = request.getChildName() != null
                ? request.getChildName()
                : profile.getChildName();

        String primaryLang = request.getPrimaryLanguage() != null
                ? request.getPrimaryLanguage()
                : profile.getPrimaryLanguage();

        String secondaryLang = request.getSecondaryLanguage() != null
                ? request.getSecondaryLanguage()
                : profile.getSecondaryLanguage();

        StoryGenerateResponse response = aiStoryService.generateStory(
                request.getPrompt(),
                childName,
                primaryLang,
                secondaryLang
        );

        response.getSlides().forEach(slide -> {
            try {
                if (slide.getTextKr() != null) {
                    slide.setAudioUrlKr(
                            ttsService.generateTTS(slide.getTextKr(), primaryLang + "-KR")
                    );
                }
                if (slide.getTextNative() != null) {
                    slide.setAudioUrlNative(
                            ttsService.generateTTS(
                                    slide.getTextNative(),
                                    secondaryLang + "-" + secondaryLang.toUpperCase()
                            )
                    );
                }
            } catch (Exception e) {
                log.error(
                        "TTS 생성 중 오류 발생 (건너뜀) - slideOrder={}",
                        slide.getOrder(),
                        e
                );
            }
        });

        return response;
    }

    // 동화 저장
    @Transactional
    public StoryResponse saveStory(String email, StorySaveRequest request) {
        User user = getUserByEmail(email);

        // 1. 프로필 존재 여부 확인 (유저 조건 없이 조회)
        UserProfile profile = userProfileRepository.findById(request.getProfileId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));

        // 2. 프로필 소유권 검증
        if (!profile.getUser().getUserId().equals(user.getUserId())) {
            log.warn(
                    "보안 위반 시도 - 유저 {}가 유저 {}의 프로필 {}을 사용하려고 함",
                    user.getUserId(),
                    profile.getUser().getUserId(),
                    profile.getProfileId()
            );
            throw new BusinessException(ErrorCode.STORY_ACCESS_DENIED);
        }

        // 3. Story 엔티티 생성
        Story story = Story.builder()
                .title(request.getTitle())
                .prompt(request.getPrompt())
                .user(user)
                .childName(profile.getChildName())
                .primaryLanguage(profile.getPrimaryLanguage())
                .secondaryLanguage(profile.getSecondaryLanguage())
                .isPublic(false)
                .build();

        // 4. Slide 엔티티 생성 및 연관관계 설정
        if (request.getSlides() != null) {
            request.getSlides().forEach(slideReq -> {
                Slide slide = Slide.builder()
                        .order(slideReq.getOrder())
                        .imageUrl(slideReq.getImageUrl())
                        .textKr(slideReq.getTextKr())
                        .textNative(slideReq.getTextNative())
                        .audioUrlKr(slideReq.getAudioUrlKr())
                        .audioUrlNative(slideReq.getAudioUrlNative())
                        .build();
                story.addSlide(slide);
            });
        }

        // 5. 저장
        Story savedStory = storyRepository.save(story);
        slideRepository.saveAll(savedStory.getSlides());

        log.info(
                "동화 저장 완료 - storyId={}, userId={}",
                savedStory.getStoryId(),
                user.getUserId()
        );

        return StoryResponse.from(savedStory);
    }

    // 특정 동화 상세 조회 (슬라이드 포함)
    public StoryResponse getStoryDetail(String email, Long storyId) {
        User user = getUserByEmail(email);
        Story story = storyRepository.findByIdWithSlides(storyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORY_NOT_FOUND));

        if (!story.getUser().equals(user) && !story.getIsPublic()) {
            throw new BusinessException(ErrorCode.STORY_ACCESS_DENIED);
        }

        return StoryResponse.from(story);
    }

    // 내 동화 목록 조회
    public List<StoryListResponse> getMyStories(String email) {
        User user = getUserByEmail(email);
        return storyRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(StoryListResponse::from)
                .collect(Collectors.toList());
    }

    // 공개 동화 목록 조회
    public List<StoryListResponse> getPublicStories() {
        return storyRepository.findByIsPublicTrueOrderByCreatedAtDesc()
                .stream()
                .map(StoryListResponse::from)
                .collect(Collectors.toList());
    }

    // 동화 공유 설정 변경
    @Transactional
    public void updateStoryShareStatus(String email, Long storyId, StoryShareRequest request) {
        User user = getUserByEmail(email);
        Story story = storyRepository.findByStoryIdAndUser(storyId, user)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORY_NOT_FOUND));

        story.setIsPublic(request.getIsPublic());
    }

    // 동화 삭제
    @Transactional
    public void deleteStory(String email, Long storyId) {
        User user = getUserByEmail(email);
        Story story = storyRepository.findByStoryIdAndUser(storyId, user)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORY_NOT_FOUND));

        storyRepository.delete(story);
    }

    // 이메일로 사용자 조회
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private UserProfile getUserProfile(User user, Long profileId) {
        if (profileId != null) {
            return userProfileRepository
                    .findByProfileIdAndUser_UserId(profileId, user.getUserId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
        }
        return userProfileRepository
                .findFirstByUserOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
    }
}
