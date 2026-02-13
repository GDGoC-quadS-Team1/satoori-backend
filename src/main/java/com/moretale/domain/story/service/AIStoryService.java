package com.moretale.domain.story.service;

import com.moretale.domain.story.dto.StoryGenerateRequest;
import com.moretale.domain.story.dto.StoryGenerateResponse;

public interface AIStoryService {

    // AI를 통해 이중언어 동화 생성
    StoryGenerateResponse generateStory(
            String prompt,
            String childName,
            String primaryLanguage,
            String secondaryLanguage,
            StoryGenerateRequest request
    );

    default StoryGenerateResponse generateStory(
            String prompt,
            String childName,
            String primaryLanguage,
            String secondaryLanguage
    ) {
        return generateStory(prompt, childName, primaryLanguage, secondaryLanguage, null);
    }
}
