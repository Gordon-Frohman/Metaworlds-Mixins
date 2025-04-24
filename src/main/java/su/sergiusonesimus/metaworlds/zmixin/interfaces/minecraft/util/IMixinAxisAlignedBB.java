package su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.util;

import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import su.sergiusonesimus.metaworlds.util.OrientedBB;

public interface IMixinAxisAlignedBB {

    public MovingObjectPosition calculateIntercept(Vec3 p_72327_1_, Vec3 p_72327_2_, World parWorldObj);

    public AxisAlignedBB getTransformedToGlobalBoundingBox(World transformerWorld);

    public AxisAlignedBB getTransformedToLocalBoundingBox(World transformerWorld);

    public OrientedBB rotateYaw(double targetYaw);

    public OrientedBB rotateYaw(double targetYaw, double centerX, double centerZ);

    public OrientedBB getOrientedBB();
}
