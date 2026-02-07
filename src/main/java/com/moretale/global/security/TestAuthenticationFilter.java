package com.moretale.global.security;

import com.moretale.domain.user.entity.User;
import com.moretale.domain.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// 로컬 개발 및 테스트를 위한 임시 인증 필터
// HTTP 헤더에 'X-User-Id: 3'과 같이 전달하면 해당 ID의 사용자로 로그인된 것처럼 동작
@Slf4j
@Component
@RequiredArgsConstructor
public class TestAuthenticationFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. 요청 헤더에서 테스트용 사용자 ID를 추출
        String userIdHeader = request.getHeader("X-User-Id");

        // 2. 헤더가 존재하고 아직 인증되지 않은 요청인 경우에만 처리
        if (userIdHeader != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Long userId = Long.parseLong(userIdHeader);
                // 3. DB에서 사용자 조회
                User user = userRepository.findById(userId).orElse(null);

                if (user != null) {
                    // 4. 인증 객체 생성 및 SecurityContext 등록
                    UserPrincipal userPrincipal = UserPrincipal.create(user);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("테스트 인증 성공 - userId: {}", userId);
                }
            } catch (NumberFormatException e) {
                log.warn("잘못된 X-User-Id 헤더: {}", userIdHeader);
            }
        }
        // 5. 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }
}
