package com.moretale.domain.story.service.impl;

import com.moretale.domain.story.dto.StoryGenerateRequest;
import com.moretale.domain.story.dto.StoryGenerateResponse;
import com.moretale.domain.story.service.AIStoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIStoryServiceImpl implements AIStoryService {

    // TODO: 실제 AI API 연동 (Gemini)

    @Override
    public StoryGenerateResponse generateStory(
            String prompt,
            String childName,
            String primaryLanguage,
            String secondaryLanguage,
            StoryGenerateRequest request
    ) {
        log.info("AI 동화 생성 요청 (확장)");
        log.info("- prompt: {}", prompt);
        log.info("- childName: {}", childName);
        log.info("- languages: {}/{}", primaryLanguage, secondaryLanguage);

        if (request != null) {
            log.info("- ageGroup: {}", request.getAgeGroup());
            log.info("- proficiency: 1st={}, 2nd={}",
                    request.getFirstLanguageProficiency(),
                    request.getSecondLanguageProficiency());
            log.info("- storyPreference: {}", request.getStoryPreference());
            log.info("- recommendedTale: {}", request.getRecommendedTaleTitle());
        }

        // TODO: 실제 AI API 호출
        // 1. prompt를 AI 모델에 전달
        // 2. request의 제약 조건(난이도, 나이)을 반영한 응답 생성
        // 3. 5개의 슬라이드 구조로 파싱 (추후 수정 필요)

        // 임시 더미 데이터 (실제로는 AI API 응답 파싱)
        List<StoryGenerateResponse.GeneratedSlide> slides = new ArrayList<>();

        String baseTaleTitle = (request != null && request.getRecommendedTaleTitle() != null)
                ? request.getRecommendedTaleTitle()
                : childName + "의 모험";

        for (int i = 1; i <= 5; i++) {
            slides.add(StoryGenerateResponse.GeneratedSlide.builder()
                    .order(i)
                    .imageUrl("https://storage.moretale.ai/temp/" + baseTaleTitle + "-slide" + i + ".png")
                    .textKr(generateKoreanText(childName, i, request))
                    .textNative(generateNativeText(i, secondaryLanguage, request))
                    .audioUrlKr(null) // TTS는 별도로 생성
                    .audioUrlNative(null)
                    .build());
        }

        return StoryGenerateResponse.builder()
                .title(baseTaleTitle)
                .childName(childName)
                .primaryLanguage(primaryLanguage)
                .secondaryLanguage(secondaryLanguage)
                .slides(slides)
                .build();
    }

    // 한국어 텍스트 생성 (난이도 반영 더미)
    private String generateKoreanText(String childName, int slideNumber, StoryGenerateRequest request) {
        // TODO: 실제로는 AI가 생성
        if (request != null && request.getFirstLanguageProficiency() != null) {
            switch (request.getFirstLanguageProficiency()) {
                case EGG:
                    return childName + " 가요. " + slideNumber + " 봐요.";
                case LARVA:
                    return childName + "이는 " + slideNumber + "번째 장면을 봤어요.";
                case PUPA:
                    return childName + "이는 " + slideNumber + "번째 장면에서 새로운 모험을 시작했어요.";
                case BEE:
                    return childName + "이는 " + slideNumber + "번째 장면에서 용기를 내어 커다란 도전에 맞섰습니다.";
            }
        }
        return childName + "이는 " + slideNumber + "번째 장면을 봤어요.";
    }

    // 보조 언어 텍스트 생성 (난이도 반영 더미)
    private String generateNativeText(int slideNumber, String language, StoryGenerateRequest request) {
        // TODO: 실제로는 AI가 해당 언어로 생성
        return "Sample text in " + language + " - slide " + slideNumber;
    }
}
