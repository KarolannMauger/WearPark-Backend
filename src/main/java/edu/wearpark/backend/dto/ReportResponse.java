package edu.wearpark.backend.dto;

import edu.wearpark.backend.domain.Report;

import java.time.Instant;

public record ReportResponse(
        String  id,
        int     year,
        int     month,
        String  title,
        Instant generatedAt
) {
    public static ReportResponse from(Report r) {
        return new ReportResponse(
                r.getId().toHexString(),
                r.getYear(),
                r.getMonth(),
                r.getTitle(),
                r.getGeneratedAt()
        );
    }
}
