package cc.spea.CoreProtectTimeLapse;

import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import net.coreprotect.CoreProtect;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import static org.bukkit.Bukkit.getServer;

public class CommandManager {
    JavaPlugin plugin;
    public CommandManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerAll() {
        new CommandTree("cptl")
                .withPermission(CommandPermission.OP)
                .withAliases("coreprotecttimelapse")
                .then(new LiteralArgument("start")
                        .then(new IntegerArgument("radius", 100, 512)
                                .then(new IntegerArgument("startTime", 0)
                                        .then(new IntegerArgument("endTime", 0)
                                                .executesPlayer((player, args) -> {
                                                    player.sendMessage("started " + args);
                                                    player.sendMessage(String.valueOf(System.currentTimeMillis() / 1000L));
                                                })))))
                .then(new LiteralArgument("stop")
                        .executesPlayer((player, args) -> {
                            player.sendMessage("stopped " + args);
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
