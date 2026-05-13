package edu.wearpark.backend.service.report;

import edu.wearpark.backend.domain.Prediction;
import edu.wearpark.backend.domain.view.MotionDailySummary;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static edu.wearpark.backend.service.report.ReportPdfHtmlUtils.nvl;


public final class ReportPdfModels {

    private ReportPdfModels() {}

    public record EnrichedDay(
            Instant start,
            Instant end,
            Double  coverage,
            Double  meanAmplitude,
            Double  peakAmplitude,
            Double  variance,
            Double  deltaMeanAmplitude,
            Double  deltaVariance
    ) {}

    public record MlStats(
            long   okCount,
            long   monitoringCount,
            long   parkinsonCount,
            long   lowConfCount,
            double avgProb
    ) {
        String dominant() {
            if (parkinsonCount >= monitoringCount && parkinsonCount >= okCount) return "parkinson";
            if (monitoringCount >= okCount) return "monitoring";
            return "ok";
        }
        String stateClass() {
            return switch (dominant()) {
                case "parkinson" -> "danger";
                case "monitoring" -> "warn";
                default -> "ok";
            };
        }
        String stateLabel() {
            return switch (dominant()) {
                case "parkinson" -> "Signal Parkinson";
                case "monitoring" -> "Surveillance";
                default -> "Normal";
            };
        }
    }

    public static List<EnrichedDay> enrichDays(List<MotionDailySummary> days) {
        List<EnrichedDay> result = new ArrayList<>();
        Double prevVariance = null;
        for (MotionDailySummary d : days) {
            Double dv = null;
            if (d.getVariance() != null && prevVariance != null)
                dv = d.getVariance() - prevVariance;
            result.add(new EnrichedDay(
                    d.getStart(), d.getEnd(),
                    d.getCoverage(), d.getMeanAmplitude(), d.getPeakAmplitude(),
                    d.getVariance(), d.getDeltaMeanAmplitude(), dv));
            if (d.getVariance() != null) prevVariance = d.getVariance();
        }
        return result;
    }

    public static MlStats computeMlStats(List<Prediction> predictions) {
        long ok = 0, monitoring = 0, parkinson = 0, lowConf = 0;
        double sumProb = 0, countProb = 0;

        for (Prediction p : predictions) {
            if (p.getProbability() != null) { sumProb += p.getProbability(); countProb++; }
            String conf = nvl(p.getConfidence(), "LOW");
            int    pred = p.getPrediction()  != null ? p.getPrediction()  : 0;
            double prob = p.getProbability() != null ? p.getProbability() : 0.0;

            if ("LOW".equalsIgnoreCase(conf)) {
                monitoring++;
                lowConf++;
            } else if (pred == 0) {
                ok++;
            } else if (prob >= 0.65) {
                parkinson++;
            } else {
                monitoring++;
            }
        }
        return new MlStats(ok, monitoring, parkinson, lowConf,
                countProb > 0 ? sumProb / countProb : 0);
    }

    public static String classifyPrediction(Prediction p) {
        String conf = nvl(p.getConfidence(), "LOW");
        if ("LOW".equalsIgnoreCase(conf)) return "uncertain";
        int    pred = p.getPrediction()  != null ? p.getPrediction()  : 0;
        double pr   = p.getProbability() != null ? p.getProbability() : 0.0;
        if (pred == 0)  return "ok";
        if (pr >= 0.65) return "parkinson";
        return "monitoring";
    }

    public static String computeTrend(List<Prediction> predictions) {
        if (predictions.size() < 4) return "Stable";
        List<Prediction> sorted = predictions.stream()
                .filter(p -> p.getProbability() != null)
                .sorted(Comparator.comparing(Prediction::getCreatedAt))
                .toList();
        if (sorted.size() < 4) return "Stable";
        int half   = sorted.size() / 2;
        double first  = sorted.subList(0, half).stream()
                .mapToDouble(Prediction::getProbability).average().orElse(0);
        double second = sorted.subList(half, sorted.size()).stream()
                .mapToDouble(Prediction::getProbability).average().orElse(0);
        double diff   = second - first;
        if (diff < -0.05) return "Amélioration";
        if (diff >  0.05) return "Dégradation";
        return "Stable";
    }
}
