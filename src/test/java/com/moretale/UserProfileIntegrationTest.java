package com.moretale;

import com.moretale.domain.profile.dto.UserProfileRequest;
import com.moretale.domain.profile.dto.UserProfileResponse;
import com.moretale.domain.profile.entity.UserProfile;
import com.moretale.domain.profile.repository.UserProfileRepository;
import com.moretale.domain.profile.service.UserProfileService;
import com.moretale.domain.user.entity.User;
import com.moretale.domain.user.repository.UserRepository;
import com.moretale.global.exception.CustomException;
import com.moretale.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
public class UserProfileIntegrationTest {

    @Autowired private UserProfileService userProfileService;
    @Autowired private UserRepository userRepository;
    @Autowired private UserProfileRepository userProfileRepository;

    private User savedUser;

    @BeforeEach
    void setUp() {
        savedUser = userRepository.save(User.builder()
                .email("mom@example.com").nickname("화잉").role(User.Role.USER).build());
    }

    @Test
    @DisplayName("사용자는 자녀 프로필을 등록할 수 있고, 동일한 이름의 자녀는 중복 등록할 수 없다")
    void createProfile_Duplicate_Test() {
        // 1. Given: 첫 번째 자녀 '유찬' 등록
        UserProfileRequest request = UserProfileRequest.builder()
                .childName("유찬").childAge(8).childNationality("KR")
                .parentCountry("China").primaryLanguage("ko").secondaryLanguage("zh")
                .build();

        userProfileService.createProfile(savedUser.getUserId(), request);

        // 2. When & Then: 같은 이름 '유찬'으로 또 등록 시도
        assertThatThrownBy(() -> userProfileService.createProfile(savedUser.getUserId(), request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROFILE_ALREADY_EXISTS);

        // 3. Verify: DB에는 '유찬' 프로필이 딱 하나만 있어야 함
        long count = userProfileRepository.findAllByUser_UserId(savedUser.getUserId()).size();
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("자녀의 이중언어 설정을 변경하면 즉시 반영된다")
    void updateLanguage_Test() {
        // 1. Given: 기존 프로필 (중국어 설정)
        UserProfile profile = userProfileRepository.save(UserProfile.builder()
                .childName("유찬").childAge(8).parentCountry("China")
                .primaryLanguage("ko").secondaryLanguage("zh").user(savedUser).build());

        // 2. When: 베트남어로 변경 요청
        com.moretale.domain.profile.dto.LanguageUpdateRequest updateRequest =
                new com.moretale.domain.profile.dto.LanguageUpdateRequest("ko", "vi");

        userProfileService.updateLanguage(profile.getProfileId(), updateRequest);

        // 3. Then: 데이터 확인
        UserProfile updated = userProfileRepository.findById(profile.getProfileId()).get();
        assertThat(updated.getSecondaryLanguage()).isEqualTo("vi");
    }
}
