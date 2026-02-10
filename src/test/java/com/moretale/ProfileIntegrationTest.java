package com.moretale;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moretale.domain.profile.dto.UserProfileRequest;
import com.moretale.domain.user.entity.User.Role;
import com.moretale.domain.user.entity.User;
import com.moretale.domain.user.repository.UserRepository;
import com.moretale.global.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ProfileIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // DB에 테스트 유저 생성 (Service에서 userId로 조회할 수 있어야 함)
        testUser = User.builder()
                .email("test@example.com")
                .nickname("테스트유저")
                .role(Role.USER)
                .build();
        userRepository.save(testUser);
    }

    @Test
    @DisplayName("새로운 아이 프로필 생성 성공 테스트")
    void createProfileSuccess() throws Exception {
        // given
        UserProfileRequest request = UserProfileRequest.builder()
                .childName("유찬")
                .childAge(5)
                .childNationality("대한민국")
                .parentCountry("베트남")
                .primaryLanguage("ko")
                .secondaryLanguage("vi")
                .build();

        // UserPrincipal 객체를 직접 생성하여 MockUser로 주입 (컨트롤러의 UserPrincipal 대응)
        UserPrincipal principal = UserPrincipal.create(testUser);

        // when & then
        mockMvc.perform(post("/api/users/profile")
                        .with(SecurityMockMvcRequestPostProcessors.user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.childName").value("유찬"));
    }

    @Test
    @DisplayName("필수 정보(부모 국가) 누락 시 프로필 생성 실패 테스트")
    @WithMockUser // Validation 에러는 Principal 이전에 발생하므로 기본 MockUser도 무방
    void createProfileValidationFail() throws Exception {
        // given: @NotBlank인 parentCountry 누락
        UserProfileRequest request = UserProfileRequest.builder()
                .childName("유찬")
                .childAge(5)
                .primaryLanguage("ko")
                .secondaryLanguage("vi")
                .build();

        // when & then
        mockMvc.perform(post("/api/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("로그인하지 않은 사용자의 프로필 접근 차단 테스트")
    void profileAccessDeniedWithoutLogin() throws Exception {
        mockMvc.perform(get("/api/users/profile/list"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}
