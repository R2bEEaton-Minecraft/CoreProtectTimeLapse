package cc.spea.CoreProtectTimeLapse;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    FileConfiguration config = getConfig();

    @Override
    public void onEnable() {
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
}
