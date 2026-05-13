package edu.wearpark.backend.service.report;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.svgsupport.BatikSVGDrawer;
import edu.wearpark.backend.domain.Prediction;
import edu.wearpark.backend.domain.User;
import edu.wearpark.backend.domain.view.MotionDailySummary;
import edu.wearpark.backend.domain.view.MotionMonthlyAnalysis;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import static edu.wearpark.backend.service.report.ReportPdfConstants.MONTHS_FR;
import static edu.wearpark.backend.service.report.ReportPdfHtmlUtils.*;
import static edu.wearpark.backend.service.report.ReportPdfModels.*;
import static edu.wearpark.backend.service.report.ReportPdfSections.*;


@Service
public class ReportPdfService {

    public byte[] generate(User user, int year, int month,
                           MotionMonthlyAnalysis monthly,
                           List<Prediction> predictions) throws Exception {
        String html = buildHtml(user, year, month, monthly, predictions);
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.useSVGDrawer(new BatikSVGDrawer());
            builder.withHtmlContent(html, null);
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        }
    }

    private String buildHtml(User user, int year, int month,
                             MotionMonthlyAnalysis monthly,
                             List<Prediction> predictions) {

        String monthName = MONTHS_FR[month - 1];
        String period    = monthName + " " + year;
        String fullName  = nvl(user.getFirstName(), "") + " " + nvl(user.getLastName(), "");
        long   age       = computeAge(user.getDateOfBirth());
        String gender    = formatGender(user.getGender());
        String diagnosis = Boolean.TRUE.equals(user.getHasDiagnosis())
                ? nvl(user.getDiagnosis(), "Maladie de Parkinson confirmée")
                : "Aucun diagnostic connu";
        String today = LocalDate.now().format(
                DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH));

        List<MotionDailySummary> days = monthly.getDays() != null ? monthly.getDays() : List.of();
        double coverage  = monthly.getCoverage()      != null ? monthly.getCoverage() * 100      : 0;
        double meanAmp   = monthly.getMeanAmplitude() != null ? monthly.getMeanAmplitude()        : 0;
        double peakAmp   = days.stream().filter(d -> d.getPeakAmplitude() != null)
                              .mapToDouble(MotionDailySummary::getPeakAmplitude).max().orElse(0);
        int daysWithData = (int) days.stream()
                .filter(d -> d.getCoverage() != null && d.getCoverage() > 0).count();

        List<EnrichedDay> enriched = enrichDays(days);

        MlStats ml    = computeMlStats(predictions);
        String  trend = computeTrend(predictions);

        return "<?xml version='1.0' encoding='UTF-8'?>\n"
             + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\""
             + " \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n"
             + "<html xmlns='http://www.w3.org/1999/xhtml'>\n"
             + "<head>\n"
             + "<meta http-equiv='Content-Type' content='text/html; charset=UTF-8'/>\n"
             + "<style type='text/css'>\n" + ReportPdfCss.build() + "\n</style>\n"
             + "</head>\n<body>\n"
             + sectionCover(fullName, age, gender, diagnosis, period, today)
             + sectionSummary(coverage, meanAmp, peakAmp, daysWithData, days.size(),
                              predictions.size(), ml, enriched)
             + sectionCalendarHeatmap(enriched, year, month, monthName)
             + sectionWeeklyBreakdown(enriched, predictions)
             + "<div class='page-break'></div>\n"
             + sectionMotion(enriched, monthName)
             + "<div class='page-break'></div>\n"
             + sectionMl(ml, trend, predictions)
             + sectionInterpretation(ml, coverage, meanAmp, trend, enriched)
             + "</body>\n</html>";
    }
}
