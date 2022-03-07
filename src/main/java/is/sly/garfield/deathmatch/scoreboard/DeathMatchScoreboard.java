package is.sly.garfield.deathmatch.scoreboard;

import is.sly.garfield.deathmatch.DeathMatch;
import is.sly.garfield.deathmatch.enums.GameState;
import is.sly.garfield.deathmatch.objects.Match;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DeathMatchScoreboard {

    private final DeathMatch deathMatch;

    public DeathMatchScoreboard(DeathMatch deathMatch) {
        this.deathMatch = deathMatch;
    }


    /**
     * Function that creates a new scoreboard containing game information for a player.
     *
     * @param player the player to set the scoreboard for.
     */
    public void setScoreBoard(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("ServerName", "dummy");

        // Scoreboard title
        obj.setDisplayName(ChatColor.RED.toString() + ChatColor.BOLD + "Death " + ChatColor.GOLD + ChatColor.BOLD + "Match");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        if (deathMatch.getGameState() == GameState.IN_LOBBY) {
            // Lobby scoreboard

            // Blank scoreboard entry that acts as a spacer between the title and next map name.
            Score titleSpacer = obj.getScore(" ");
            titleSpacer.setScore(7);

            // Scoreboard team used to display the map of the next Match.
            Team map = board.registerNewTeam("map");
            map.setPrefix(ChatColor.BLUE + "Next Map: ");
            map.addEntry(ChatColor.AQUA.toString() + ChatColor.AQUA + ChatColor.AQUA);
            map.setSuffix(ChatColor.AQUA + (deathMatch.getCurrentMatch().getMatchArena() == null ? "Unknown Map" : deathMatch.getCurrentMatch().getMatchArena().getDisplayName()));
            obj.getScore(ChatColor.AQUA.toString() + ChatColor.AQUA + ChatColor.AQUA).setScore(6);

            // Scoreboard team used to display the winner of the previous Match.
            Team lastWinner = board.registerNewTeam("winner");
            lastWinner.setPrefix(ChatColor.BLUE + "Last Winner: ");
            String winnerString = ChatColor.AQUA + (deathMatch.getLastMatch() == null || deathMatch.getLastMatch().getWinner() == null ? "No Winner" : deathMatch.getLastMatch().getWinner().getProfileName());

            // Substring the String if it is greater than 16 characters (length limit on 1.12 clients).
            if (winnerString.length() > 16) {
                winnerString = winnerString.substring(0, 16);
            }
            lastWinner.setSuffix(winnerString);
            lastWinner.addEntry(ChatColor.AQUA.toString() + ChatColor.AQUA + ChatColor.AQUA + ChatColor.AQUA);
            obj.getScore(ChatColor.AQUA.toString() + ChatColor.AQUA + ChatColor.AQUA + ChatColor.AQUA).setScore(5);

            // Scoreboard team used to display the timer until the next Match starts.
            Team timer = board.registerNewTeam("timer");
            timer.setPrefix(ChatColor.BLUE + "Starting in: ");
            timer.addEntry(ChatColor.AQUA.toString() + ChatColor.AQUA);
            timer.setSuffix("" + getTimeRemaining(deathMatch.getLobbyTimerStart(), DeathMatch.PRE_GAME_TIME));
            obj.getScore(ChatColor.AQUA.toString() + ChatColor.AQUA).setScore(4);

        } else if (deathMatch.getGameState() == GameState.IN_GAME) {
            // In-game scoreboard

            // Integer used ot take into account the size of the kill leaderboard.
            int start = 9 + (deathMatch.getCurrentMatch().getKillCounter().size() > 0 ? deathMatch.getCurrentMatch().getKillCounter().size() : -2);

            // Blank scoreboard entry that acts as a spacer between the title and next map name.
            Score titleSpacer = obj.getScore(" ");
            titleSpacer.setScore(start);

            // Scoreboard team used to display the map of the current Match.
            Team map = board.registerNewTeam("map");
            map.setPrefix(ChatColor.BLUE + "Map:");
            map.addEntry(ChatColor.AQUA.toString() + ChatColor.AQUA + ChatColor.AQUA);
            map.setSuffix(ChatColor.AQUA + (deathMatch.getCurrentMatch().getMatchArena() == null ? "Unknown Map" : deathMatch.getCurrentMatch().getMatchArena().getDisplayName()));
            obj.getScore(ChatColor.AQUA.toString() + ChatColor.AQUA + ChatColor.AQUA).setScore(start - 1);


            // Scoreboard team used to display the winner of the previous Match.
            Team lastWinner = board.registerNewTeam("winner");
            lastWinner.setPrefix(ChatColor.BLUE + "Last Winner: ");
            String winnerString = ChatColor.AQUA + (deathMatch.getLastMatch() == null || deathMatch.getLastMatch().getWinner() == null ? "No Winner" : deathMatch.getLastMatch().getWinner().getProfileName());

            // Substring strings that are too long to be a suffix for 1.12 clients.
            if (winnerString.length() > 16) {
                winnerString = winnerString.substring(0, 16);
            }
            lastWinner.setSuffix(winnerString);
            lastWinner.addEntry(ChatColor.AQUA.toString() + ChatColor.AQUA + ChatColor.AQUA + ChatColor.AQUA);
            obj.getScore(ChatColor.AQUA.toString() + ChatColor.AQUA + ChatColor.AQUA + ChatColor.AQUA).setScore(start - 2);

            // Scoreboard team used to display a timer of the length remaining in a Match.
            Team timer = board.registerNewTeam("timer");
            timer.setPrefix(ChatColor.BLUE + "Ending in: ");
            timer.addEntry(ChatColor.AQUA.toString() + ChatColor.AQUA);
            timer.setSuffix("" + getTimeRemaining(deathMatch.getCurrentMatch().getMatchStart(), Match.MATCH_LENGTH));
            obj.getScore(ChatColor.AQUA.toString() + ChatColor.AQUA).setScore(start - 3);

            // There are kills to display on the Scoreboard.
            if (deathMatch.getCurrentMatch().getKillCounter().size() > 0) {

                // Scoreboard entry for a spacer between the timer and the kill board heading.
                Score killBoardSpacer = obj.getScore("  ");
                killBoardSpacer.setScore(start - 4);

                // Scoreboard entry thta displays the kill board heading.
                Score killBoard = obj.getScore(ChatColor.BLUE + "Top Kills: ");
                killBoard.setScore(start - 5);

                // String builder used to generate a unique scoreboard entry for each leaderboard entry.
                StringBuilder stringBuilder = new StringBuilder().append(ChatColor.BLACK).append(ChatColor.AQUA);

                List<Map.Entry<UUID, Integer>> sortedKillBoard = deathMatch.getCurrentMatch().getSortedKillBoard();

                // For loop for the top 3 entries (or the number of kills on the leaderboard, which ever number is lower)
                for (int i = 0; i < Math.min(3, deathMatch.getCurrentMatch().getKillCounter().size()); i++) {

                    // Scoreboard team registered to display a player entry on the kill leaderboard.
                    Team kill = board.registerNewTeam("kill" + i);

                    String playerEntry = ChatColor.BLUE + deathMatch.getPlayerProfiles().get(sortedKillBoard.get(i).getKey()).getProfileName() + ChatColor.AQUA + " - " + sortedKillBoard.get(i).getValue();

                    // Use substrings, prefixes, and suffixes for long player entries (1.12 clients)
                    if (playerEntry.length() > 16) {

                        // Shorten the string if it is too long to be displayed with a prefix and suffix
                        if (playerEntry.length() > 32) {
                            playerEntry = playerEntry.substring(0, 32);
                        }

                        // Check to not break color formatting for long usernames (prevent separation of the & character
                        // with the color value)
                        if (playerEntry.charAt(15) != '§') {
                            kill.setPrefix(playerEntry.substring(0, 16));
                            kill.setSuffix(playerEntry.substring(16));
                        } else {
                            if (playerEntry.length() > 31) {
                                playerEntry = playerEntry.substring(0, 31);
                            }
                            kill.setPrefix(playerEntry.substring(0, 15));
                            kill.setSuffix(playerEntry.substring(15));
                        }
                    } else {
                        // If the entry is short enough, just set as a prefix.
                        kill.setPrefix(playerEntry);
                    }
                    kill.addEntry(stringBuilder.toString());
                    obj.getScore(stringBuilder.toString()).setScore(start - (6 + i));

                    // Append to generate a unique entry.
                    stringBuilder.insert(0, ChatColor.BLACK);
                }
            }
        }

        // Spacer used to separate the player count and the previous entry (either timer or kill board)
        Score onlineSpacer = obj.getScore("   ");
        onlineSpacer.setScore(3);

        // Scoreboard team used to display the number of players on the server.
        Team onlineCounter = board.registerNewTeam("onlineCounter");
        onlineCounter.addEntry(ChatColor.RED.toString() + ChatColor.BLUE);
        obj.getScore(ChatColor.RED.toString() + ChatColor.BLUE).setScore(2);
        onlineCounter.setPrefix(ChatColor.BLUE + "Online: " + ChatColor.AQUA + Bukkit.getOnlinePlayers().size());

        // Spacer to separate the online counter with the server address.
        Score serverAddressSpacer = obj.getScore("");
        serverAddressSpacer.setScore(1);

        // Scoreboard entry used to display the server address.
        Score serverAddress = obj.getScore(ChatColor.YELLOW + "sly.is");
        serverAddress.setScore(0);

        player.setScoreboard(board);
    }

    /**
     * Function used to update the player's scoreboard for dynamically changing game information.
     *
     * @param player the player to update the Scoreboard for.
     */
    public void updateScoreBoard(Player player) {

        Scoreboard board = player.getScoreboard();

        // Get the onlineCounter scoreboard team and update the prefix.
        Team onlineCounter = board.getTeam("onlineCounter");
        onlineCounter.setPrefix(ChatColor.BLUE + "Online: " + ChatColor.AQUA + Bukkit.getOnlinePlayers().size());

        if (deathMatch.getGameState() == GameState.IN_LOBBY) {
            // Kill leaderboard should no longer be displayed in lobby, generate a scoreboard that does not contain it.
            if (board.getTeam("kill" + 0) != null) {
                Bukkit.getScheduler().runTask(deathMatch, () -> {
                    setScoreBoard(player);
                });
            } else {
                // Get the map scoreboard team and update the prefix and suffix.
                Team map = board.getTeam("map");
                map.setPrefix(ChatColor.BLUE + "Next Map: ");
                map.setSuffix(ChatColor.AQUA + (deathMatch.getCurrentMatch().getMatchArena() == null ? "Unknown Map" : deathMatch.getCurrentMatch().getMatchArena().getDisplayName()));

                // Get the timer scoreboard team and update the prefix and suffix.
                Team timer = board.getTeam("timer");
                timer.setPrefix(ChatColor.BLUE + "Starting in: ");
                timer.setSuffix(ChatColor.AQUA + getTimeRemaining(deathMatch.getLobbyTimerStart(), DeathMatch.PRE_GAME_TIME));

                // Get the winner scoreboard team and update the suffix.
                Team lastWinner = board.getTeam("winner");
                String winnerString = ChatColor.AQUA + (deathMatch.getLastMatch() == null || deathMatch.getLastMatch().getWinner() == null ? "No Winner" : deathMatch.getLastMatch().getWinner().getProfileName());

                // Shorten the string for 1.12 clients.
                if (winnerString.length() > 16) {
                    winnerString = winnerString.substring(0, 16);
                }
                lastWinner.setSuffix(winnerString);
            }
        } else if (deathMatch.getGameState() == GameState.IN_GAME) {
            // Get the map scoreboard team and update the prefix and suffix.
            Team map = board.getTeam("map");
            map.setPrefix(ChatColor.BLUE + "Map: ");
            map.setSuffix(ChatColor.AQUA + (deathMatch.getCurrentMatch().getMatchArena() == null ? "Unknown Map" : deathMatch.getCurrentMatch().getMatchArena().getDisplayName()));

            // Get the timer scoreboard team and update the prefix and suffix.
            Team timer = board.getTeam("timer");
            timer.setPrefix(ChatColor.BLUE + "Ending in: ");
            timer.setSuffix(ChatColor.AQUA + getTimeRemaining(deathMatch.getCurrentMatch().getMatchStart(), Match.MATCH_LENGTH));

            // Get the winner scoreboard team and update the suffix.
            Team lastWinner = board.getTeam("winner");
            lastWinner.setPrefix(ChatColor.BLUE + "Last Winner: ");
            String winnerString = ChatColor.AQUA + (deathMatch.getLastMatch() == null || deathMatch.getLastMatch().getWinner() == null ? "No Winner" : deathMatch.getLastMatch().getWinner().getProfileName());

            // Shorten the string for 1.12 clients.
            if (winnerString.length() > 16) {
                winnerString = winnerString.substring(0, 16);
            }
            lastWinner.setSuffix(winnerString);

            // Checks for entries in the kill leaderboard.
            if (deathMatch.getCurrentMatch().getKillCounter().size() > 0) {
                // Checks whether the scoreboard contains enough entries to display the number of entries on the
                // kill leaderboard.
                if (board.getTeam("kill" + (Math.min(3, deathMatch.getCurrentMatch().getKillCounter().size()) - 1)) == null) {
                    Bukkit.getScheduler().runTask(deathMatch, () -> {
                        setScoreBoard(player);
                    });
                } else {
                    List<Map.Entry<UUID, Integer>> sortedKillBoardEntries = deathMatch.getCurrentMatch().getSortedKillBoard();
                    for (int i = 0; i < Math.min(3, sortedKillBoardEntries.size()); i++) {

                        Team kill = board.getTeam("kill" + i);
                        String playerEntry = ChatColor.BLUE + deathMatch.getPlayerProfiles().get(sortedKillBoardEntries.get(i).getKey()).getProfileName() + ChatColor.AQUA + " - " + sortedKillBoardEntries.get(i).getValue();

                        // Use substrings, prefixes, and suffixes for long player entries (1.12 clients)
                        if (playerEntry.length() > 16) {

                            // Shorten the string if it is too long to be displayed with a prefix and suffix
                            if (playerEntry.length() > 32) {
                                playerEntry = playerEntry.substring(0, 32);
                            }

                            // Check to not break color formatting for long usernames (prevent separation of the & character
                            // with the color value)
                            if (playerEntry.charAt(15) != '§') {
                                kill.setPrefix(playerEntry.substring(0, 16));
                                kill.setSuffix(playerEntry.substring(16));
                            } else {
                                if (playerEntry.length() > 31) {
                                    playerEntry = playerEntry.substring(0, 31);
                                }
                                kill.setPrefix(playerEntry.substring(0, 15));
                                kill.setSuffix(playerEntry.substring(15));
                            }
                        } else {
                            kill.setPrefix(playerEntry);
                            // Remove the suffix in-case the old entry in its position contained one.
                            kill.setSuffix("");
                        }
                    }
                }
            }
        }
    }

    /**
     * Function that returns the time remaining given a start time and timer length.
     *
     * @param startTimeStamp the timer start time, a long
     * @param totalTime      the timer total time, a long
     * @return the time remaining in mm:ss format, a String.
     */
    private String getTimeRemaining(long startTimeStamp, long totalTime) {
        long timeRemaining = startTimeStamp + totalTime - System.currentTimeMillis();
        return Math.max(0, TimeUnit.MILLISECONDS.toMinutes(timeRemaining)) + ":" + String.format("%02d", Math.max(0, TimeUnit.MILLISECONDS.toSeconds(timeRemaining - TimeUnit.MINUTES.toMillis(TimeUnit.MILLISECONDS.toMinutes(timeRemaining)))));
    }
}
