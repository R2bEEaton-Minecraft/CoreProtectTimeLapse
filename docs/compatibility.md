# Compatibility

CoreProtectTimeLapse currently supports:

- Minecraft `1.21` through `1.21.X`
- Java `21`
- CoreProtect `23.1+` (API v11+)

## Why this matters

CPTL uses the official public CoreProtect API instead of internal CoreProtect classes. This gives better compatibility across patch versions.

As of CPTL `1.2.0`, commands are handled with Bukkit's built-in command API and declared in `plugin.yml`. This avoids loading a shaded CommandAPI dependency on newer Paper/Spigot versions.

## Startup compatibility report

On startup, CPTL logs a compatibility report that includes:

- Detected Bukkit version
- Supported Minecraft range
- Detected CoreProtect version and API version
- Final status (`compatible` or `compatibility warning`)

If the status is a warning, verify your server and dependency versions first.
