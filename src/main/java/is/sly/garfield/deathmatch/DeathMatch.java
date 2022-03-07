package is.sly.garfield.deathmatch;

import is.sly.garfield.deathmatch.economy.EconomyHandler;
import is.sly.garfield.deathmatch.economy.listeners.PlayerBrewingStandInteractListener;
import is.sly.garfield.deathmatch.economy.listeners.PlayerChestInteractListener;
import is.sly.garfield.deathmatch.economy.listeners.PlayerEnchantmentTableInteractListener;
import is.sly.garfield.deathmatch.economy.listeners.PlayerShopInventoryClickListener;
import is.sly.garfield.deathmatch.enums.GameState;
import is.sly.garfield.deathmatch.listeners.*;
import is.sly.garfield.deathmatch.objects.Arena;
import is.sly.garfield.deathmatch.objects.Match;
import is.sly.garfield.deathmatch.objects.PlayerProfile;
import is.sly.garfield.deathmatch.scoreboard.DeathMatchScoreboard;
import is.sly.garfield.deathmatch.utils.InventorySerialization;
import is.sly.garfield.deathmatch.utils.ItemStackUtils;
import is.sly.garfield.deathmatch.utils.MapIOUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.WorldCreator;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public final class DeathMatch extends JavaPlugin {

    public static final long PRE_GAME_TIME = 30000L;
    public static final String PREFIX = ChatColor.RED.toString() + ChatColor.BOLD + "Death " + ChatColor.GOLD + ChatColor.BOLD + "Match" + ChatColor.GRAY + " ≫ " + ChatColor.YELLOW;
    public HashMap<UUID, PlayerProfile> playerProfiles;
    public List<Arena> arenas;
    private DeathMatchScoreboard deathMatchScoreboard;
    private File shopGuisFile;
    private File playerProfilesFile;
    private FileConfiguration shopGuisFileConfiguration;
    private FileConfiguration playerProfilesFileConfiguration;
    private File arenasFile;
    private FileConfiguration arenasFileConfiguration;
    private GameState gameState;
    private Match lastMatch;
    private Match currentMatch;

    private EconomyHandler economyHandler;

    private long lobbyTimerStart;

    private Location lobbyLocation;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        createCustomConfigs();

        loadPlayerProfiles();
        loadArenas();

        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerEnterRegionListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerVoidTeleportListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ItemDropListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerConsumeListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerRespawnListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerChestInteractListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerBrewingStandInteractListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerEnchantmentTableInteractListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerShopInventoryClickListener(this), this);

        this.lobbyLocation = new Location(Bukkit.getWorld(getConfig().getString("lobby.world")), getConfig().getDouble("lobby.spawn.x"), getConfig().getDouble("lobby.spawn.y"), getConfig().getDouble("lobby.spawn.z"));

        this.lobbyLocation.getWorld().setSpawnLocation(this.lobbyLocation);

        this.gameState = GameState.IN_LOBBY;
        this.deathMatchScoreboard = new DeathMatchScoreboard(this);
        this.lobbyTimerStart = System.currentTimeMillis();

        this.economyHandler = new EconomyHandler(this);

        startGameTimer();

        // Repeating Runnable that updates the Scoreboard of currently online players.
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                getDeathMatchScoreboard().updateScoreBoard(p);
            }
        }, 0L, 10L);
    }

    @Override
    public void onDisable() {
    }

    /**
     * Function that returns the economy handler for the server.
     *
     * @return the game's EconomyHandler
     */
    public EconomyHandler getEconomyHandler() {
        return economyHandler;
    }

    /**
     * Function that starts a timer on the main server thread and deals with the game logic.
     */
    public void startGameTimer() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (getGameState() == GameState.IN_LOBBY) {
                if (currentMatch == null) {
                    generateNewMatch();
                }

                if (lobbyTimerStart + PRE_GAME_TIME <= System.currentTimeMillis()) {
                    Bukkit.broadcastMessage(PREFIX + "Game starting, loading " + currentMatch.getMatchArena().getDisplayName());

                    loadArena(currentMatch.getMatchArena());
                    gameState = GameState.IN_GAME;
                }
            } else if (getGameState() == GameState.IN_GAME) {
                if (currentMatch.isOver()) {

                    if (currentMatch.getSortedKillBoard().size() > 0) {
                        currentMatch.setWinner(getPlayerProfiles().get(currentMatch.getSortedKillBoard().get(0).getKey()));
                    }

                    Bukkit.broadcastMessage(PREFIX + "Game ending, " + (currentMatch.getWinner() == null ? "there was no winner." : currentMatch.getWinner().getProfileName() + " was the winner!"));

                    gameState = GameState.IN_LOBBY;
                    lobbyTimerStart = System.currentTimeMillis();

                    unloadArena(currentMatch.getMatchArena().getWorldName());

                    generateNewMatch();
                }
            }
        }, 0, 20L);
    }

    /**
     * Function that generates a new match and saves the old match to memory.
     */
    public void generateNewMatch() {
        this.lastMatch = this.currentMatch;
        this.currentMatch = new Match(getArenas().get((int) Math.floor(Math.random() * getArenas().size())));
    }

    /**
     * Function that returns the current, ongoing match
     *
     * @return the current Match
     */
    public Match getCurrentMatch() {
        return this.currentMatch;
    }

    /**
     * Function that returns the last match
     *
     * @return the last Match
     */
    public Match getLastMatch() {
        return this.lastMatch;
    }

    /**
     * Function that returns the configured and loaded arenas.
     *
     * @return List of Arena objects.
     */
    public List<Arena> getArenas() {
        return this.arenas;
    }

    /**
     * Function that returns the configuration file of player profiles.
     *
     * @return the playerProfiles configuration file, a FileConfiguration.
     */
    public FileConfiguration getPlayerProfilesFileConfiguration() {
        return this.playerProfilesFileConfiguration;
    }

    /**
     * Function that returns the shop GUI configuration file.
     *
     * @return the shopGUis configuration file, a FileConfiguration.
     */
    public FileConfiguration getShopGuisFileConfiguration() {
        return shopGuisFileConfiguration;
    }

    /**
     * Function that returns the configuration file for the arenas.
     *
     * @return the arenas configuration file, a FileConfiguration.
     */
    public FileConfiguration getArenasFileConfiguration() {
        return this.arenasFileConfiguration;
    }

    /**
     * Function that returns the hashmap of UUID to PlayerProfiles
     *
     * @return HashMap of UUID to PlayerProfiles
     */
    public HashMap<UUID, PlayerProfile> getPlayerProfiles() {
        return this.playerProfiles;
    }

    /**
     * Function that returns the current game state.
     *
     * @return GameState
     */
    public GameState getGameState() {
        return this.gameState;
    }

    /**
     * Function that returns the current lobby timer start time.
     *
     * @return time in milliseconds in which the lobby timer started, a long
     */
    public long getLobbyTimerStart() {
        return this.lobbyTimerStart;
    }

    /**
     * Function that returns the lobby location, as set in config.yml
     *
     * @return the lobby Location
     */
    public Location getLobbyLocation() {
        return this.lobbyLocation;
    }

    /**
     * Function that returns the object that deals with player scoreboards.
     *
     * @return the DeathMatchScoreboard
     */
    public DeathMatchScoreboard getDeathMatchScoreboard() {
        return this.deathMatchScoreboard;
    }

    /**
     * Function that sets a player's inventory, XP level, etc., based on stored values.
     *
     * @param p the player to perform actions on.
     */
    public void playerBackToSpawn(Player p) {
        p.getInventory().clear();

        if (!getPlayerProfiles().containsKey(p.getUniqueId())) {
            getPlayerProfiles().put(p.getUniqueId(), new PlayerProfile(p.getName(), p.getUniqueId(), 500, p.getInventory().getContents(), p.getInventory().getArmorContents()));
        }

        PlayerProfile playerProfile = getPlayerProfiles().get(p.getUniqueId());

        if (!playerProfile.getProfileName().equalsIgnoreCase(p.getName())) {
            playerProfile.setProfileName(p.getName());
        }

        p.setLevel(playerProfile.getMoney());
        p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(40.0D);
        p.setHealth(40.0D);
        p.setFoodLevel(20);


        Bukkit.getScheduler().runTaskLaterAsynchronously(this, () -> {
            p.getInventory().clear();
            // Have to make a Deep Copy because ItemStacks change while in-game.
            p.getInventory().setContents(ItemStackUtils.itemStackDeepCopy(playerProfile.getMainInventory()));
            p.getInventory().setArmorContents(ItemStackUtils.itemStackDeepCopy(playerProfile.getArmorSlots()));
            p.updateInventory();
        }, 5L);

        for (PotionEffect effect : p.getActivePotionEffects()) {
            p.removePotionEffect(effect.getType());
        }

        Bukkit.getScheduler().runTaskLater(this, () -> {
            p.teleport(getLobbyLocation());
        }, 5L);
        getDeathMatchScoreboard().setScoreBoard(p);
    }

    /**
     * Function that creates the playerProfiles.yml and arenas.yml configs.
     */
    private void createCustomConfigs() {
        playerProfilesFile = new File(getDataFolder(), "playerProfiles.yml");
        if (!playerProfilesFile.exists()) {
            playerProfilesFile.getParentFile().mkdirs();
            saveResource("playerProfiles.yml", false);
        }


        shopGuisFile = new File(getDataFolder(), "shopGuis.yml");
        if (!shopGuisFile.exists()) {
            shopGuisFile.getParentFile().mkdirs();
            saveResource("shopGuis.yml", false);
        }

        arenasFile = new File(getDataFolder(), "arenas.yml");
        if (!arenasFile.exists()) {
            arenasFile.getParentFile().mkdirs();
            saveResource("arenas.yml", false);
        }

        playerProfilesFileConfiguration = new YamlConfiguration();
        try {
            playerProfilesFileConfiguration.load(playerProfilesFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        arenasFileConfiguration = new YamlConfiguration();
        try {
            arenasFileConfiguration.load(arenasFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        shopGuisFileConfiguration = new YamlConfiguration();
        try {
            shopGuisFileConfiguration.load(shopGuisFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Function loads all the player profiles from playerProfiles.yml and adds them to the playerProfiles List
     */
    public void loadPlayerProfiles() {
        this.playerProfiles = new HashMap<>();
        if (getPlayerProfilesFileConfiguration().getConfigurationSection("players") != null) {
            for (String playerUUID : getPlayerProfilesFileConfiguration().getConfigurationSection("players").getKeys(false)) {
                try {
                    this.playerProfiles.put(UUID.fromString(playerUUID), new PlayerProfile(getPlayerProfilesFileConfiguration().getString("players." + playerUUID + ".profileName"), UUID.fromString(playerUUID), getPlayerProfilesFileConfiguration().getInt("players." + playerUUID + ".money"), InventorySerialization.itemStackArrayFromBase64(getPlayerProfilesFileConfiguration().getString("players." + playerUUID + ".inventory.contents")), InventorySerialization.itemStackArrayFromBase64(getPlayerProfilesFileConfiguration().getString("players." + playerUUID + ".inventory.armor"))));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Function that saves the player profiles to playerProfiles.yml
     */
    public void savePlayerProfiles() {
        for (PlayerProfile playerProfile : getPlayerProfiles().values()) {
            String[] inventoryContents = {InventorySerialization.itemStackArrayToBase64(playerProfile.getMainInventory()), InventorySerialization.itemStackArrayToBase64(playerProfile.getArmorSlots())};
            getPlayerProfilesFileConfiguration().set("players." + playerProfile.getPlayerUUID() + ".inventory.contents", inventoryContents[0]);
            getPlayerProfilesFileConfiguration().set("players." + playerProfile.getPlayerUUID() + ".inventory.armor", inventoryContents[1]);
            getPlayerProfilesFileConfiguration().set("players." + playerProfile.getPlayerUUID() + ".money", playerProfile.getMoney());
            getPlayerProfilesFileConfiguration().set("players." + playerProfile.getPlayerUUID() + ".profileName", playerProfile.getProfileName());
        }
        try {
            getPlayerProfilesFileConfiguration().save(playerProfilesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Function that extracts and arena's archive and loads the map with Bukkit.
     *
     * @param arena the arena to load.
     */
    public void loadArena(Arena arena) {
        try {
            String worldName = arena.getWorldName();
            MapIOUtils.extract(Paths.get(worldName + ".zip"), Paths.get("").toAbsolutePath());
            new WorldCreator(worldName).createWorld();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Function that unloads an arena using Bukkit.
     *
     * @param worldName the name of the arena world to unload.
     */
    public void unloadArena(String worldName) {
        try {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getWorld().getName().equalsIgnoreCase(worldName)) {
                    playerBackToSpawn(p);
                }
            }
            Bukkit.unloadWorld(worldName, false);
            MapIOUtils.deleteDirectory(Paths.get(worldName));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Function that loads the arenas from arena.yml and deletes any residual arena folders.
     */
    public void loadArenas() {
        this.arenas = new ArrayList<>();
        if (getArenasFileConfiguration().getConfigurationSection("arenas").getKeys(false) != null) {
            for (String name : getArenasFileConfiguration().getConfigurationSection("arenas").getKeys(false)) {
                this.arenas.add(new Arena(getArenasFileConfiguration().getString("arenas." + name + ".name"), getArenasFileConfiguration().getString("arenas." + name + ".world"), getArenasFileConfiguration().getDouble("arenas." + name + ".spawn.x"), getArenasFileConfiguration().getDouble("arenas." + name + ".spawn.y"), getArenasFileConfiguration().getDouble("arenas." + name + ".spawn.z")));
                try {
                    MapIOUtils.deleteDirectory(Paths.get(getArenasFileConfiguration().getString("arenas." + name + ".world")));
                } catch (IOException e) {
                    getLogger().log(Level.WARNING, getArenasFileConfiguration().getString("arenas." + name + ".world") + " was not present to delete.");
                }
            }
        }
    }
}

