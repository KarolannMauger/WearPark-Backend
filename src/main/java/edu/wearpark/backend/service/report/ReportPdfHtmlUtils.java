package edu.wearpark.backend.service.report;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;


public final class ReportPdfHtmlUtils {

    private ReportPdfHtmlUtils() {}

    public static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    public static String nvl(String s, String def) {
        return s != null ? s : def;
    }

    public static String infoRow(String label, String value) {
        return "<tr class='info-row'><td class='lbl'>" + label
             + "</td><td class='val'>" + value + "</td></tr>\n";
    }

    public static String card(String num, String unit, String label, String cls) {
        return "<td><div class='card " + cls + "'>"
             + "<div class='num'>" + esc(num) + "</div>"
             + "<div class='unit'>" + unit + "</div>"
             + "<div class='lbl'>" + label + "</div>"
             + "</div></td>\n";
    }

    public static String tdCard(String content, String label, String cls) {
        return "<td><div class='card " + cls + "'>" + content
             + "<div class='lbl' style='margin-top:4px;'>" + label + "</div>"
             + "</div></td>\n";
    }

    public static String ampClass(double amp) {
        if (amp < 1.0) return "ok";
        if (amp < 2.0) return "warn";
        return "danger";
    }

    public static String ampBadge(double amp) {
        if (amp < 1.0) return "<span class='badge faible'>Faible</span>";
        if (amp < 2.0) return "<span class='badge modere'>Modéré</span>";
        return "<span class='badge eleve'>Élevé</span>";
    }

    public static int pct(long count, long total) {
        return total == 0 ? 0 : (int) Math.round(count * 100.0 / total);
    }

    public static String formatDay(Instant instant) {
        return instant.atZone(ZoneOffset.UTC).toLocalDate().format(ReportPdfConstants.DAY_FMT);
    }

    public static long computeAge(Instant dob) {
        if (dob == null) return 0;
        return ChronoUnit.YEARS.between(
                dob.atZone(ZoneOffset.UTC).toLocalDate(), LocalDate.now());
    }

    public static String formatGender(String gender) {
        if (gender == null) return "Non précisé";
        return switch (gender.toLowerCase()) {
            case "male",   "homme", "m" -> "Homme";
            case "female", "femme", "f" -> "Femme";
            default                     -> gender;
        };
    }
}
