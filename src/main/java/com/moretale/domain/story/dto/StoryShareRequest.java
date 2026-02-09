package com.moretale.domain.story.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoryShareRequest {

    @NotNull(message = "공유 여부를 설정해주세요.")
    private Boolean isPublic;
}
