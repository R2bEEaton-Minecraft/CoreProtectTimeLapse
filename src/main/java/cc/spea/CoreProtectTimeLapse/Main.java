package cc.spea.CoreProtectTimeLapse;

import net.coreprotect.database.Rollback;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;

import java.util.ArrayList;
import java.util.Objects;

public class Main extends JavaPlugin {
    @Override
    public void onEnable() {
        Bukkit.broadcastMessage("CoreProtectTimeLapse enabled.");

        Objects.requireNonNull(getCommand("cptl")).setExecutor(new CommandManager(this));
        Objects.requireNonNull(getCommand("cptl")).setTabCompleter(new CommandManager(this));
    }

    @Override
    public void onDisable() {

    }
}
