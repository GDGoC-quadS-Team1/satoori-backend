package com.moretale;

import com.moretale.domain.profile.entity.UserProfile;
import com.moretale.domain.profile.repository.UserProfileRepository;
import com.moretale.domain.story.dto.StoryResponse;
import com.moretale.domain.story.dto.StorySaveRequest;
import com.moretale.domain.story.dto.StoryShareRequest;
import com.moretale.domain.story.entity.Story;
import com.moretale.domain.story.repository.StoryRepository;
import com.moretale.domain.story.service.StoryService;
import com.moretale.domain.user.entity.User;
import com.moretale.domain.user.repository.UserRepository;
import com.moretale.global.exception.BusinessException;
import com.moretale.global.exception.ErrorCode;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class StoryServiceDetailTest {

    @InjectMocks
    private StoryService storyService;

    @Mock
    private StoryRepository storyRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserProfileRepository userProfileRepository;

    private User user;
    private User otherUser;
    private UserProfile profile;
    private Story story;

    @BeforeEach
    void setUp() {
        user = User.builder().userId(1L).email("test@example.com").build();
        otherUser = User.builder().userId(2L).email("other@example.com").build();
        profile = UserProfile.builder().profileId(3L).childName("민지").user(user).build();

        story = Story.builder()
                .storyId(100L)
                .title("테스트 동화")
                .user(user)
                .isPublic(false)
                .slides(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("동화 저장: 슬라이드 목록을 포함하여 동화를 정상적으로 저장한다")
    void saveStory_Success() {
        // given
        StorySaveRequest.SlideRequest slideReq = StorySaveRequest.SlideRequest.builder()
                .order(1).textKr("안녕").imageUrl("http://image.png").build();

        StorySaveRequest request = StorySaveRequest.builder()
                .title("새로운 동화")
                .profileId(3L)
                .slides(List.of(slideReq))
                .build();

        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
        given(userProfileRepository.findByProfileIdAndUser_UserId(anyLong(), anyLong())).willReturn(Optional.of(profile));
        given(storyRepository.save(any(Story.class))).willReturn(story);

        // when
        StoryResponse response = storyService.saveStory(user.getEmail(), request);

        // then
        assertThat(response.getStoryId()).isEqualTo(100L);
        verify(storyRepository).save(any(Story.class));
    }

    @Test
    @DisplayName("상세 조회: 본인의 동화인 경우 상세 정보를 반환한다")
    void getStoryDetail_Owner_Success() {
        // given
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
        given(storyRepository.findByIdWithSlides(anyLong())).willReturn(Optional.of(story));

        // when
        StoryResponse response = storyService.getStoryDetail(user.getEmail(), 100L);

        // then
        assertThat(response.getTitle()).isEqualTo("테스트 동화");
    }

    @Test
    @DisplayName("상세 조회 실패: 타인의 비공개 동화에 접근하면 예외가 발생한다")
    void getStoryDetail_AccessDenied() {
        // given
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(otherUser));
        given(storyRepository.findByIdWithSlides(anyLong())).willReturn(Optional.of(story));

        // when & then
        assertThatThrownBy(() -> storyService.getStoryDetail(otherUser.getEmail(), 100L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STORY_ACCESS_DENIED);
    }

    @Test
    @DisplayName("공유 설정: 본인의 동화 공유 상태를 변경할 수 있다")
    void updateShareStatus_Success() {
        // given
        StoryShareRequest request = new StoryShareRequest();
        request.setIsPublic(true);

        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
        given(storyRepository.findByStoryIdAndUser(anyLong(), any(User.class))).willReturn(Optional.of(story));

        // when
        storyService.updateStoryShareStatus(user.getEmail(), 100L, request);

        // then
        assertThat(story.getIsPublic()).isTrue();
    }

    @Test
    @DisplayName("동화 삭제: 본인의 동화를 정상적으로 삭제한다")
    void deleteStory_Success() {
        // given
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
        given(storyRepository.findByStoryIdAndUser(anyLong(), any(User.class))).willReturn(Optional.of(story));

        // when
        storyService.deleteStory(user.getEmail(), 100L);

        // then
        verify(storyRepository).delete(story);
    }
}
