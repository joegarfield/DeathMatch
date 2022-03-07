package is.sly.garfield.deathmatch.listeners;

import is.sly.garfield.deathmatch.DeathMatch;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final DeathMatch deathMatch;

    public PlayerJoinListener(DeathMatch deathMatch) {
        this.deathMatch = deathMatch;
    }

    /**
     * Event handler that resets the player back to the lobby on login.
     *
     * @param e - the PlayerJoinEvent event
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        deathMatch.playerBackToSpawn(e.getPlayer());
    }
}
