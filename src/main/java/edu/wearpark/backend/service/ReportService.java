package edu.wearpark.backend.service;

import edu.wearpark.backend.domain.Report;
import edu.wearpark.backend.domain.User;
import edu.wearpark.backend.domain.view.MotionMonthlyAnalysis;
import edu.wearpark.backend.dto.ReportResponse;
import edu.wearpark.backend.exception.NotFoundException;
import edu.wearpark.backend.repository.PredictionRepository;
import edu.wearpark.backend.repository.ReportRepository;
import edu.wearpark.backend.service.report.ReportPdfService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;

import static edu.wearpark.backend.service.report.ReportPdfConstants.MONTHS_FR;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository      reportRepo;
    private final PredictionRepository  predictionRepo;
    private final MotionViewService     motionViewService;
    private final ReportPdfService      reportPdfService;

    public byte[] generatePdf(User user, int year, int month) throws Exception {
        Instant start = YearMonth.of(year, month).atDay(1)
                .atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end   = YearMonth.of(year, month).plusMonths(1).atDay(1)
                .atStartOfDay(ZoneOffset.UTC).toInstant();

        MotionMonthlyAnalysis monthly   = motionViewService.makeMonthlyAnalysis(start, user);
        var                   predictions = predictionRepo.findByUserIdAndPeriod(user.getId(), start, end);

        reportRepo.findByUserIdAndYearAndMonth(user.getId(), year, month)
                .orElseGet(() -> reportRepo.save(Report.builder()
                        .userId(user.getId())
                        .year(year)
                        .month(month)
                        .title("Rapport " + MONTHS_FR[month - 1] + " " + year)
                        .generatedAt(Instant.now())
                        .build()));

        return reportPdfService.generate(user, year, month, monthly, predictions);
    }

    public List<ReportResponse> history(ObjectId userId, int page, int size) {
        return reportRepo.findHistoryByUserId(userId, PageRequest.of(page, size))
                .stream().map(ReportResponse::from).toList();
    }

    public byte[] download(User user, String reportId) throws Exception {
        var report = reportRepo.findById(new ObjectId(reportId))
                .orElseThrow(() -> new NotFoundException("report"));
        if (!report.getUserId().equals(user.getId()))
            throw new NotFoundException("report");

        return generatePdf(user, report.getYear(), report.getMonth());
    }
}
