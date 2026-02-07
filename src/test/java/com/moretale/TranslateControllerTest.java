package com.moretale;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moretale.domain.translate.controller.TranslateController;
import com.moretale.domain.translate.dto.ReconvertRequest;
import com.moretale.domain.translate.dto.TranslateRequest;
import com.moretale.domain.translate.dto.TranslateResponse;
import com.moretale.domain.translate.service.TranslateService;
import com.moretale.global.exception.EmptyInputException;
import com.moretale.global.exception.InvalidRegionException;
import com.moretale.global.security.jwt.JwtAuthenticationFilter;
import com.moretale.global.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TranslateController.class)
@AutoConfigureMockMvc(addFilters = false)
class TranslateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TranslateService translateService;

    // Security 관련 빈들을 Mock으로 추가
    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("정상 변환 요청 시 201과 변환 결과를 반환")
    void translate_success() throws Exception {
        TranslateResponse mockResponse = TranslateResponse.builder()
                .historyId(1L)
                .original("우리가 왔다고 전해.")
                .converted("우리가 와불따고 전하랑게.")
                .region("JEONRA")
                .regionDisplayName("전라도")
                .audioUrl("https://storage.example.com/tts/abc-123.mp3")
                .build();

        when(translateService.translate(any(TranslateRequest.class), isNull()))
                .thenReturn(mockResponse);

        TranslateRequest request = new TranslateRequest("우리가 왔다고 전해.", "JEONRA");

        mockMvc.perform(post("/api/translate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.historyId").value(1))
                .andExpect(jsonPath("$.original").value("우리가 왔다고 전해."))
                .andExpect(jsonPath("$.converted").value("우리가 와불따고 전하랑게."))
                .andExpect(jsonPath("$.region").value("JEONRA"))
                .andExpect(jsonPath("$.regionDisplayName").value("전라도"))
                .andExpect(jsonPath("$.audioUrl").value("https://storage.example.com/tts/abc-123.mp3"));

        verify(translateService, times(1)).translate(any(TranslateRequest.class), isNull());
    }

    @Test
    @DisplayName("빈 문장 입력 시 400 반환")
    void translate_emptyText() throws Exception {
        when(translateService.translate(any(TranslateRequest.class), isNull()))
                .thenThrow(new EmptyInputException("변환할 문장을 입력해주세요."));

        TranslateRequest request = new TranslateRequest("", "JEONRA");

        mockMvc.perform(post("/api/translate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("변환할 문장을 입력해주세요."));
    }

    @Test
    @DisplayName("지원되지 않는 지역 코드 입력 시 400 반환")
    void translate_invalidRegion() throws Exception {
        when(translateService.translate(any(TranslateRequest.class), isNull()))
                .thenThrow(new InvalidRegionException("지원하지 않는 지역 코드입니다: INVALID_REGION"));

        TranslateRequest request = new TranslateRequest("테스트 문장", "INVALID_REGION");

        mockMvc.perform(post("/api/translate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("지원하지 않는 지역 코드")));
    }

    @Test
    @DisplayName("다른 사투리 보기 → 정상 재변환 시 201 반환")
    void reconvert_success() throws Exception {
        TranslateResponse mockResponse = TranslateResponse.builder()
                .historyId(2L)
                .original("우리가 왔다고 전해.")
                .converted("우리가 왔다카이 전해라.")
                .region("GYEONGSANG_NAMDO")
                .regionDisplayName("경상남도")
                .audioUrl("https://storage.example.com/tts/def-456.mp3")
                .build();

        when(translateService.reconvert(any(ReconvertRequest.class), isNull()))
                .thenReturn(mockResponse);

        ReconvertRequest request = new ReconvertRequest(1L, "GYEONGSANG_NAMDO");

        mockMvc.perform(post("/api/translate/reconvert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.historyId").value(2))
                .andExpect(jsonPath("$.region").value("GYEONGSANG_NAMDO"))
                .andExpect(jsonPath("$.regionDisplayName").value("경상남도"));

        verify(translateService, times(1)).reconvert(any(ReconvertRequest.class), isNull());
    }

    @Test
    @DisplayName("존재하지 않는 historyId 입력 시 400 반환")
    void reconvert_historyNotFound() throws Exception {
        when(translateService.reconvert(any(ReconvertRequest.class), isNull()))
                .thenThrow(new IllegalArgumentException("해당 변환 기록을 찾을 수 없습니다. historyId=999"));

        ReconvertRequest request = new ReconvertRequest(999L, "JEJU");

        mockMvc.perform(post("/api/translate/reconvert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("찾을 수 없습니다")));
    }
}
