package su.sergiusonesimus.metaworlds.patcher;

import net.minecraft.world.World;

public interface SubWorldFactory {

    World CreateSubWorld(World var1, int var2);
}
