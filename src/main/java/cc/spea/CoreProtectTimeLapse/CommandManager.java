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
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;

import static org.bukkit.Bukkit.getServer;

public class CommandManager {
    JavaPlugin plugin;
    FakeCoreProtectAPI api;
    public CommandManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.api = getCoreProtect();
    }
    private Thread rollbackThread;
    private Thread undoThread;
    ArrayList<long[]> rolledBack = new ArrayList<>();
    int lastRadius = -1;
    Location lastLocation = null;

    public void registerAll() {
        new CommandTree("cptl")
            .withPermission(CommandPermission.OP)
            .withAliases("coreprotecttimelapse")
            .then(new LiteralArgument("start")
                .then(new IntegerArgument("radius", 100, 512)
                    .then(new LongArgument("startTime", 0)
                        .then(new LongArgument("endTime", 0)
                            .then(new IntegerArgument("interval", 0)
                                .then(new LocationArgument("center", LocationType.BLOCK_POSITION)
                                    .executesPlayer((player, args) -> {
                                        if (undoThread != null && undoThread.isAlive()) {
                                            player.sendMessage("Undo in progress. You must wait for this to finish.");
                                            return;
                                        }
                                        if (rollbackThread != null && rollbackThread.isAlive()) {
                                            player.sendMessage("Ummmm the thread is already running you bozo");
                                            return;
                                        }

                                        lastRadius = (int) args.get("radius");
                                        lastLocation = (Location) args.get("center");

                                        rollbackThread = new Thread(() -> {
                                            BossBar bossBar = Bukkit.createBossBar("Timelapse", BarColor.GREEN, BarStyle.SEGMENTED_10);
                                            bossBar.addPlayer(player);
                                            bossBar.setProgress(0);

                                            player.sendMessage("Running initial rollback.");
                                            rolledBack.clear();

                                            long currentTime = System.currentTimeMillis() / 1000L;
                                            long startTime = (long) args.get("startTime");
                                            long endTime = (long) args.get("endTime");

                                            if (endTime < startTime) {
                                                long temp = startTime; startTime = endTime; endTime = temp;
                                            }

                                            api.performRollback(endTime, currentTime, null, null, null, null, null, (int) args.get("radius"), (Location) args.get("center"));
                                            rolledBack.add(new long[]{endTime, currentTime});

                                            player.sendMessage("Now stepping through your interval.");

                                            for (long i = endTime; i >= startTime; i -= (int) args.get("interval")) {
                                                if (Thread.currentThread().isInterrupted()) {
                                                    player.sendMessage("Stopped job. Run `/cptl undo` to undo those changes.");
                                                    bossBar.removeAll();
                                                    return;
                                                }
                                                bossBar.setProgress((double)(endTime - i) / (double)(endTime - startTime));
                                                api.performRollback(i - (int) args.get("interval"), i, null, null, null, null, null, (int) args.get("radius"), (Location) args.get("center"));
                                                rolledBack.add(new long[]{i - (int) args.get("interval"), i});
                                            }
                                            player.sendMessage("Job finished! Run `/cptl undo` to undo those changes.");
                                            bossBar.removeAll();
                                        });

                                        rollbackThread.start();
                                    }
                )))))))
            .then(new LiteralArgument("stop")
                .executesPlayer((player, args) -> {
                    if (rollbackThread == null || !rollbackThread.isAlive()) {
                        player.sendMessage("There is no running timelapse.");
                        return;
                    }
                    rollbackThread.interrupt();
                }))
            .then(new LiteralArgument("undo")
                .executesPlayer((player, args) -> {
                    if (rollbackThread != null && rollbackThread.isAlive()) {
                        player.sendMessage("Timelapse in progress.");
                        return;
                    }
                    if (undoThread != null && undoThread.isAlive()) {
                        player.sendMessage("Undo in progress. You must wait for this to finish.");
                        return;
                    }
                    if (rolledBack.isEmpty()) {
                        player.sendMessage("Nothing to undo.");
                        return;
                    }

                    undoThread = new Thread(() -> {
                        Collections.reverse(rolledBack);
                        int i = 1;

                        BossBar bossBar = Bukkit.createBossBar("Timelapse", BarColor.GREEN, BarStyle.SEGMENTED_10);
                        bossBar.addPlayer(player);

                        bossBar.setProgress(0);
                        for (long[] times : rolledBack) {
                            api.performRestore(times[0], times[1], null, null, null, null, null, lastRadius, lastLocation);
                            bossBar.setProgress((double) i / rolledBack.size());
                            i++;
                        }
                        rolledBack.clear();
                        player.sendMessage("Undo complete!");
                        bossBar.removeAll();
                    });

                    undoThread.start();

                    // TODO: Handle vine, decay, water flow, fire
                    // TODO: Make more helpful messages
                    // TODO: /cptl stop does not work
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
