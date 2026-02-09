package com.moretale;

import com.moretale.domain.profile.entity.UserProfile;
import com.moretale.domain.profile.repository.UserProfileRepository;
import com.moretale.domain.story.dto.StoryGenerateRequest;
import com.moretale.domain.story.dto.StoryGenerateResponse;
import com.moretale.domain.story.service.AIStoryService;
import com.moretale.domain.story.service.StoryService;
import com.moretale.domain.story.service.TTSService;
import com.moretale.domain.user.entity.User;
import com.moretale.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class StoryServiceBusinessLogicTest {

    @InjectMocks
    private StoryService storyService;

    @Mock private AIStoryService aiStoryService;
    @Mock private TTSService ttsService;
    @Mock private UserRepository userRepository;
    @Mock private UserProfileRepository userProfileRepository;

    @Test
    @DisplayName("아이 이름 미입력 시 프로필의 이름이 적용되고, 언어별로 정확한 TTS 코드가 매칭된다")
    void personalizationAndBilingualTtsTest() {
        // 1. Given: '유찬'과 '중국어(zh)' 설정 프로필 준비
        String email = "hwaying@example.com";
        User user = User.builder().userId(1L).email(email).build();
        UserProfile profile = UserProfile.builder()
                .profileId(10L).childName("유찬")
                .primaryLanguage("ko").secondaryLanguage("zh").build();

        // 요청 데이터 (childName을 비워서 보냄)
        StoryGenerateRequest request = StoryGenerateRequest.builder()
                .prompt("엄마와 함께하는 요리")
                .profileId(10L).build();

        // AI 응답 모킹
        StoryGenerateResponse aiResponse = StoryGenerateResponse.builder()
                .title("유찬이의 요리").slides(List.of(
                        StoryGenerateResponse.GeneratedSlide.builder()
                                .textKr("맛있는 만두").textNative("好吃的饺子").build()
                )).build();

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(userProfileRepository.findByProfileIdAndUser_UserId(10L, 1L)).willReturn(Optional.of(profile));
        given(aiStoryService.generateStory(anyString(), anyString(), anyString(), anyString())).willReturn(aiResponse);

        // 2. When: 서비스 실행
        storyService.generateStory(email, request);

        // 3. Then:
        // 1) 개인화 확인: AI 호출 시 아이 이름이 '유찬'으로 넘어갔는지 확인
        verify(aiStoryService).generateStory(eq("엄마와 함께하는 요리"), eq("유찬"), eq("ko"), eq("zh"));

        // 2) TTS 매칭 확인: 한국어(ko-KR)와 중국어(zh-ZH) 코드로 각각 요청되었는지 확인
        verify(ttsService).generateTTS(eq("맛있는 만두"), eq("ko-KR"));
        verify(ttsService).generateTTS(eq("好吃的饺子"), eq("zh-ZH"));
    }
}
