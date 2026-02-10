package com.moretale;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moretale.domain.tts.dto.TTSRequest;
import com.moretale.domain.tts.service.TTSService;
import com.moretale.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TtsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TTSService ttsService;

    @Test
    @DisplayName("1. 한국어 TTS 생성 요청 성공 테스트")
    @WithMockUser // Security permitAll 설정 확인용
    void generateKoreanTtsSuccess() throws Exception {
        // given
        TTSRequest request = TTSRequest.builder()
                .text("안녕하세요, 테스트 음성입니다.")
                .language("ko-KR")
                .style("child_friendly")
                .build();

        // when & then
        mockMvc.perform(post("/api/tts/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.audioUrl").isNotEmpty())
                .andExpect(jsonPath("$.data.language").value("ko-KR"));
    }

    @Test
    @DisplayName("2. 다국어(베트남어) TTS 생성 요청 성공 테스트")
    void generateVietnameseTtsSuccess() throws Exception {
        // given
        TTSRequest request = TTSRequest.builder()
                .text("Xin chào, đây là giọng nói thử nghiệm.")
                .language("vi-VN")
                .build();

        // when & then
        mockMvc.perform(post("/api/tts/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.language").value("vi-VN"));
    }

    @Test
    @DisplayName("3. 유효하지 않은 언어 코드 요청 시 에러 발생")
    void generateTtsInvalidLanguageFail() throws Exception {
        // given
        TTSRequest request = TTSRequest.builder()
                .text("Test")
                .language("invalid-code")
                .build();

        // when & then
        mockMvc.perform(post("/api/tts/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        // ErrorCode 적용 방식에 따라 jsonPath("$.code").value("T002") 추가 가능
    }

    @Test
    @DisplayName("4. 생성된 파일의 URL로 실제 파일 접근 가능 여부 테스트")
    void verifyGeneratedFileAccess() throws Exception {
        // 1. 먼저 TTS 생성
        TTSRequest request = TTSRequest.builder()
                .text("파일 접근 테스트")
                .language("ko-KR")
                .build();

        String responseString = mockMvc.perform(post("/api/tts/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        // 2. 응답에서 URL 추출 (예: http://localhost:8080/uploads/...)
        String audioUrl = objectMapper.readTree(responseString).path("data").path("audioUrl").asText();
        String pathOnly = audioUrl.substring(audioUrl.indexOf("/uploads"));

        // 3. 정적 리소스 핸들러를 통해 파일 접근 시도
        mockMvc.perform(get(pathOnly))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("audio/mpeg"));
    }

    @Test
    @DisplayName("5. 빈 텍스트 요청 시 Validation 에러 테스트")
    void generateTtsEmptyTextFail() throws Exception {
        // given
        TTSRequest request = TTSRequest.builder()
                .text("") // 빈 값
                .language("ko-KR")
                .build();

        // when & then
        mockMvc.perform(post("/api/tts/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
