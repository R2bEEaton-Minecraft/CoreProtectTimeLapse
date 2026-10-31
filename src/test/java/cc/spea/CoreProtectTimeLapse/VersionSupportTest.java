package cc.spea.CoreProtectTimeLapse;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionSupportTest {
    @Test
    void supportsLegacyStyle121Releases() {
        assertTrue(VersionSupport.isSupportedMinecraft("1.21-R0.1-SNAPSHOT"));
        assertTrue(VersionSupport.isSupportedMinecraft("1.21.4-R0.1-SNAPSHOT"));
        assertTrue(VersionSupport.isSupportedMinecraft("1.21.10-R0.1-SNAPSHOT"));
        assertTrue(VersionSupport.isSupportedMinecraft("1.21.11-R0.1-SNAPSHOT"));
    }

    @Test
    void supportsYearStyle26Releases() {
        assertTrue(VersionSupport.isSupportedMinecraft("26.1-R0.1-SNAPSHOT"));
        assertTrue(VersionSupport.isSupportedMinecraft("26.1.2-R0.1-SNAPSHOT"));
        assertTrue(VersionSupport.isSupportedMinecraft("26.2-R0.1-SNAPSHOT"));
        assertTrue(VersionSupport.isSupportedMinecraft("26.2.1-R0.1-SNAPSHOT"));
    }

    @Test
    void rejectsOutOfRangeVersions() {
        assertFalse(VersionSupport.isSupportedMinecraft("1.20.6-R0.1-SNAPSHOT"));
        assertFalse(VersionSupport.isSupportedMinecraft("1.22-R0.1-SNAPSHOT"));
        assertFalse(VersionSupport.isSupportedMinecraft("26.3-R0.1-SNAPSHOT"));
        assertFalse(VersionSupport.isSupportedMinecraft("27.1-R0.1-SNAPSHOT"));
        assertFalse(VersionSupport.isSupportedMinecraft("garbage"));
        assertFalse(VersionSupport.isSupportedMinecraft(null));
    }

    @Test
    void acceptsCoreProtectApi11AndNewer() {
        assertFalse(VersionSupport.isSupportedCoreProtectApi(10));
        assertTrue(VersionSupport.isSupportedCoreProtectApi(11));
        assertTrue(VersionSupport.isSupportedCoreProtectApi(12));
        assertTrue(VersionSupport.isSupportedCoreProtectApi(13));
    }
}
