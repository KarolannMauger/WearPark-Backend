package edu.wearpark.backend.service.report;

import edu.wearpark.backend.domain.Prediction;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

import static edu.wearpark.backend.service.report.ReportPdfConstants.*;
import static edu.wearpark.backend.service.report.ReportPdfHtmlUtils.*;
import static edu.wearpark.backend.service.report.ReportPdfModels.*;


public final class ReportPdfSections {

    private ReportPdfSections() {}

    public static String sectionCover(String name, long age, String gender,
                                      String diagnosis, String period, String today) {
        return "<div class='header'>"
             + "<h1>Rapport mensuel WearPark</h1>"
             + "<p class='sub'>Suivi des tremblements &#8212; " + esc(period) + "</p>"
             + "<p>G&#233;n&#233;r&#233; le " + esc(today) + "</p>"
             + "</div>\n"
             + "<div class='section-title'>Informations du patient</div>\n"
             + "<table class='info-grid'><tbody>"
             + infoRow("Nom complet",  esc(name.trim().isEmpty() ? "N/A" : name))
             + infoRow("&#194;ge",     age > 0 ? age + " ans" : "Non renseign&#233;")
             + infoRow("Genre",        esc(gender))
             + infoRow("Diagnostic",   esc(diagnosis))
             + infoRow("P&#233;riode", esc(period))
             + "</tbody></table>\n";
    }

    public static String sectionSummary(double coverage, double meanAmp, double peakAmp,
                                        int daysWithData, int totalDays, int totalPred,
                                        MlStats ml, List<EnrichedDay> enriched) {
        Optional<EnrichedDay> bestDay  = enriched.stream()
                .filter(d -> d.meanAmplitude() != null && d.meanAmplitude() > 0)
                .min(Comparator.comparingDouble(EnrichedDay::meanAmplitude));
        Optional<EnrichedDay> worstDay = enriched.stream()
                .filter(d -> d.meanAmplitude() != null)
                .max(Comparator.comparingDouble(EnrichedDay::meanAmplitude));
        double avgVariance = enriched.stream()
                .filter(d -> d.variance() != null)
                .mapToDouble(EnrichedDay::variance).average().orElse(0);

        String ampCls   = ampClass(meanAmp);
        String stateCls = ml.stateClass();

        StringBuilder sb = new StringBuilder();
        sb.append("<div class='section-title'>R&#233;sum&#233; du mois</div>\n");

        sb.append("<table class='cards'><tr>")
          .append(card(String.format("%.0f", coverage),     "%",       "Couverture moy.", ""))
          .append(card(String.format("%.2f", meanAmp),      "m/s&#178;", "Intensit&#233; moy. des tremblements", ampCls))
          .append(card(String.format("%.2f", peakAmp),      "m/s&#178;", "Pic max. du mois", ampCls))
          .append(card(String.format("%.4f", avgVariance),  "variance",  "Stabilit&#233; moy.", ""))
          .append("</tr></table>\n");

        sb.append("<table class='cards'><tr>")
          .append(card(daysWithData + "/" + totalDays, "jours",    "Jours avec donn&#233;es", ""))
          .append(card(String.valueOf(totalPred),       "analyses", "Pr&#233;dictions ML", "accent"))
          .append(card(String.format("%.1f%%", ml.avgProb() * 100), "probabilit&#233; moy.",
                  "Probabilit&#233; Parkinson", stateCls));

        if (bestDay.isPresent() && worstDay.isPresent()) {
            sb.append("<td><div class='card'>"
                + "<div style='font-size:9px;color:" + C_SUCCESS + ";font-weight:bold;'>Meilleur jour</div>"
                + "<div style='font-size:11px;font-weight:bold;color:" + C_TEXT + ";'>" + formatDay(bestDay.get().start()) + "</div>"
                + "<div style='font-size:9px;color:" + C_MUTED + ";'>" + String.format("%.2f", bestDay.get().meanAmplitude()) + " m/s&#178;</div>"
                + "<div style='font-size:9px;color:" + C_DANGER + ";font-weight:bold;margin-top:4px;'>Pire jour</div>"
                + "<div style='font-size:11px;font-weight:bold;color:" + C_TEXT + ";'>" + formatDay(worstDay.get().start()) + "</div>"
                + "<div style='font-size:9px;color:" + C_MUTED + ";'>" + String.format("%.2f", worstDay.get().meanAmplitude()) + " m/s&#178;</div>"
                + "</div></td>\n");
        } else {
            sb.append(card("--", "", "Meilleur / pire jour", ""));
        }
        sb.append("</tr></table>\n");
        return sb.toString();
    }

