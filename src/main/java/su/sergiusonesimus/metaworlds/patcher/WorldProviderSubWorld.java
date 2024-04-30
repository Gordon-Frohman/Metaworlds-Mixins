package su.sergiusonesimus.metaworlds.patcher;

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
