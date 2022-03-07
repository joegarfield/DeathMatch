package is.sly.garfield.deathmatch.listeners;

import is.sly.garfield.deathmatch.DeathMatch;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerVoidTeleportListener implements Listener {

    private final DeathMatch deathMatch;

    public PlayerVoidTeleportListener(DeathMatch deathMatch) {
        this.deathMatch = deathMatch;
    }

    /**
     * Event handler that teleports a player to the world spawn if they enter the void, if the player is in the lobbyY.
     *
     * @param e the PlayerMoveEvent event
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (p.getLocation().getWorld().getName().equals(deathMatch.getLobbyLocation().getWorld().getName())) {
            if (p.getLocation().getY() < 0) {
                p.teleport(deathMatch.getLobbyLocation());
            }
        }
    }
}
