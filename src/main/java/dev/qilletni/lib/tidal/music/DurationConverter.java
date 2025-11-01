package dev.qilletni.lib.tidal.music;

import java.time.Duration;
import java.time.format.DateTimeParseException;

/**
 * Utility class for converting duration strings to numeric values.
 */
public class DurationConverter {

    /**
     * Converts an ISO 8601 duration string to seconds.
     * <p>
     * Examples:
     * <ul>
     *   <li>"PT2M58S" -> 178 seconds (2 minutes, 58 seconds)</li>
     *   <li>"PT1H30M" -> 5400 seconds (1 hour, 30 minutes)</li>
     *   <li>"PT45S" -> 45 seconds</li>
     *   <li>"PT0S" -> 0 seconds</li>
     * </ul>
     *
     * @param iso8601Duration the ISO 8601 duration string (e.g., "PT2M58S")
     * @return the duration in seconds, or -1 if the input is null or invalid
     */
    public static int parseDurationToSeconds(String iso8601Duration) {
        if (iso8601Duration == null || iso8601Duration.isEmpty()) {
            return -1;
        }

        try {
            Duration duration = Duration.parse(iso8601Duration);
            return (int) duration.getSeconds();
        } catch (DateTimeParseException e) {
            return -1;
        }
    }
}