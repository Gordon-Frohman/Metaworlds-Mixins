package net.tclproject.metaworlds.mixins.util;

import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.tclproject.metaworlds.api.SubWorld;
import net.tclproject.metaworlds.patcher.OBBPool;
import net.tclproject.metaworlds.patcher.OrientedBB;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AxisAlignedBB.class)
public abstract class MixinAxisAlignedBB implements IMixinAxisAlignedBB {

    @Shadow(remap = true)
    public double minX;

    @Shadow(remap = true)
    public double maxX;

    @Shadow(remap = true)
    private double minY;

    @Shadow(remap = true)
    private double maxY;

    @Shadow(remap = true)
    private double minZ;

    @Shadow(remap = true)
    private double maxZ;

    @Shadow(remap = true)
    public abstract boolean isVecInXY(Vec3 vec);

    @Shadow(remap = true)
    public abstract boolean isVecInXZ(Vec3 vec);

    @Shadow(remap = true)
    public abstract boolean isVecInYZ(Vec3 vec);

    /**
     * Returns whether the given bounding box intersects with this one. Args: axisAlignedBB
     */
    @Overwrite
    public boolean intersectsWith(AxisAlignedBB other) {
        if (other instanceof OrientedBB) return ((OrientedBB) other).intersectsWith((AxisAlignedBB) (Object) this);
        return other.maxX > this.minX && other.minX < this.maxX
            ? (other.maxY > this.minY && other.minY < this.maxY ? other.maxZ > this.minZ && other.minZ < this.maxZ
                : false)
            : false;
    }

    @Overwrite
    public MovingObjectPosition calculateIntercept(Vec3 p_72327_1_, Vec3 p_72327_2_) {
        return calculateIntercept(p_72327_1_, p_72327_2_, null);
    }

    public AxisAlignedBB getTransformedToGlobalBoundingBox(World transformerWorld) {
        return (AxisAlignedBB) (transformerWorld instanceof SubWorld ? OBBPool.createOBB((AxisAlignedBB) (Object) this)
            .transformBoundingBoxToGlobal(transformerWorld) : (AxisAlignedBB) (Object) this);
    }

    public AxisAlignedBB getTransformedToLocalBoundingBox(World transformerWorld) {
        return (AxisAlignedBB) (transformerWorld instanceof SubWorld ? OBBPool.createOBB((AxisAlignedBB) (Object) this)
            .transformBoundingBoxToLocal(transformerWorld) : (AxisAlignedBB) (Object) this);
    }

    public OrientedBB rotateYaw(double targetYaw) {
        return OBBPool.createOBB((AxisAlignedBB) (Object) this)
            .rotateYaw(targetYaw);
    }

    public OrientedBB getOrientedBB() {
        return OBBPool.createOBB((AxisAlignedBB) (Object) this);
    }

    public MovingObjectPosition calculateIntercept(Vec3 p_72327_1_, Vec3 p_72327_2_, World parWorldObj) {
        Vec3 vec32 = p_72327_1_.getIntermediateWithXValue(p_72327_2_, this.minX);
        Vec3 vec33 = p_72327_1_.getIntermediateWithXValue(p_72327_2_, this.maxX);
        Vec3 vec34 = p_72327_1_.getIntermediateWithYValue(p_72327_2_, this.minY);
        Vec3 vec35 = p_72327_1_.getIntermediateWithYValue(p_72327_2_, this.maxY);
        Vec3 vec36 = p_72327_1_.getIntermediateWithZValue(p_72327_2_, this.minZ);
        Vec3 vec37 = p_72327_1_.getIntermediateWithZValue(p_72327_2_, this.maxZ);

        if (!this.isVecInYZ(vec32)) {
            vec32 = null;
        }

        if (!this.isVecInYZ(vec33)) {
            vec33 = null;
        }

        if (!this.isVecInXZ(vec34)) {
            vec34 = null;
        }

        if (!this.isVecInXZ(vec35)) {
            vec35 = null;
        }

        if (!this.isVecInXY(vec36)) {
            vec36 = null;
        }

        if (!this.isVecInXY(vec37)) {
            vec37 = null;
        }

        Vec3 vec38 = null;

        if (vec32 != null
            && (vec38 == null || p_72327_1_.squareDistanceTo(vec32) < p_72327_1_.squareDistanceTo(vec38))) {
            vec38 = vec32;
        }

        if (vec33 != null
            && (vec38 == null || p_72327_1_.squareDistanceTo(vec33) < p_72327_1_.squareDistanceTo(vec38))) {
            vec38 = vec33;
        }

        if (vec34 != null
            && (vec38 == null || p_72327_1_.squareDistanceTo(vec34) < p_72327_1_.squareDistanceTo(vec38))) {
            vec38 = vec34;
        }

        if (vec35 != null
            && (vec38 == null || p_72327_1_.squareDistanceTo(vec35) < p_72327_1_.squareDistanceTo(vec38))) {
            vec38 = vec35;
        }

        if (vec36 != null
            && (vec38 == null || p_72327_1_.squareDistanceTo(vec36) < p_72327_1_.squareDistanceTo(vec38))) {
            vec38 = vec36;
        }

        if (vec37 != null
            && (vec38 == null || p_72327_1_.squareDistanceTo(vec37) < p_72327_1_.squareDistanceTo(vec38))) {
            vec38 = vec37;
        }

        if (vec38 == null) {
            return null;
        } else {
            byte b0 = -1;

            if (vec38 == vec32) {
                b0 = 4;
            }

            if (vec38 == vec33) {
                b0 = 5;
            }

            if (vec38 == vec34) {
                b0 = 0;
            }

            if (vec38 == vec35) {
                b0 = 1;
            }

            if (vec38 == vec36) {
                b0 = 2;
            }

            if (vec38 == vec37) {
                b0 = 3;
            }

            return (MovingObjectPosition) (Object) (((IMixinMovingObjectPosition) new MovingObjectPosition(
                0,
                0,
                0,
                b0,
                vec38)).setWorld(parWorldObj));
        }
    }

}