    public static String sectionCalendarHeatmap(List<EnrichedDay> enriched,
                                                int year, int month, String monthName) {
        Map<Integer, EnrichedDay> byDay = new LinkedHashMap<>();
        for (EnrichedDay d : enriched) {
            if (d.start() != null) {
                int dom = d.start().atZone(ZoneOffset.UTC).getDayOfMonth();
                byDay.put(dom, d);
            }
        }

        DayOfWeek firstDow  = LocalDate.of(year, month, 1).getDayOfWeek();
        int startOffset     = firstDow.getValue() - 1;
        int daysInMonth     = LocalDate.of(year, month, 1).lengthOfMonth();

        StringBuilder sb = new StringBuilder();
        sb.append("<div class='section-title'>Vue calendrier &#8212; ").append(esc(monthName)).append("</div>\n");
        sb.append("<p style='font-size:8px;color:").append(C_MUTED).append(";margin-bottom:6px;'>"
                + "Intensit&#233; moyenne des tremblements par jour (m/s&#178;). "
                + "Vert&#160;: faible, Orange&#160;: mod&#233;r&#233;, Rouge&#160;: &#233;lev&#233;, Gris&#160;: aucune donn&#233;e.</p>\n");

        sb.append("<table class='cal-table'><thead><tr>");
        for (String dn : DAYS_FR_SHORT)
            sb.append("<th class='cal-th' style='width:14.28%;'>").append(dn).append("</th>");
        sb.append("</tr></thead><tbody>\n");

        int col = startOffset;
        boolean firstRow = true;
        if (col > 0) {
            sb.append("<tr>");
            for (int i = 0; i < col; i++) sb.append("<td class='cal-cell cal-empty'>&#160;</td>");
            firstRow = false;
        }

        for (int day = 1; day <= daysInMonth; day++) {
            if (col == 0) {
                if (!firstRow) sb.append("</tr>");
                sb.append("<tr>");
                firstRow = false;
            }

            EnrichedDay ed = byDay.get(day);
            Double amp = ed != null ? ed.meanAmplitude() : null;
            String bg, txtColor;
            if (amp == null || amp == 0) {
                bg = C_NODATA; txtColor = "#fff";
            } else if (amp < 1.0) {
                bg = "#B7E4C7"; txtColor = "#1A5E2A";
            } else if (amp < 2.0) {
                bg = "#FDDCAA"; txtColor = "#7A4A00";
            } else {
                bg = "#FFBBBB"; txtColor = "#7A0000";
            }

            String deltaArrow = "";
            if (ed != null && ed.deltaMeanAmplitude() != null
                    && Double.isFinite(ed.deltaMeanAmplitude())
                    && Math.abs(ed.deltaMeanAmplitude()) > 0.02) {
                deltaArrow = ed.deltaMeanAmplitude() > 0
                        ? "<span style='color:" + C_DANGER  + ";font-size:8px;font-weight:bold;'>+</span>"
                        : "<span style='color:" + C_SUCCESS + ";font-size:8px;font-weight:bold;'>-</span>";
            }

            sb.append("<td class='cal-cell' style='background:").append(bg).append(";'>");
            sb.append("<div class='cal-num' style='color:").append(txtColor).append(";'>").append(day).append(deltaArrow).append("</div>");
            if (amp != null && amp > 0) {
                sb.append("<div class='cal-amp' style='color:").append(txtColor).append(";'>")
                  .append(String.format("%.2f", amp)).append("</div>");
            } else {
                sb.append("<div class='cal-amp'>&#160;</div>");
            }
            sb.append("</td>");
            col = (col + 1) % 7;
        }

        if (col > 0) {
            for (int i = col; i < 7; i++) sb.append("<td class='cal-cell cal-empty'>&#160;</td>");
        }
        sb.append("</tr>");  // toujours fermer la dernière ligne (même si col==0, i.e. semaine complète)
        sb.append("</tbody></table>\n");

        sb.append("<p style='font-size:8px;color:").append(C_MUTED).append(";margin-top:4px;'>"
                + "<span style='color:").append(C_DANGER).append(";font-weight:bold;'>+</span>"
                + " = intensit&#233; en hausse vs jour pr&#233;c&#233;dent&#160;&#160;"
                + "<span style='color:").append(C_SUCCESS).append(";font-weight:bold;'>-</span>"
                + " = intensit&#233; en baisse vs jour pr&#233;c&#233;dent</p>\n");

        return sb.toString();
    }

