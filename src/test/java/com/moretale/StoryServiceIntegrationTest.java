package com.moretale;

import com.moretale.domain.profile.entity.UserProfile;
import com.moretale.domain.profile.repository.UserProfileRepository;
import com.moretale.domain.story.dto.StoryResponse;
import com.moretale.domain.story.dto.StorySaveRequest;
import com.moretale.domain.story.entity.Story;
import com.moretale.domain.story.repository.StoryRepository;
import com.moretale.domain.story.service.StoryService;
import com.moretale.domain.user.entity.User;
import com.moretale.domain.user.entity.User.Role;
import com.moretale.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class StoryServiceIntegrationTest {

    @Autowired
    private StoryService storyService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private StoryRepository storyRepository;

    private User savedUser;
    private UserProfile savedProfile;

    @BeforeEach
    void setUp() {
        // 1. User 생성 및 저장
        // User.Role.USER 형식으로 직접 접근하거나, import 후 Role.USER 사용
        User user = User.builder()
                .email("real-test@example.com")
                .nickname("테스터")
                .role(Role.USER)
                .build();
        savedUser = userRepository.save(user);

        // 2. UserProfile 생성 및 저장
        UserProfile profile = UserProfile.builder()
                .childName("민지")
                .childAge(5)
                .childNationality("South Korea")
                .parentCountry("South Korea")
                .primaryLanguage("ko")
                .secondaryLanguage("en")
                .user(savedUser)
                .build();
        savedProfile = userProfileRepository.save(profile);
    }

    @Test
    @DisplayName("saveStory 실행 시 PostgreSQL DB에 Story와 Slide가 함께 영속화된다")
    void saveStoryRealDbTest() {
        // given
        StorySaveRequest.SlideRequest slide1 = StorySaveRequest.SlideRequest.builder()
                .order(1).textKr("첫 번째 장면").textNative("Scene one").imageUrl("https://image1.png").build();
        StorySaveRequest.SlideRequest slide2 = StorySaveRequest.SlideRequest.builder()
                .order(2).textKr("두 번째 장면").textNative("Scene two").imageUrl("https://image2.png").build();

        StorySaveRequest request = StorySaveRequest.builder()
                .title("DB 통합 테스트 동화")
                .prompt("테스트 프롬프트")
                .profileId(savedProfile.getProfileId())
                .slides(List.of(slide1, slide2))
                .build();

        // when
        StoryResponse response = storyService.saveStory(savedUser.getEmail(), request);

        // then
        assertThat(response.getStoryId()).isNotNull();

        Story foundStory = storyRepository.findByIdWithSlides(response.getStoryId())
                .orElseThrow(() -> new AssertionError("저장된 동화를 찾을 수 없습니다."));

        assertThat(foundStory.getTitle()).isEqualTo("DB 통합 테스트 동화");
        assertThat(foundStory.getSlides()).hasSize(2);

        System.out.println("통합 테스트 성공: 생성된 Story ID = " + foundStory.getStoryId());
    }
}
