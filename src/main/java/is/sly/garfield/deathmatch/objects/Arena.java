package is.sly.garfield.deathmatch.objects;

import lombok.Getter;

@Getter
public class Arena {
    private final String worldName;
    private final String displayName;
    private final double spawnX;
    private final double spawnY;
    private final double spawnZ;

    public Arena(String displayName, String worldName, double spawnX, double spawnY, double spawnZ) {
        this.displayName = displayName;
        this.worldName = worldName;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.spawnZ = spawnZ;
    }
}
