package com.moretale;

import com.moretale.domain.story.entity.Slide;
import com.moretale.domain.story.entity.Story;
import com.moretale.domain.story.repository.SlideRepository;
import com.moretale.domain.story.repository.StoryRepository;
import com.moretale.domain.story.service.StoryService;
import com.moretale.domain.user.entity.User;
import com.moretale.domain.user.entity.User.Role;
import com.moretale.domain.user.repository.UserRepository;
import com.moretale.global.exception.BusinessException;
import com.moretale.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
public class StoryServiceSecurityTest {

    @Autowired
    private StoryService storyService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoryRepository storyRepository;

    @Autowired
    private SlideRepository slideRepository;

    private User owner;
    private User hacker;
    private Story privateStory;

    @BeforeEach
    void setUp() {
        // 1. 동화 주인 생성
        owner = userRepository.save(User.builder()
                .email("owner@example.com").nickname("주인").role(Role.USER).build());

        // 2. 남의 동화 넘보는 사용자 생성
        hacker = userRepository.save(User.builder()
                .email("hacker@example.com").nickname("해커").role(Role.USER).build());

        // 3. 주인의 비공개 동화 생성 (isPublic = false)
        Story story = Story.builder()
                .title("주인의 비밀 일기")
                .user(owner)
                .isPublic(false)
                .build();

        // 슬라이드 추가 (삭제 테스트용)
        story.addSlide(Slide.builder().order(1).textKr("비밀 내용").build());

        privateStory = storyRepository.save(story);
    }

    @Test
    @DisplayName("타인의 비공개 동화를 상세 조회하려 하면 STORY_ACCESS_DENIED 예외가 발생한다")
    void getStoryDetail_AccessDeniedTest() {
        // when & then
        assertThatThrownBy(() -> storyService.getStoryDetail(hacker.getEmail(), privateStory.getStoryId()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STORY_ACCESS_DENIED);
    }

    @Test
    @DisplayName("동화를 삭제하면 연관된 슬라이드들도 DB에서 함께 삭제된다 (Orphan Removal)")
    void deleteStory_WithSlidesTest() {
        // given
        Long storyId = privateStory.getStoryId();
        Long slideId = privateStory.getSlides().get(0).getSlideId();

        // 삭제 전 존재 확인
        assertThat(storyRepository.existsById(storyId)).isTrue();
        assertThat(slideRepository.existsById(slideId)).isTrue();

        // when
        storyService.deleteStory(owner.getEmail(), storyId);

        // then
        // 1. 동화가 삭제되었는지 확인
        assertThat(storyRepository.existsById(storyId)).isFalse();

        // 2. [핵심] 고아 객체 제거/Cascade 기능으로 인해 슬라이드도 삭제되었는지 확인
        assertThat(slideRepository.existsById(slideId)).isFalse();
    }
}