    public static String sectionWeeklyBreakdown(List<EnrichedDay> enriched,
                                                List<Prediction> predictions) {
        if (enriched.isEmpty()) return "";

        List<List<EnrichedDay>> weeks = new ArrayList<>();
        List<EnrichedDay> current = new ArrayList<>();
        int colStart = 0;

        EnrichedDay first = enriched.get(0);
        if (first.start() != null) {
            colStart = first.start().atZone(ZoneOffset.UTC).getDayOfWeek().getValue() - 1;
        }
        for (int i = 0; i < colStart; i++) current.add(null);
        for (EnrichedDay d : enriched) {
            current.add(d);
            if (current.size() == 7) { weeks.add(current); current = new ArrayList<>(); }
        }
        if (!current.isEmpty()) {
            while (current.size() < 7) current.add(null);
            weeks.add(current);
        }

        Map<Integer, List<Double>> probByWeek = new HashMap<>();
        for (Prediction p : predictions) {
            if (p.getCreatedAt() == null || p.getProbability() == null) continue;
            LocalDate pd = p.getCreatedAt().atZone(ZoneOffset.UTC).toLocalDate();
            for (int wi = 0; wi < weeks.size(); wi++) {
                boolean inWeek = weeks.get(wi).stream()
                        .filter(Objects::nonNull)
                        .anyMatch(d -> d.start() != null
                                && d.start().atZone(ZoneOffset.UTC).toLocalDate().equals(pd));
                if (inWeek) {
                    probByWeek.computeIfAbsent(wi, k -> new ArrayList<>()).add(p.getProbability());
                    break;
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<div class='section-title'>Analyse semaine par semaine</div>\n");
        sb.append("<table class='data-table'><thead><tr>")
          .append("<th>Semaine</th>")
          .append("<th>Jours actifs</th>")
          .append("<th>Intensit&#233; moy.</th>")
          .append("<th>Pic moy.</th>")
          .append("<th>Stabilit&#233; (var.)</th>")
          .append("<th>Couverture moy.</th>")
          .append("<th>Proba ML moy.</th>")
          .append("<th>Tendance intensit&#233;</th>")
          .append("</tr></thead><tbody>\n");

        double prevWeekMean = -1;
        for (int wi = 0; wi < weeks.size(); wi++) {
            List<EnrichedDay> week = weeks.get(wi);
            List<EnrichedDay> active = week.stream()
                    .filter(d -> d != null && d.meanAmplitude() != null && d.meanAmplitude() > 0)
                    .toList();
            if (active.isEmpty()) continue;

            OptionalDouble mean = active.stream().mapToDouble(EnrichedDay::meanAmplitude).average();
            OptionalDouble peak = active.stream().mapToDouble(d -> d.peakAmplitude() != null ? d.peakAmplitude() : 0).average();
            OptionalDouble var  = active.stream().filter(d -> d.variance() != null).mapToDouble(EnrichedDay::variance).average();
            OptionalDouble cov  = active.stream().filter(d -> d.coverage() != null).mapToDouble(d -> d.coverage() * 100).average();
            List<Double> probs  = probByWeek.getOrDefault(wi, List.of());
            OptionalDouble prob = probs.stream().mapToDouble(Double::doubleValue).average();

            String trendArrow = "--";
            if (prevWeekMean > 0 && mean.isPresent()) {
                double diff = mean.getAsDouble() - prevWeekMean;
                if      (diff >  0.05) trendArrow = "<span class='up'>(+) +" + String.format("%.2f", diff) + "</span>";
                else if (diff < -0.05) trendArrow = "<span class='down'>(-) " + String.format("%.2f", diff) + "</span>";
                else                   trendArrow = "<span class='flat'>(=) stable</span>";
            }

            String weekLabel = "S" + (wi + 1);
            Optional<EnrichedDay> firstReal = week.stream().filter(Objects::nonNull).filter(d -> d.start() != null).findFirst();
            Optional<EnrichedDay> lastReal  = week.stream().filter(Objects::nonNull).filter(d -> d.start() != null).reduce((a, b) -> b);
            if (firstReal.isPresent() && lastReal.isPresent())
                weekLabel = formatDay(firstReal.get().start()) + " &#8212; " + formatDay(lastReal.get().start());

            sb.append("<tr>")
              .append("<td>").append(weekLabel).append("</td>")
              .append("<td>").append(active.size()).append(" / 7</td>")
              .append("<td style='font-weight:bold;'>").append(mean.isPresent() ? String.format("%.3f m/s&#178;", mean.getAsDouble()) : "--").append("</td>")
              .append("<td>").append(peak.isPresent() ? String.format("%.3f", peak.getAsDouble()) : "--").append("</td>")
              .append("<td>").append(var.isPresent()  ? String.format("%.4f", var.getAsDouble())  : "--").append("</td>")
              .append("<td>").append(cov.isPresent()  ? String.format("%.0f%%", cov.getAsDouble()) : "--").append("</td>")
              .append("<td>").append(prob.isPresent() ? String.format("%.1f%%", prob.getAsDouble() * 100) : "--").append("</td>")
              .append("<td>").append(trendArrow).append("</td>")
              .append("</tr>\n");

            if (mean.isPresent()) prevWeekMean = mean.getAsDouble();
        }
        sb.append("</tbody></table>\n");
        sb.append("<p style='font-size:8px;color:").append(C_MUTED).append(";margin-top:4px;'>"
                + "Intensit&#233; moy.&#160;: intensit&#233; moyenne des tremblements sur la semaine. "
                + "Stabilit&#233;&#160;: variance (plus bas = plus r&#233;guliers). "
                + "Tendance&#160;: comparaison vs semaine pr&#233;c&#233;dente.</p>\n");
        return sb.toString();
    }

    public static String sectionMotion(List<EnrichedDay> enriched, String monthName) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class='section-title'>Activit&#233; des tremblements</div>\n");

        if (enriched.isEmpty()) {
            sb.append("<p style='color:").append(C_MUTED)
              .append(";font-style:italic;'>Aucune donn&#233;e disponible pour ce mois.</p>\n");
            return sb.toString();
        }

        sb.append("<div class='chart-box'>\n")
          .append(buildBarChart(enriched))
          .append("<p>Intensit&#233; moyenne des tremblements par jour (m/s&#178;) &#8212; "
                + "Vert&#160;: faible (&lt;1), Orange&#160;: mod&#233;r&#233; (1-2), Rouge&#160;: &#233;lev&#233; (&gt;2)</p>\n")
          .append("</div>\n");

        sb.append("<div class='section-title' style='font-size:11px;'>D&#233;tail journalier &#8212; ")
          .append(esc(monthName)).append("</div>\n");

        long emptyDays = enriched.stream()
                .filter(d -> d.meanAmplitude() == null || d.meanAmplitude() == 0
                          || d.coverage() == null || d.coverage() == 0)
                .count();
        if (emptyDays > 0) {
            sb.append("<p style='font-size:8.5px;color:").append(C_MUTED)
              .append(";background:#F3F4F6;border-left:3px solid ").append(C_BORDER)
              .append(";padding:5px 8px;border-radius:0 4px 4px 0;margin-bottom:6px;'>")
              .append(emptyDays).append(" jour(s) sans donn&#233;es bracelet ce mois (affich&#233;s en gris).")
              .append("</p>\n");
        }

        sb.append("<table class='data-table'><thead><tr>")
          .append("<th>Date</th>")
          .append("<th>Couverture</th>")
          .append("<th>Intensit&#233; moy.</th>")
          .append("<th>Pic intensit&#233;</th>")
          .append("<th>Stabilit&#233; (var.)</th>")
          .append("<th>Var. Intensit&#233; J-1</th>")
          .append("<th>Var. Stabilit&#233; J-1</th>")
          .append("<th>Niveau</th>")
          .append("</tr></thead><tbody>\n");

        for (EnrichedDay d : enriched) {
            String date    = d.start() != null ? esc(formatDay(d.start())) : "--";
            boolean hasData = d.meanAmplitude() != null && d.meanAmplitude() > 0
                           && d.coverage()      != null && d.coverage() > 0;

            if (!hasData) {
                sb.append("<tr style='color:").append(C_NODATA).append(";'>")
                  .append("<td>").append(date).append("</td>")
                  .append("<td colspan='7' style='font-style:italic;font-size:8px;'>Aucune donn&#233;e</td>")
                  .append("</tr>\n");
                continue;
            }

            String cov  = String.format("%.0f%%", d.coverage() * 100);
            String amp  = String.format("%.3f", d.meanAmplitude());
            String peak = (d.peakAmplitude() != null && Double.isFinite(d.peakAmplitude()) && d.peakAmplitude() > 0)
                          ? String.format("%.3f", d.peakAmplitude()) : "--";
            String var_ = (d.variance() != null && Double.isFinite(d.variance()))
                          ? String.format("%.4f", d.variance()) : "--";
            String badge = ampBadge(d.meanAmplitude());

            String deltaAmp = "--";
            if (d.deltaMeanAmplitude() != null && Double.isFinite(d.deltaMeanAmplitude())
                    && Math.abs(d.deltaMeanAmplitude()) > 0.001) {
                String sign  = d.deltaMeanAmplitude() >= 0 ? "+" : "";
                String cls   = d.deltaMeanAmplitude() >  0.02 ? "up" : d.deltaMeanAmplitude() < -0.02 ? "down" : "flat";
                String label = d.deltaMeanAmplitude() >  0.02 ? "(+) " : d.deltaMeanAmplitude() < -0.02 ? "(-) " : "(=) ";
                deltaAmp = "<span class='" + cls + "'>" + label + sign
                         + String.format("%.3f", d.deltaMeanAmplitude()) + "</span>";
            }

            String deltaVar = "--";
            if (d.deltaVariance() != null && Double.isFinite(d.deltaVariance())
                    && Math.abs(d.deltaVariance()) > 0.0001) {
                String sign  = d.deltaVariance() >= 0 ? "+" : "";
                String cls   = d.deltaVariance() >  0.001 ? "up" : d.deltaVariance() < -0.001 ? "down" : "flat";
                String label = d.deltaVariance() >  0.001 ? "(+) " : d.deltaVariance() < -0.001 ? "(-) " : "(=) ";
                deltaVar = "<span class='" + cls + "'>" + label + sign
                         + String.format("%.4f", d.deltaVariance()) + "</span>";
            }

            sb.append("<tr>")
              .append("<td>").append(date).append("</td>")
              .append("<td>").append(cov).append("</td>")
              .append("<td>").append(amp).append(" m/s&#178;</td>")
              .append("<td>").append(peak).append("</td>")
              .append("<td>").append(var_).append("</td>")
              .append("<td>").append(deltaAmp).append("</td>")
              .append("<td>").append(deltaVar).append("</td>")
              .append("<td>").append(badge).append("</td>")
              .append("</tr>\n");
        }
        sb.append("</tbody></table>\n");
        return sb.toString();
    }

    public static String sectionMl(MlStats ml, String trend, List<Prediction> predictions) {
        if (predictions.isEmpty()) {
            return "<div class='section-title'>R&#233;sultats de l'analyse IA</div>\n"
                 + "<p style='color:" + C_MUTED + ";font-style:italic;'>Aucune pr&#233;diction disponible.</p>\n";
        }

        int total         = predictions.size();
        int okPct         = pct(ml.okCount(),        total);
        int monitoringPct = pct(ml.monitoringCount(), total);
        int parkinsonPct  = Math.max(0, 100 - okPct - monitoringPct);
        long monitoringCertain = ml.monitoringCount() - ml.lowConfCount();

        StringBuilder sb = new StringBuilder();
        sb.append("<div class='section-title'>R&#233;sultats de l'analyse IA</div>\n");

        sb.append("<table class='cards'><tr>")
          .append(card(String.valueOf(ml.okCount()),        "sessions", "&#201;tat normal",    "ok"))
          .append(card(String.valueOf(monitoringCertain),   "sessions", "Surveillance",         "warn"))
          .append(card(String.valueOf(ml.lowConfCount()),   "sessions", "Mod&#232;le incertain",""))
          .append(card(String.valueOf(ml.parkinsonCount()), "sessions", "Signal Parkinson",     "danger"))
          .append("</tr></table>\n");
        sb.append("<table class='cards'><tr>")
          .append(card(String.format("%.1f%%", ml.avgProb() * 100), "probabilit&#233; moy.",
                  "Probabilit&#233; Parkinson moy.", ampClass(ml.avgProb() * 2)))
          .append("</tr></table>\n");

        int uncertainPct        = pct(ml.lowConfCount(), total);
        int monitoringCertainPct = Math.max(0, monitoringPct - uncertainPct);
        sb.append("<p style='font-size:9px;color:").append(C_MUTED).append(";margin:6px 0 2px;'>Distribution des &#233;tats ML</p>");
        sb.append("<table class='dist-bar'><tr>");
        if (okPct > 0)
            sb.append("<td class='dist-seg' style='width:").append(okPct)
              .append("%;background:").append(C_SUCCESS).append(";border-radius:4px 0 0 4px;'></td>");
        if (monitoringCertainPct > 0)
            sb.append("<td class='dist-seg' style='width:").append(monitoringCertainPct)
              .append("%;background:").append(C_WARNING).append(";'></td>");
        if (uncertainPct > 0)
            sb.append("<td class='dist-seg' style='width:").append(uncertainPct)
              .append("%;background:#9CA3AF;'></td>");
        if (parkinsonPct > 0)
            sb.append("<td class='dist-seg' style='width:").append(parkinsonPct)
              .append("%;background:").append(C_DANGER).append(";border-radius:0 4px 4px 0;'></td>");
        sb.append("</tr></table>\n");
        sb.append("<p style='font-size:8px;color:").append(C_MUTED).append(";margin-top:3px;'>")
          .append("<span style='color:").append(C_SUCCESS).append(";font-weight:bold;'>&#9632;</span> Normal (").append(okPct).append("%)&#160;")
          .append("<span style='color:").append(C_WARNING).append(";font-weight:bold;'>&#9632;</span> Surveillance (").append(monitoringCertainPct).append("%)&#160;")
          .append("<span style='color:#9CA3AF;font-weight:bold;'>&#9632;</span> Incertain (").append(uncertainPct).append("%)&#160;")
          .append("<span style='color:").append(C_DANGER).append(";font-weight:bold;'>&#9632;</span> Parkinson (").append(parkinsonPct).append("%) ")
          .append("</p>\n");

        if (ml.lowConfCount() > 0) {
            double lowConfPct = ml.lowConfCount() * 100.0 / total;
            sb.append("<p style='font-size:8.5px;color:#374151;background:#F3F4F6;border-left:3px solid #9CA3AF;"
                    + "padding:5px 8px;border-radius:0 4px 4px 0;margin:6px 0;'>"
                    + "<strong>&#9432; Analyse incertaine :</strong> ")
              .append(ml.lowConfCount()).append(" analyse(s) (")
              .append(String.format("%.0f%%", lowConfPct))
              .append(") ont une confiance faible (LOW). Ces r&#233;sultats ne permettent pas de conclure&#160;; "
                    + "ils sont affich&#233;s en surveillance par pr&#233;caution.</p>\n");
        }

        String trendClass = switch (trend) {
            case "Amélioration" -> "trend-improve";
            case "Dégradation"  -> "trend-worsen";
            default             -> "trend-stable";
        };
        sb.append("<p style='margin:10px 0 5px;font-size:10px;'>Tendance probabilit&#233; ML sur le mois : <span class='")
          .append(trendClass).append("'>").append(esc(trend)).append("</span></p>\n");

        List<Prediction> last = predictions.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(10).toList();

        sb.append("<div class='section-title' style='font-size:11px;'>Derni&#232;res pr&#233;dictions</div>\n");
        sb.append("<table class='data-table'><thead><tr>")
          .append("<th>Date</th><th>Verdict ML</th><th>Probabilit&#233;</th><th>Pr&#233;diction brute</th><th>Confiance</th>")
          .append("</tr></thead><tbody>\n");

        for (Prediction p : last) {
            String date    = p.getCreatedAt()  != null ? esc(formatDay(p.getCreatedAt()))  : "--";
            String rawLbl  = nvl(p.getLabel(), nvl(p.getState(), "--"));
            String prob    = p.getProbability() != null ? String.format("%.1f%%", p.getProbability() * 100) : "--";
            String predStr = p.getPrediction()  != null
                    ? (p.getPrediction() == 1 ? "Tremor d&#233;tect&#233;" : "Aucun tremor") : "--";
            String conf    = nvl(p.getConfidence(), "LOW");
            String cls     = classifyPrediction(p);

            String confColor = switch (conf.toUpperCase()) {
                case "HIGH"   -> C_SUCCESS;
                case "MEDIUM" -> C_WARNING;
                default       -> "#9CA3AF";
            };
            String confCell  = "<span style='font-weight:bold;color:" + confColor + ";'>" + esc(conf) + "</span>";
            String badgeLabel = "LOW".equalsIgnoreCase(conf) ? "Incertain" : esc(rawLbl);

            sb.append("<tr>")
              .append("<td>").append(date).append("</td>")
              .append("<td><span class='badge ").append(cls).append("'>").append(badgeLabel).append("</span></td>")
              .append("<td>").append(prob).append("</td>")
              .append("<td>").append(predStr).append("</td>")
              .append("<td>").append(confCell).append("</td>")
              .append("</tr>\n");
        }
        sb.append("</tbody></table>\n");
        return sb.toString();
    }

    public static String sectionInterpretation(MlStats ml, double coverage, double meanAmp,
                                               String trend, List<EnrichedDay> enriched) {
        long certainParkinson  = ml.parkinsonCount();
        long certainMonitoring = ml.monitoringCount() - ml.lowConfCount();
        long total             = ml.okCount() + ml.monitoringCount() + ml.parkinsonCount();
        double uncertainRatio  = total > 0 ? (double) ml.lowConfCount() / total : 0;

        String stateText;
        if (uncertainRatio > 0.5) {
            stateText = "Plus de la moiti&#233; des analyses de ce mois ont une <strong>confiance faible</strong> "
                + "(" + String.format("%.0f%%", uncertainRatio * 100) + " des sessions en mode &#171;&#160;incertain&#160;&#187;). "
                + "Le mod&#232;le n'a pas pu se prononcer clairement. "
                + "<strong>Ces r&#233;sultats ne doivent pas &#234;tre interpr&#233;t&#233;s seuls</strong> &#8212; "
                + "portez le bracelet plus longtemps pour obtenir des analyses fiables.";
        } else if (certainParkinson > ml.okCount() && certainParkinson > certainMonitoring) {
            stateText = "Les analyses IA de ce mois d&#233;tectent des signes fr&#233;quents de tremblements "
                + "pouvant &#234;tre associ&#233;s &#224; la maladie de Parkinson "
                + "(probabilit&#233; moyenne : " + String.format("%.1f%%", ml.avgProb() * 100) + "). "
                + (ml.lowConfCount() > 0 ? ml.lowConfCount() + " session(s) &#233;tant incertaines, ce chiffre doit &#234;tre nuanc&#233;. " : "")
                + "<strong>Consultez votre neurologue d&#232;s que possible</strong> pour discuter de ces r&#233;sultats.";
        } else if (certainMonitoring >= ml.okCount()) {
            stateText = "Les analyses IA indiquent des niveaux interm&#233;diaires n&#233;cessitant une surveillance "
                + "(probabilit&#233; moyenne : " + String.format("%.1f%%", ml.avgProb() * 100) + "). "
                + (ml.lowConfCount() > 0 ? "Dont " + ml.lowConfCount() + " session(s) avec confiance faible &#8212; r&#233;sultats incomplets. " : "")
                + "Il est recommand&#233; de <strong>mentionner ces r&#233;sultats &#224; votre m&#233;decin</strong> "
                + "lors de votre prochaine consultation.";
        } else {
            stateText = "Les analyses IA indiquent une activit&#233; motrice globalement normale ce mois "
                + "(probabilit&#233; Parkinson moyenne : " + String.format("%.1f%%", ml.avgProb() * 100) + "). "
                + (ml.lowConfCount() > 0 ? ml.lowConfCount() + " session(s) restent marqu&#233;es incertaines et sont exclues du verdict. " : "")
                + "Continuez &#224; porter votre bracelet WearPark r&#233;guli&#232;rement.";
        }

        String ampText = meanAmp < 1.0
            ? "L'intensit&#233; moyenne des tremblements est <strong>faible (" + String.format("%.2f", meanAmp) + " m/s&#178;)</strong>, "
            + "dans les limites normales de l'activit&#233; physique quotidienne."
            : meanAmp < 2.0
            ? "L'intensit&#233; moyenne des tremblements est <strong>mod&#233;r&#233;e (" + String.format("%.2f", meanAmp) + " m/s&#178;)</strong>. "
            + "Cela peut correspondre &#224; une activit&#233; normale ou &#224; des tremblements l&#233;gers &#224; mod&#233;r&#233;s."
            : "L'intensit&#233; moyenne des tremblements est <strong>&#233;lev&#233;e (" + String.format("%.2f", meanAmp) + " m/s&#178;)</strong>. "
            + "Des tremblements significatifs ont &#233;t&#233; d&#233;tect&#233;s. Partagez ce rapport avec votre m&#233;decin.";

        double avgVar = enriched.stream().filter(d -> d.variance() != null)
                .mapToDouble(EnrichedDay::variance).average().orElse(0);
        long unstableCount = enriched.stream()
                .filter(d -> d.deltaVariance() != null && d.deltaVariance() > 0.005).count();
        String varText = avgVar < 0.05
            ? "La <strong>stabilit&#233; des tremblements</strong> est bonne ce mois (variance moyenne : " + String.format("%.4f", avgVar) + "). "
            + "Les tremblements sont r&#233;guliers et pr&#233;dictibles."
            : avgVar < 0.15
            ? "La <strong>stabilit&#233; des tremblements</strong> est mod&#233;r&#233;e (variance moyenne : " + String.format("%.4f", avgVar) + "). "
            + "On observe des variations d'intensit&#233; au cours du mois"
            + (unstableCount > 0 ? ", notamment sur " + unstableCount + " jour(s) avec une instabilit&#233; accrue." : ".")
            : "La <strong>stabilit&#233; des tremblements est faible</strong> ce mois (variance &#233;lev&#233;e : " + String.format("%.4f", avgVar) + "). "
            + "Les tremblements varient fortement d'un moment &#224; l'autre &#8212; cela m&#233;rite une attention particuli&#232;re.";

        String covText = coverage >= 80
            ? "Excellente couverture (" + String.format("%.0f", coverage) + "%). Le bracelet a &#233;t&#233; port&#233; tr&#232;s r&#233;guli&#232;rement &#8212; les r&#233;sultats sont fiables."
            : coverage >= 50
            ? "Couverture satisfaisante (" + String.format("%.0f", coverage) + "%). Pour de meilleurs r&#233;sultats, portez le bracelet le plus souvent possible."
            : "Couverture limit&#233;e (" + String.format("%.0f", coverage) + "%). <strong>Portez le bracelet plus souvent</strong> pour que les analyses soient repr&#233;sentatives.";

        String trendText = switch (trend) {
            case "Amélioration" ->
                "La tendance ML sur le mois est <strong style='color:" + C_SUCCESS + "'>en am&#233;lioration</strong> : "
                + "la probabilit&#233; de Parkinson a diminu&#233; en cours de mois. Bonne nouvelle !";
            case "Dégradation" ->
                "La tendance ML sur le mois est <strong style='color:" + C_DANGER + "'>en d&#233;gradation</strong> : "
                + "la probabilit&#233; de Parkinson a augment&#233; en cours de mois. Consultez votre m&#233;decin.";
            default ->
                "La tendance ML sur le mois est <strong>stable</strong>, sans &#233;volution significative.";
        };

        return "<div class='section-title'>Interpr&#233;tation et recommandations</div>\n"
             + "<div class='interpretation'>"
             + "<p>" + stateText + "</p>"
             + "<p>" + ampText   + "</p>"
             + "<p>" + varText   + "</p>"
             + "<p>" + covText   + "</p>"
             + "<p>" + trendText + "</p>"
             + "</div>\n"
             + "<div class='page-break'></div>\n"
             + "<div class='section-title' style='font-size:11px;'>Guide des indicateurs</div>\n"
             + "<table class='data-table'><thead><tr>"
             + "<th>Indicateur</th><th>Signification</th><th>Valeur cible</th>"
             + "</tr></thead><tbody>"
             + "<tr><td>Couverture</td><td>% du mois o&#249; le bracelet &#233;tait port&#233;</td><td>&gt; 80%</td></tr>"
             + "<tr><td>Intensit&#233; moy. tremblements</td><td>Amplitude RMS des tremblements (m/s&#178;)</td><td>&lt; 1,0 m/s&#178;</td></tr>"
             + "<tr><td>Pic intensit&#233;</td><td>Valeur maximale des tremblements dans la journ&#233;e</td><td>Informationnel</td></tr>"
             + "<tr><td>Stabilit&#233; (variance)</td><td>R&#233;gularit&#233; des tremblements &#8212; plus bas = plus stables</td><td>La plus faible possible</td></tr>"
             + "<tr><td>Var. Intensit&#233; (J/J-1)</td><td>(+) = aggravation vs hier, (-) = am&#233;lioration</td><td>0 (stable)</td></tr>"
             + "<tr><td>Var. Stabilit&#233; (J/J-1)</td><td>(+) = instabilit&#233; croissante vs hier</td><td>0 (stable)</td></tr>"
             + "<tr><td>Probabilit&#233; ML</td><td>Probabilit&#233; IA de tremblements Parkinsoniens (valide si HIGH ou MEDIUM)</td><td>&lt; 35%</td></tr>"
             + "<tr><td>Confiance (HIGH/MEDIUM/LOW)</td><td>Certitude du mod&#232;le. LOW = inconclusif, class&#233; en surveillance par pr&#233;caution</td><td>HIGH ou MEDIUM</td></tr>"
             + "</tbody></table>\n"
             + "<div class='disclaimer'>"
             + "<p><strong>Avertissement m&#233;dical :</strong> Ce rapport est un outil de suivi personnel "
             + "g&#233;n&#233;r&#233; automatiquement par WearPark. Il ne constitue en aucun cas un diagnostic m&#233;dical. "
             + "Les r&#233;sultats doivent &#234;tre interpr&#233;t&#233;s par un professionnel de sant&#233; qualifi&#233;. "
             + "En cas de doute, consultez votre m&#233;decin ou neurologue.</p>"
             + "</div>\n"
             + "<div class='footer'>"
             + "<p>WearPark &#8212; Rapport g&#233;n&#233;r&#233; automatiquement | Donn&#233;es confidentielles</p>"
             + "</div>\n";
    }

    public static String buildBarChart(List<EnrichedDay> enriched) {
        final int W = 520, H = 160, padL = 35, padR = 10, padT = 10, padB = 28;
        final int chartW = W - padL - padR;
        final int chartH = H - padT - padB;

        double maxAmp = enriched.stream()
                .filter(d -> d.meanAmplitude() != null)
                .mapToDouble(EnrichedDay::meanAmplitude).max().orElse(1.0);
        if (maxAmp < 0.01) maxAmp = 1.0;

        int    n    = enriched.size();
        double bW   = (double) chartW / n * 0.65;
        double step = (double) chartW / n;

        StringBuilder svg = new StringBuilder();
        svg.append(String.format("<svg xmlns='http://www.w3.org/2000/svg' width='%d' height='%d'>", W, H));
        svg.append("<rect width='100%' height='100%' fill='").append(C_CARD).append("' rx='6'/>");

        for (double thr : new double[]{1.0, 2.0}) {
            if (thr <= maxAmp) {
                int yThr = padT + chartH - (int)(thr / maxAmp * chartH);
                svg.append(String.format(
                    "<line x1='%d' y1='%d' x2='%d' y2='%d' stroke='%s' stroke-width='0.7' stroke-dasharray='3,3'/>",
                    padL, yThr, W - padR, yThr, thr < 1.5 ? C_WARNING : C_DANGER));
            }
        }

        for (int i = 0; i <= 4; i++) {
            int yPos = padT + (int)(chartH * i / 4.0);
            svg.append(String.format(
                "<line x1='%d' y1='%d' x2='%d' y2='%d' stroke='%s' stroke-width='0.5'/>",
                padL, yPos, W - padR, yPos, C_BORDER));
        }

        svg.append(String.format("<text x='%d' y='%d' font-size='7' fill='%s' text-anchor='end'>%.1f</text>",
            padL - 2, padT + 5, C_MUTED, maxAmp));
        svg.append(String.format("<text x='%d' y='%d' font-size='7' fill='%s' text-anchor='end'>0</text>",
            padL - 2, padT + chartH, C_MUTED));

        for (int i = 0; i < n; i++) {
            EnrichedDay d = enriched.get(i);
            int x = padL + (int)(i * step + (step - bW) / 2);

            if (d.meanAmplitude() == null || d.meanAmplitude() < 0.001) {
                svg.append(String.format("<rect x='%d' y='%d' width='%d' height='2' rx='1' fill='%s'/>",
                    x, padT + chartH - 2, (int) bW, C_BORDER));
            } else {
                int barH = Math.max(2, (int)(d.meanAmplitude() / maxAmp * chartH));
                int y    = padT + chartH - barH;
                String fill = d.meanAmplitude() < 1.0 ? C_SUCCESS : d.meanAmplitude() < 2.0 ? C_WARNING : C_DANGER;
                svg.append(String.format("<rect x='%d' y='%d' width='%d' height='%d' rx='2' fill='%s'/>",
                    x, y, (int) bW, barH, fill));

                if (d.deltaMeanAmplitude() != null && Double.isFinite(d.deltaMeanAmplitude())
                        && Math.abs(d.deltaMeanAmplitude()) > 0.05) {
                    String marker    = d.deltaMeanAmplitude() > 0 ? "+" : "-";
                    String markerClr = d.deltaMeanAmplitude() > 0 ? C_DANGER : C_SUCCESS;
                    svg.append(String.format(
                        "<text x='%d' y='%d' font-size='7' font-weight='bold' fill='%s' text-anchor='middle'>%s</text>",
                        x + (int)(bW / 2), y - 2, markerClr, marker));
                }
            }

            if (i == 0 || (i + 1) % 5 == 0 || i == n - 1) {
                svg.append(String.format(
                    "<text x='%d' y='%d' font-size='7' fill='%s' text-anchor='middle'>%d</text>",
                    x + (int)(bW / 2), padT + chartH + 12, C_MUTED, i + 1));
            }
        }

        svg.append(String.format("<line x1='%d' y1='%d' x2='%d' y2='%d' stroke='%s' stroke-width='1'/>",
            padL, padT + chartH, W - padR, padT + chartH, C_BORDER));
        svg.append("</svg>");
        return svg.toString();
    }
}
