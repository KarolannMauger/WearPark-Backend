package edu.wearpark.backend.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class DurationCodecTest {
    @Nested
    class decode {
        @Test
        void shouldDecode_whenNoP() {
            assertEquals(DurationCodec.decode("t1h"), Duration.ofHours(1));
        }
        @Test
        void shouldDecode_withSpaces() {
            assertEquals(DurationCodec.decode("1d t 1h"), Duration.ofDays(1).plus(Duration.ofHours(1)));
        }
    }
}