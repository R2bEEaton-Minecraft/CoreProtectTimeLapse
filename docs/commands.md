# Commands

**WARNING:** Before you can run any commands, you must first acknowledge the destructive properties of this plugin by changing `plugins/CoreProtectTimeLapse/config.yml` to `acknowledgesDestruction: true`.

Only run this on a backup of the world (and backup your CoreProtect database as well). Replay and undo are destructive operations, and some game behavior cannot be perfectly restored.

## Setup
```
/cptl setup
```

**⚠️ WARNING! ⚠️:** This is a destructive process, please either make a backup or ONLY run this on a backup!

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
- time set 0
- weather clear

## Start
```
/cptl start <radius> <startTime> <endTime> <interval> <center>
```

**WARNING:** This is a destructive process. Use a backup.

This command will start the timelapse, so make sure you have ReplayMod or your recording software set up.

- Radius (in blocks) controls how much area around `center` is included in the timelapse.
- `startTime` and `endTime` should be entered in POSIX/Unix time. [https://www.epochconverter.com/](https://www.epochconverter.com/)
- Interval (in seconds) is the duration between each timelapse snapshot. Minimum value is `1`.
- Internally, CPTL converts your timestamps into CoreProtect's "seconds back from now" rollback format.
- Center is the XYZ coordinate of the center of this radius. You can enter `~ ~ ~` to use the current player's location.

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
