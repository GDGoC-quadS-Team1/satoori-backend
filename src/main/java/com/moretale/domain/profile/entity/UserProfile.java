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

    @Column(name = "child_age", nullable = false)
    private Integer childAge;

    @Column(name = "child_nationality", length = 50)
    private String childNationality;

    @Column(name = "parent_country", nullable = false, length = 50)
    private String parentCountry;

    @Column(name = "primary_language", nullable = false, length = 10)
    @Builder.Default
    private String primaryLanguage = "ko"; // 기본값 한국어

    @Column(name = "secondary_language", nullable = false, length = 10)
    private String secondaryLanguage; // 부모의 모국어 (이중언어 설정용)

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
