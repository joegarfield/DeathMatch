package is.sly.garfield.deathmatch.economy.objects;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.enchantments.Enchantment;

import java.util.HashMap;
import java.util.Objects;

@Getter
@Setter
public class EnchantmentGroup {

    HashMap<Enchantment, Integer> enchantToLevel;

    public EnchantmentGroup() {
        this.enchantToLevel = new HashMap<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnchantmentGroup that = (EnchantmentGroup) o;
        return Objects.equals(enchantToLevel, that.enchantToLevel);
    }

    // Function needed to be able to use it a key in a Map
    @Override
    public int hashCode() {
        return Objects.hash(enchantToLevel);
    }
}
