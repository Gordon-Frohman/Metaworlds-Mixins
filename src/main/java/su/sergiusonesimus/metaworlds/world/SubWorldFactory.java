package su.sergiusonesimus.metaworlds.world;

import net.minecraft.world.World;

public interface SubWorldFactory {

    World CreateSubWorld(World var1, int var2);
}
