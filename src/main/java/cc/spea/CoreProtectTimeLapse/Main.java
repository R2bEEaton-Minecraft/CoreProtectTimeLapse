package cc.spea.CoreProtectTimeLapse;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;

import java.util.ArrayList;

public class Main extends JavaPlugin {
    @Override
    public void onEnable() {
        Bukkit.broadcastMessage("CoreProtectTimeLapse enabled.");

        CoreProtectAPI api = getCoreProtect();
        if (api != null){ // Ensure we have access to the API
            api.testAPI(); // Will print out "[CoreProtect] API test successful." in the console.

            // TODO:
            // Need to turn this into a command
            // Need to potentially modify CoreProtectAPI to allow startTime - endTime
            // Look into decay, water spread, etc.

            ArrayList<Thread> threadArrayList = new ArrayList<>();

            for (int i = 86400; i < 86400 * 30; i += 86400) {
                threadArrayList.add(getThread(api, i));
            }

            new Thread(() -> {
                for (Thread thread : threadArrayList) {
                    thread.start();
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }
    }

    @Override
    public void onDisable() {

    }

    private Thread getThread(CoreProtectAPI api, int time) {
        return new Thread(() -> {
            api.performRollback(time, null, null, null, null, null, 512, Bukkit.getWorlds().get(0).getSpawnLocation());
            Bukkit.broadcastMessage("done!");
        });
    }

    private CoreProtectAPI getCoreProtect() {
        Plugin plugin = getServer().getPluginManager().getPlugin("CoreProtect");

        // Check that CoreProtect is loaded
        if (!(plugin instanceof CoreProtect)) {
            return null;
        }

        // Check that the API is enabled
        CoreProtectAPI CoreProtect = ((CoreProtect) plugin).getAPI();
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
