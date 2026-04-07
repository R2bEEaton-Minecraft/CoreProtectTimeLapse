# Full Usage Guide

<iframe width="560" height="315" src="https://www.youtube.com/embed/87pdEmr7hWw" title="Core Protect Time-Lapse Tutorial" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" referrerpolicy="strict-origin-when-cross-origin" allowfullscreen></iframe>

## Startup Compatibility Check

When the plugin enables, it prints a compatibility report to console:

- Detected Bukkit version
- Supported Minecraft range (`1.21` to `1.21.X`)
- Detected CoreProtect version and API version
- Final compatibility status

If you get a compatibility warning:

- Ensure your server is in the supported Minecraft range
- Ensure CoreProtect is installed and updated to `23.1+` (API v11+)
- Restart and check the report again

## Command Setup

CPTL `1.2.0` registers `/cptl` through Bukkit's built-in command handling. You do not need to install CommandAPI separately.

Before running any command, set `acknowledgesDestruction: true` in `plugins/CoreProtectTimeLapse/config.yml`, then restart or reload the server. Commands require `coreprotecttimelapse.use`, which defaults to operators.

## Starting A Timelapse

Use:

```
/cptl start <radius> <startTime> <endTime> <interval> <x> <y> <z>
```

The radius must be between `100` and `512`, timestamps must be POSIX/Unix time, and the interval must be greater than `0`. The coordinate arguments mark the center of the rollback area and can use `~` relative coordinates.
