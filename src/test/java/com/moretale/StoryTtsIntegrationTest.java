package com.moretale;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moretale.domain.profile.entity.UserProfile;
import com.moretale.domain.profile.repository.UserProfileRepository;
import com.moretale.domain.story.dto.StorySaveRequest;
import com.moretale.domain.user.entity.User.Role;
import com.moretale.domain.user.entity.User;
import com.moretale.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class StoryTtsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    private User testUser;
    private UserProfile testProfile;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .nickname("테스트유저")
                .role(Role.USER)
                .build();
        userRepository.save(testUser);

        testProfile = UserProfile.builder()
                .childName("민준")
                .childAge(5)
                .parentCountry("Vietnam")
                .primaryLanguage("ko")
                .secondaryLanguage("vi")
                .user(testUser)
                .build();
        userProfileRepository.save(testProfile);
    }

    @Test
    @DisplayName("동화 저장 시 TTS URL이 슬라이드에 정상적으로 포함되어 저장되는지 테스트")
    @WithMockUser(username = "test@example.com")
    void createStoryWithTtsUrlsSuccess() throws Exception {
        // given
        StorySaveRequest.SlideRequest slide1 = StorySaveRequest.SlideRequest.builder()
                .order(1)
                .textKr("민준이는 사자를 봤어요.")
                .textNative("Minjun đã nhìn thấy con sư tử.")
                .audioUrlKr("http://localhost:8080/uploads/tts/audio/ko_sample.mp3")
                .audioUrlNative("http://localhost:8080/uploads/tts/audio/vi_sample.mp3")
                .imageUrl("http://example.com/image1.png")
                .build();

        StorySaveRequest request = StorySaveRequest.builder()
                .title("사자 이야기")
                .prompt("사자와 민준이의 만남")
                .profileId(testProfile.getProfileId())
                .slides(List.of(slide1))
                .build();

        // when & then: status().isCreated() 로 수정 (201 응답 대응)
        MvcResult result = mockMvc.perform(post("/api/stories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated()) // 핵심 수정 사항: isOk() -> isCreated()
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        long storyId = objectMapper.readTree(responseBody).path("data").path("storyId").asLong();

        // 상세 조회는 컨트롤러에서 @ResponseStatus가 없으므로 200 OK
        mockMvc.perform(get("/api/stories/" + storyId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.slides[0].audioUrlKr").value("http://localhost:8080/uploads/tts/audio/ko_sample.mp3"))
                .andExpect(jsonPath("$.data.slides[0].audioUrlNative").value("http://localhost:8080/uploads/tts/audio/vi_sample.mp3"));
    }

    @Test
    @DisplayName("필수 값(제목) 누락 시 Validation 에러 발생 테스트")
    @WithMockUser(username = "test@example.com")
    void createStoryValidationFail() throws Exception {
        StorySaveRequest request = StorySaveRequest.builder()
                .title("")
                .slides(List.of())
                .build();

        mockMvc.perform(post("/api/stories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
