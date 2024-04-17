package cc.spea.CoreProtectTimeLapse;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.jorel.commandapi.CommandAPILogger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
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

        CommandManager cm = new CommandManager(this);
        cm.registerAll();

        // TODO:
        // Fix entities, tile entities
        // Look into decay, water spread, etc.
        // Change start time and end time to absolute times
        // Stop tick speed
        // Custom radius
        // Allow undo at the end
        // Get list of affected regions for backup restore purposes

        // api.performRollback(i, 60, null, null, null, null, null, 512, Bukkit.getWorlds().get(0).getSpawnLocation());

        Bukkit.broadcastMessage("CoreProtectTimeLapse enabled.");
    }

    @Override
    public void onDisable() {
        CommandAPI.onDisable();
    }
}
