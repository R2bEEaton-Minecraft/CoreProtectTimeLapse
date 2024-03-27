package cc.spea.CoreProtectTimeLapse;

import dev.jorel.commandapi.*;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import net.coreprotect.database.Rollback;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class Main extends JavaPlugin {
    @Override
    public void onLoad() {
        // Set CommandAPI to use this plugin's logger
        CommandAPI.setLogger(CommandAPILogger.fromJavaLogger(getLogger()));

        // Load the CommandAPI
        CommandAPI.onLoad(
                // Configure the CommandAPI
                new CommandAPIBukkitConfig(this)
                        // Turn on verbose output for command registration logs
                        .verboseOutput(true)
                        // Give file where Brigadier's command registration tree should be dumped
                        .dispatcherFile(new File(getDataFolder(), "command_registration.json"))
                        .shouldHookPaperReload(true)
        );
    }

    @Override
    public void onEnable() {
        CommandAPI.onEnable();

        new CommandTree("cptl")
                .withAliases("coreprotecttimelapse")
                .then(new LiteralArgument("start")
                        .then(new IntegerArgument("radius", 100, 512)
                                .then(new IntegerArgument("startTime", 0)
                                        .then(new IntegerArgument("endTime", 0)
                                            .executesPlayer((player, args) -> {
                                                player.sendMessage("started " + args);
                                            })))))
                .then(new LiteralArgument("stop")
                    .executesPlayer((player, args) -> {
                        player.sendMessage("stopped " + args);
                    }))
                .register();

        Bukkit.broadcastMessage("CoreProtectTimeLapse enabled.");
    }

    @Override
    public void onDisable() {
        CommandAPI.onDisable();
    }
}
