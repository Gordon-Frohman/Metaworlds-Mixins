package su.sergiusonesimus.metaworlds.zmixin.mixins.warpdrive;

import net.minecraft.util.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import cr0s.warpdrive.render.EntityCamera;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;
import su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.entity.MixinEntityLivingBase;

@Mixin(EntityCamera.class)
public abstract class MixinEntityCamera extends MixinEntityLivingBase {

    @WrapOperation(
        method = "onEntityUpdate",
        at = @At(value = "INVOKE", target = "Lcr0s/warpdrive/render/EntityCamera;setPosition(DDD)V"))
    public void setPositionGlobal(EntityCamera camera, double x, double y, double z, Operation<Void> original) {
        Vec3 globalPos = ((IMixinWorld) this.worldObj).transformToGlobal(x, y, z);
        original.call(camera, globalPos.xCoord, globalPos.yCoord, globalPos.zCoord);
    }

}
