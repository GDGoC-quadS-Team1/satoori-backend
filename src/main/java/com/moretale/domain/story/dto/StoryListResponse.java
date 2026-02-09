package com.moretale.domain.story.dto;

import com.moretale.domain.story.entity.Story;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoryListResponse {

    private Long storyId;
    private String title;
    private String childName;
    private Boolean isPublic;
    private LocalDateTime createdAt;
    private Integer slideCount;

    public static StoryListResponse from(Story story) {
        return StoryListResponse.builder()
                .storyId(story.getStoryId())
                .title(story.getTitle())
                .childName(story.getChildName())
                .isPublic(story.getIsPublic())
                .createdAt(story.getCreatedAt())
                .slideCount(story.getSlides().size())
                .build();
    }
}
