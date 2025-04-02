package cc.spea.CoreProtectTimeLapse;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class Helpers {
    public static void sendFancy(CommandSender ce, String s) {
        ce.sendMessage ("[" + ChatColor.RED + "CPTL" + ChatColor.RESET + "] " + s);
    }
}
