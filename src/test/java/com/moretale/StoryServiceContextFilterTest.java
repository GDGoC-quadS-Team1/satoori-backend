package com.moretale;

import com.moretale.domain.story.dto.StoryListResponse;
import com.moretale.domain.story.entity.Story;
import com.moretale.domain.story.repository.StoryRepository;
import com.moretale.domain.story.service.StoryService;
import com.moretale.domain.user.entity.User;
import com.moretale.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class StoryServiceContextFilterTest {

    @Autowired private StoryService storyService;
    @Autowired private UserRepository userRepository;
    @Autowired private StoryRepository storyRepository;

    @Test
    @DisplayName("중국어 설정 유저 A는 자신이 만든 동화만 조회되며 타인(유저 B)의 동화는 보이지 않는다")
    void libraryFilteringByUserTest() {
        // 1. Given: 두 명의 사용자 생성
        User userA = userRepository.save(User.builder().email("userA@zh.com").nickname("화잉").build());
        User userB = userRepository.save(User.builder().email("userB@vi.com").nickname("민지").build());

        // 각 사용자별 동화 저장
        storyRepository.save(Story.builder().title("유찬이의 중국어 동화").user(userA).build());
        storyRepository.save(Story.builder().title("민준이의 베트남어 동화").user(userB).build());

        // 2. When: 유저 A의 이름으로 목록 조회
        List<StoryListResponse> myStories = storyService.getMyStories(userA.getEmail());

        // 3. Then:
        // 전체 DB에는 2개의 동화가 있지만, 유저 A에게는 본인 것 1개만 나와야 함
        assertThat(myStories).hasSize(1);
        assertThat(myStories.get(0).getTitle()).isEqualTo("유찬이의 중국어 동화");
        assertThat(myStories.get(0).getTitle()).isNotEqualTo("민준이의 베트남어 동화");
    }
}
