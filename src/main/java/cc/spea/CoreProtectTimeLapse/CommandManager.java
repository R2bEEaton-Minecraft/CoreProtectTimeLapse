package cc.spea.CoreProtectTimeLapse;

import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.*;
import net.coreprotect.CoreProtect;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

import static org.bukkit.Bukkit.getServer;

public class CommandManager {
    JavaPlugin plugin;
    FakeCoreProtectAPI api;
    public CommandManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.api = getCoreProtect();
    }
    private Thread rollbackThread;
    List<long[]> rolledBack;

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
                                        player.sendMessage("started " + args);
                                        player.sendMessage(String.valueOf(System.currentTimeMillis() / 1000L));

                                        if (rollbackThread != null && rollbackThread.isAlive()) {
                                            player.sendMessage("Ummmm the thread is already running you bozo");
                                            return;
                                        }

                                        rollbackThread = new Thread(() -> {
                                            player.sendMessage("first");
                                            rolledBack.clear();

                                            long currentTime = System.currentTimeMillis() / 1000L;
                                            api.performRollback((long) args.get("endTime"), currentTime, null, null, null, null, null, (int) args.get("radius"), (Location) args.get("center"));
                                            rolledBack.add(new long[]{(long) args.get("endTime"), currentTime});
                                            for (long i = (long) args.get("endTime"); i >= (long) args.get("startTime"); i -= (int) args.get("interval")) {
                                                if (Thread.currentThread().isInterrupted()) {
                                                    player.sendMessage("Stopped job. Run `/cptl undo` to undo those changes.");
                                                    return;
                                                }
                                                player.sendMessage(String.valueOf(i));
                                                api.performRollback(i - (int) args.get("interval"), i, null, null, null, null, null, (int) args.get("radius"), (Location) args.get("center"));
                                                rolledBack.add(new long[]{i - (int) args.get("interval"), i});
                                            }
                                            player.sendMessage("Job finished! Run `/cptl undo` to undo those changes.");
                                        });

                                        rollbackThread.start();
                                    }
                )))))))
            .then(new LiteralArgument("stop")
                .executesPlayer((player, args) -> {
                    if (rollbackThread == null) {
                        player.sendMessage("not started bozo");
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
                    if (rolledBack.isEmpty()) {
                        player.sendMessage("Nothing to undo.");
                        return;
                    }

                    // TODO: Loop and undo it, also need to add a check in start for not starting if undoing
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
