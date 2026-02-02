package com.satoori.global.security.oauth;

import com.satoori.domain.user.entity.User;
import com.satoori.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oauth2User.getAttributes();

        log.info("OAuth2 Login - Provider: {}, Attributes: {}", registrationId, attributes);

        // Google에서 받은 정보 추출
        String providerId = oauth2User.getAttribute("sub");
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        // 사용자 조회 또는 생성
        User user = userRepository.findByProviderAndProviderId(registrationId, providerId)
                .orElseGet(() -> createUser(registrationId, providerId, email, name));

        return new CustomOAuth2User(user, attributes);
    }

    private User createUser(String provider, String providerId, String email, String name) {
        User newUser = User.builder()
                .email(email)
                .nickname(name != null ? name : email.split("@")[0])
                .provider(provider)
                .providerId(providerId)
                .role(User.Role.USER)
                .build();

        log.info("Creating new user: {}", email);
        return userRepository.save(newUser);
    }
}