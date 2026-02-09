package com.moretale.domain.story.service;

import com.moretale.domain.story.dto.StoryGenerateResponse;

public interface AIStoryService {

    // AI를 통해 이중언어 동화 생성
    StoryGenerateResponse generateStory(
            String prompt,            // 사용자 입력 프롬프트
            String childName,          // 아이 이름
            String primaryLanguage,    // 기본 언어 (ex. ko)
            String secondaryLanguage   // 부모 언어 (ex. ja)
    );
}
