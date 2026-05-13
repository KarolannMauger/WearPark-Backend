package edu.wearpark.backend.controller;

import edu.wearpark.backend.domain.User;
import edu.wearpark.backend.dto.ReportResponse;
import edu.wearpark.backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/report")
public class ReportController {

    private final ReportService reportService;

    /**
     * Generate (or regenerate) the PDF for a given year/month.
     * Returns the PDF bytes as application/pdf.
     */
    @PostMapping("/generate")
    ResponseEntity<byte[]> generate(
            @AuthenticationPrincipal User user,
            @RequestParam int year,
            @RequestParam int month
    ) throws Exception {
        byte[] pdf = reportService.generatePdf(user, year, month);
        return pdfResponse(pdf, "rapport_" + year + "_" + String.format("%02d", month) + ".pdf");
    }

    @GetMapping("/history")
    ResponseEntity<List<ReportResponse>> history(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(reportService.history(user.getId(), page, size));
    }

    @GetMapping("/{id}/download")
    ResponseEntity<byte[]> download(
            @AuthenticationPrincipal User user,
            @PathVariable String id
    ) throws Exception {
        byte[] pdf = reportService.download(user, id);
        return pdfResponse(pdf, "rapport_" + id + ".pdf");
    }

    private ResponseEntity<byte[]> pdfResponse(byte[] pdf, String filename) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .body(pdf);
    }
}
