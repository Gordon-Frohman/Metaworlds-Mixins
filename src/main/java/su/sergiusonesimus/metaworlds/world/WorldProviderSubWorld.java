package su.sergiusonesimus.metaworlds.world;

import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class WorldProviderSubWorld extends WorldProvider {

    public World m_parentWorld;

    public WorldProviderSubWorld(World parentWorld) {
        this.m_parentWorld = parentWorld;
    }

    public String getDimensionName() {
        return "SubWorld";
    }

    @SideOnly(Side.CLIENT)
    public float getSunBrightness(float par1) {
        return m_parentWorld.getSunBrightness(par1);
    }

    public float calculateCelestialAngle(long p_76563_1_, float p_76563_3_) {
        return m_parentWorld.provider.calculateCelestialAngle(p_76563_1_, p_76563_3_);
    }
}
