package com.satoori.domain.dialect.dto;

import com.satoori.domain.dialect.entity.Dialect;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class DialectResponse {
    private Long dialectId;
    private String dialect;
    private String standard;
    private String meaning;
    private String origin;
    private String region;
    private String example;
    private Boolean verified;
    private String source;
    private String ttsUrl;
    private Double latitude;
    private Double longitude;
    private LocalDateTime createdAt;
    private Boolean bookmarked;

    public static DialectResponse from(Dialect dialect) {
        return DialectResponse.builder()
                .dialectId(dialect.getDialectId())
                .dialect(dialect.getDialect())
                .standard(dialect.getStandard())
                .meaning(dialect.getMeaning())
                .origin(dialect.getOrigin())
                .region(dialect.getRegion())
                .example(dialect.getExample())
                .verified(dialect.getVerified())
                .source(dialect.getSource())
                .ttsUrl(dialect.getTtsUrl())
                .latitude(dialect.getLatitude())
                .longitude(dialect.getLongitude())
                .createdAt(dialect.getCreatedAt())
                .bookmarked(false)
                .build();
    }

    public static DialectResponse from(Dialect dialect, boolean bookmarked) {
        DialectResponse response = from(dialect);
        return DialectResponse.builder()
                .dialectId(response.getDialectId())
                .dialect(response.getDialect())
                .standard(response.getStandard())
                .meaning(response.getMeaning())
                .origin(response.getOrigin())
                .region(response.getRegion())
                .example(response.getExample())
                .verified(response.getVerified())
                .source(response.getSource())
                .ttsUrl(response.getTtsUrl())
                .latitude(response.getLatitude())
                .longitude(response.getLongitude())
                .createdAt(response.getCreatedAt())
                .bookmarked(bookmarked)
                .build();
    }
}