package cc.spea.CoreProtectTimeLapse;

import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.*;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;

import static cc.spea.CoreProtectTimeLapse.Helpers.sendFancy;
import static org.bukkit.Bukkit.getServer;

public class CommandManager {
    JavaPlugin plugin;
    FileConfiguration config;
    CoreProtectAPI api;
    public CommandManager(JavaPlugin plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.api = getCoreProtect();
        this.config = config;
    }
    private InterruptableThread rollbackThread;
    private Thread undoThread;
    ArrayList<Integer> rollbackDepths = new ArrayList<>();
    int lastRadius = -1;
    Location lastLocation = null;

    public void registerAll() {
        if (!config.getBoolean("acknowledgesDestruction")) {
            new CommandTree("cptl")
                .withAliases("coreprotecttimelapse")
                .executes((executor, types) -> {
                    sendFancy(executor, "Please update the config file in your plugins/CoreProtectTimeLapse folder to acknowledge the " +
                            "destructive ability of this plugin. ONLY run this plugin on a backup! Restart or reload " +
                            "when the config has been updated.");
                });
            return;
        }

        new CommandTree("cptl")
            .withAliases("coreprotecttimelapse")
            .then(new LiteralArgument("setup")
                .executesPlayer((player, args) -> {
                    Bukkit.dispatchCommand(player, "gamerule randomTickSpeed 1000");
                    Bukkit.dispatchCommand(player, "gamerule doTileDrops false");
                    Bukkit.dispatchCommand(player, "gamerule doFireTick false");
                    Bukkit.dispatchCommand(player, "gamerule doEntityDrops false");
                    Bukkit.dispatchCommand(player, "gamerule doMobSpawning false");
                    Bukkit.dispatchCommand(player, "gamerule doPatrolSpawning false");
                    Bukkit.dispatchCommand(player, "gamerule doTraderSpawning false");
                    Bukkit.dispatchCommand(player, "gamerule doWeatherCycle false");
                    Bukkit.dispatchCommand(player, "gamerule doMobLoot false");
                    Bukkit.dispatchCommand(player, "gamerule tntExplodes false");
                    Bukkit.dispatchCommand(player, "kill @e[type=!player]");
                    Bukkit.dispatchCommand(player, "kill @e[type=!player]");
                    Bukkit.dispatchCommand(player, "kill @e[type=!player]");
                    Bukkit.dispatchCommand(player, "kill @e[type=!player]");
                    Bukkit.dispatchCommand(player, "say Natural tree growth is always disabled with CPTL.");
                    Bukkit.dispatchCommand(player, "time set 0");
                    Bukkit.dispatchCommand(player, "weather clear");
                    sendFancy(player, "Setup complete.");
                }))
            .then(new LiteralArgument("start")
                .then(new IntegerArgument("radius", 100, 512)
                    .then(new LongArgument("startTime", 0)
                        .then(new LongArgument("endTime", 0)
                            .then(new IntegerArgument("interval", 1)
                                .then(new LocationArgument("center", LocationType.BLOCK_POSITION)
                                    .executesPlayer((player, args) -> {
                                        if (api == null) {
                                            sendFancy(player, "CoreProtect API is unavailable or too old (requires API v11+).");
                                            return;
                                        }
                                        if (undoThread != null && undoThread.isAlive()) {
                                            sendFancy(player, "Undo in progress. You must wait for this to finish.");
                                            return;
                                        }
                                        if (rollbackThread != null && rollbackThread.isAlive()) {
                                            sendFancy(player, "Timelapse in progress. To stop it, use the `/cptl stop` command.");
                                            return;
                                        }

                                        lastRadius = (int) args.get("radius");
                                        lastLocation = (Location) args.get("center");

                                        rollbackThread = new InterruptableThread(() -> {
                                            BossBar bossBar = Bukkit.createBossBar("Timelapse", BarColor.GREEN, BarStyle.SEGMENTED_10);
                                            bossBar.addPlayer(player);
                                            bossBar.setProgress(0);

                                            sendFancy(player, "Running initial rollback...");
                                            rollbackDepths.clear();

                                            long currentTime = System.currentTimeMillis() / 1000L;
                                            long startTime = (long) args.get("startTime");
                                            long endTime = (long) args.get("endTime");

                                            if (endTime < startTime) {
                                                long temp = startTime; startTime = endTime; endTime = temp;
                                            }

                                            int initialRollbackDepth = toRollbackDepth(endTime, currentTime);
                                            api.performRollback(initialRollbackDepth, null, null, null, null, null, (int) args.get("radius"), (Location) args.get("center"));
                                            rollbackDepths.add(initialRollbackDepth);

                                            if (rollbackThread.getInterrupt()) {
                                                sendFancy(player, "Stopped job. Run `/cptl undo` to undo those changes.");
                                                bossBar.removeAll();
                                                return;
                                            }

                                            sendFancy(player, "Now stepping through your interval...");

                                            int interval = (int) args.get("interval");
                                            long cursor = endTime;
                                            while (cursor > startTime) {
                                                if (rollbackThread.getInterrupt()) {
                                                    sendFancy(player, "Stopped job. Run `/cptl undo` to undo those changes.");
                                                    bossBar.removeAll();
                                                    return;
                                                }

                                                long nextCursor = Math.max(startTime, cursor - interval);
                                                int rollbackDepth = toRollbackDepth(nextCursor, currentTime);

                                                api.performRollback(rollbackDepth, null, null, null, null, null, (int) args.get("radius"), (Location) args.get("center"));
                                                rollbackDepths.add(rollbackDepth);

                                                if (endTime == startTime) {
                                                    bossBar.setProgress(1);
                                                } else {
                                                    bossBar.setProgress((double) (endTime - nextCursor) / (double) (endTime - startTime));
                                                }

                                                cursor = nextCursor;
                                            }
                                            sendFancy(player, "Job finished! Run `/cptl undo` to undo those changes.");
                                            bossBar.removeAll();
                                        });

                                        rollbackThread.start();
                                    }
                )))))))
            .then(new LiteralArgument("stop")
                .executesPlayer((player, args) -> {
                    if (rollbackThread == null || !rollbackThread.isAlive()) {
                        sendFancy(player, "There is no running timelapse.");
                        return;
                    }
                    sendFancy(player, "Stopping as soon as possible, please wait...");
                    rollbackThread.setInterrupt(true);
                }))
            .then(new LiteralArgument("undo")
                .executesPlayer((player, args) -> {
                    if (rollbackThread != null && rollbackThread.isAlive()) {
                        sendFancy(player, "Timelapse in progress.");
                        return;
                    }
                    if (undoThread != null && undoThread.isAlive()) {
                        sendFancy(player, "Undo in progress. You must wait for this to finish.");
                        return;
                    }
                    if (rollbackDepths.isEmpty()) {
                        sendFancy(player, "Nothing to undo.");
                        return;
                    }

                    undoThread = new Thread(() -> {
                        sendFancy(player, "Starting undo. Please do not reload or stop the server.");
                        BossBar bossBar = Bukkit.createBossBar("Undo", BarColor.RED, BarStyle.SEGMENTED_10);
                        bossBar.addPlayer(player);

                        bossBar.setProgress(0);
                        int maxRollbackDepth = Collections.max(rollbackDepths);
                        api.performRestore(maxRollbackDepth, null, null, null, null, null, lastRadius, lastLocation);
                        bossBar.setProgress(1);
                        rollbackDepths.clear();
                        sendFancy(player, "Undo complete!");
                        bossBar.removeAll();
                    });

                    undoThread.start();

                    // TODO: Handle vine, decay, water flow, fire
                    // TODO: Add a way to do min and max time found in database
                }))
            .register();
    }

    private CoreProtectAPI getCoreProtect() {
        Plugin plugin = getServer().getPluginManager().getPlugin("CoreProtect");

        // Check that CoreProtect is loaded
        if (!(plugin instanceof CoreProtect coreProtectPlugin)) {
            return null;
        }

        // Check that the API is enabled
        CoreProtectAPI coreProtectAPI = coreProtectPlugin.getAPI();
        if (coreProtectAPI == null || !coreProtectAPI.isEnabled()) {
            return null;
        }

        // CoreProtect API v11+ (CoreProtect 23.1+) is required for modern 1.21.x compatibility
        if (coreProtectAPI.APIVersion() < 11) {
            return null;
        }

        return coreProtectAPI;
    }

    private int toRollbackDepth(long targetTimestamp, long nowTimestamp) {
        long rollbackDepth = Math.max(0, nowTimestamp - targetTimestamp);
        if (rollbackDepth > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) rollbackDepth;
    }
}
