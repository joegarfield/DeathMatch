package is.sly.garfield.deathmatch.listeners;

import is.sly.garfield.deathmatch.DeathMatch;
import is.sly.garfield.deathmatch.enums.GameState;
import is.sly.garfield.deathmatch.objects.Arena;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

public class PlayerEnterRegionListener implements Listener {

    private final DeathMatch deathMatch;

    public PlayerEnterRegionListener(DeathMatch deathMatch) {
        this.deathMatch = deathMatch;
    }

    /**
     * Function that checks whether a player is inside a cuboid or not.
     *
     * @param pos1      one corner of the cuboid region, a Location
     * @param pos2      second corner of the cuboid region, a Location
     * @param playerPos the player's position, a Location
     * @return true if player is in the cuboid, false if not.
     */

    public static boolean isPlayerInRegion(Location pos1, Location pos2, Location playerPos) {
        return playerPos.toVector().isInAABB(new Vector(Math.min(pos1.getX(), pos2.getX()), Math.min(pos1.getY(), pos2.getY()), Math.min(pos1.getZ(), pos2.getZ())), new Vector(Math.max(pos1.getX(), pos2.getX()), Math.max(pos1.getY(), pos2.getY()), Math.max(pos1.getZ(), pos2.getZ())));
    }

    /**
     * Listener that uses the isPlayerInRegion() method to check if a player entered a cuboid region associated with the
     * arena teleport.
     *
     * @param e PlayerMoveEvent event
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Location pos1 = new Location(deathMatch.getLobbyLocation().getWorld(), deathMatch.getConfig().getDouble("lobby.portal.pos1.x"), deathMatch.getConfig().getDouble("lobby.portal.pos1.y"), deathMatch.getConfig().getDouble("lobby.portal.pos1.z"));
        Location pos2 = new Location(deathMatch.getLobbyLocation().getWorld(), deathMatch.getConfig().getDouble("lobby.portal.pos2.x"), deathMatch.getConfig().getDouble("lobby.portal.pos2.y"), deathMatch.getConfig().getDouble("lobby.portal.pos2.z"));

        if (isPlayerInRegion(pos1, pos2, e.getPlayer().getLocation())) {
            if (deathMatch.getGameState() == GameState.IN_LOBBY) {
                e.getPlayer().teleport(deathMatch.getLobbyLocation());
            } else {
                Arena currentMatchArena = deathMatch.getCurrentMatch().getMatchArena();
                e.getPlayer().teleport(new Location(Bukkit.getWorld(currentMatchArena.getWorldName()), currentMatchArena.getSpawnX(), currentMatchArena.getSpawnY(), currentMatchArena.getSpawnZ()));
            }
        }
    }
}
