package edu.wearpark.backend.controller;

import edu.wearpark.backend.config.AppConfig;
import edu.wearpark.backend.config.MockSecurityConfig;
import edu.wearpark.backend.domain.Report;
import edu.wearpark.backend.dto.ReportResponse;
import edu.wearpark.backend.security.EmailPasswordAuthProvider;
import edu.wearpark.backend.service.JwtService;
import edu.wearpark.backend.service.ReportService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
@Import({AppConfig.class, MockSecurityConfig.class})
class ReportControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean ReportService             reportService;
    @MockitoBean EmailPasswordAuthProvider emailPasswordAuthProvider;
    @MockitoBean JwtService               jwtService;

    private ReportResponse buildResponse() {
        return new ReportResponse(
                new ObjectId("000000000000000000000001").toHexString(),
                2026, 4, "Rapport Avril 2026", Instant.now()
        );
    }

    @Test
    void generate_returnsPdf() throws Exception {
        when(reportService.generatePdf(any(), eq(2026), eq(4)))
                .thenReturn(new byte[]{1, 2, 3});

        mockMvc.perform(post("/report/generate")
                        .param("year", "2026")
                        .param("month", "4"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }

    @Test
    void history_returnsList() throws Exception {
        when(reportService.history(any(), eq(0), eq(10)))
                .thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/report/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].year").value(2026))
                .andExpect(jsonPath("$[0].month").value(4))
                .andExpect(jsonPath("$[0].title").value("Rapport Avril 2026"));
    }

    @Test
    void history_returnsEmptyList() throws Exception {
        when(reportService.history(any(), anyInt(), anyInt()))
                .thenReturn(List.of());

        mockMvc.perform(get("/report/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void download_returnsPdf() throws Exception {
        String id = "000000000000000000000001";
        when(reportService.download(any(), eq(id)))
                .thenReturn(new byte[]{4, 5, 6});

        mockMvc.perform(get("/report/{id}/download", id))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }
}
