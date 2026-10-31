package cc.spea.CoreProtectTimeLapse;

import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    FileConfiguration config = getConfig();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        logCompatibilityReport();

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

    private void logCompatibilityReport() {
        String bukkitVersion = Bukkit.getBukkitVersion();
        boolean inSupportedRange = VersionSupport.isSupportedMinecraft(bukkitVersion);

        Plugin coreProtectPlugin = getServer().getPluginManager().getPlugin("CoreProtect");
        String coreProtectVersion = coreProtectPlugin == null ? "not installed" : coreProtectPlugin.getDescription().getVersion();
        int coreProtectApi = -1;
        boolean coreProtectReady = false;

        if (coreProtectPlugin instanceof CoreProtect coreProtect) {
            CoreProtectAPI coreProtectAPI = coreProtect.getAPI();
            if (coreProtectAPI != null && coreProtectAPI.isEnabled()) {
                coreProtectApi = coreProtectAPI.APIVersion();
                coreProtectReady = VersionSupport.isSupportedCoreProtectApi(coreProtectApi);
            }
        }

        getLogger().info("Compatibility report:");
        getLogger().info(" - Detected Bukkit version: " + bukkitVersion);
        getLogger().info(" - Supported Minecraft range: " + VersionSupport.SUPPORTED_MINECRAFT_RANGE);
        getLogger().info(" - Detected CoreProtect version: " + coreProtectVersion + (coreProtectApi > -1 ? " (API v" + coreProtectApi + ")" : ""));

        if (inSupportedRange && coreProtectReady) {
            getLogger().info(" - Status: compatible.");
            return;
        }

        String mcStatus = inSupportedRange ? "ok" : "outside supported range";
        String cpStatus = coreProtectReady ? "ok" : "requires CoreProtect API v11+ (CoreProtect 23.1+)";
        getLogger().warning(" - Status: compatibility warning (Minecraft: " + mcStatus + ", CoreProtect: " + cpStatus + ").");
    }
}
