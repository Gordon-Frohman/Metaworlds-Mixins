package su.sergiusonesimus.metaworlds.zmixin.mixins.angelica;

import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.entity.Entity;

import org.embeddedt.embeddium.impl.render.viewport.Viewport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.zmixin.MixinPriorities;

@Mixin(value = EffectRenderer.class, priority = MixinPriorities.ANGELICA)
public class MixinEffectRenderer {

    @Shadow(remap = false)
    private Viewport cullingViewport;

    private Viewport cullingViewportToStore;

    @Inject(
        method = { "renderParticles", "renderLitParticles" },
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/particle/EntityFX;renderParticle(Lnet/minecraft/client/renderer/Tessellator;FFFFFF)V",
            shift = Shift.BEFORE))
    private void disableCulling(Entity player, float partialTickTime, CallbackInfo ci,
        @Local(name = "entityfx") EntityFX entityfx) {
        if (cullingViewport != null && entityfx.worldObj instanceof SubWorld) {
            cullingViewportToStore = cullingViewport;
            cullingViewport = null;
        }
    }

    @Inject(
        method = { "renderParticles", "renderLitParticles" },
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/particle/EntityFX;renderParticle(Lnet/minecraft/client/renderer/Tessellator;FFFFFF)V",
            shift = Shift.AFTER))
    private void enableCulling(Entity player, float partialTickTime, CallbackInfo ci,
        @Local(name = "entityfx") EntityFX entityfx) {
        if (cullingViewportToStore != null && entityfx.worldObj instanceof SubWorld) {
            cullingViewport = cullingViewportToStore;
            cullingViewportToStore = null;
        }
    }

}
