package is.sly.garfield.deathmatch.economy.objects;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

import java.util.List;

@Getter
@Setter
public class BrewingStandShop {

    private Location brewingStandLocation;
    private List<String> brewingStandContents;

    public BrewingStandShop(Location brewingStandLocation, List<String> brewingStandContents) {
        this.brewingStandLocation = brewingStandLocation;
        this.brewingStandContents = brewingStandContents;
    }
}
