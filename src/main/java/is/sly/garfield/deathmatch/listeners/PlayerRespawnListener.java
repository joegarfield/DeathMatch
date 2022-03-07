package is.sly.garfield.deathmatch.listeners;

import is.sly.garfield.deathmatch.DeathMatch;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerRespawnListener implements Listener {

    private final DeathMatch deathMatch;

    public PlayerRespawnListener(DeathMatch deathMatch) {
        this.deathMatch = deathMatch;
    }

    /**
     * Event handler that listens to player respawns and resets the player back to the lobby.
     *
     * @param e - the PlayerRespawnEvent event
     */
    @EventHandler
    public void onPlayerRepsawn(PlayerRespawnEvent e) {
        deathMatch.playerBackToSpawn(e.getPlayer());
    }
}
