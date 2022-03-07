package is.sly.garfield.deathmatch.economy.listeners;

import is.sly.garfield.deathmatch.DeathMatch;
import is.sly.garfield.deathmatch.economy.utils.ItemStackPriceUtils;
import is.sly.garfield.deathmatch.objects.PlayerProfile;
import is.sly.garfield.deathmatch.utils.ItemStackUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;

public class PlayerShopInventoryClickListener implements Listener {

    private final DeathMatch deathMatch;

    public PlayerShopInventoryClickListener(DeathMatch deathMatch) {
        this.deathMatch = deathMatch;
    }

    /**
     * Event handler that deals with inventory clicks in the shop GUIs.
     *
     * @param e the event being listened to, InventoryClickEvent
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player) {

            // Don't deal with clicking on empty areas.
            if (e.getClickedInventory() == null) {
                return;
            }

            // Disable Crafting inventory.
            if (e.getClickedInventory().getType() == InventoryType.CRAFTING) {
                e.setCancelled(true);
                return;
            }

            // Do not deal with inventory clicks outside of the lobby.
            if (!e.getWhoClicked().getLocation().getWorld().equals(deathMatch.getLobbyLocation().getWorld())) {
                return;
            }

            // Update player's money, in-case it was not deducted previously.
            PlayerProfile playerProfile = deathMatch.getPlayerProfiles().get(e.getWhoClicked().getUniqueId());
            playerProfile.setMoney(500 - deathMatch.getEconomyHandler().calculateInventoryValue((Player) e.getWhoClicked()));
            ((Player) e.getWhoClicked()).setLevel(playerProfile.getMoney());

            int moneyToDeduct = 0;
            int moneyToGive = 0;
            if (e.getClickedInventory().getType() == InventoryType.PLAYER) {
                // Clear item lore (which contains XP cost)
                if (e.getCursor() != null && e.getCursor().getType() != Material.AIR) {
                    moneyToDeduct = deathMatch.getEconomyHandler().getItemCost(e.getCursor());
                    ItemMeta currentItemMeta = e.getCursor().getItemMeta();
                    currentItemMeta.setLore(new ArrayList<>());
                    e.getCursor().setItemMeta(currentItemMeta);
                }
                if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY && e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR && (e.getWhoClicked().getOpenInventory().getTopInventory().getType() == InventoryType.CHEST || e.getWhoClicked().getOpenInventory().getTopInventory().getType() == InventoryType.DISPENSER)) {
                    if (e.getWhoClicked().getOpenInventory().getTopInventory().firstEmpty() != -1) {
                        moneyToGive = deathMatch.getEconomyHandler().getItemCost(e.getCurrentItem());
                    }
                }
            }
            if (e.getClickedInventory().getType() == InventoryType.DISPENSER || e.getClickedInventory().getType() == InventoryType.CHEST) {
                if (e.getCurrentItem() != null) {
                    if (!playerProfile.canAfford(deathMatch.getEconomyHandler().getItemCost(e.getCurrentItem()))) {
                        e.getWhoClicked().sendMessage(ChatColor.RED + "You cannot afford this item!");
                        e.setCancelled(true);
                        return;
                    }
                    if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY && e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
                        if (e.getWhoClicked().getOpenInventory().getBottomInventory().firstEmpty() != -1) {
                            moneyToDeduct = deathMatch.getEconomyHandler().getItemCost(e.getCurrentItem());
                            ItemStack currentItem = e.getCurrentItem();

                            ItemMeta currentItemMeta = currentItem.getItemMeta();
                            currentItemMeta.setLore(new ArrayList<>());
                            currentItem.setItemMeta(currentItemMeta);

                            Bukkit.getScheduler().runTaskLaterAsynchronously(deathMatch, () ->
                                            e.getWhoClicked().getInventory().addItem(currentItem)
                                    , 1L);
                            e.setCurrentItem(new ItemStack(Material.AIR));
                        }
                    }
                }

                if (e.getAction() == InventoryAction.HOTBAR_SWAP || e.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD) {
                    ItemStack movedItem = e.getWhoClicked().getInventory().getItem(e.getHotbarButton());

                    if (movedItem != null && movedItem.getType() != Material.AIR) {
                        moneyToGive = deathMatch.getEconomyHandler().getItemCost(movedItem);
                        ItemMeta swappedItemMeta = movedItem.getItemMeta();
                        swappedItemMeta.setLore(new ArrayList<>());
                        movedItem.setItemMeta(swappedItemMeta);
                    }

                    if (e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
                        if (e.getWhoClicked().getOpenInventory().getBottomInventory().firstEmpty() != -1) {
                            moneyToDeduct = deathMatch.getEconomyHandler().getItemCost(e.getCurrentItem());
                            ItemStack currentItem = e.getCurrentItem();
                            ItemMeta currentItemMeta = currentItem.getItemMeta();
                            currentItemMeta.setLore(new ArrayList<>());
                            currentItem.setItemMeta(currentItemMeta);

                            Bukkit.getScheduler().runTaskLaterAsynchronously(deathMatch, () ->
                                            e.getWhoClicked().getInventory().addItem(currentItem)
                                    , 1L);
                            e.setCurrentItem(new ItemStack(Material.AIR));
                        }
                    }
                }
            }


            if ((500 - (deathMatch.getEconomyHandler().calculateInventoryValue((Player) e.getWhoClicked()) + moneyToDeduct) + moneyToGive) < 0) {
                e.getWhoClicked().sendMessage(ChatColor.RED + "You cannot afford this item!");
                e.setCancelled(true);
            } else {
                playerProfile.setMoney(500 - (deathMatch.getEconomyHandler().calculateInventoryValue((Player) e.getWhoClicked()) + moneyToDeduct) + moneyToGive);

                ((Player) e.getWhoClicked()).setLevel(playerProfile.getMoney());
                updateInventoryLores(e.getWhoClicked().getOpenInventory().getTopInventory(), playerProfile);

                Bukkit.getScheduler().runTaskLaterAsynchronously(deathMatch, () ->
                {
                    ((Player) e.getWhoClicked()).updateInventory();
                    if (!ItemStackUtils.isItemStackArrayEmpty(e.getWhoClicked().getInventory().getContents()) && !ItemStackUtils.isItemStackArrayEmpty(e.getWhoClicked().getInventory().getArmorContents())) {
                        ItemStack[] deepCopyContents = ItemStackUtils.itemStackDeepCopy(e.getWhoClicked().getInventory().getContents());
                        ItemStack[] deepCopyArmorContents = ItemStackUtils.itemStackDeepCopy(e.getWhoClicked().getInventory().getArmorContents());

                        playerProfile.setMainInventory(deepCopyContents);
                        playerProfile.setArmorSlots(deepCopyArmorContents);

                        deathMatch.savePlayerProfiles();
                    }
                }, 2L);
            }
        }
    }

    /**
     * Event handler that cancels inventory drag events due to them messing with the calculations for click events.
     *
     * @param e the event being listened to, InventoryDragEvent
     */
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        e.setCancelled(true);
    }

     /**
     * Function that updates the lore of ItemStacks in an inventory, in case the player can't afford an item anymore.
     *
     * @param inventory the inventory whose ItemStacks lore needs to be modified.
     * @param playerProfile the player profile, containing whether they can afford the ItemStack.
     */
    private void updateInventoryLores(Inventory inventory, PlayerProfile playerProfile) {
        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack != null && itemStack.getType() != Material.AIR) {
                int price = deathMatch.getEconomyHandler().getItemCost(itemStack);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setLore(Collections.singletonList(ItemStackPriceUtils.getLoreText(price, playerProfile.canAfford(price))));
                itemStack.setItemMeta(itemMeta);
            }
        }
    }
}
