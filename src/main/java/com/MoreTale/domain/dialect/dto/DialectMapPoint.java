package com.MoreTale.domain.dialect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class DialectMapPoint {
    private Long dialectId;
    private String dialect;
    private String standard;
    private String region;
    private Double latitude;
    private Double longitude;
}
