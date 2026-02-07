package com.MoreTale.domain.dialect.entity;

import com.MoreTale.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "dialects", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"dialect", "region"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dialect {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dialect_id")
    private Long dialectId;

    @Column(nullable = false)
    private String dialect; // 사투리 표현

    @Column(nullable = false)
    private String standard; // 표준어 표현

    @Column(length = 1000)
    private String meaning; // 의미 해설

    @Column(length = 1000)
    private String origin; // 유래

    @Column(nullable = false)
    private String region; // 사용 지역

    @Column(length = 500)
    private String example; // 예문

    @Column(nullable = false)
    @Builder.Default
    private Boolean verified = false; // 관리자 검수 여부

    @Column(length = 500)
    private String source; // 출처 정보

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User uploadedBy; // 업로드한 사용자

    @Column(name = "tts_url")
    private String ttsUrl; // TTS 음성 파일 URL

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 지도용 위치 정보
    @Column(name = "latitude")
    private Double latitude;  // 위도

    @Column(name = "longitude")
    private Double longitude; // 경도

    // 방언에 연결된 북마크 목록 (방언 삭제 시 함께 삭제)
    @OneToMany(mappedBy = "dialect", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    private List<DialectBookmark> bookmarks = new ArrayList<>();

    // 좌표 자동 설정
    @PrePersist
    @PreUpdate
    private void setDefaultCoordinates() {
        if (latitude == null || longitude == null) {
            double[] coords = getDefaultCoordinatesForRegion(region);
            this.latitude = coords[0];
            this.longitude = coords[1];
        }
    }

    // 좌표 자동 설정 로직 (경상도만 분리)
    private static double[] getDefaultCoordinatesForRegion(String region) {
        if (region == null) return new double[]{37.5665, 126.9780}; // 기본값: 서울

        Map<String, double[]> regionCoords = Map.of(
                "서울", new double[]{37.5665, 126.9780},
                "경기도", new double[]{37.4138, 127.5183},
                "강원도", new double[]{37.8228, 128.1555},
                "충청도", new double[]{36.6357, 127.4917},
                "전라도", new double[]{35.7175, 127.1530},
                "경상북도", new double[]{36.4919, 128.8889},
                "경상남도", new double[]{35.4606, 128.2132},
                "제주도", new double[]{33.4996, 126.5312}
        );

        return regionCoords.getOrDefault(region, new double[]{37.5665, 126.9780}); // 기본값: 서울
    }
}
