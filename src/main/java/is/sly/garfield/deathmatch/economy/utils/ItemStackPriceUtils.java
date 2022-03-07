package is.sly.garfield.deathmatch.economy.utils;

import is.sly.garfield.deathmatch.DeathMatch;
import is.sly.garfield.deathmatch.economy.objects.EnchantmentGroup;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class ItemStackPriceUtils {

    private DeathMatch deathMatch;

    public ItemStackPriceUtils(DeathMatch deathMatch) {
        this.deathMatch = deathMatch;
    }

    /**
     * Function that generates a String, taking in account whether a player can afford it.
     *
     * @param price the price of the item in EXP, an int
     * @param canAfford whether the player can afford it, a boolean
     * @return lore text, a String
     */
    public static String getLoreText(int price, boolean canAfford) {
        String loreText = "";

        ChatColor darkColor;
        ChatColor lightColor;

        if (!canAfford) {
            darkColor = ChatColor.DARK_RED;
            lightColor = ChatColor.RED;
        } else {
            darkColor = ChatColor.GOLD;
            lightColor = ChatColor.YELLOW;
        }

        loreText = darkColor.toString() + ChatColor.UNDERLINE + "EXP COST:" + lightColor + " " + price + "xp";
        return loreText;
    }

    /**
     * Function that gets the category of enchants of a Material.
     *
     * @param material material to get the enchantment category of
     * @return category of enchants, a String
     */
    public static String getEnchantmentType(Material material) {
        if (material.toString().contains("AXE")) return "AXE";
        if (material.toString().contains("SWORD")) return "SWORD";
        if (material.toString().contains("BOW")) return "BOW";
        if (material.toString().contains("HELMET") || material.toString().contains("CHESTPLATE") || material.toString().contains("LEGGINGS") || material.toString().contains("BOOTS"))
            return "ARMOR";
        return null;
    }

    /**
     * Function that generates an EnchantmentGroup from an ItemStack.
     *
     * @param itemStack the item to generate the EnchantmentGroup from, an ItemStack
     * @return the EnchantmentGroup of the ItemStack
     */
    public static EnchantmentGroup enchantmentGroupFromItemStack(ItemStack itemStack) {
        EnchantmentGroup enchantmentGroup = new EnchantmentGroup();
        enchantmentGroup.setEnchantToLevel(new HashMap<>(itemStack.getEnchantments()));
        return enchantmentGroup;
    }
}
