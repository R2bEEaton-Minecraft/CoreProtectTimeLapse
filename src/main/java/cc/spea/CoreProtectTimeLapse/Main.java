package cc.spea.CoreProtectTimeLapse;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.jorel.commandapi.CommandAPILogger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    FileConfiguration config = getConfig();

    @Override
    public void onLoad() {
        // Set CommandAPI to use this plugin's logger
        CommandAPI.setLogger(CommandAPILogger.fromJavaLogger(getLogger()));

        // Load the CommandAPI
        CommandAPI.onLoad(
            // Configure the CommandAPI
            new CommandAPIBukkitConfig(this)
                .shouldHookPaperReload(true)
        );
    }

    @Override
    public void onEnable() {
        CommandAPI.onEnable();

        saveDefaultConfig();

        CommandManager cm = new CommandManager(this, config);
        cm.registerAll();

        getServer().getPluginManager().registerEvents(new Listeners(), this);

        // TODO:
        // Look into decay, water spread, etc.
        // Get list of affected regions for backup restore purposes

        // BStats
        int pluginId = 21753; // <-- Replace with the id of your plugin!
        Metrics metrics = new Metrics(this, pluginId);

        Bukkit.broadcastMessage("CoreProtectTimeLapse enabled.");
    }

    @Override
    public void onDisable() {
        CommandAPI.onDisable();
    }
}
