package is.sly.garfield.deathmatch.economy.listeners;

import is.sly.garfield.deathmatch.DeathMatch;
import is.sly.garfield.deathmatch.economy.objects.BrewingStandShop;
import is.sly.garfield.deathmatch.economy.utils.ItemStackPriceUtils;
import is.sly.garfield.deathmatch.objects.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.Collections;

public class PlayerBrewingStandInteractListener implements Listener {

    private final DeathMatch deathMatch;

    public PlayerBrewingStandInteractListener(DeathMatch deathMatch) {
        this.deathMatch = deathMatch;
    }

    /**
     * Event handler that loads a brewing stand shop based on the right-clicked Brewing Stand Location.
     *
     * @param e the event being listened to, PlayerInteractEvent
     */
    @EventHandler
    public void onPlayerJoin(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.BREWING_STAND) {
            if (deathMatch.getEconomyHandler().getLocationBrewingStand().containsKey(e.getClickedBlock().getLocation())) {

                BrewingStandShop brewingStandShop = deathMatch.getEconomyHandler().getLocationBrewingStand().get(e.getClickedBlock().getLocation());

                Inventory inventory = Bukkit.createInventory(null, InventoryType.DISPENSER);

                for (int i = 0; i < brewingStandShop.getBrewingStandContents().size(); i++) {
                    String material = brewingStandShop.getBrewingStandContents().get(i);
                    if (!material.equals("AIR")) {
                        String[] potionInfo = material.split(":");

                        ItemStack potionToAdd = getPotionItemStack(PotionType.valueOf(potionInfo[0]), Boolean.parseBoolean(potionInfo[1]), Boolean.parseBoolean(potionInfo[2]), Boolean.parseBoolean(potionInfo[3]));

                        PlayerProfile playerProfile = deathMatch.getPlayerProfiles().get(e.getPlayer().getUniqueId());
                        int price = deathMatch.getEconomyHandler().getItemCost(potionToAdd);

                        ItemMeta itemMeta = potionToAdd.getItemMeta();
                        itemMeta.setLore(Collections.singletonList(ItemStackPriceUtils.getLoreText(price, playerProfile.canAfford(price))));
                        potionToAdd.setItemMeta(itemMeta);

                        inventory.setItem(i, potionToAdd);
                    }
                }
                e.getPlayer().openInventory(inventory);
                e.setCancelled(true);
            }
        }
    }

    /**
     * @param type
     * @param extended
     * @param upgraded
     * @param splash
     * @return
     */
    private ItemStack getPotionItemStack(PotionType type, boolean extended, boolean upgraded, boolean splash) {
        ItemStack potion = new ItemStack((splash ? Material.SPLASH_POTION : Material.POTION), 1);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        meta.setBasePotionData(new PotionData(type, extended, upgraded));
        potion.setItemMeta(meta);
        return potion;
    }
}
