package com.moretale;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moretale.domain.user.controller.UserController;
import com.moretale.domain.user.dto.UserResponse;
import com.moretale.domain.user.entity.User;
import com.moretale.domain.user.repository.UserRepository; // ✅ 추가
import com.moretale.domain.user.service.UserService;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController 테스트 클래스
 * @WebMvcTest를 통해 웹 계층만 테스트하며, 필요한 의존성은 MockBean으로 주입합니다.
 */
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
    private UserRepository userRepository; // ✅ TestAuthenticationFilter 의존성 해결을 위해 추가

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp(WebApplicationContext context) {
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
                .region("서울")
                .role(User.Role.USER)
                .provider("google")
                .providerId("google-123")
                .build();
    }

    private UserPrincipal createUserPrincipal(User user) {
        return UserPrincipal.create(user);
    }

    private UserResponse createMockUserResponse() {
        return UserResponse.builder()
                .userId(1L)
                .email("test@example.com")
                .nickname("테스트유저")
                .region("서울")
                .role("USER")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("현재 로그인한 사용자 정보 조회 시 200과 사용자 정보 반환")
    void getCurrentUser_success() throws Exception {
        // given
        User mockUser = createMockUser();
        UserPrincipal userPrincipal = createUserPrincipal(mockUser);
        UserResponse mockResponse = createMockUserResponse();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, userPrincipal.getAuthorities()
        );

        when(userService.getUserInfo(1L)).thenReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/users/me")
                        .with(authentication(auth)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("테스트유저"));
    }

    @Test
    @DisplayName("사용자 지역 정보 수정 시 200과 수정된 사용자 정보 반환")
    void updateRegion_success() throws Exception {
        // given
        User mockUser = createMockUser();
        UserPrincipal userPrincipal = createUserPrincipal(mockUser);

        UserResponse updatedResponse = UserResponse.builder()
                .userId(1L)
                .email("test@example.com")
                .nickname("테스트유저")
                .region("부산")
                .role("USER")
                .createdAt(LocalDateTime.now())
                .build();

        when(userService.updateRegion(eq(1L), eq("부산"))).thenReturn(updatedResponse);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, userPrincipal.getAuthorities()
        );

        // when & then
        mockMvc.perform(patch("/api/users/me/region")
                        .param("region", "부산")
                        .with(authentication(auth))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.region").value("부산"))
                .andExpect(jsonPath("$.message").value("지역이 설정되었습니다."));
    }
}