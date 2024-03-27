package cc.spea.CoreProtectTimeLapse;

import net.coreprotect.database.Rollback;
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

        FakeCoreProtectAPI api = getCoreProtect();
        if (api != null) {
            // TODO:
            // Need to turn this into a command
            // Fix entities, tile entities
            // Look into decay, water spread, etc.
            // Change start time and end time to absolute times
            // Stop tick speed
            // Custom radius
            // Allow undo at the end

            ArrayList<Thread> threadArrayList = new ArrayList<>();

            for (int i = 86400; i < 86400 * 30; i += 86400) {
                threadArrayList.add(getThread(api, i, 86400));
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

    private Thread getThread(FakeCoreProtectAPI api, int time, int interval) {
        return new Thread(() -> {
            api.performRollback(time, interval, null, null, null, null, null, 512, Bukkit.getWorlds().get(0).getSpawnLocation());
            Bukkit.broadcastMessage("done!");
        });
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
