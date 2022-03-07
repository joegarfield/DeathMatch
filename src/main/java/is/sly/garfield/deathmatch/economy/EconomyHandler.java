package is.sly.garfield.deathmatch.economy;

import is.sly.garfield.deathmatch.DeathMatch;
import is.sly.garfield.deathmatch.economy.objects.BrewingStandShop;
import is.sly.garfield.deathmatch.economy.objects.ChestShop;
import is.sly.garfield.deathmatch.economy.objects.EnchantmentGroup;
import is.sly.garfield.deathmatch.economy.objects.EnchantmentTableShop;
import is.sly.garfield.deathmatch.economy.utils.ItemStackPriceUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;

@Getter
public class EconomyHandler {

    private DeathMatch deathMatch;

    private HashMap<Location, ChestShop> locationChestShop;
    private HashMap<Location, BrewingStandShop> locationBrewingStand;

    private HashMap<String, EnchantmentTableShop> enchantmentTableShop;

    private Location enchantmentTableLocation;

    private ItemStackPriceUtils itemStackPriceUtils;

    public EconomyHandler(DeathMatch deathMatch) {
        this.deathMatch = deathMatch;

        this.locationChestShop = new HashMap<>();
        this.locationBrewingStand = new HashMap<>();
        this.enchantmentTableShop = new HashMap<>();

        this.enchantmentTableLocation = new Location(Bukkit.getWorld(deathMatch.getShopGuisFileConfiguration().getString("enchantment_table.world")), deathMatch.getShopGuisFileConfiguration().getInt("enchantment_table.x"), deathMatch.getShopGuisFileConfiguration().getInt("enchantment_table.y"), deathMatch.getShopGuisFileConfiguration().getInt("enchantment_table.z"));

        this.itemStackPriceUtils = new ItemStackPriceUtils(deathMatch);

        loadChestLocations();
        loadBrewingStands();
        loadEnchantmentTable();
    }

    /**
     * Function that loads chest shop shops from shopGuis.yml
     */
    private void loadChestLocations() {
        if (deathMatch.getShopGuisFileConfiguration().getConfigurationSection("chests").getKeys(false) != null) {
            for (String chestType : deathMatch.getShopGuisFileConfiguration().getConfigurationSection("chests").getKeys(false)) {
                Location chestLocation = new Location(Bukkit.getWorld(deathMatch.getShopGuisFileConfiguration().getString("chests." + chestType + ".world")), deathMatch.getShopGuisFileConfiguration().getInt("chests." + chestType + ".x"), deathMatch.getShopGuisFileConfiguration().getInt("chests." + chestType + ".y"), deathMatch.getShopGuisFileConfiguration().getInt("chests." + chestType + ".z"));
                this.locationChestShop.put(chestLocation, new ChestShop(chestLocation, deathMatch.getShopGuisFileConfiguration().getString("chests." + chestType + ".chest_name"), deathMatch.getShopGuisFileConfiguration().getStringList("chests." + chestType + ".layout")));
            }
        }
    }

    /**
     * Function that loads brewing stand shops from shopGuis.yml
     */
    private void loadBrewingStands() {
        if (deathMatch.getShopGuisFileConfiguration().getConfigurationSection("brewing_stands").getKeys(false) != null) {
            for (String chestType : deathMatch.getShopGuisFileConfiguration().getConfigurationSection("brewing_stands").getKeys(false)) {
                Location brewingStandLocation = new Location(Bukkit.getWorld(deathMatch.getShopGuisFileConfiguration().getString("brewing_stands." + chestType + ".world")), deathMatch.getShopGuisFileConfiguration().getInt("brewing_stands." + chestType + ".x"), deathMatch.getShopGuisFileConfiguration().getInt("brewing_stands." + chestType + ".y"), deathMatch.getShopGuisFileConfiguration().getInt("brewing_stands." + chestType + ".z"));
                this.locationBrewingStand.put(brewingStandLocation, new BrewingStandShop(brewingStandLocation, deathMatch.getShopGuisFileConfiguration().getStringList("brewing_stands." + chestType + ".layout")));
            }
        }
    }

    /**
     * Function that loads the enchantment table shops and types location from shopGuis.yml
     */
    private void loadEnchantmentTable() {
        for (String enchantmentType : deathMatch.getShopGuisFileConfiguration().getConfigurationSection("enchantment_table.types").getKeys(false)) {
            getEnchantmentTableShop().put(enchantmentType, new EnchantmentTableShop());
            for (String section : deathMatch.getShopGuisFileConfiguration().getConfigurationSection("enchantment_table.types." + enchantmentType).getKeys(false)) {
                EnchantmentGroup enchantmentGroup = new EnchantmentGroup();

                List<String> enchants = deathMatch.getShopGuisFileConfiguration().getStringList("enchantment_table.types." + enchantmentType + "." + section + ".enchants");

                for (String enchant : enchants) {
                    String[] enchantment = enchant.split(":");
                    enchantmentGroup.getEnchantToLevel().put(Enchantment.getByName(enchantment[0]), Integer.valueOf(enchantment[1]));
                }
                getEnchantmentTableShop().get(enchantmentType).getEnchantmentGroupPrice().put(enchantmentGroup, deathMatch.getShopGuisFileConfiguration().getInt("enchantment_table.types." + enchantmentType + "." + section + ".price"));
            }
        }
    }

    /**
     * Function that calculates the inventory value of a player.
     *
     * @param p the player whose inventory value should be calculated for
     * @return the player's inventory value, an Integer
     */
    public Integer calculateInventoryValue(Player p) {
        int value = 0;
        for (ItemStack itemStack : p.getInventory().getContents()) {
            if (itemStack != null) {
                value += getItemCost(itemStack);
            }
        }
        return value;
    }

    /**
     * Function that returns an item cost.
     *
     * @param itemStack the item to check it's value
     * @return the price of an ItemStack, an Integer
     */
    public Integer getItemCost(ItemStack itemStack) {
        int cost = 0;

        if (itemStack.getEnchantments().size() > 0) {
            cost += deathMatch.getEconomyHandler().getEnchantmentTableShop().get(ItemStackPriceUtils.getEnchantmentType(itemStack.getType())).getEnchantmentGroupPrice().get(ItemStackPriceUtils.enchantmentGroupFromItemStack(itemStack));
        }
        cost += deathMatch.getConfig().getInt("base_item_prices." + itemStack.getType()) * itemStack.getAmount();

        return cost;
    }
}
