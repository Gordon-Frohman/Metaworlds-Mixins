package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.util.IMixinAxisAlignedBB;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {

    @Shadow(remap = true)
    private Minecraft mc;

    // getMouseOver

    private Entity storedEntity;

    @ModifyVariable(method = "getMouseOver", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private Entity storeEntity(Entity original) {
        storedEntity = original;
        return original;
    }

    @ModifyVariable(method = "getMouseOver", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private AxisAlignedBB transformAABB(AxisAlignedBB original) {
        return ((IMixinAxisAlignedBB) original).getTransformedToGlobalBoundingBox(storedEntity.worldObj);
    }

    @WrapOperation(
        method = "getMouseOver",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/AxisAlignedBB;calculateIntercept(Lnet/minecraft/util/Vec3;Lnet/minecraft/util/Vec3;)Lnet/minecraft/util/MovingObjectPosition;"))
    private MovingObjectPosition calculateInterceptLocal(AxisAlignedBB instance, Vec3 vec3, Vec3 vec32,
        Operation<MovingObjectPosition> original) {
        return ((IMixinAxisAlignedBB) instance).calculateIntercept(vec3, vec32, storedEntity.worldObj);
    }

    // renderWorld

    @Inject(
        method = "renderWorld",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/EntityRenderer;getMouseOver(F)V"))
    private void injectRenderWorld(float p_78471_1_, long p_78471_2_, CallbackInfo ci) {
        for (World curWorld : ((IMixinWorld) this.mc.theWorld).getSubWorlds())
            ((IMixinWorld) curWorld).doTickPartial(p_78471_1_);
    }

}
