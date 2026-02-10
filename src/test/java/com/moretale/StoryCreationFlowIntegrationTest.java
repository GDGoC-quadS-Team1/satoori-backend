package com.moretale;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moretale.domain.profile.dto.UserProfileRequest;
import com.moretale.domain.profile.entity.UserProfile;
import com.moretale.domain.profile.repository.UserProfileRepository;
import com.moretale.domain.story.dto.StorySaveRequest;
import com.moretale.domain.tts.dto.TTSRequest;
import com.moretale.domain.user.entity.User;
import com.moretale.domain.user.entity.User.Role;
import com.moretale.domain.user.repository.UserRepository;
import com.moretale.global.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class StoryCreationFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    private User testUser;
    private UserPrincipal principal;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("flow-test@example.com")
                .nickname("시나리오유저")
                .role(Role.USER)
                .build();
        userRepository.save(testUser);
        principal = UserPrincipal.create(testUser);
    }

    @Test
    @DisplayName("전체 시나리오 테스트: 프로필 생성 -> TTS 준비 -> 동화 저장 -> 목록 조회")
    void fullStoryCreationFlowTest() throws Exception {

        // 1. 아이 프로필 생성
        UserProfileRequest profileReq = UserProfileRequest.builder()
                .childName("유찬")
                .childAge(5)
                .childNationality("South Korea")
                .parentCountry("Vietnam")
                .primaryLanguage("ko")
                .secondaryLanguage("vi")
                .build();

        MvcResult profileResult = mockMvc.perform(post("/api/users/profile")
                        .with(SecurityMockMvcRequestPostProcessors.user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileReq)))
                .andExpect(status().isCreated())
                .andReturn();

        Long profileId = objectMapper.readTree(profileResult.getResponse().getContentAsString())
                .path("data").path("profileId").asLong();

        // 2. TTS 음성 미리 생성 (동화 슬라이드에 넣을 용도)
        TTSRequest ttsReq = TTSRequest.builder()
                .text("옛날 옛적에 사자가 살았어요.")
                .language("ko-KR")
                .build();

        MvcResult ttsResult = mockMvc.perform(post("/api/tts/generate")
                        .with(SecurityMockMvcRequestPostProcessors.user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ttsReq)))
                .andExpect(status().isOk())
                .andReturn();

        String generatedAudioUrl = objectMapper.readTree(ttsResult.getResponse().getContentAsString())
                .path("data").path("audioUrl").asText();

        // 3. 생성된 프로필과 TTS URL을 사용하여 동화 저장
        StorySaveRequest.SlideRequest slide = StorySaveRequest.SlideRequest.builder()
                .order(1)
                .textKr("옛날 옛적에 사자가 살았어요.")
                .textNative("Ngày xửa ngày xưa, có một con sư tử.")
                .audioUrlKr(generatedAudioUrl) // 위에서 생성된 URL 사용
                .audioUrlNative("http://example.com/native-audio.mp3")
                .imageUrl("http://example.com/lion.png")
                .build();

        StorySaveRequest storyReq = StorySaveRequest.builder()
                .title("용감한 사자 유찬이")
                .prompt("사자와 아이의 우정")
                .profileId(profileId) // 위에서 생성된 프로필 ID 사용
                .slides(List.of(slide))
                .build();

        mockMvc.perform(post("/api/stories")
                        .with(SecurityMockMvcRequestPostProcessors.user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(storyReq)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("용감한 사자 유찬이"));

        // 4. 사용자의 전체 동화 목록 조회하여 저장 확인
        mockMvc.perform(get("/api/stories/my")
                        .with(SecurityMockMvcRequestPostProcessors.user(principal)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                // 목록 중 첫 번째 항목의 제목이 저장한 것과 일치하는지 확인
                .andExpect(jsonPath("$.data[0].title").value("용감한 사자 유찬이"));
    }

    @Test
    @DisplayName("예외 시나리오: 다른 사용자의 프로필 ID로 동화 저장을 시도할 경우 403 에러 발생")
    void createStoryWithOtherUserProfileFail() throws Exception {
        // 1. 새로운 '남의 유저'와 '남의 프로필' 생성
        User otherUser = User.builder()
                .email("other@example.com")
                .nickname("남의이름")
                .role(Role.USER)
                .build();
        userRepository.save(otherUser);

        UserProfile otherProfile = UserProfile.builder()
                .childName("남의아이")
                .childAge(7)
                .childNationality("KR")
                .parentCountry("VN")
                .user(otherUser)
                .primaryLanguage("ko")
                .secondaryLanguage("en")
                .build();
        userProfileRepository.save(otherProfile);

        // 2. 현재 로그인한 사용자(testUser)가 남의 프로필 ID로 저장 요청
        StorySaveRequest.SlideRequest slide = StorySaveRequest.SlideRequest.builder()
                .order(1)
                .textKr("훔치기 테스트")
                .build();

        StorySaveRequest request = StorySaveRequest.builder()
                .title("가로채기 시도")
                .prompt("남의 프로필로 저장")
                .profileId(otherProfile.getProfileId()) // 남의 프로필 ID 입력
                .slides(List.of(slide))
                .build();

        // 3. 요청 실행 및 결과 검증 (403 Forbidden 기대)
        mockMvc.perform(post("/api/stories")
                        .with(SecurityMockMvcRequestPostProcessors.user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("S002"))
                .andExpect(jsonPath("$.success").value(false));
    }
}
