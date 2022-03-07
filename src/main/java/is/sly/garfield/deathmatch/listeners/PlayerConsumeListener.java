package is.sly.garfield.deathmatch.listeners;

import is.sly.garfield.deathmatch.DeathMatch;
import net.minecraft.server.v1_12_R1.MobEffect;
import net.minecraft.server.v1_12_R1.PotionRegistry;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_12_R1.potion.CraftPotionUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Collection;

public class PlayerConsumeListener implements Listener {

    private final DeathMatch deathMatch;

    public PlayerConsumeListener(DeathMatch deathMatch) {
        this.deathMatch = deathMatch;
    }

    /**
     * Event handler that listens to player right click interactions when holding consumable items to disable these
     * interactions in the lobby world.
     * In the lobby world, consume potion to give the potion's effects instantly.
     *
     * @param e - the PlayerInteract event
     */
    @EventHandler
    public void onItemConsume(PlayerInteractEvent e) {
        if (e.getPlayer().getWorld().equals(deathMatch.getLobbyLocation().getWorld()) && e.getItem() != null && e.getHand() == EquipmentSlot.HAND && (e.getItem().getType() == Material.MILK_BUCKET || e.getItem().getType() == Material.TNT || e.getItem().getType() == Material.POTION || e.getItem().getType() == Material.SPLASH_POTION || e.getItem().getType() == Material.ENDER_PEARL || e.getItem().getType() == Material.FLINT_AND_STEEL || e.getItem().getType() == Material.BOW || e.getItem().getType() == Material.WATER_BUCKET || e.getItem().getType() == Material.BUCKET || e.getItem().getType() == Material.LAVA_BUCKET || e.getItem().getType() == Material.MELON || e.getItem().getType() == Material.BREAD || e.getItem().getType() == Material.COOKED_BEEF)) {
            e.setCancelled(true);
            return;
        }
        if (e.getItem() != null && e.getHand() == EquipmentSlot.HAND && e.getItem().getType() == Material.POTION && (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            final Collection<PotionEffect> effects = getBasePotionEffect(e.getItem());

            if (effects != null) {
                for (PotionEffect pe : effects) {
                    e.getPlayer().removePotionEffect(pe.getType());
                }
                e.getPlayer().addPotionEffects(effects);
            }
            e.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        }
    }

    /**
     * Function to get a potion's effects from NBT tags using NMS.
     *
     * @param potion the potion to get the effects form, an ItemStack.
     * @return a Collection containing the potion's effects.
     */
    private Collection<PotionEffect> getBasePotionEffect(ItemStack potion) {
        final net.minecraft.server.v1_12_R1.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(potion);
        if (!nmsItemStack.hasTag()) return null;
        String potionName = nmsItemStack.getTag().getString("Potion");
        if (potionName == null) return null;
        final String[] split = potionName.split(":");
        if (split.length == 2) {
            potionName = split[1];
        }
        final PotionRegistry reg = PotionRegistry.a(potionName);
        if (reg == null) {
            return null;
        }
        final Collection<PotionEffect> effects = new ArrayList<>();
        for (final MobEffect me : reg.a()) {
            effects.add(CraftPotionUtil.toBukkit(me));
        }
        return effects;
    }
}
