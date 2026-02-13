package com.moretale.domain.story.service;

import com.moretale.domain.profile.entity.StoryPreference;
import com.moretale.domain.story.dto.*;
import com.moretale.domain.story.entity.Slide;
import com.moretale.domain.story.entity.Story;
import com.moretale.domain.story.enums.TraditionalTale;
import com.moretale.domain.story.repository.SlideRepository;
import com.moretale.domain.story.repository.StoryRepository;
import com.moretale.domain.story.util.PromptBuilder;
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

    // 온보딩 데이터 기반 동화 생성 초기값 조회
    // GET /api/stories/init
    public StoryInitResponse getStoryInitData(String email, Long profileId) {
        User user = getUserByEmail(email);
        UserProfile profile = getUserProfile(user, profileId);

        // 이야기 선호도에 맞는 전래동화 자동 매핑
        TraditionalTale recommendedTale;

        if (profile.getStoryPreference() == StoryPreference.CUSTOM &&
                profile.getCustomStoryPreference() != null) {
            // 🔧 CUSTOM인 경우 customStoryPreference 텍스트 분석
            recommendedTale = TraditionalTale.findByCustomText(
                    profile.getCustomStoryPreference()
            );
        } else {
            // 일반적인 경우
            recommendedTale = TraditionalTale.findByPreference(
                    profile.getStoryPreference()
            );
        }

        log.info("동화 초기값 조회 - userId={}, profileId={}, 추천 전래동화={}",
                user.getUserId(), profile.getProfileId(), recommendedTale.getTitle());

        return StoryInitResponse.from(profile, recommendedTale.getTitle());
    }

    // 온보딩 직후 자동 동화 생성 (추천 동화)
    // POST /api/stories/auto-generate
    @Transactional
    public StoryGenerateResponse autoGenerateStory(String email, Long profileId) {
        User user = getUserByEmail(email);
        UserProfile profile = getUserProfile(user, profileId);

        // 추천 전래동화 선택
        TraditionalTale tale;

        if (profile.getStoryPreference() == StoryPreference.CUSTOM &&
                profile.getCustomStoryPreference() != null) {
            // 🔧 CUSTOM인 경우 customStoryPreference 텍스트 분석
            tale = TraditionalTale.findByCustomText(profile.getCustomStoryPreference());

            // CUSTOM이면 전래동화 대신 사용자 입력 텍스트 사용
            if (tale == TraditionalTale.CUSTOM) {
                log.info("사용자 맞춤 동화 생성 - customStoryPreference 사용");
            }
        } else {
            tale = TraditionalTale.findByPreference(profile.getStoryPreference());
        }

        log.info("자동 동화 생성 시작 - userId={}, profileId={}, 전래동화={}",
                user.getUserId(), profile.getProfileId(), tale.getTitle());

        // 프롬프트 결정: CUSTOM이면 사용자 입력, 아니면 전래동화 설명
        String basePrompt = (tale == TraditionalTale.CUSTOM && profile.getCustomStoryPreference() != null)
                ? profile.getCustomStoryPreference()
                : tale.getDescription();

        // 자동 생성 요청 구성
        StoryGenerateRequest autoRequest = StoryGenerateRequest.builder()
                .prompt(basePrompt)
                .profileId(profileId)
                .childName(profile.getChildName())
                .primaryLanguage(profile.getFirstLanguage())
                .secondaryLanguage(profile.getSecondLanguage())
                .ageGroup(profile.getAgeGroup())
                .childAge(profile.getChildAge())
                .firstLanguageProficiency(profile.getFirstLanguageProficiency())
                .secondLanguageProficiency(profile.getSecondLanguageProficiency())
                .firstLanguageListening(profile.getFirstLanguageListening())
                .firstLanguageSpeaking(profile.getFirstLanguageSpeaking())
                .secondLanguageListening(profile.getSecondLanguageListening())
                .secondLanguageSpeaking(profile.getSecondLanguageSpeaking())
                .storyPreference(profile.getStoryPreference())
                .customStoryPreference(profile.getCustomStoryPreference())
                .autoGenerated(true)
                .recommendedTaleTitle(tale.getTitle())
                .build();

        // 동화 생성 (기존 generateStory 재사용)
        return generateStory(email, autoRequest);
    }

    // 동화 생성 (AI 연동) - 확장 버전
    // 온보딩 데이터 기반 제약 조건 반영
    // 지능형 프롬프트 조립
    @Transactional
    public StoryGenerateResponse generateStory(String email, StoryGenerateRequest request) {
        User user = getUserByEmail(email);
        UserProfile profile = getUserProfile(user, request.getProfileId());

        // 값 병합: 요청값이 없으면 프로필 데이터 사용
        String childName = request.getChildName() != null
                ? request.getChildName()
                : profile.getChildName();

        String primaryLang = request.getPrimaryLanguage() != null
                ? request.getPrimaryLanguage()
                : profile.getFirstLanguage();

        String secondaryLang = request.getSecondaryLanguage() != null
                ? request.getSecondaryLanguage()
                : profile.getSecondLanguage();

        // 온보딩 데이터가 요청에 없으면 프로필에서 가져옴
        if (request.getAgeGroup() == null) {
            request.setAgeGroup(profile.getAgeGroup());
            request.setChildAge(profile.getChildAge());
            request.setFirstLanguageProficiency(profile.getFirstLanguageProficiency());
            request.setSecondLanguageProficiency(profile.getSecondLanguageProficiency());
            request.setFirstLanguageListening(profile.getFirstLanguageListening());
            request.setFirstLanguageSpeaking(profile.getFirstLanguageSpeaking());
            request.setSecondLanguageListening(profile.getSecondLanguageListening());
            request.setSecondLanguageSpeaking(profile.getSecondLanguageSpeaking());
            request.setStoryPreference(profile.getStoryPreference());
            request.setCustomStoryPreference(profile.getCustomStoryPreference());
        }

        // 지능형 프롬프트 조립
        String enhancedPrompt = PromptBuilder.buildPrompt(
                request, childName, primaryLang, secondaryLang
        );

        // AI 동화 생성
        StoryGenerateResponse response = aiStoryService.generateStory(
                enhancedPrompt,
                childName,
                primaryLang,
                secondaryLang,
                request // 온보딩 제약 조건 전달
        );

        // TTS 생성
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

        log.info("동화 생성 완료 - userId={}, 자동생성={}, 제목={}",
                user.getUserId(), request.getAutoGenerated(), response.getTitle());

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
