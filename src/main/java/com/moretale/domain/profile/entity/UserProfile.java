package com.moretale.domain.profile.entity;

import com.moretale.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

// 자녀 프로필 정보 엔티티
// 한 명의 사용자가 여러 명의 자녀를 가질 수 있다.
@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long profileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 부모 엔티티(User)와의 다대일 연관관계 설정

    @Column(name = "child_name", nullable = false, length = 50)
    private String childName;

    // 나이 그룹 (Enum)
    @Enumerated(EnumType.STRING)
    @Column(name = "age_group", nullable = false)
    private AgeGroup ageGroup;

    // 실제 나이 (대표값, 자동 계산)
    @Column(name = "child_age", nullable = false)
    private Integer childAge;

    // 언어 설정
    @Column(name = "first_language", nullable = false, length = 10)
    private String firstLanguage;

    @Enumerated(EnumType.STRING)
    @Column(name = "first_language_proficiency", nullable = false)
    private LanguageProficiency firstLanguageProficiency;

    @Column(name = "second_language", nullable = false, length = 10)
    private String secondLanguage;

    @Enumerated(EnumType.STRING)
    @Column(name = "second_language_proficiency", nullable = false)
    private LanguageProficiency secondLanguageProficiency;

    // 언어 능력 (듣기/말하기)
    @Enumerated(EnumType.STRING)
    @Column(name = "first_language_listening", nullable = false)
    private LanguageProficiency firstLanguageListening;

    @Enumerated(EnumType.STRING)
    @Column(name = "first_language_speaking", nullable = false)
    private LanguageProficiency firstLanguageSpeaking;

    @Enumerated(EnumType.STRING)
    @Column(name = "second_language_listening", nullable = false)
    private LanguageProficiency secondLanguageListening;

    @Enumerated(EnumType.STRING)
    @Column(name = "second_language_speaking", nullable = false)
    private LanguageProficiency secondLanguageSpeaking;

    // 가족 구조
    @Enumerated(EnumType.STRING)
    @Column(name = "family_structure", nullable = false)
    private FamilyStructure familyStructure;

    @Column(name = "custom_family_structure", length = 200)
    private String customFamilyStructure;

    // 이야기 선호도
    @Enumerated(EnumType.STRING)
    @Column(name = "story_preference", nullable = false)
    private StoryPreference storyPreference;

    @Column(name = "custom_story_preference", length = 200)
    private String customStoryPreference;

    // 부가 정보 (선택)
    @Column(name = "child_nationality", length = 50)
    private String childNationality;

    @Column(name = "parent_country", length = 50)
    private String parentCountry;

    // 하위 호환성 유지 (기존 필드)
    @Deprecated
    @Column(name = "primary_language", length = 10)
    private String primaryLanguage;

    @Deprecated
    @Column(name = "secondary_language", length = 10)
    private String secondaryLanguage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 비즈니스 로직

    // 프로필 업데이트
    public void updateProfile(
            String childName,
            AgeGroup ageGroup,
            String firstLanguage,
            LanguageProficiency firstLanguageProficiency,
            String secondLanguage,
            LanguageProficiency secondLanguageProficiency,
            LanguageProficiency firstLanguageListening,
            LanguageProficiency firstLanguageSpeaking,
            LanguageProficiency secondLanguageListening,
            LanguageProficiency secondLanguageSpeaking,
            FamilyStructure familyStructure,
            String customFamilyStructure,
            StoryPreference storyPreference,
            String customStoryPreference,
            String childNationality,
            String parentCountry
    ) {
        this.childName = childName;
        this.ageGroup = ageGroup;
        // 나이 그룹의 대표값으로 childAge 갱신
        this.childAge = ageGroup.getRepresentativeAge();
        this.firstLanguage = firstLanguage;
        this.firstLanguageProficiency = firstLanguageProficiency;
        this.secondLanguage = secondLanguage;
        this.secondLanguageProficiency = secondLanguageProficiency;
        this.firstLanguageListening = firstLanguageListening;
        this.firstLanguageSpeaking = firstLanguageSpeaking;
        this.secondLanguageListening = secondLanguageListening;
        this.secondLanguageSpeaking = secondLanguageSpeaking;
        this.familyStructure = familyStructure;
        this.customFamilyStructure = customFamilyStructure;
        this.storyPreference = storyPreference;
        this.customStoryPreference = customStoryPreference;
        this.childNationality = childNationality;
        this.parentCountry = parentCountry;

        // 하위 호환성 필드 동기화
        syncLegacyLanguages();

        // updatedAt 명시적 갱신 (Hibernate가 처리하지만 명확성을 위해 유지)
        this.updatedAt = LocalDateTime.now();
    }

    // 나이 그룹에서 실제 나이 자동 계산 (@PrePersist / @PreUpdate)
    @PrePersist
    @PreUpdate
    public void calculateChildAge() {
        if (this.ageGroup != null) {
            this.childAge = this.ageGroup.getRepresentativeAge();
        }
    }

    // 하위 호환성: primaryLanguage/secondaryLanguage 자동 동기화
    public void syncLegacyLanguages() {
        this.primaryLanguage = this.firstLanguage;
        this.secondaryLanguage = this.secondLanguage;
    }
}
