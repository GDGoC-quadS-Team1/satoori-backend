package com.moretale.domain.story.service.impl;

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
    public StoryGenerateResponse generateStory(String prompt, String childName,
                                               String primaryLanguage, String secondaryLanguage) {

        log.info("AI 동화 생성 요청 - prompt: {}, childName: {}, languages: {}/{}",
                prompt, childName, primaryLanguage, secondaryLanguage);

        // 임시 더미 데이터 (실제로는 AI API 호출)
        List<StoryGenerateResponse.GeneratedSlide> slides = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            slides.add(StoryGenerateResponse.GeneratedSlide.builder()
                    .order(i)
                    .imageUrl("https://storage.moretale.ai/temp/slide" + i + ".png")
                    .textKr(childName + "이는 " + i + "번째 장면을 봤어요.")
                    .textNative("Sample text in native language - slide " + i)
                    .audioUrlKr(null) // TTS는 별도로 생성
                    .audioUrlNative(null)
                    .build());
        }

        return StoryGenerateResponse.builder()
                .title(childName + "의 모험")
                .childName(childName)
                .primaryLanguage(primaryLanguage)
                .secondaryLanguage(secondaryLanguage)
                .slides(slides)
                .build();
    }
}
