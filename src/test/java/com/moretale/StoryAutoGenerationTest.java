package com.moretale;

import com.moretale.domain.profile.entity.*;
import com.moretale.domain.profile.repository.UserProfileRepository;
import com.moretale.domain.story.dto.StoryGenerateResponse;
import com.moretale.domain.story.dto.StoryInitResponse;
import com.moretale.domain.story.enums.TraditionalTale;
import com.moretale.domain.story.service.StoryService;
import com.moretale.domain.user.entity.User;
import com.moretale.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class StoryAutoGenerationTest {

    @Autowired
    private StoryService storyService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    private User testUser;
    private UserProfile testProfile;

    @BeforeEach
    void setUp() {
        // 1. 테스트 유저 생성
        testUser = User.builder()
                .email("test_" + System.currentTimeMillis() + "@moretale.com") // 중복 방지
                .nickname("테스트유저")
                .provider("google")
                .providerId("12345_" + System.currentTimeMillis())
                .role(User.Role.USER)
                .build();
        userRepository.save(testUser);

        // 2. 온보딩 프로필 생성
        // 로그의 23502 에러 해결을 위해 parent_country 등 필수값 모두 기입
        testProfile = UserProfile.builder()
                .user(testUser)
                .childName("민준")
                .ageGroup(AgeGroup.AGE_5_6)
                .childAge(5)
                .firstLanguage("ko")
                .firstLanguageProficiency(LanguageProficiency.PUPA)
                .secondLanguage("en")
                .secondLanguageProficiency(LanguageProficiency.LARVA)
                .firstLanguageListening(LanguageProficiency.BEE)
                .firstLanguageSpeaking(LanguageProficiency.BEE)
                .secondLanguageListening(LanguageProficiency.LARVA)
                .secondLanguageSpeaking(LanguageProficiency.EGG)
                .familyStructure(FamilyStructure.TWO_PARENTS)
                .storyPreference(StoryPreference.WARM_HUG)
                .parentCountry("KR") // 👈 DB 제약 조건 해결을 위해 추가
                .childNationality("KR") // 👈 필수 필드인 경우 추가
                .build();

        // 엔티티 내부의 하위 호환성 동기화 메서드 호출 (필요 시)
        testProfile.syncLegacyLanguages();

        userProfileRepository.save(testProfile);
    }

    @Test
    @DisplayName("온보딩 초기값 조회 시 선호도에 맞는 전래동화가 추천되어야 한다")
    void getStoryInitData_ShouldRecommendCorrectTale() {
        // when
        StoryInitResponse response = storyService.getStoryInitData(testUser.getEmail(), testProfile.getProfileId());

        // then
        assertThat(response.getChildName()).isEqualTo("민준");
        assertThat(response.getStoryPreference()).isEqualTo(StoryPreference.WARM_HUG);
        assertThat(response.getRecommendedTaleTitle()).isEqualTo("흥부와 놀부");
    }

    @Test
    @DisplayName("커스텀 선호도 키워드 분석을 통해 적절한 전래동화가 매핑되어야 한다")
    void findByCustomText_ShouldMapAdventureKeywords() {
        // given
        String customPreference = "우주선을 타고 외계인 친구를 만나는 신나는 모험";

        // when
        TraditionalTale mappedTale = TraditionalTale.findByCustomText(customPreference);

        // then
        assertThat(mappedTale).isEqualTo(TraditionalTale.GOLD_AXE_SILVER_AXE);
    }

    @Test
    @DisplayName("자동 생성 API 호출 시 프로필 제약 조건이 반영된 동화가 생성되어야 한다")
    void autoGenerateStory_ShouldReflectProfileConstraints() {
        // when
        StoryGenerateResponse response = storyService.autoGenerateStory(testUser.getEmail(), testProfile.getProfileId());

        // then
        assertThat(response.getTitle()).isEqualTo("흥부와 놀부");
        assertThat(response.getSlides()).hasSize(5);
        assertThat(response.getSlides().get(0).getTextKr())
                .contains("민준")
                .contains("새로운 모험을 시작했어요");
    }
}