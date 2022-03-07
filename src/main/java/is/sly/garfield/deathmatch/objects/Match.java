package is.sly.garfield.deathmatch.objects;

import is.sly.garfield.deathmatch.DeathMatch;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class Match {

    public static final long MATCH_LENGTH = 300000L;

    private final long matchStart;
    private final HashMap<UUID, Integer> killCounter;

    private Arena matchArena;
    private PlayerProfile winner;


    public Match(Arena matchArena) {
        this.matchStart = System.currentTimeMillis();

        this.matchArena = matchArena;
        this.killCounter = new HashMap<>();
    }

    /**
     * Method that returns the Match's start time, in milliseconds. Accounts for the Lobby time since Matches are generated
     * when the previous match ends.
     *
     * @return
     */
    public long getMatchStart() {
        return matchStart + DeathMatch.PRE_GAME_TIME; // Account for the PRE_GAME_TIME as the Match generates 30 seconds before.
    }

    /**
     * M<ethod that checks whether a Match is over or not.
     *
     * @return true if Match is over, false if Match is ongoing.
     */
    public boolean isOver() {
        return getMatchStart() + MATCH_LENGTH <= System.currentTimeMillis();
    }

    /**
     * Method that returns a sorted kill board.
     *
     * @return List containing Entries of UUIDs to Integers.
     */
    public List<Map.Entry<UUID, Integer>> getSortedKillBoard() {
        List<Map.Entry<UUID, Integer>> hashMapEntries = new ArrayList<>(killCounter.entrySet());
        hashMapEntries.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        return hashMapEntries;
    }
}
