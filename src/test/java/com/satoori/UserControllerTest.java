package com.satoori;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.satoori.domain.user.controller.UserController;
import com.satoori.domain.user.entity.User;
import com.satoori.domain.user.service.UserService;
import com.satoori.global.exception.GlobalExceptionHandler;
import com.satoori.global.security.jwt.JwtAuthenticationFilter;
import com.satoori.global.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = UserController.class,
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp(WebApplicationContext context) {
        // MockMvc에 Spring Security 설정을 적용하여 @AuthenticationPrincipal 주입 및 CSRF 설정을 활성화합니다.
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    private User createMockUser() {
        return User.builder()
                .userId(1L)
                .email("test@example.com")
                .nickname("테스트유저")
                .region("SEOUL")
                .role(User.Role.USER)
                .provider("google")
                .providerId("google-123")
                .build();
    }

    @Test
    @DisplayName("현재 로그인한 사용자 정보 조회 시 200과 사용자 정보 반환")
    void getCurrentUser_success() throws Exception {
        // given
        User mockUser = createMockUser();
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                mockUser, null, mockUser.getAuthorities()
        );

        // when & then
        mockMvc.perform(get("/api/users/me")
                        .with(authentication(auth))) // GET은 CSRF가 필요 없음
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.nickname").value("테스트유저"));
    }

    @Test
    @DisplayName("사용자 지역 정보 수정 시 200과 수정된 사용자 정보 반환")
    void updateRegion_success() throws Exception {
        // given
        User mockUser = createMockUser();
        User updatedUser = User.builder()
                .userId(1L)
                .email("test@example.com")
                .nickname("테스트유저")
                .region("GYEONGSANG")
                .role(User.Role.USER)
                .build();

        when(userService.updateRegion(eq(1L), eq("GYEONGSANG"))).thenReturn(updatedUser);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                mockUser, null, mockUser.getAuthorities()
        );

        // when & then
        mockMvc.perform(patch("/api/users/me/region")
                        .param("region", "GYEONGSANG")
                        .with(authentication(auth))
                        .with(csrf())) // PATCH 요청 시 CSRF 토큰 주입 필수
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.region").value("GYEONGSANG"));
    }

    @Test
    @DisplayName("잘못된 지역 정보로 수정 시 400 반환")
    void updateRegion_invalidRegion() throws Exception {
        // given
        User mockUser = createMockUser();

        when(userService.updateRegion(anyLong(), eq("INVALID_REGION")))
                .thenThrow(new IllegalArgumentException("유효하지 않은 지역 코드입니다."));

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                mockUser, null, mockUser.getAuthorities()
        );

        // when & then
        mockMvc.perform(patch("/api/users/me/region")
                        .param("region", "INVALID_REGION")
                        .with(authentication(auth))
                        .with(csrf())) // PATCH 요청 시 CSRF 토큰 주입 필수
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("유효하지 않은 지역 코드입니다."));
    }
}
