# Commands

**WARNING:** Before you can run any commands, you must first acknowledge the destructive properties of this plugin by changing `plugins/CoreProtectTimeLapse/config.yml` to `acknowledgesDestruction: true`.

Only run this on a backup of the world (and backup your CoreProtect database as well). Replay and undo are destructive operations, and some game behavior cannot be perfectly restored.

All commands require `coreprotecttimelapse.use`, which defaults to server operators. The `/cptl` command is also available as `/coreprotecttimelapse`.

## Setup
```
/cptl setup
```

**WARNING:** This is a destructive process, please either make a backup or ONLY run this on a backup!

This is not a necessary command, but performs a series of helpful other commands that can make the time-lapse look better.
Currently, it runs:

- gamerule randomTickSpeed 1000
- gamerule doTileDrops false
- gamerule doFireTick false
- gamerule doEntityDrops false
- gamerule doMobSpawning false
- gamerule doPatrolSpawning false
- gamerule doTraderSpawning false
- gamerule doWeatherCycle false
- gamerule doMobLoot false
- gamerule tntExplodes false
- kill @e[type=!player]
- kill @e[type=!player]
- kill @e[type=!player]
- kill @e[type=!player]
- say Natural tree growth is always disabled with CPTL.
- time set 0
- weather clear

## Start
```
/cptl start
/cptl start <radius> <startTime> <endTime> <interval> <x> <y> <z>
/cptl start cancel
```

**WARNING:** This is a destructive process. Use a backup.

This command will start the timelapse, so make sure you have ReplayMod, Flashback, or your recording software set up.

Running `/cptl start` without arguments opens the CPTL `1.3.0` interactive chat picker. The picker walks through radius, start time, end time, interval, center location, and a final confirmation.

The bracketed suggestions shown in chat are clickable. For example, you can click `[256]` for radius, `[7d ago]` for start time, `[now]` for end time, `[1d]` for interval, `[current]` for center location, and `[yes]` on the confirmation step. You can also type exact values if the suggested buttons do not match what you need. Type `back` to change the previous answer or `cancel` to exit. `/cptl start cancel` also cancels your active picker session.

Example picker flow:

1. Run `/cptl start`.
2. Click `[256]`, click `[7d ago]`, click `[now]`, click `[1d]`, click `[current]`, then review the summary and click `[yes]`.
3. If you need exact values, type values like `2026-04-01 18:30`, `~10 ~ ~-5`, or `3600` instead of clicking a suggestion.

The full command form is still available for scripted use or for experienced users:

- Radius (in blocks) controls how much area around the center coordinate is included in the timelapse. It must be between `100` and `512`.
- `startTime` and `endTime` in the full command form should be entered in POSIX/Unix time. [https://www.epochconverter.com/](https://www.epochconverter.com/)
- The wizard also accepts `now`, relative times like `30s ago`, `15m ago`, `6h ago`, `7d ago`, `2w ago`, compact relative times like `7d`, and local server date/time values like `2026-04-01`, `2026-04-01 18:30`, `2026-04-01 18:30:00`, `2026-04-01T18:30`, or `2026-04-01T18:30:00`.
- Wizard date/time values are interpreted in the server JVM timezone shown in the prompt. Date-only values resolve to local start of day. Future wizard times are rejected.
- Interval is the duration between each timelapse snapshot. In the full command form it is raw seconds and must be greater than `0`. In the wizard it can be raw seconds or duration forms like `1h`, `6h`, `1d`, or `1w`.
- Internally, CPTL converts your timestamps into CoreProtect's "seconds back from now" rollback format.
- `x`, `y`, and `z` are the block coordinates for the center of the radius. You can enter `~ ~ ~` to use the current player's location, or offsets such as `~10 ~ ~-5`.
- If `startTime` and `endTime` are entered in reverse order, CPTL will normalize them before running.

When this is run, the server rolls back to your desired `endTime` and then continues moving backward every `interval` seconds until `startTime`.

If CoreProtect is missing, disabled, or older than API v11 (CoreProtect 23.1+), the command will not start.

## Stop
```
/cptl stop
```

This stops (or pauses) the current timelapse. You cannot continue a timelapse after stopping it; you can only undo and restart.

## Undo
```
/cptl undo
```

Once the timelapse is over or stopped, you can undo the changes and bring the server back to its current state. This will not be perfect; there may be issues with entities, leaves, plants, and similar behavior. Only run this process on a backup.
