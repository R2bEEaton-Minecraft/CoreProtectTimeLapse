package cc.spea.SpigotTemplate;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    @Override
    public void onEnable() {
        Bukkit.broadcastMessage("SpigotTemplate enabled.");
    }

    @Override
    public void onDisable() {

    }
}
