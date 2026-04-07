package cc.spea.CoreProtectTimeLapse;

import org.bukkit.Location;

public record StartOptions(
    int radius,
    long startTime,
    long endTime,
    int interval,
    Location center
) {
}
