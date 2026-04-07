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

CPTL `1.3.0` registers `/cptl` through Bukkit's built-in command handling. You do not need to install CommandAPI separately.

Before running any command, set `acknowledgesDestruction: true` in `plugins/CoreProtectTimeLapse/config.yml`, then restart or reload the server. Commands require `coreprotecttimelapse.use`, which defaults to operators.

## Starting A Timelapse

The recommended way to start a timelapse in CPTL `1.3.0` is the interactive chat picker:

```
/cptl start
```

This opens a guided chat workflow for radius, start time, end time, interval, center location, and final confirmation. Each step shows clickable suggestions in chat, such as `[100]`, `[7d ago]`, `[now]`, `[1d]`, `[current]`, and `[yes]`. You can click those bracketed chat options instead of typing them, or type an exact value when you need more control.

Example chat picker flow:

1. Run `/cptl start`.
2. Click `[256]` for radius, or type a radius from `100` to `512`.
3. Click `[7d ago]` for start time, or type a date like `2026-04-01 18:30`.
4. Click `[now]` for end time, or type another date/time.
5. Click `[1d]` for interval, or type `1h`, `6h`, `1d`, `1w`, or raw seconds.
6. Click `[current]` for the center location, or type coordinates like `~ ~ ~`, `100 64 -200`, or `~10 ~ ~-5`.
7. Review the summary and click `[yes]` to start, click `[back]` to change the previous answer, or click `[cancel]` to exit.

You can also type `back` at any picker step after the first, type `cancel` to exit, or run `/cptl start cancel` to cancel your active picker session.

The full command form is still available for scripted use:

```
/cptl start <radius> <startTime> <endTime> <interval> <x> <y> <z>
```

For the full command form, the radius must be between `100` and `512`, timestamps must be POSIX/Unix time, and the interval must be greater than `0`. The coordinate arguments mark the center of the rollback area and can use `~` relative coordinates.

Wizard time inputs use the server JVM timezone shown in the prompt. Accepted wizard time formats include Unix seconds, `now`, relative values like `7d ago` or `7d`, and local date/time values like `2026-04-01`, `2026-04-01 18:30`, or `2026-04-01T18:30:00`. Wizard intervals accept raw seconds or durations like `1h`, `6h`, `1d`, or `1w`.
