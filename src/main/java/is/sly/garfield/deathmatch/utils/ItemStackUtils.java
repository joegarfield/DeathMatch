package is.sly.garfield.deathmatch.utils;

import org.bukkit.inventory.ItemStack;

public class ItemStackUtils {

    /**
     * Create a deep copy of an ItemStack array.
     *
     * @param toCopy the ItemStack array to copy
     * @return the deep copied ItemStack array
     */
    public static ItemStack[] itemStackDeepCopy(ItemStack[] toCopy) {
        ItemStack[] deepCopiedArray = new ItemStack[toCopy.length];

        for (int i = 0; i < toCopy.length; i++) {
            if (toCopy[i] != null) {
                deepCopiedArray[i] = toCopy[i].clone();
            }
        }
        return deepCopiedArray;
    }
}
