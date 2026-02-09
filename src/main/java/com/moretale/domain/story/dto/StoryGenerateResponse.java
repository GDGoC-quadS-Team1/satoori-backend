package com.moretale.domain.story.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoryGenerateResponse {

    private String title;
    private String childName;
    private String primaryLanguage;
    private String secondaryLanguage;
    private List<GeneratedSlide> slides;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GeneratedSlide {
        private Integer order;
        private String imageUrl;
        private String textKr;
        private String textNative;
        private String audioUrlKr;
        private String audioUrlNative;
    }
}
