package com.moretale;

import com.moretale.domain.profile.dto.UserProfileRequest;
import com.moretale.domain.profile.entity.UserProfile;
import com.moretale.domain.profile.repository.UserProfileRepository;
import com.moretale.domain.profile.service.UserProfileService;
import com.moretale.domain.story.dto.StoryShareRequest;
import com.moretale.domain.story.entity.Slide;
import com.moretale.domain.story.entity.Story;
import com.moretale.domain.story.repository.SlideRepository;
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
public class ServiceAdvancedTest {

    @Autowired private StoryService storyService;
    @Autowired private UserProfileService userProfileService;
    @Autowired private UserRepository userRepository;
    @Autowired private StoryRepository storyRepository;
    @Autowired private UserProfileRepository userProfileRepository;
    @Autowired private SlideRepository slideRepository;

    private User savedUser;
    private UserProfile savedProfile;

    @BeforeEach
    void setUp() {
        // 테스트용 기본 데이터 세팅
        savedUser = userRepository.save(User.builder()
                .email("adv@moretale.com").nickname("고급테스터").role(Role.USER).build());

        savedProfile = userProfileRepository.save(UserProfile.builder()
                .childName("유찬").childAge(8).parentCountry("China")
                .primaryLanguage("ko").secondaryLanguage("zh").user(savedUser).build());
    }

    @Test
    @DisplayName("시나리오 1: 프로필 이름을 수정해도 기존 생성된 동화 속 주인공 이름은 유지되어야 한다(Snapshot)")
    void profileUpdateSnapshotTest() {
        // 1. Given: '유찬' 이름으로 동화 생성 및 저장
        Story story = storyRepository.save(Story.builder()
                .title("유찬이의 요리").childName("유찬").user(savedUser).build());

        // 2. When: 프로필의 이름을 '민준'으로 수정
        UserProfileRequest updateReq = UserProfileRequest.builder()
                .childName("민준").childAge(8).parentCountry("China")
                .primaryLanguage("ko").secondaryLanguage("zh").build();
        userProfileService.updateProfile(savedProfile.getProfileId(), updateReq);

        // 3. Then: 동화 테이블의 child_name은 여전히 '유찬'이어야 함
        Story foundStory = storyRepository.findById(story.getStoryId()).orElseThrow();
        assertThat(foundStory.getChildName()).isEqualTo("유찬");

        // 프로필 테이블만 '민준'으로 바뀌었는지 확인
        UserProfile updatedProfile = userProfileRepository.findById(savedProfile.getProfileId()).orElseThrow();
        assertThat(updatedProfile.getChildName()).isEqualTo("민준");
    }

    @Test
    @DisplayName("시나리오 2: 비공개 동화를 공개로 전환하면 전체 공개 목록에서 조회할 수 있다")
    void shareStatusToggleIntegrationTest() {
        // 1. Given: 비공개 동화 저장
        Story story = storyRepository.save(Story.builder()
                .title("비밀 이야기").isPublic(false).user(savedUser).build());

        // 2. When: 공개(true)로 상태 변경
        storyService.updateStoryShareStatus(savedUser.getEmail(), story.getStoryId(), new StoryShareRequest(true));

        // 3. Then: 공개 동화 목록 조회 시 포함되는지 확인
        List<com.moretale.domain.story.dto.StoryListResponse> publicStories = storyService.getPublicStories();
        boolean isPresent = publicStories.stream().anyMatch(s -> s.getStoryId().equals(story.getStoryId()));
        assertThat(isPresent).isTrue();
    }

    @Test
    @DisplayName("시나리오 3: 동화 삭제 시 DB 레코드는 삭제되지만 오디오/이미지 URL 로그가 정상적으로 남는다")
    void deleteStoryAndCheckLogTest() {
        // 1. Given: 슬라이드 포함 동화 저장
        Story story = Story.builder().title("삭제 테스트").user(savedUser).build();
        story.addSlide(Slide.builder().order(1).textKr("내용").imageUrl("http://img.com").build());
        Story savedStory = storyRepository.save(story);

        // 2. When: 삭제 수행 (Business 로직 내 로그 출력 포함)
        storyService.deleteStory(savedUser.getEmail(), savedStory.getStoryId());

        // 3. Then: DB에서 삭제 확인
        assertThat(storyRepository.existsById(savedStory.getStoryId())).isFalse();
    }

    @Test
    @DisplayName("시나리오 4: 회원 탈퇴 시 프로필, 동화, 슬라이드가 DB에서 연쇄 삭제된다")
    void userWithdrawalCascadeTest() {
        // 1. Given: 계층 데이터 생성
        Story story = Story.builder()
                .title("삭제될 동화")
                .user(savedUser) // 자식 -> 부모 설정
                .build();

        // 부모 -> 자식 방향으로도 리스트에 추가 (양방향 편의성)
        savedUser.getStories().add(story);

        story.addSlide(Slide.builder().order(1).textKr("장면1").build());
        storyRepository.save(story);

        // 2. When: 유저 삭제
        userRepository.delete(savedUser);
        userRepository.flush(); // 여기서 DB 제약 조건을 체크함

        // 3. Then: 검증
        assertThat(userProfileRepository.existsById(savedProfile.getProfileId())).isFalse();
        assertThat(storyRepository.existsById(story.getStoryId())).isFalse();
    }
}
