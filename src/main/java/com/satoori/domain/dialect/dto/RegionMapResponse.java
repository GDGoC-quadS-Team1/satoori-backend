package com.satoori.domain.dialect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class RegionMapResponse {
    private String region;
    private Long count;
    private Double latitude;
    private Double longitude;
    private List<DialectResponse> samples; // 샘플 방언 5개
}