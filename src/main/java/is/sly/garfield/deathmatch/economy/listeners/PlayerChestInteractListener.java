package is.sly.garfield.deathmatch.economy.listeners;

import is.sly.garfield.deathmatch.DeathMatch;
import is.sly.garfield.deathmatch.economy.objects.ChestShop;
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

import java.util.Collections;

public class PlayerChestInteractListener implements Listener {

    private final DeathMatch deathMatch;

    public PlayerChestInteractListener(DeathMatch deathMatch) {
        this.deathMatch = deathMatch;
    }


    /**
     * Event handler that loads a chest shop based on the right-clicked Brewing Stand Location.
     *
     * @param e the event being listened to, PlayerInteractEvent
     */
    @EventHandler
    public void onPlayerJoin(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.CHEST) {
            if (deathMatch.getEconomyHandler().getLocationChestShop().containsKey(e.getClickedBlock().getLocation())) {

                ChestShop chestShop = deathMatch.getEconomyHandler().getLocationChestShop().get(e.getClickedBlock().getLocation());

                Inventory inventory = Bukkit.createInventory(null, InventoryType.CHEST, chestShop.getChestName());

                for (int i = 0; i < chestShop.getChestContents().size(); i++) {
                    String material = chestShop.getChestContents().get(i);
                    if (!material.equals("AIR")) {
                        int amount = material.split(":").length == 1 ? 1 : Integer.parseInt(material.split(":")[1]);
                        if (material.split(":").length > 1) {
                            material = material.split(":")[0];
                        }

                        ItemStack itemToAdd = new ItemStack(Material.getMaterial(material), amount);

                        PlayerProfile playerProfile = deathMatch.getPlayerProfiles().get(e.getPlayer().getUniqueId());
                        int price = deathMatch.getEconomyHandler().getItemCost(itemToAdd);

                        ItemMeta itemMeta = itemToAdd.getItemMeta();
                        itemMeta.setLore(Collections.singletonList(ItemStackPriceUtils.getLoreText(price, playerProfile.canAfford(price))));
                        itemToAdd.setItemMeta(itemMeta);

                        inventory.setItem(i, itemToAdd);
                    }
                }
                e.getPlayer().openInventory(inventory);
                e.setCancelled(true);
            }
        }
    }
}
