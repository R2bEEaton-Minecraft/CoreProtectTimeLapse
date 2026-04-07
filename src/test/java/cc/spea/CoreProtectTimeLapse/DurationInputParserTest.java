package cc.spea.CoreProtectTimeLapse;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DurationInputParserTest {
    @Test
    void parsesRawSeconds() {
        assertEquals(3600, DurationInputParser.parseSeconds("3600"));
    }

    @Test
    void parsesDurationUnits() {
        assertEquals(1, DurationInputParser.parseSeconds("1s"));
        assertEquals(60, DurationInputParser.parseSeconds("1m"));
        assertEquals(3600, DurationInputParser.parseSeconds("1h"));
        assertEquals(21600, DurationInputParser.parseSeconds("6h"));
        assertEquals(86400, DurationInputParser.parseSeconds("1d"));
        assertEquals(604800, DurationInputParser.parseSeconds("1w"));
    }

    @Test
    void rejectsInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> DurationInputParser.parseSeconds("0"));
        assertThrows(IllegalArgumentException.class, () -> DurationInputParser.parseSeconds("-1"));
        assertThrows(IllegalArgumentException.class, () -> DurationInputParser.parseSeconds("abc"));
        assertThrows(IllegalArgumentException.class, () -> DurationInputParser.parseSeconds("999999999999999999999999d"));
        assertThrows(IllegalArgumentException.class, () -> DurationInputParser.parseSeconds(Long.toString((long) Integer.MAX_VALUE + 1L)));
    }
}
