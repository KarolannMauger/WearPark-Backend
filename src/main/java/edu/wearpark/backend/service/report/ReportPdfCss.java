package edu.wearpark.backend.service.report;

import static edu.wearpark.backend.service.report.ReportPdfConstants.*;


public final class ReportPdfCss {

    private ReportPdfCss() {}

    public static String build() {
        return
            "@page { size: A4; margin: 18mm 15mm 15mm 15mm; }\n"
          + "* { margin: 0; padding: 0; box-sizing: border-box; }\n"
          + "body { font-family: Arial, Helvetica, sans-serif; font-size: 10px;"
          +       " color: " + C_TEXT + "; background: #fff; }\n"
          + ".page-break { page-break-before: always; }\n"

          + ".header { background: " + C_PRIMARY + "; color: white; padding: 14px 20px;"
          +          " border-radius: 8px; margin-bottom: 18px; }\n"
          + ".header h1 { font-size: 18px; font-weight: bold; margin-bottom: 3px; }\n"
          + ".header p  { font-size: 10px; color: #F2E8E8; }\n"
          + ".header .sub { font-size: 12px; color: #F2E8E8; margin-top: 4px; }\n"

          + ".section-title { font-size: 13px; font-weight: bold; color: " + C_PRIMARY + ";"
          +                  " border-bottom: 2px solid " + C_PRIMARY + "; padding-bottom: 4px;"
          +                  " margin: 18px 0 10px; }\n"

          + ".info-grid { width: 100%; border-collapse: collapse; margin-bottom: 14px; }\n"
          + ".info-grid td { padding: 5px 10px; font-size: 10px; }\n"
          + ".info-grid .lbl { color: " + C_MUTED + "; width: 140px; }\n"
          + ".info-grid .val { font-weight: bold; }\n"
          + ".info-row { background: " + C_CARD + "; border-radius: 6px; }\n"

          + ".cards { width: 100%; border-collapse: collapse; margin-bottom: 10px; }\n"
          + ".cards td { padding: 6px; width: 25%; vertical-align: top; }\n"
          + ".card { background: " + C_CARD + "; border-radius: 8px; padding: 10px 12px;"
          +        " border-left: 3px solid " + C_PRIMARY + "; }\n"
          + ".card .num  { font-size: 20px; font-weight: bold; color: " + C_PRIMARY + "; }\n"
          + ".card .unit { font-size: 9px; color: " + C_MUTED + "; }\n"
          + ".card .lbl  { font-size: 9px; color: " + C_MUTED + "; margin-top: 2px; }\n"
          + ".card.ok         { border-left-color: " + C_SUCCESS + "; }\n"
          + ".card.ok .num    { color: " + C_SUCCESS + "; }\n"
          + ".card.warn       { border-left-color: " + C_WARNING + "; }\n"
          + ".card.warn .num  { color: " + C_WARNING + "; }\n"
          + ".card.danger     { border-left-color: " + C_DANGER + "; }\n"
          + ".card.danger .num { color: " + C_DANGER + "; }\n"
          + ".card.accent     { border-left-color: " + C_ACCENT + "; }\n"
          + ".card.accent .num { color: " + C_ACCENT + "; }\n"

          + ".data-table { width: 100%; border-collapse: collapse; margin: 10px 0; font-size: 9px; }\n"
          + ".data-table th { background: " + C_PRIMARY + "; color: white; padding: 5px 6px;"
          +                 " text-align: left; font-weight: bold; }\n"
          + ".data-table td { padding: 4px 6px; border-bottom: 1px solid " + C_BORDER + "; }\n"
          + ".data-table tr:nth-child(even) td { background: " + C_BG + "; }\n"

          + ".badge { display: inline-block; padding: 2px 7px; border-radius: 10px;"
          +         " font-size: 8px; font-weight: bold; }\n"
          + ".badge.ok         { background: #D1FAE5; color: #065F46; }\n"
          + ".badge.monitoring { background: #FEF3C7; color: #92400E; }\n"
          + ".badge.parkinson  { background: #FEE2E2; color: #991B1B; }\n"
          + ".badge.uncertain  { background: #E5E7EB; color: #374151; }\n"
          + ".badge.faible     { background: #D1FAE5; color: #065F46; }\n"
          + ".badge.modere     { background: #FEF3C7; color: #92400E; }\n"
          + ".badge.eleve      { background: #FEE2E2; color: #991B1B; }\n"

          + ".chart-box { background: " + C_CARD + "; border-radius: 8px; padding: 10px; margin: 10px 0; }\n"
          + ".chart-box p { font-size: 8px; color: " + C_MUTED + "; margin-top: 5px; }\n"
          + ".cal-table { width: 100%; border-collapse: collapse; margin: 10px 0; }\n"
          + ".cal-th { background: " + C_PRIMARY + "; color: white; text-align: center;"
          +          " font-size: 9px; padding: 4px 0; font-weight: bold; }\n"
          + ".cal-cell { text-align: center; padding: 3px 1px; border: 1px solid " + C_BORDER + "; vertical-align: top; }\n"
          + ".cal-num  { font-size: 9px; font-weight: bold; color: " + C_TEXT + "; }\n"
          + ".cal-amp  { font-size: 8px; color: " + C_MUTED + "; margin-top: 1px; }\n"
          + ".cal-empty { background: #fff; }\n"

          + ".interpretation { background: " + C_CARD + "; border-radius: 8px; padding: 12px 14px; margin: 10px 0; }\n"
          + ".interpretation p { font-size: 10px; line-height: 1.6; margin-bottom: 8px; }\n"
          + ".disclaimer { border: 1px solid " + C_BORDER + "; border-radius: 6px; padding: 8px 12px;"
          +              " background: #FFFBEB; margin-top: 12px; }\n"
          + ".disclaimer p { font-size: 8.5px; color: #78716C; line-height: 1.5; }\n"
          + ".footer { margin-top: 20px; border-top: 1px solid " + C_BORDER + "; padding-top: 8px; }\n"
          + ".footer p { font-size: 8px; color: " + C_MUTED + "; text-align: center; }\n"

          + ".trend-stable  { color: " + C_MUTED   + "; font-weight: bold; }\n"
          + ".trend-improve { color: " + C_SUCCESS  + "; font-weight: bold; }\n"
          + ".trend-worsen  { color: " + C_DANGER   + "; font-weight: bold; }\n"
          + ".dist-bar { width: 100%; border-collapse: collapse; margin: 6px 0; }\n"
          + ".dist-seg { height: 14px; }\n"
          + ".up   { color: " + C_DANGER  + "; font-weight: bold; }\n"
          + ".down { color: " + C_SUCCESS + "; }\n"
          + ".flat { color: " + C_MUTED   + "; }\n";
    }
}
