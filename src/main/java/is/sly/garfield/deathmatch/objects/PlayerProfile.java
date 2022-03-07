package is.sly.garfield.deathmatch.objects;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

@Getter
@Setter
public class PlayerProfile {
    private int money;
    private String profileName;
    private UUID playerUUID;
    private ItemStack[] mainInventory;
    private ItemStack[] armorSlots;

    public PlayerProfile(String profileName, UUID playerUUID, int money, ItemStack[] mainInventory, ItemStack[] armorSlots) {
        this.profileName = profileName;
        this.money = money;
        this.playerUUID = playerUUID;
        this.mainInventory = mainInventory;
        this.armorSlots = armorSlots;
    }

    /**
     * Function which checks whether a player has enough money to purchase an item.
     *
     * @param cost the price of an item, an Integer
     * @return true if the player has enough money, false if the player does not.
     */
    public boolean canAfford(int cost) {
        return cost <= money;
    }
}
