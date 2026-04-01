package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.client.renderer.entity;

import net.minecraft.client.renderer.entity.RenderMinecart;
import net.minecraft.entity.item.EntityMinecart;

import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

@Mixin(RenderMinecart.class)
public class MixinRenderMinecart extends MixinRender {

    // doRender

    @Inject(method = "doRender", at = @At(value = "HEAD"))
    private void shareEntity(EntityMinecart entity, double x, double y, double z, float rotationYaw, float rotatioRoll,
        CallbackInfo ci, @Share("entity") LocalRef<EntityMinecart> sharedEntity) {
        sharedEntity.set(entity);
    }

    @ModifyVariable(method = "doRender", at = @At(value = "STORE", opcode = Opcodes.FSTORE, ordinal = 0), ordinal = 0)
    private float modifyRotation(float original, @Share("entity") LocalRef<EntityMinecart> sharedEntity) {
        return original - (float) ((IMixinWorld) sharedEntity.get().worldObj).getRotationYaw() % 360;
    }

}
