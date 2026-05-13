package edu.wearpark.backend.service.report;

import java.time.format.DateTimeFormatter;
import java.util.Locale;


public final class ReportPdfConstants {

    private ReportPdfConstants() {}

    public static final String C_PRIMARY  = "#820D0B";
    public static final String C_BG       = "#FCF7F7";
    public static final String C_CARD     = "#F2E8E8";
    public static final String C_BORDER   = "#E8D1D1";
    public static final String C_TEXT     = "#1C0D0D";
    public static final String C_MUTED    = "#994D4D";
    public static final String C_SUCCESS  = "#2D7C41";
    public static final String C_WARNING  = "#E99623";
    public static final String C_DANGER   = "#A02721";
    public static final String C_ACCENT   = "#17A6CA";
    public static final String C_NODATA   = "#D1C8C8";

    public static final String[] MONTHS_FR = {
        "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
        "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
    };

    public static final String[] DAYS_FR_SHORT = { "Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim" };

    public static final DateTimeFormatter DAY_FMT =
            DateTimeFormatter.ofPattern("dd MMM", Locale.FRENCH);
}
