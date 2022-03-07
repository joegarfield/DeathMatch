package is.sly.garfield.deathmatch.listeners;

import is.sly.garfield.deathmatch.DeathMatch;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class PlayerTNTPlaceEvent implements Listener {

    private final DeathMatch deathMatch;

    public PlayerTNTPlaceEvent(DeathMatch deathMatch) {
        this.deathMatch = deathMatch;
    }

    /**
     * Event handler that replaces a TNT block with a primed TNT entity.
     *
     * @param e - the event to listen to, BlockPlaceEvent
     */
    @EventHandler
    public void onTNTPlace(BlockPlaceEvent e) {
        if (!e.getPlayer().getLocation().equals(deathMatch.getLobbyLocation())) {
            if (e.getBlock().getType() == Material.TNT) {
                e.getBlock().setType(Material.AIR);
                Location location = e.getBlock().getLocation();
                location.setX(location.getX() + 0.5);
                location.setY(location.getY() + 0.5);
                location.setZ(location.getZ() + 0.5);
                e.getBlock().getWorld().spawn(location, TNTPrimed.class);
            }
        }
    }
}
