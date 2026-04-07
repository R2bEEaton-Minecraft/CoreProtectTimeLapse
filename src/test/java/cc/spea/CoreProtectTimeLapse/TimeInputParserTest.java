package cc.spea.CoreProtectTimeLapse;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TimeInputParserTest {
    private static final ZoneId ZONE = ZoneId.of("America/New_York");
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-04-07T16:00:00Z"), ZONE);

    @Test
    void parsesEpochSeconds() {
        assertEquals(1710000000L, TimeInputParser.parsePastOrNow("1710000000", CLOCK, ZONE));
    }

    @Test
    void parsesNow() {
        assertEquals(Instant.now(CLOCK).getEpochSecond(), TimeInputParser.parsePastOrNow("now", CLOCK, ZONE));
    }

    @Test
    void parsesRelativeTimes() {
        long now = Instant.now(CLOCK).getEpochSecond();

        assertEquals(now - 7L * 24L * 60L * 60L, TimeInputParser.parsePastOrNow("7d", CLOCK, ZONE));
        assertEquals(now - 7L * 24L * 60L * 60L, TimeInputParser.parsePastOrNow("7d ago", CLOCK, ZONE));
        assertEquals(now - 6L * 60L * 60L, TimeInputParser.parsePastOrNow("6h ago", CLOCK, ZONE));
    }

    @Test
    void parsesAbsoluteLocalDateTimes() {
        assertEquals(
            LocalDateTime.of(2026, 4, 1, 18, 30).atZone(ZONE).toEpochSecond(),
            TimeInputParser.parsePastOrNow("2026-04-01 18:30", CLOCK, ZONE)
        );
        assertEquals(
            LocalDateTime.of(2026, 4, 1, 18, 30, 15).atZone(ZONE).toEpochSecond(),
            TimeInputParser.parsePastOrNow("2026-04-01T18:30:15", CLOCK, ZONE)
        );
        assertEquals(
            LocalDate.of(2026, 4, 1).atStartOfDay(ZONE).toEpochSecond(),
            TimeInputParser.parsePastOrNow("2026-04-01", CLOCK, ZONE)
        );
    }

    @Test
    void rejectsInvalidAndFutureValues() {
        assertThrows(IllegalArgumentException.class, () -> TimeInputParser.parsePastOrNow("not a date", CLOCK, ZONE));
        assertThrows(IllegalArgumentException.class, () -> TimeInputParser.parsePastOrNow("2026-04-08 18:30", CLOCK, ZONE));
        assertThrows(IllegalArgumentException.class, () -> TimeInputParser.parsePastOrNow(Long.MAX_VALUE + "s", CLOCK, ZONE));
    }
}
