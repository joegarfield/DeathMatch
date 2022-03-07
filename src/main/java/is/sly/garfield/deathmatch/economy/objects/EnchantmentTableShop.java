package is.sly.garfield.deathmatch.economy.objects;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;

@Getter
@Setter
public class EnchantmentTableShop {

    private LinkedHashMap<EnchantmentGroup, Integer> enchantmentGroupPrice;

    public EnchantmentTableShop() {
        this.enchantmentGroupPrice = new LinkedHashMap<>();
    }

}
