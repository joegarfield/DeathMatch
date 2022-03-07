package is.sly.garfield.deathmatch.listeners;

import is.sly.garfield.deathmatch.DeathMatch;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class ItemDropListener implements Listener {

    private final DeathMatch deathMatch;

    public ItemDropListener(DeathMatch deathMatch) {
        this.deathMatch = deathMatch;
    }

    /**
     * Event handler that listens for block breaking events, and clearing the items that drop from it.
     *
     * @param e - the BlockBreakEvent event
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        e.setDropItems(false);
    }

    /**
     * Event handler that listens for players dropping items (through inventory GUIs or using the Q button) to cancel
     * it.
     *
     * @param e - the PlayerDropItemEvent event
     */
    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        e.setCancelled(true);
    }
}
