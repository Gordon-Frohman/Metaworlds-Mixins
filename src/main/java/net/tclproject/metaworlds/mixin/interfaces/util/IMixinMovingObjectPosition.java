package net.tclproject.metaworlds.mixin.interfaces.util;

import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public interface IMixinMovingObjectPosition {

    public MovingObjectPosition setWorld(World world);

    public World getWorld();

}
