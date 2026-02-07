package com.moretale;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moretale.domain.profile.controller.UserProfileController;
import com.moretale.domain.profile.dto.UserProfileRequest;
import com.moretale.domain.profile.dto.UserProfileResponse;
import com.moretale.domain.profile.service.UserProfileService;
import com.moretale.domain.user.entity.User;
import com.moretale.domain.user.repository.UserRepository;
import com.moretale.global.exception.GlobalExceptionHandler;
import com.moretale.global.security.UserPrincipal;
import com.moretale.global.security.jwt.JwtAuthenticationFilter;
import com.moretale.global.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = UserProfileController.class,
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
class UserProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserProfileService userProfileService;

    @MockBean
    private UserRepository userRepository; // 보안 필터 의존성 해결

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UserPrincipal userPrincipal;
    private UsernamePasswordAuthenticationToken auth;

    @BeforeEach
    void setUp(WebApplicationContext context) {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        User mockUser = User.builder()
                .userId(1L)
                .email("test@example.com")
                .nickname("테스트부모")
                .build();
        userPrincipal = UserPrincipal.create(mockUser);
        auth = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
    }

    @Test
    @DisplayName("프로필 생성 성공 시 201 반환")
    void createProfile_success() throws Exception {
        // given
        UserProfileRequest request = UserProfileRequest.builder()
                .childName("유찬")
                .childAge(8)
                .parentCountry("베트남")
                .primaryLanguage("ko")
                .secondaryLanguage("vi")
                .build();

        UserProfileResponse response = UserProfileResponse.builder()
                .profileId(100L)
                .userId(1L)
                .childName("유찬")
                .build();

        when(userProfileService.createProfile(eq(1L), any(UserProfileRequest.class))).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/users/profile")
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.childName").value("유찬"));
    }

    @Test
    @DisplayName("사용자의 전체 자녀 목록 조회 성공")
    void getAllProfiles_success() throws Exception {
        // given
        UserProfileResponse child1 = UserProfileResponse.builder().profileId(1L).childName("유찬").build();
        UserProfileResponse child2 = UserProfileResponse.builder().profileId(2L).childName("민지").build();

        when(userProfileService.getAllProfiles(1L)).thenReturn(List.of(child1, child2));

        // when & then
        mockMvc.perform(get("/api/users/profile/list")
                        .with(authentication(auth)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].childName").value("유찬"))
                .andExpect(jsonPath("$.data[1].childName").value("민지"));
    }

    @Test
    @DisplayName("특정 아이 프로필 수정 성공")
    void updateProfile_success() throws Exception {
        // given
        Long profileId = 100L;
        UserProfileRequest request = UserProfileRequest.builder()
                .childName("유찬수정")
                .childAge(9)
                .parentCountry("베트남")
                .primaryLanguage("ko")
                .secondaryLanguage("vi")
                .build();

        UserProfileResponse response = UserProfileResponse.builder()
                .profileId(profileId)
                .childName("유찬수정")
                .build();

        when(userProfileService.updateProfile(eq(profileId), any(UserProfileRequest.class))).thenReturn(response);

        // when & then
        mockMvc.perform(patch("/api/users/profile/{profileId}", profileId)
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.childName").value("유찬수정"))
                .andExpect(jsonPath("$.message").value("프로필이 수정되었습니다."));
    }
}
