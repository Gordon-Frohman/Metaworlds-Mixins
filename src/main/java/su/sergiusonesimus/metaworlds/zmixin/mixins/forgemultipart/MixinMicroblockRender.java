package su.sergiusonesimus.metaworlds.zmixin.mixins.forgemultipart;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import codechicken.microblock.CommonMicroClass;
import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.integrations.ForgeMultipartIntegration;
import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.metaworlds.util.Direction.AxisDirection;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.util.IMixinMovingObjectPosition;

@Mixin(codechicken.microblock.MicroblockRender$.class)
public class MixinMicroblockRender {

    Vec3 dVec;

    @Inject(method = "renderHighlight", remap = false, at = @At(value = "HEAD"))
    public void storeMOP(final EntityPlayer player, final MovingObjectPosition hit, final CommonMicroClass mcrClass,
        final int size, final int material, CallbackInfo ci) {
        ForgeMultipartIntegration.currentMOP = hit;
    }

    @WrapOperation(
        method = "renderHighlight",
        remap = false,
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glTranslated(DDD)V", ordinal = 0))
    public void glTranslated1(double x, double y, double z, Operation<Void> original) {
        if (((IMixinMovingObjectPosition) ForgeMultipartIntegration.currentMOP)
            .getWorld() instanceof SubWorld subworld) {
            Vec3 globalCenter = subworld.transformToGlobal(
                ForgeMultipartIntegration.currentMOP.blockX + 0.5,
                ForgeMultipartIntegration.currentMOP.blockY + 0.5,
                ForgeMultipartIntegration.currentMOP.blockZ + 0.5);
            dVec = Vec3.createVectorHelper(-0.5D, -0.5D, -0.5D);
            Direction dir = Direction.from3DDataValue(ForgeMultipartIntegration.currentMOP.sideHit);
            double d = dir.getAxisDirection() == AxisDirection.POSITIVE ? 1.0D : -1.0D;
            switch (dir.getAxis()) {
                case X:
                    dVec.xCoord += d;
                    break;
                case Y:
                    dVec.yCoord += d;
                    break;
                case Z:
                    dVec.zCoord += d;
                    break;
            }
            GL11.glTranslated(globalCenter.xCoord, globalCenter.yCoord, globalCenter.zCoord);
            GL11.glRotated(subworld.getRotationYaw() % 360D, 0.0D, 1.0D, 0.0D);
            GL11.glRotated(subworld.getRotationRoll() % 360D, 1.0D, 0.0D, 0.0D);
            GL11.glRotated(subworld.getRotationPitch() % 360D, 0.0D, 0.0D, 1.0D);
        } else {
            original.call(x, y, z);
        }
    }

    @WrapOperation(
        method = "renderHighlight",
        remap = false,
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glTranslated(DDD)V", ordinal = 1))
    public void glTranslated2(double x, double y, double z, Operation<Void> original) {
        if (((IMixinMovingObjectPosition) ForgeMultipartIntegration.currentMOP)
            .getWorld() instanceof SubWorld subworld) {
            original.call(dVec.xCoord, dVec.yCoord, dVec.zCoord);
        } else {
            original.call(x, y, z);
        }
    }

}
