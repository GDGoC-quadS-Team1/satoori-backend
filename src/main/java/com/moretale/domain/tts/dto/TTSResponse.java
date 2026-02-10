package com.moretale.domain.tts.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TTSResponse {

    private String audioUrl;
    private String language;
    private Integer duration; // 음성 길이 (초)
    private String message;
}
