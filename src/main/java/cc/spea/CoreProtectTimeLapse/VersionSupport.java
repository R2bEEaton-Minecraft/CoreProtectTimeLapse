package cc.spea.CoreProtectTimeLapse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Minecraft / CoreProtect version compatibility rules.
 * <p>
 * Minecraft switched from {@code 1.x.y} to year-based {@code YY.N[.patch]} versioning with 26.1,
 * so both schemes are handled here.
 */
final class VersionSupport {
    static final String SUPPORTED_MINECRAFT_RANGE = "1.21 - 1.21.11, 26.1 - 26.2.x";
    static final int MIN_COREPROTECT_API = 11;

    private static final Pattern VERSION = Pattern.compile("(\\d+)\\.(\\d+)(?:\\.(\\d+))?");

    private VersionSupport() {
    }

    static boolean isSupportedMinecraft(String bukkitVersion) {
        if (bukkitVersion == null) {
            return false;
        }
        Matcher matcher = VERSION.matcher(bukkitVersion);
        if (!matcher.find()) {
            return false;
        }

        int major = Integer.parseInt(matcher.group(1));
        int minor = Integer.parseInt(matcher.group(2));
        int patch = matcher.group(3) == null ? 0 : Integer.parseInt(matcher.group(3));

        if (major == 1) {
            return minor == 21 && patch <= 11;
        }
        return major == 26 && (minor == 1 || minor == 2);
    }

    static boolean isSupportedCoreProtectApi(int apiVersion) {
        return apiVersion >= MIN_COREPROTECT_API;
    }
}
