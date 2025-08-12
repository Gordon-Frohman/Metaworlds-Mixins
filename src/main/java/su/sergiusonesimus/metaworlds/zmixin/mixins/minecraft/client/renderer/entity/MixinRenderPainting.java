package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.client.renderer.entity;

import net.minecraft.client.renderer.entity.RenderPainting;
import net.minecraft.entity.item.EntityPainting;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

@Mixin(RenderPainting.class)
public class MixinRenderPainting extends MixinRender {

    // doRender

    @Inject(
        method = "doRender(Lnet/minecraft/entity/item/EntityPainting;DDDFF)V",
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glEnable(I)V", remap = false))
    private void rotatePainting(EntityPainting entity, double x, double y, double z, float rotationYaw,
        float rotationRoll, CallbackInfo ci) {
        float xAngle = 0;
        float zAngle = 0;
        switch (entity.hangingDirection) {
            default:
                break;
            case 0:
                xAngle = -(float) ((IMixinWorld) entity.worldObj).getRotationRoll() % 360;
                zAngle = -(float) ((IMixinWorld) entity.worldObj).getRotationPitch() % 360;
                break;
            case 1:
                xAngle = (float) ((IMixinWorld) entity.worldObj).getRotationPitch() % 360;
                zAngle = (float) ((IMixinWorld) entity.worldObj).getRotationRoll() % 360;
                break;
            case 2:
                xAngle = (float) ((IMixinWorld) entity.worldObj).getRotationRoll() % 360;
                zAngle = (float) ((IMixinWorld) entity.worldObj).getRotationPitch() % 360;
                break;
            case 3:
                xAngle = -(float) ((IMixinWorld) entity.worldObj).getRotationPitch() % 360;
                zAngle = -(float) ((IMixinWorld) entity.worldObj).getRotationRoll() % 360;
                break;
        }
        GL11.glRotatef(xAngle, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(zAngle, 0.0F, 0.0F, 1.0F);
    }

}
