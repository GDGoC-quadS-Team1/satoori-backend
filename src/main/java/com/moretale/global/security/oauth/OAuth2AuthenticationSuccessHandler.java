package com.moretale.global.security.oauth;

import com.moretale.global.security.jwt.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider; // JWT 토큰 생성하는 Provider

    private static final String REDIRECT_URI = "http://localhost:8080/"; // 로그인 성공 후 토큰을 전달할 프론트엔드 URL

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        // 1. Principal 객체를 CustomOAuth2User로 캐스팅 (ClassCastException 해결)
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        // 2. CustomOAuth2User 내부에 구현된 메서드를 통해 userId와 email 추출
        Long userId = oAuth2User.getUserId();
        String email = oAuth2User.getEmail();

        // 3. JWT 토큰 생성 (userId 기반)
        String token = jwtTokenProvider.generateTokenFromUserId(userId);

        log.info("OAuth2 로그인 성공 - userId: {}, email: {}", userId, email);
        log.info("JWT Token 생성 완료: {}", token);

        // 4. 리다이렉트 URL 생성
        // 프론트엔드에서 토큰을 인지할 수 있도록 쿼리 파라미터에 token과 userId를 포함
        String targetUrl = UriComponentsBuilder.fromUriString(REDIRECT_URI)
                .queryParam("token", token)
                .queryParam("userId", userId)
                .build()
                .toUriString();

        log.info("리다이렉트 실행 URL: {}", targetUrl);

        // 5. 실제 리다이렉트 수행
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
