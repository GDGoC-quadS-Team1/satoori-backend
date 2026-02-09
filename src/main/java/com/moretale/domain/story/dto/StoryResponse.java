package com.moretale.domain.story.dto;

import com.moretale.domain.story.entity.Story;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoryResponse {

    private Long storyId;
    private String title;
    private String prompt;
    private String childName;
    private String primaryLanguage;
    private String secondaryLanguage;
    private Boolean isPublic;
    private LocalDateTime createdAt;
    private List<SlideResponse> slides;

    public static StoryResponse from(Story story) {
        return StoryResponse.builder()
                .storyId(story.getStoryId())
                .title(story.getTitle())
                .prompt(story.getPrompt())
                .childName(story.getChildName())
                .primaryLanguage(story.getPrimaryLanguage())
                .secondaryLanguage(story.getSecondaryLanguage())
                .isPublic(story.getIsPublic())
                .createdAt(story.getCreatedAt())
                .slides(story.getSlides().stream()
                        .map(SlideResponse::from)
                        .collect(Collectors.toList()))
                .build();
    }
}
