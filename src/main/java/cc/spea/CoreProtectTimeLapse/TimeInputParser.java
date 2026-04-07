package cc.spea.CoreProtectTimeLapse;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TimeInputParser {
    private static final Pattern RELATIVE_PATTERN = Pattern.compile("^(\\d+)\\s*([smhdw])(?:\\s+ago)?$");
    private static final List<DateTimeFormatter> DATE_TIME_FORMATTERS = List.of(
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
    );

    private TimeInputParser() {
    }

    public static long parsePastOrNow(String rawInput, Clock clock, ZoneId zoneId) {
        String input = rawInput == null ? "" : rawInput.trim();
        if (input.isEmpty()) {
            throw new IllegalArgumentException("Time is required.");
        }

        long epochSecond = parseEpochSecond(input, clock, zoneId);
        long now = Instant.now(clock).getEpochSecond();
        if (epochSecond > now) {
            throw new IllegalArgumentException("Time cannot be in the future.");
        }
        return epochSecond;
    }

    private static long parseEpochSecond(String input, Clock clock, ZoneId zoneId) {
        String lowered = input.toLowerCase(Locale.ROOT);
        if ("now".equals(lowered)) {
            return Instant.now(clock).getEpochSecond();
        }

        if (input.matches("^\\d+$")) {
            try {
                return Long.parseLong(input);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Timestamp is too large.");
            }
        }

        Matcher relativeMatcher = RELATIVE_PATTERN.matcher(lowered);
        if (relativeMatcher.matches()) {
            long value = parsePositiveLong(relativeMatcher.group(1));
            long multiplier = switch (relativeMatcher.group(2)) {
                case "s" -> 1L;
                case "m" -> 60L;
                case "h" -> 60L * 60L;
                case "d" -> 24L * 60L * 60L;
                case "w" -> 7L * 24L * 60L * 60L;
                default -> throw new IllegalArgumentException("Unsupported time unit.");
            };

            long secondsBack;
            try {
                secondsBack = Math.multiplyExact(value, multiplier);
            } catch (ArithmeticException ex) {
                throw new IllegalArgumentException("Relative time is too large.");
            }
            try {
                return Instant.now(clock).minusSeconds(secondsBack).getEpochSecond();
            } catch (DateTimeException | ArithmeticException ex) {
                throw new IllegalArgumentException("Relative time is too large.");
            }
        }

        try {
            LocalDate date = LocalDate.parse(input, DateTimeFormatter.ISO_LOCAL_DATE);
            return date.atStartOfDay(zoneId).toEpochSecond();
        } catch (DateTimeParseException ignored) {
        }

        for (DateTimeFormatter formatter : DATE_TIME_FORMATTERS) {
            try {
                LocalDateTime dateTime = LocalDateTime.parse(input, formatter);
                return dateTime.atZone(zoneId).toEpochSecond();
            } catch (DateTimeException ignored) {
            }
        }

        throw new IllegalArgumentException("Time must be Unix time, now, a relative time like 7d ago, or a date like 2026-04-01 18:30.");
    }

    private static long parsePositiveLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Relative time is too large.");
        }
    }
}
