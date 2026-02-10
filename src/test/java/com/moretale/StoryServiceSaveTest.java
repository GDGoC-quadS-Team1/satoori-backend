package com.moretale;

import com.moretale.domain.profile.entity.UserProfile;
import com.moretale.domain.profile.repository.UserProfileRepository;
import com.moretale.domain.story.dto.StoryResponse;
import com.moretale.domain.story.dto.StorySaveRequest;
import com.moretale.domain.story.entity.Story;
import com.moretale.domain.story.repository.SlideRepository;
import com.moretale.domain.story.repository.StoryRepository;
import com.moretale.domain.story.service.StoryService;
import com.moretale.domain.user.entity.User;
import com.moretale.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class StoryServiceSaveTest {

    @InjectMocks
    private StoryService storyService;

    @Mock
    private StoryRepository storyRepository;

    @Mock
    private SlideRepository slideRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    private User user;
    private UserProfile profile;
    private final String email = "chae_y@sookmyung.ac.kr";

    @BeforeEach
    void setUp() {
        user = User.builder()
                .userId(3L)
                .email(email)
                .build();

        profile = UserProfile.builder()
                .profileId(2L)
                .childName("유찬")
                .primaryLanguage("ko")
                .secondaryLanguage("zh")
                .user(user)
                .build();
    }

    @Test
    @DisplayName("동화 저장 요청 시 Story와 Slide 연관 관계가 정상적으로 맺어지며 저장된다")
    void saveStorySuccessTest() {
        // Given
        StorySaveRequest.SlideRequest slideReq1 = StorySaveRequest.SlideRequest.builder()
                .order(1)
                .textKr("사자")
                .imageUrl("img1.png")
                .build();

        StorySaveRequest request = StorySaveRequest.builder()
                .title("동물 친구들")
                .prompt("정글 이야기")
                .profileId(2L)
                .slides(List.of(slideReq1))
                .build();

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        // 서비스 로직 변경에 맞춰 findById 사용
        given(userProfileRepository.findById(2L)).willReturn(Optional.of(profile));

        given(storyRepository.save(any(Story.class))).willAnswer(invocation -> {
            Story story = invocation.getArgument(0);
            return story;
        });

        // When
        StoryResponse result = storyService.saveStory(email, request);

        // Then
        assertThat(result.getTitle()).isEqualTo("동물 친구들");
        assertThat(result.getChildName()).isEqualTo("유찬");
        verify(storyRepository).save(any(Story.class));
    }

    @Test
    @DisplayName("동화 저장 시 이중언어 데이터가 누락 없이 매핑되어야 한다")
    void saveStory_DualLanguageMapping_Success() {
        // Given
        StorySaveRequest.SlideRequest slideReq = StorySaveRequest.SlideRequest.builder()
                .order(1)
                .textKr("유찬이는 1번째 장면을 봤어요.")
                .textNative("Sample text in zh")
                .audioUrlKr("https://moretale.ai/ko.mp3")
                .audioUrlNative("https://moretale.ai/zh.mp3")
                .imageUrl("https://moretale.ai/image1.png")
                .build();

        StorySaveRequest request = StorySaveRequest.builder()
                .title("유찬의 모험")
                .profileId(2L)
                .slides(List.of(slideReq))
                .build();

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        // 서비스 로직 변경에 맞춰 findById 사용
        given(userProfileRepository.findById(2L)).willReturn(Optional.of(profile));

        given(storyRepository.save(any(Story.class))).willAnswer(invocation -> invocation.getArgument(0));

        // When
        StoryResponse response = storyService.saveStory(email, request);

        // Then
        assertThat(response.getPrimaryLanguage()).isEqualTo("ko");
        assertThat(response.getSecondaryLanguage()).isEqualTo("zh");
        assertThat(response.getSlides().get(0).getTextNative()).isEqualTo("Sample text in zh");
        assertThat(response.getSlides().get(0).getAudioUrlKr()).isEqualTo("https://moretale.ai/ko.mp3");
    }
}
