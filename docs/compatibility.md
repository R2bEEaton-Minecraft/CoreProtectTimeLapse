# Compatibility

CoreProtectTimeLapse currently supports:

- Minecraft `1.21` through `1.21.10`
- Java `21`
- CoreProtect `23.1+` (API v11+)

## Why this matters

CPTL uses the official public CoreProtect API instead of internal CoreProtect classes. This gives better compatibility across patch versions.

## Startup compatibility report

On startup, CPTL logs a compatibility report that includes:

- Detected Bukkit version
- Supported Minecraft range
- Detected CoreProtect version and API version
- Final status (`compatible` or `compatibility warning`)

If the status is a warning, verify your server and dependency versions first.
