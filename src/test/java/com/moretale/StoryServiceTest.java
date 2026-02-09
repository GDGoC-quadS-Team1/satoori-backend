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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StoryServiceTest {

    @InjectMocks
    private StoryService storyService;

    @Mock
    private AIStoryService aiStoryService;

    @Mock
    private TTSService ttsService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    private User user;
    private UserProfile profile;
    private final String email = "test@example.com";

    @BeforeEach
    void setUp() {
        // 모든 테스트에서 공통으로 사용할 유저와 프로필 정보 설정
        user = User.builder()
                .userId(1L)
                .email(email)
                .build();

        profile = UserProfile.builder()
                .profileId(3L)
                .childName("민지")
                .primaryLanguage("Korean")
                .secondaryLanguage("English")
                .build();
    }

    @Test
    @DisplayName("프롬프트를 통해 AI 동화를 생성하고 각 슬라이드별로 TTS URL이 포함된 응답을 반환한다")
    void generateStorySuccess() {
        // 1. Given
        StoryGenerateRequest request = StoryGenerateRequest.builder()
                .prompt("정글 모험 이야기")
                .profileId(3L)
                .build();

        // AI가 2개의 슬라이드를 생성했다고 가정
        List<StoryGenerateResponse.GeneratedSlide> mockSlides = new ArrayList<>();
        mockSlides.add(StoryGenerateResponse.GeneratedSlide.builder()
                .order(1).textKr("사자가 나타났어요").textNative("A lion appeared").build());
        mockSlides.add(StoryGenerateResponse.GeneratedSlide.builder()
                .order(2).textKr("코끼리가 도와줬어요").textNative("An elephant helped").build());

        StoryGenerateResponse aiResponse = StoryGenerateResponse.builder()
                .title("정글 모험")
                .childName("민지")
                .slides(mockSlides)
                .build();

        // Mock 객체 동작 정의
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
        given(userProfileRepository.findByProfileIdAndUser_UserId(eq(3L), eq(1L))).willReturn(Optional.of(profile));
        given(aiStoryService.generateStory(anyString(), anyString(), anyString(), anyString())).willReturn(aiResponse);
        given(ttsService.generateTTS(anyString(), anyString())).willReturn("http://audio.url/sample.mp3");

        // 2. When
        StoryGenerateResponse result = storyService.generateStory(email, request);

        // 3. Then
        assertThat(result.getTitle()).isEqualTo("정글 모험");
        assertThat(result.getSlides()).hasSize(2);

        // 데이터 정합성 확인 (TTS URL 주입 여부)
        assertThat(result.getSlides().get(0).getAudioUrlKr()).isEqualTo("http://audio.url/sample.mp3");
        assertThat(result.getSlides().get(0).getAudioUrlNative()).isEqualTo("http://audio.url/sample.mp3");

        // 호출 횟수 및 인자 검증
        // AI 서비스 호출 확인
        verify(aiStoryService, times(1)).generateStory(eq("정글 모험 이야기"), eq("민지"), anyString(), anyString());

        // TTS 서비스 호출 확인 (슬라이드 2개 * 언어 2개 = 총 4번)
        verify(ttsService, times(4)).generateTTS(anyString(), anyString());

        // 특정 언어 코드 조합(Korean-KR)이 서비스 로직 내에서 잘 생성되어 전달되었는지 확인
        verify(ttsService, atLeastOnce()).generateTTS(anyString(), contains("Korean-KR"));
    }
}
