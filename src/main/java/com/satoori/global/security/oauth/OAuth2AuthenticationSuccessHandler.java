package com.satoori.global.security.oauth;

import com.satoori.global.security.jwt.JwtTokenProvider;
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

    private static final String REDIRECT_URI = "http://localhost:3000/oauth2/redirect"; // 로그인 성공 후 토큰을 전달할 프론트엔드 URL

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        if (!(authentication.getPrincipal() instanceof CustomOAuth2User oAuth2User)) {
            throw new IllegalStateException("OAuth2User 타입이 아닙니다.");
        }

        String email = oAuth2User.getEmail();

        // JWT 토큰 생성
        String token = jwtTokenProvider.generateTokenFromEmail(email);

        // 토큰 로그 출력 (디버깅 용도)
        log.info("OAuth2 login success - Email: {}", email);
        log.info("JWT Token: {}", token);

        // 프론트엔드로 리다이렉트 (토큰 전달)
        String targetUrl = UriComponentsBuilder.fromUriString(REDIRECT_URI)
                .queryParam("token", token)
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
