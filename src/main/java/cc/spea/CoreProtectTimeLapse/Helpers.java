package cc.spea.CoreProtectTimeLapse;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Helpers {
    public static void sendFancy(Player p, String s) {
        p.sendMessage("[" + ChatColor.RED + "CPTL" + ChatColor.RESET + "] " + s);
    }
}
