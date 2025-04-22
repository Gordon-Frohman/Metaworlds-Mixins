package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.client.renderer;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import su.sergiusonesimus.metaworlds.zmixin.interfaces.entity.IMixinEntity;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.util.IMixinAxisAlignedBB;

@Mixin(value = WorldRenderer.class)
public abstract class MixinWorldRenderer {

    @Shadow(remap = true)
    public int posXPlus;

    @Shadow(remap = true)
    public int posYPlus;

    @Shadow(remap = true)
    public int posZPlus;

    @Shadow(remap = true)
    public World worldObj;

    @Shadow(remap = true)
    private boolean isInitialized;

    @Inject(method = "distanceToEntitySquared", at = @At(value = "HEAD"), cancellable = true)
    private void injectDistanceToEntitySquared(Entity par1Entity, CallbackInfoReturnable<Float> ci) {
        ci.setReturnValue(
            (float) ((IMixinEntity) par1Entity).getLocalPos(this.worldObj)
                .squareDistanceTo((double) this.posXPlus, (double) this.posYPlus, (double) this.posZPlus));
        ci.cancel();
    }

    @WrapOperation(
        method = "updateInFrustum",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/renderer/WorldRenderer;rendererBoundingBox:Lnet/minecraft/util/AxisAlignedBB;"))
    private AxisAlignedBB wrapRendererBoundingBox(WorldRenderer instance, Operation<AxisAlignedBB> original) {
        return ((IMixinAxisAlignedBB) original.call(instance)).getTransformedToGlobalBoundingBox(this.worldObj);
    }

    public boolean isInitialized() {
        return this.isInitialized;
    }

}
