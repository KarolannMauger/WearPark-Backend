package edu.wearpark.backend.util;

import org.junit.jupiter.api.Test;
import org.springframework.data.util.Pair;

import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DateUtilTest {

    @Test
    void getDayInterval_shouldReturnUtcDayBounds() {
        Instant instant = Instant.parse("2026-05-09T15:42:10Z");

        Pair<Instant, Instant> result = DateUtil.getDayInterval(instant);

        assertEquals(
                Instant.parse("2026-05-09T00:00:00Z"),
                result.getFirst()
        );

        assertEquals(
                Instant.parse("2026-05-09T23:59:59Z"),
                result.getSecond()
        );
    }

    @Test
    void getDayInterval_shouldRespectCustomZone() {
        Instant instant = Instant.parse("2026-05-09T03:00:00Z");

        ZoneId zone = ZoneId.of("America/Montreal");

        Pair<Instant, Instant> result = DateUtil.getDayInterval(instant, zone);

        assertEquals(
                Instant.parse("2026-05-08T04:00:00Z"),
                result.getFirst()
        );

        assertEquals(
                Instant.parse("2026-05-09T03:59:59Z"),
                result.getSecond()
        );
    }

    @Test
    void getMonthInterval_shouldReturnUtcMonthBounds() {
        Instant instant = Instant.parse("2026-02-15T12:00:00Z");

        Pair<Instant, Instant> result = DateUtil.getMonthInterval(instant);

        assertEquals(
                Instant.parse("2026-02-01T00:00:00Z"),
                result.getFirst()
        );

        assertEquals(
                Instant.parse("2026-02-28T23:59:59Z"),
                result.getSecond()
        );
    }

    @Test
    void getMonthInterval_shouldRespectCustomZone() {
        Instant instant = Instant.parse("2026-03-15T12:00:00Z");

        ZoneId zone = ZoneId.of("America/Montreal");

        Pair<Instant, Instant> result = DateUtil.getMonthInterval(instant, zone);

        assertEquals(
                Instant.parse("2026-03-01T05:00:00Z"),
                result.getFirst()
        );

        assertEquals(
                Instant.parse("2026-04-01T03:59:59Z"),
                result.getSecond()
        );
    }

    @Test
    void getMonthInterval_shouldHandleLeapYear() {
        Instant instant = Instant.parse("2024-02-15T12:00:00Z");

        Pair<Instant, Instant> result = DateUtil.getMonthInterval(instant);

        assertEquals(
                Instant.parse("2024-02-01T00:00:00Z"),
                result.getFirst()
        );

        assertEquals(
                Instant.parse("2024-02-29T23:59:59Z"),
                result.getSecond()
        );
    }
}