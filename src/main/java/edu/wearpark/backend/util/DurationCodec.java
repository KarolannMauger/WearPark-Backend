package edu.wearpark.backend.util;

import java.time.Duration;

/**
 * Utility to decode/encode {@link Duration}
 *
 * @see Duration
 */
public class DurationCodec {
    /**
     * Decode a string into a {@link Duration}
     * <p>White spaces and cases are ignored</p>
     * <p>Any period with a {@code month} or {@code year} may have an undetermined amount of seconds</p>
     * <br/>
     * <b>Accepted format</b>
     * <pre>{@code
     *      "T10m" // 10 minutes
     *      "t 5h 30s" // 5 hours and 50 seconds
     *      "PT5S" // 5 seconds
     *      "1d T20h" // 1 day and 20 hours
     *      "1y 3m 5d" // 1 year, 3 months and 5 day
     *      "P4DT12H30M" // 4 day 12 hours and 30 minutes
     * }</pre>
     *
     * @param encodedDuration the encoded duration
     * @return the decoded {@link Duration}
     */
    public static Duration decode(String encodedDuration) {
        if(encodedDuration == null || encodedDuration.isEmpty())
            return Duration.ZERO;
        ///
        var normal = encodedDuration
                .toLowerCase()
                .replaceAll("\\s", "");
        if(!normal.startsWith("p"))
            normal = "p" + normal;
        normal = normal.toUpperCase();
        return Duration.parse(normal);
    }
}