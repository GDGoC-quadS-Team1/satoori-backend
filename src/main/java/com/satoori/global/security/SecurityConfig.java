package com.satoori.global.security;

import com.satoori.global.security.jwt.JwtAuthenticationFilter;
import com.satoori.global.security.oauth.CustomOAuth2UserService;
import com.satoori.global.security.oauth.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/oauth2/**", "/login/**").permitAll() // 공개 접근

                        .requestMatchers(HttpMethod.GET, "/api/dictionary/**").permitAll() // 방언 사전 조회는 비회원도 허용

                        // 북마크는 로그인 필요 → 토큰 없으면 401
                        .requestMatchers(HttpMethod.POST, "/api/dictionary/*/bookmark").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/dictionary/*/bookmark").authenticated()

                        .requestMatchers(HttpMethod.PATCH, "/api/dictionary/*/verify").hasRole("ADMIN") // 방언 검증은 ADMIN만 → 일반 유저면 403

                        .requestMatchers("/api/admin/**").hasRole("ADMIN") // 관리자 전용 경로

                        .anyRequest().authenticated()
                )

                // OAuth2 로그인 처리 설정
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2AuthenticationSuccessHandler) // 로그인 성공 시 JWT 발급 + 리다이렉트
                )

                // 인증 실패 및 인가 실패 처리 핸들링
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((req, res, ex) -> {
                            // API 요청이면 JSON 401
                            if (req.getRequestURI().startsWith("/api/")) {
                                res.setStatus(401);
                                res.setContentType("application/json");
                                res.getWriter().write("{\"message\": \"Unauthorized\"}");
                            } else {
                                // API 아닌 경로는 OAuth2 로그인으로 리다이렉트
                                res.sendRedirect("/oauth2/authorization/google");
                            }
                        })

                        // 권한 부족 (ex. 일반 사용자가 관리자 API 접근)
                        .accessDeniedHandler((req, res, ex) -> {
                            res.setStatus(403);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"message\": \"Forbidden: 관리자 권한 필요\"}");
                        })
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
