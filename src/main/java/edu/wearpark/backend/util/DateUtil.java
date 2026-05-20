package edu.wearpark.backend.util;

import org.springframework.data.util.Pair;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

public class DateUtil {
    private DateUtil(){}
    public static final ZoneId DEFAULT_ZONE = ZoneId.of("Z");

    public static Pair<Instant, Instant> getDayInterval(Instant instant) {
        return getDayInterval(instant, DEFAULT_ZONE);
    }
    public static Pair<Instant, Instant> getDayInterval(Instant instant, ZoneId zone) {
        var zonedDate = instant.atZone(zone).toLocalDate();
        var start = zonedDate
                .atStartOfDay(zone)
                .toInstant();
        var end = zonedDate
                .plusDays(1)
                .atStartOfDay(zone)
                .minus(Duration.ofSeconds(1))
                .toInstant();
        return Pair.of(start, end);
    }
    public static Pair<Instant, Instant> getMonthInterval(Instant instant) {
        return getMonthInterval(instant, DEFAULT_ZONE);
    }
    public static Pair<Instant, Instant> getMonthInterval(Instant instant, ZoneId zone) {
        var zonedDate = instant.atZone(zone).toLocalDate();
        var start = zonedDate
                .withDayOfMonth(1)
                .atStartOfDay(zone)
                .toInstant();
        var end = zonedDate
                .with(TemporalAdjusters.lastDayOfMonth())
                .atStartOfDay(zone)
                .plusDays(1)
                .minusSeconds(1)
                .toInstant();
        return Pair.of(start, end);
    }
}
