package cc.spea.CoreProtectTimeLapse;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.StructureGrowEvent;

public class Listeners implements Listener {
    public Listeners() { }

    @EventHandler
    public void structureGrowEvent(StructureGrowEvent event) {
        if (!event.isFromBonemeal()) event.setCancelled(true);
    }
}
