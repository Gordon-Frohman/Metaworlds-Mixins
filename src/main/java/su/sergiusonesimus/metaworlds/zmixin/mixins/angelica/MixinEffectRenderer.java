package su.sergiusonesimus.metaworlds.zmixin.mixins.angelica;

import java.lang.reflect.Field;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.gtnewhorizons.angelica.rendering.celeritas.CeleritasWorldRenderer;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import su.sergiusonesimus.metaworlds.zmixin.MixinPriorities;

@Mixin(value = EffectRenderer.class, priority = MixinPriorities.ANGELICA)
public class MixinEffectRenderer {

    @Shadow(remap = true)
    protected World worldObj;

    @WrapOperation(
        method = "setupCullingViewport",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lcom/gtnewhorizons/angelica/rendering/celeritas/CeleritasWorldRenderer;getInstance()Lcom/gtnewhorizons/angelica/rendering/celeritas/CeleritasWorldRenderer;",
            remap = false))
    private CeleritasWorldRenderer getActualRenderer(Operation<CeleritasWorldRenderer> original) {
        try {
            Field celeritas$renderer = RenderGlobal.class.getDeclaredField("celeritas$renderer");
            celeritas$renderer.setAccessible(true);
            return (CeleritasWorldRenderer) celeritas$renderer.get(((WorldClient) this.worldObj).mc.renderGlobal);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            return original.call();
        }
    }

}
