package net.tclproject.metaworlds.mixins.util;

import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.tclproject.metaworlds.patcher.OrientedBB;

public interface IMixinAxisAlignedBB {

    public MovingObjectPosition calculateIntercept(Vec3 p_72327_1_, Vec3 p_72327_2_, World parWorldObj);

    public AxisAlignedBB getTransformedToGlobalBoundingBox(World transformerWorld);

    public AxisAlignedBB getTransformedToLocalBoundingBox(World transformerWorld);

    public OrientedBB rotateYaw(double targetYaw);

    public OrientedBB getOrientedBB();
}
