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

@Service
@RequiredArgsConstructor
@Slf4j
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

        // 사용자 프로필에서 기본값 가져오기
        String childName = request.getChildName() != null ?
                request.getChildName() : profile.getChildName();
        String primaryLang = request.getPrimaryLanguage() != null ?
                request.getPrimaryLanguage() : profile.getPrimaryLanguage();
        String secondaryLang = request.getSecondaryLanguage() != null ?
                request.getSecondaryLanguage() : profile.getSecondaryLanguage();

        // AI를 통한 동화 생성
        StoryGenerateResponse response = aiStoryService.generateStory(
                request.getPrompt(), childName, primaryLang, secondaryLang);

        // 각 슬라이드에 TTS 추가
        response.getSlides().forEach(slide -> {
            if (slide.getTextKr() != null) {
                String audioUrlKr = ttsService.generateTTS(slide.getTextKr(), primaryLang + "-KR");
                slide.setAudioUrlKr(audioUrlKr);
            }
            if (slide.getTextNative() != null) {
                String audioUrlNative = ttsService.generateTTS(slide.getTextNative(),
                        secondaryLang + "-" + secondaryLang.toUpperCase());
                slide.setAudioUrlNative(audioUrlNative);
            }
        });

        return response;
    }

    // 동화 저장
    @Transactional
    public StoryResponse saveStory(String email, StorySaveRequest request) {
        User user = getUserByEmail(email);
        UserProfile profile = getUserProfile(user, request.getProfileId());

        // Story 엔티티 생성
        Story story = Story.builder()
                .title(request.getTitle())
                .prompt(request.getPrompt())
                .user(user)
                .childName(profile.getChildName())
                .primaryLanguage(profile.getPrimaryLanguage())
                .secondaryLanguage(profile.getSecondaryLanguage())
                .isPublic(false)
                .build();

        // Slide 엔티티 생성 및 연관관계 설정
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

        Story savedStory = storyRepository.save(story);
        log.info("동화 저장 완료 - storyId: {}, userId: {}, profileId: {}",
                savedStory.getStoryId(), user.getUserId(), profile.getProfileId());

        return StoryResponse.from(savedStory);
    }

    // 특정 동화 상세 조회 (슬라이드 포함)
    public StoryResponse getStoryDetail(String email, Long storyId) {
        User user = getUserByEmail(email);
        Story story = storyRepository.findByIdWithSlides(storyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORY_NOT_FOUND));

        // 권한 체크: 본인 동화이거나 공개된 동화만 조회 가능
        if (!story.getUser().equals(user) && !story.getIsPublic()) {
            throw new BusinessException(ErrorCode.STORY_ACCESS_DENIED);
        }

        return StoryResponse.from(story);
    }

    // 내 동화 목록 조회
    public List<StoryListResponse> getMyStories(String email) {
        User user = getUserByEmail(email);
        List<Story> stories = storyRepository.findByUserOrderByCreatedAtDesc(user);

        return stories.stream()
                .map(StoryListResponse::from)
                .collect(Collectors.toList());
    }

    // 공개 동화 목록 조회
    public List<StoryListResponse> getPublicStories() {
        List<Story> stories = storyRepository.findByIsPublicTrueOrderByCreatedAtDesc();

        return stories.stream()
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
        log.info("동화 공유 설정 변경 - storyId: {}, isPublic: {}", storyId, request.getIsPublic());
    }

    // 동화 삭제
    @Transactional
    public void deleteStory(String email, Long storyId) {
        User user = getUserByEmail(email);
        Story story = storyRepository.findByStoryIdAndUser(storyId, user)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORY_NOT_FOUND));

        storyRepository.delete(story);
        log.info("동화 삭제 완료 - storyId: {}, userId: {}", storyId, user.getUserId());
    }

    // 이메일로 사용자 조회
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    // 사용자 프로필 조회 (profileId 있으면 해당 프로필, 없으면 최신 프로필)
    private UserProfile getUserProfile(User user, Long profileId) {
        if (profileId != null) {
            // 특정 프로필 ID로 조회 (권한 체크 포함)
            log.info("특정 프로필 조회 - userId: {}, profileId: {}", user.getUserId(), profileId);
            return userProfileRepository.findByProfileIdAndUser_UserId(profileId, user.getUserId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
        } else {
            // 프로필 ID가 없으면 가장 최근 프로필 사용
            log.info("최근 프로필 자동 선택 - userId: {}", user.getUserId());
            return userProfileRepository.findFirstByUserOrderByCreatedAtDesc(user)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
        }
    }
}
