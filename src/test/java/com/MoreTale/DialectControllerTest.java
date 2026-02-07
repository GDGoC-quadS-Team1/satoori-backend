package com.MoreTale;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.MoreTale.domain.dialect.controller.DialectController;
import com.MoreTale.domain.dialect.dto.DialectRequest;
import com.MoreTale.domain.dialect.dto.DialectResponse;
import com.MoreTale.domain.dialect.service.DialectService;
import com.MoreTale.global.security.jwt.JwtAuthenticationFilter;
import com.MoreTale.global.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DialectController.class)
@AutoConfigureMockMvc(addFilters = false)
class DialectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DialectService dialectService;

    // Security 관련 빈들을 Mock으로 추가
    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("방언 전체 조회 성공")
    void getAllDialects_success() throws Exception {
        DialectResponse r1 = DialectResponse.builder()
                .dialectId(1L)
                .dialect("정구지")
                .standard("부추")
                .region("경상도")
                .build();

        Page<DialectResponse> page = new PageImpl<>(
                List.of(r1),
                PageRequest.of(0, 10),
                1
        );

        when(dialectService.getAllDialects(any(), isNull()))
                .thenReturn(page);

        mockMvc.perform(get("/api/dictionary")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].dialect").value("정구지"));
    }

    @Test
    @DisplayName("방언 상세 조회 성공")
    void getDialect_success() throws Exception {
        DialectResponse response = DialectResponse.builder()
                .dialectId(1L)
                .dialect("정구지")
                .standard("부추")
                .region("경상도")
                .build();

        when(dialectService.getDialectById(eq(1L), isNull()))
                .thenReturn(response);

        mockMvc.perform(get("/api/dictionary/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dialect").value("정구지"));
    }

    @Test
    @DisplayName("방언 등록 성공")
    void createDialect_success() throws Exception {
        DialectRequest request = new DialectRequest();
        request.setDialect("정구지");
        request.setStandard("부추");
        request.setRegion("경상도");

        doNothing().when(dialectService).createDialect(any(DialectRequest.class), isNull());

        mockMvc.perform(post("/api/dictionary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
