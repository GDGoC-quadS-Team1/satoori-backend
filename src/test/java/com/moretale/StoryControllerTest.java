package com.moretale;

import com.moretale.domain.story.controller.StoryController;
import com.moretale.domain.story.dto.StoryGenerateResponse;
import com.moretale.domain.story.dto.StoryListResponse;
import com.moretale.domain.story.dto.StoryResponse;
import com.moretale.domain.story.service.StoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class StoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StoryService storyService;

    @Test
    @DisplayName("동화 생성 요청 시 200 응답 반환")
    void generateStoryControllerTest() throws Exception {
        // Given
        StoryGenerateResponse mockResponse = StoryGenerateResponse.builder()
                .title("테스트 동화")
                .childName("민지")
                .slides(new ArrayList<>())
                .build();

        given(storyService.generateStory(anyString(), any())).willReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/stories/generate")
                        .with(oauth2Login()
                                .attributes(attrs -> attrs.put("email", "test@example.com")))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prompt\": \"숲속 이야기\", \"profileId\": 3}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("테스트 동화"))
                .andExpect(jsonPath("$.data.childName").value("민지"));
    }

    @Test
    @DisplayName("동화 상세 조회 시 200 응답 반환")
    void getStoryDetailControllerTest() throws Exception {
        // Given
        StoryResponse mockResponse = StoryResponse.builder()
                .storyId(1L)
                .title("상세 제목")
                .slides(new ArrayList<>())
                .build();

        given(storyService.getStoryDetail(anyString(), anyLong())).willReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/stories/{storyId}", 1L)
                        .with(oauth2Login()
                                .attributes(attrs -> attrs.put("email", "test@example.com"))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.storyId").value(1))
                .andExpect(jsonPath("$.data.title").value("상세 제목"));
    }

    @Test
    @DisplayName("동화 삭제 시 200 응답 반환")
    void deleteStoryControllerTest() throws Exception {
        // Given
        willDoNothing().given(storyService).deleteStory(anyString(), anyLong());

        // When & Then
        mockMvc.perform(delete("/api/stories/{storyId}", 1L)
                        .with(oauth2Login()
                                .attributes(attrs -> attrs.put("email", "test@example.com")))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("동화가 삭제되었습니다."));
    }

    @Test
    @DisplayName("내 동화 목록 조회 시 200 응답과 리스트를 반환한다")
    void getMyStoriesControllerTest() throws Exception {
        // Given
        StoryListResponse story1 = StoryListResponse.builder()
                .storyId(1L)
                .title("첫 번째 이야기")
                .childName("민지")
                .build();

        StoryListResponse story2 = StoryListResponse.builder()
                .storyId(2L)
                .title("두 번째 이야기")
                .childName("민지")
                .build();

        List<StoryListResponse> mockList = Arrays.asList(story1, story2);

        given(storyService.getMyStories(anyString())).willReturn(mockList);

        // When & Then
        mockMvc.perform(get("/api/stories/my")
                        .with(oauth2Login()
                                .attributes(attrs -> attrs.put("email", "test@example.com"))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].title").value("첫 번째 이야기"))
                .andExpect(jsonPath("$.data[1].title").value("두 번째 이야기"));
    }

    @Test
    @DisplayName("공개 동화 목록 조회 시 200 응답 반환")
    void getPublicStoriesTest() throws Exception {
        // Given
        StoryListResponse story1 = StoryListResponse.builder()
                .storyId(1L)
                .title("공개 동화 1")
                .childName("테스트")
                .isPublic(true)
                .build();

        List<StoryListResponse> mockList = Arrays.asList(story1);

        given(storyService.getPublicStories()).willReturn(mockList);

        // When & Then
        mockMvc.perform(get("/api/stories/public")
                        .with(oauth2Login()
                                .attributes(attrs -> attrs.put("email", "test@example.com"))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].title").value("공개 동화 1"));
    }
}
