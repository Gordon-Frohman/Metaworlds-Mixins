package su.sergiusonesimus.metaworlds.world;

import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;

public class WorldProviderSubWorld extends WorldProvider {

    public World m_parentWorld;

    WorldProviderSubWorld(World parentWorld) {
        this.m_parentWorld = parentWorld;
    }

    public String getDimensionName() {
        return "SubWorld";
    }
}
