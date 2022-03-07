package is.sly.garfield.deathmatch.listeners;

import is.sly.garfield.deathmatch.DeathMatch;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

    private final DeathMatch deathMatch;

    public PlayerDeathListener(DeathMatch deathMatch) {
        this.deathMatch = deathMatch;
    }

    /**
     * Event handler that listens to player deaths to increase the killer (if they exist) kill statistic,
     * prevent the dead player's items from dropping, and force an instant respawn.
     *
     * @param e - PlayerDeathEvent event
     */
    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        if (e.getEntity().getKiller() != null) {
            int currentKills = deathMatch.getCurrentMatch().getKillCounter().get(e.getEntity().getKiller().getUniqueId()) == null ? 0 : deathMatch.getCurrentMatch().getKillCounter().get(e.getEntity().getKiller().getUniqueId());
            deathMatch.getCurrentMatch().getKillCounter().put(e.getEntity().getKiller().getUniqueId(), currentKills + 1);

            e.getEntity().getKiller().sendMessage(DeathMatch.PREFIX + "You have killed " + e.getEntity().getName() + " for a total of " + (currentKills + 1) + " kills.");
        }
        e.getDrops().clear();
        e.getEntity().spigot().respawn();
    }
}
