package cc.spea.CoreProtectTimeLapse;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPILogger;
import dev.jorel.commandapi.CommandAPISpigotConfig;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main extends JavaPlugin {
    FileConfiguration config = getConfig();

    @Override
    public void onLoad() {
        // Set CommandAPI to use this plugin's logger
        CommandAPI.setLogger(CommandAPILogger.fromJavaLogger(getLogger()));

        // Load the CommandAPI
        CommandAPI.onLoad(
            // Configure the CommandAPI
            new CommandAPISpigotConfig(this)
        );
    }

    @Override
    public void onEnable() {
        CommandAPI.onEnable();

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

    @Override
    public void onDisable() {
        CommandAPI.onDisable();
    }

    private void logCompatibilityReport() {
        String bukkitVersion = Bukkit.getBukkitVersion();
        boolean inSupportedRange = isInSupportedRange(bukkitVersion);

        Plugin coreProtectPlugin = getServer().getPluginManager().getPlugin("CoreProtect");
        String coreProtectVersion = coreProtectPlugin == null ? "not installed" : coreProtectPlugin.getDescription().getVersion();
        int coreProtectApi = -1;
        boolean coreProtectReady = false;

        if (coreProtectPlugin instanceof CoreProtect coreProtect) {
            CoreProtectAPI coreProtectAPI = coreProtect.getAPI();
            if (coreProtectAPI != null && coreProtectAPI.isEnabled()) {
                coreProtectApi = coreProtectAPI.APIVersion();
                coreProtectReady = coreProtectApi >= 11;
            }
        }

        getLogger().info("Compatibility report:");
        getLogger().info(" - Detected Bukkit version: " + bukkitVersion);
        getLogger().info(" - Supported Minecraft range: 1.21 - 1.21.10");
        getLogger().info(" - Detected CoreProtect version: " + coreProtectVersion + (coreProtectApi > -1 ? " (API v" + coreProtectApi + ")" : ""));

        if (inSupportedRange && coreProtectReady) {
            getLogger().info(" - Status: compatible.");
            return;
        }

        String mcStatus = inSupportedRange ? "ok" : "outside supported range";
        String cpStatus = coreProtectReady ? "ok" : "requires CoreProtect API v11+ (CoreProtect 23.1+)";
        getLogger().warning(" - Status: compatibility warning (Minecraft: " + mcStatus + ", CoreProtect: " + cpStatus + ").");
    }

    private boolean isInSupportedRange(String bukkitVersion) {
        Matcher matcher = Pattern.compile("(\\d+)\\.(\\d+)(?:\\.(\\d+))?").matcher(bukkitVersion);
        if (!matcher.find()) {
            return false;
        }

        int major = Integer.parseInt(matcher.group(1));
        int minor = Integer.parseInt(matcher.group(2));
        int patch = matcher.group(3) == null ? 0 : Integer.parseInt(matcher.group(3));
        return major == 1 && minor == 21 && patch >= 0 && patch <= 10;
    }
}
