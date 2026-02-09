package com.moretale.domain.story.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StorySaveRequest {

    @NotBlank(message = "제목을 입력해주세요.")
    private String title;

    private String prompt;

    // 프로필 ID (선택적 - 없으면 가장 최근 프로필 사용)
    private Long profileId;

    @NotEmpty(message = "슬라이드가 비어있습니다.")
    private List<SlideRequest> slides;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SlideRequest {
        private Integer order;
        private String imageUrl;
        private String textKr;
        private String textNative;
        private String audioUrlKr;
        private String audioUrlNative;
    }
}
