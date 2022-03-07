package is.sly.garfield.deathmatch.economy.listeners;

import is.sly.garfield.deathmatch.DeathMatch;
import is.sly.garfield.deathmatch.economy.objects.EnchantmentGroup;
import is.sly.garfield.deathmatch.economy.objects.EnchantmentTableShop;
import is.sly.garfield.deathmatch.economy.utils.ItemStackPriceUtils;
import is.sly.garfield.deathmatch.objects.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.Map;

public class PlayerEnchantmentTableInteractListener implements Listener {

    private final DeathMatch deathMatch;

    public PlayerEnchantmentTableInteractListener(DeathMatch deathMatch) {
        this.deathMatch = deathMatch;
    }

    /**
     * Event handler that loads a brewing stand shop based on the item the player right clicked the enchantment table
     * with.
     *
     * @param e the event being listened to, PlayerInteractEvent
     */
    @EventHandler
    public void onPlayerJoin(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.ENCHANTMENT_TABLE && e.getHand() == EquipmentSlot.HAND) {
            if (deathMatch.getEconomyHandler().getEnchantmentTableLocation().equals(e.getClickedBlock().getLocation())) {
                if (e.getItem() != null && ItemStackPriceUtils.getEnchantmentType(e.getItem().getType()) != null) {
                    PlayerProfile playerProfile = deathMatch.getPlayerProfiles().get(e.getPlayer().getUniqueId());

                    // Update the playerProfile
                    playerProfile.setMoney(500 - (deathMatch.getEconomyHandler().calculateInventoryValue(e.getPlayer())) + deathMatch.getEconomyHandler().getItemCost(e.getPlayer().getInventory().getItemInMainHand()));
                    e.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                    e.getPlayer().setLevel(playerProfile.getMoney());

                    Inventory inventory = Bukkit.createInventory(null, InventoryType.DISPENSER);

                    EnchantmentTableShop enchantmentTableShop = deathMatch.getEconomyHandler().getEnchantmentTableShop().get(ItemStackPriceUtils.getEnchantmentType(e.getItem().getType()));

                    EnchantmentGroup[] enchantmentGroups = enchantmentTableShop.getEnchantmentGroupPrice().keySet().toArray(new EnchantmentGroup[0]);

                    for (int i = 0; i < enchantmentGroups.length; i++) {
                        ItemStack itemToEnchant = new ItemStack(e.getItem().getType());

                        EnchantmentGroup enchantmentGroup = enchantmentGroups[i];

                        for (Map.Entry<Enchantment, Integer> entry : enchantmentGroup.getEnchantToLevel().entrySet()) {
                            itemToEnchant.addUnsafeEnchantment(entry.getKey(), entry.getValue());
                        }

                        int price = deathMatch.getEconomyHandler().getItemCost(itemToEnchant);

                        ItemMeta itemMeta = itemToEnchant.getItemMeta();
                        itemMeta.setLore(Collections.singletonList(ItemStackPriceUtils.getLoreText(price, playerProfile.canAfford(price))));

                        // have to do setItemMeta because getItemMeta returns an ItemMeta copy
                        itemToEnchant.setItemMeta(itemMeta);
                        inventory.setItem(i, itemToEnchant);
                    }

                    e.getPlayer().openInventory(inventory);
                }
                e.setCancelled(true);
            }
        }
    }
}
