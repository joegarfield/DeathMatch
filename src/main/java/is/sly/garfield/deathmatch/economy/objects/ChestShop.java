package is.sly.garfield.deathmatch.economy.objects;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

import java.util.List;

@Getter
@Setter
public class ChestShop {

    private Location chestLocation;
    private String chestName;
    private List<String> chestContents;

    public ChestShop(Location chestLocation, String chestName, List<String> chestContents) {
        this.chestLocation = chestLocation;
        this.chestName = chestName;
        this.chestContents = chestContents;
    }
}
