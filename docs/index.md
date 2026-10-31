# Welcome to CPTL Docs

**Create unique time-lapses of your servers spanning months at a time!** CPTL is an add-on tool for CoreProtect that will automatically step through your CoreProtect logs to help create a time-lapse from historical data of any length.

Besides using its API, this tool is not affiliated with CoreProtect.

This tool is best used with [ReplayMod](https://modrinth.com/mod/replaymod/) or [Flashback](https://modrinth.com/mod/flashback/) and *requires* [CoreProtect](https://modrinth.com/plugin/coreprotect) to be installed through the entire desired time-lapse period.

# New in CPTL 1.3.1

Added support for Minecraft `1.21.11` and `26.1`-`26.2`, and updated the build against CoreProtect CE `24.1`. Verified end-to-end (plugin load, compatibility detection, and the rollback/restore engine) on a real Minecraft 26.2 (Paper build 135) server running Java 25 and CoreProtect CE 24.1.

# CPTL 1.3.0

`/cptl start` now opens an interactive chat picker. The picker walks you through radius, start time, end time, interval, center location, and final confirmation. You can click bracketed suggestions in chat, such as `[256]`, `[7d ago]`, `[now]`, `[1d]`, `[current]`, and `[yes]`, or type exact values like `2026-04-01 18:30` or `~10 ~ ~-5`.

# Requirements

- Minecraft `1.21` through `1.21.11`, and `26.1` through `26.2.X`
- Java `21` (Minecraft 26.x servers require Java `25`; the plugin is compiled for Java 21 bytecode, which loads on newer JVMs)
- CoreProtect `23.1+` (API v11+). Built against the API through CoreProtect CE `24.1` (adds Minecraft 26.2); newer API versions (v12, v13) are backward compatible.

# Example Time-Lapses

<iframe width="560" height="315" src="https://www.youtube.com/embed/F8lxA5nd_r0" title="" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" referrerpolicy="strict-origin-when-cross-origin" allowfullscreen></iframe>

<iframe width="560" height="315" src="https://www.youtube-nocookie.com/embed/no8Xn6iR8HI" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" allowfullscreen></iframe>

Created using CPTL and ReplayMod.

# Links
| URL                    | Description                                    |
|------------------------|------------------------------------------------|
| [Commands](commands.md) | List and description of commands.              |
| [Compatibility](compatibility.md) | Supported versions and startup checks. |
| [Usage](usage.md)       | Start-to-finish description and use of plugin. |
