package cc.spea.CoreProtectTimeLapse;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DurationInputParser {
    private static final Pattern DURATION_PATTERN = Pattern.compile("^(\\d+)\\s*([smhdw])$");

    private DurationInputParser() {
    }

    public static int parseSeconds(String rawInput) {
        String input = rawInput == null ? "" : rawInput.trim().toLowerCase(Locale.ROOT);
        if (input.isEmpty()) {
            throw new IllegalArgumentException("Interval is required.");
        }

        long seconds;
        if (input.matches("^\\d+$")) {
            seconds = parsePositiveLong(input);
        } else {
            Matcher matcher = DURATION_PATTERN.matcher(input);
            if (!matcher.matches()) {
                throw new IllegalArgumentException("Interval must be seconds or a duration like 1h, 6h, 1d, or 1w.");
            }

            long value = parsePositiveLong(matcher.group(1));
            long multiplier = switch (matcher.group(2)) {
                case "s" -> 1L;
                case "m" -> 60L;
                case "h" -> 60L * 60L;
                case "d" -> 24L * 60L * 60L;
                case "w" -> 7L * 24L * 60L * 60L;
                default -> throw new IllegalArgumentException("Unsupported interval unit.");
            };

            try {
                seconds = Math.multiplyExact(value, multiplier);
            } catch (ArithmeticException ex) {
                throw new IllegalArgumentException("Interval is too large.");
            }
        }

        if (seconds <= 0) {
            throw new IllegalArgumentException("Interval must be greater than 0.");
        }
        if (seconds > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Interval is too large.");
        }
        return (int) seconds;
    }

    private static long parsePositiveLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Interval is too large.");
        }
    }
}
