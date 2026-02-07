package com.MoreTale.domain.dialect.dto;

import lombok.Data;

@Data
public class DialectRequest {
    private String dialect;
    private String standard;
    private String meaning;
    private String origin;
    private String region;
    private String example;
    private String source;
}
