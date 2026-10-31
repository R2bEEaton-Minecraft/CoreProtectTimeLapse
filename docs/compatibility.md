# Compatibility

CoreProtectTimeLapse currently supports:

- Minecraft `1.21` through `1.21.11`, and `26.1` through `26.2.X`
- Java `21` (Minecraft 26.x servers require Java `25`; the plugin is compiled for Java 21 bytecode, which loads on newer JVMs)
- CoreProtect `23.1+` (API v11+). Built against the API through CoreProtect CE `24.1` (adds Minecraft 26.2); newer API versions (v12, v13) are backward compatible.

Verified on a real Minecraft 26.2 (Paper build 135) server running Java 25 and CoreProtect CE 24.1 (API v12): CPTL loaded cleanly, reported `compatible`, and its rollback/restore calls round-tripped correctly against real CoreProtect-logged history.

## Why this matters

CPTL uses the official public CoreProtect API instead of internal CoreProtect classes. This gives better compatibility across patch versions.

CPTL `1.3.0`+ handles commands with Bukkit's built-in command API and declares them in `plugin.yml`. This avoids loading a shaded CommandAPI dependency on newer Paper/Spigot versions.

## Startup compatibility report

On startup, CPTL logs a compatibility report that includes:

- Detected Bukkit version
- Supported Minecraft range
- Detected CoreProtect version and API version
- Final status (`compatible` or `compatibility warning`)

If the status is a warning, verify your server and dependency versions first.
