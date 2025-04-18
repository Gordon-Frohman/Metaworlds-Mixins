package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.client.renderer.entity;

import net.minecraft.client.renderer.entity.RenderMinecart;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;

import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

@Mixin(RenderMinecart.class)
public class MixinRenderMinecart extends MixinRender {

    // doRender

    private Entity storedEntity;

    @Inject(method = "doRender", at = @At(value = "HEAD"))
    private void storeEntity(EntityMinecart entity, double x, double y, double z, float rotationYaw, float rotatioRoll,
        CallbackInfo ci) {
        storedEntity = entity;
    }

    @ModifyVariable(method = "doRender", at = @At(value = "STORE", opcode = Opcodes.FSTORE, ordinal = 0), ordinal = 0)
    private float modifyRotation(float original) {
        return original - (float) ((IMixinWorld) storedEntity.worldObj).getRotationYaw() % 360;
    }

}
