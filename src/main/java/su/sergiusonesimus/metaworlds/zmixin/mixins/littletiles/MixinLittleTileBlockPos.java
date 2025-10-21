package su.sergiusonesimus.metaworlds.zmixin.mixins.littletiles;

import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.creativemd.littletiles.common.utils.LittleTileBlockPos;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import su.sergiusonesimus.metaworlds.zmixin.interfaces.littletiles.IMixinLittleTileBlockPos;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.util.IMixinMovingObjectPosition;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

@Mixin(LittleTileBlockPos.class)
public class MixinLittleTileBlockPos implements IMixinLittleTileBlockPos {

    private static Vec3 localVec;

    private World worldObj;

    @Inject(method = "fromMovingObjectPosition", remap = false, at = @At(value = "HEAD"))
    private static void storeVariables(MovingObjectPosition pos, CallbackInfoReturnable<LittleTileBlockPos> cir) {
        localVec = ((IMixinWorld) ((IMixinMovingObjectPosition) pos).getWorld()).transformToLocal(pos.hitVec);
    }

    @WrapOperation(
        method = "fromMovingObjectPosition",
        remap = false,
        at = @At(value = "FIELD", target = "Lnet/minecraft/util/Vec3;xCoord:D"))
    private static double getLocalX(Vec3 instance, Operation<Double> original) {
        return localVec.xCoord;
    }

    @WrapOperation(
        method = "fromMovingObjectPosition",
        remap = false,
        at = @At(value = "FIELD", target = "Lnet/minecraft/util/Vec3;yCoord:D"))
    private static double getLocalY(Vec3 instance, Operation<Double> original) {
        return localVec.yCoord;
    }

    @WrapOperation(
        method = "fromMovingObjectPosition",
        remap = false,
        at = @At(value = "FIELD", target = "Lnet/minecraft/util/Vec3;zCoord:D"))
    private static double getLocalZ(Vec3 instance, Operation<Double> original) {
        return localVec.zCoord;
    }

    @Inject(method = "fromMovingObjectPosition", remap = false, at = @At(value = "RETURN"), cancellable = true)
    private static void setWorldObj(MovingObjectPosition pos, CallbackInfoReturnable<LittleTileBlockPos> cir) {
        cir.setReturnValue(
            ((IMixinLittleTileBlockPos) cir.getReturnValue()).setWorld(((IMixinMovingObjectPosition) pos).getWorld()));
    }

    @Override
    public LittleTileBlockPos setWorld(World world) {
        this.worldObj = world;
        return (LittleTileBlockPos) (Object) this;
    }

    @Override
    public World getWorld() {
        return worldObj;
    }

}
