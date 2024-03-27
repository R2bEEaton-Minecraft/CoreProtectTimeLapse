package cc.spea.CoreProtectTimeLapse;

import net.coreprotect.CoreProtect;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;

import static org.bukkit.Bukkit.getServer;

public class CommandManager implements TabExecutor {
    Main cptl;

    public CommandManager(Main cptl) {
        this.cptl = cptl;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Bukkit.broadcastMessage(Arrays.toString(args));

        // /cptl start <startTime> <endTime> <interval>
        // /cptl stop

//        FakeCoreProtectAPI api = getCoreProtect();
//        if (api != null) {
//            // TODO:
//            // Need to turn this into a command
//            // Fix entities, tile entities
//            // Look into decay, water spread, etc.
//            // Change start time and end time to absolute times
//            // Stop tick speed
//            // Custom radius
//            // Allow undo at the end
//            // Get list of affected regions for backup restore purposes
//
//
//            new Thread(() -> {
//                for (int i = 0; i < 86400l i += 60) {
//                    api.performRollback(i, 60, null, null, null, null, null, 512, Bukkit.getWorlds().get(0).getSpawnLocation());
//                    Bukkit.broadcastMessage("done!");
//                }
//            }).start();
//        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return List.of(new String[]{"start", "stop"});
        } else if (args.length == 2 || args[0].equalsIgnoreCase("stop")) {
            if (args[0].equalsIgnoreCase("start")) {
                return List.of(new String[]{"startTime"});
            } else {
                return List.of(new String[]{""});
            }
        } else if (args.length == 3) {
            return List.of(new String[]{"endTime"});
        } else if (args.length == 4) {
            return List.of(new String[]{"interval"});
        }

        return List.of(new String[]{""});
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