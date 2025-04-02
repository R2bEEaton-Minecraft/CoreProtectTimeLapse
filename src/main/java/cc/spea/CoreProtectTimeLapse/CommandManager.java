package cc.spea.CoreProtectTimeLapse;

import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.*;
import net.coreprotect.CoreProtect;
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
    FakeCoreProtectAPI api;
    public CommandManager(JavaPlugin plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.api = getCoreProtect();
        this.config = config;
    }
    private InterruptableThread rollbackThread;
    private Thread undoThread;
    ArrayList<long[]> rolledBack = new ArrayList<>();
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
                            .then(new IntegerArgument("interval", 0)
                                .then(new LocationArgument("center", LocationType.BLOCK_POSITION)
                                    .executesPlayer((player, args) -> {
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
                                            rolledBack.clear();

                                            long currentTime = System.currentTimeMillis() / 1000L;
                                            long startTime = (long) args.get("startTime");
                                            long endTime = (long) args.get("endTime");

                                            if (endTime < startTime) {
                                                long temp = startTime; startTime = endTime; endTime = temp;
                                            }

                                            api.performRollback(endTime, currentTime, null, null, null, null, null, (int) args.get("radius"), (Location) args.get("center"));
                                            rolledBack.add(new long[]{endTime, currentTime});

                                            if (rollbackThread.getInterrupt()) {
                                                sendFancy(player, "Stopped job. Run `/cptl undo` to undo those changes.");
                                                bossBar.removeAll();
                                                return;
                                            }

                                            sendFancy(player, "Now stepping through your interval...");

                                            for (long i = endTime; i >= startTime; i -= (int) args.get("interval")) {
                                                if (rollbackThread.getInterrupt()) {
                                                    sendFancy(player, "Stopped job. Run `/cptl undo` to undo those changes.");
                                                    bossBar.removeAll();
                                                    return;
                                                }
                                                bossBar.setProgress((double)(endTime - i) / (double)(endTime - startTime));
                                                api.performRollback(i - (int) args.get("interval"), i, null, null, null, null, null, (int) args.get("radius"), (Location) args.get("center"));
                                                rolledBack.add(new long[]{i - (int) args.get("interval"), i});
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
                    if (rolledBack.isEmpty()) {
                        sendFancy(player, "Nothing to undo.");
                        return;
                    }

                    undoThread = new Thread(() -> {
                        Collections.reverse(rolledBack);
                        int i = 1;

                        sendFancy(player, "Starting undo. Please do not reload or stop the server.");
                        BossBar bossBar = Bukkit.createBossBar("Undo", BarColor.RED, BarStyle.SEGMENTED_10);
                        bossBar.addPlayer(player);

                        bossBar.setProgress(0);
                        for (long[] times : rolledBack) {
                            api.performRestore(times[0], times[1], null, null, null, null, null, lastRadius, lastLocation);
                            bossBar.setProgress((double) i / rolledBack.size());
                            i++;
                        }
                        rolledBack.clear();
                        sendFancy(player, "Undo complete!");
                        bossBar.removeAll();
                    });

                    undoThread.start();

                    // TODO: Handle vine, decay, water flow, fire
                    // TODO: Add a way to do min and max time found in database
                }))
            .register();
    }

    private FakeCoreProtectAPI getCoreProtect() {
        Plugin plugin = getServer().getPluginManager().getPlugin("CoreProtect");

        // Check that CoreProtect is loaded
        if (!(plugin instanceof CoreProtect)) {
            return null;
        }

        // Check that the API is enabled
        FakeCoreProtectAPI CoreProtect = new FakeCoreProtectAPI();
        if (!CoreProtect.isEnabled()) {
            return null;
        }

        // Check that a compatible version of the API is loaded
        if (CoreProtect.APIVersion() < 9) {
            return null;
        }

        return CoreProtect;
    }
}
