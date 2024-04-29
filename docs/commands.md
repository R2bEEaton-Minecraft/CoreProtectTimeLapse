# Commands

## Setup
```
/cptl setup
```

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

**⚠️ WARNING! ⚠️:** This is a destructive process, please either make a backup or ONLY run this on a backup!

This command will start the timelapse, so make sure you have ReplayMod or your recording software set up.

- Radius (in blocks) will control how much area around `center` that is included in the timelapse.
- The `startTime` and `endTime` should be entered in POSIX/Unix time. [https://www.epochconverter.com/](https://www.epochconverter.com/)
- Interval (in seconds) is the duration of time between each timelapse snapshot. So, for example, if you enter 3600 (one hour), that means that the plugin will step through your block history, showing the world as it existed at hour 0, hour 1, hour 2, etc. between `startTime` and `endTime`.
- Center is the XYZ coordinate of the center of this radius. You can enter `~ ~ ~` if you want it to be the location of the current player.

When this is run, the server will roll back to your desired `endTime` and then continue moving backward every `interval` seconds until the `startTime`.

## Stop
```
/cptl stop
```

This stops (or pauses) the current time-lapse, just in case. You cannot continue a time-lapse after stopping it, you can only undo and restart.

## Undo
```
/cptl undo
```

Once the time-lapse is over or stopped, you can undo the changes and bring the server back to its current state. This will not be perfect, there may be issues with entities, leaves, plants, etc. This is why you should only run this process on a backup that you don't care about being potentially destroyed.
