package su.sergiusonesimus.metaworlds.zmixin.mixins.angelica;

import java.lang.reflect.Field;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.world.World;

import org.embeddedt.embeddium.impl.render.viewport.Viewport;
import org.embeddedt.embeddium.impl.render.viewport.ViewportProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gtnewhorizons.angelica.rendering.celeritas.CeleritasWorldRenderer;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import su.sergiusonesimus.metaworlds.client.entity.EntityClientPlayerMPSubWorldProxy;
import su.sergiusonesimus.metaworlds.zmixin.MixinPriorities;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.angelica.IMixinViewport;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.IMixinEntity;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

@Mixin(value = RenderGlobal.class, priority = MixinPriorities.ANGELICA)
public class MixinRenderGlobal {

    @Shadow(remap = true)
    private WorldClient theWorld;

    @Inject(method = "markBlocksForUpdate", at = @At(value = "TAIL"))
    public void markBlocksForUpdate(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, CallbackInfo ci) {
        markBlocksForUpdateSubworlds(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public void markBlocksForUpdateSubworlds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        for (World curWorld : ((IMixinWorld) this.theWorld).getSubWorlds()) {
            RenderGlobal rgProxy = ((EntityClientPlayerMPSubWorldProxy) ((IMixinEntity) Minecraft
                .getMinecraft().thePlayer).getProxyPlayer(curWorld)).mc.renderGlobal;
            try {
                Field celeritas$renderer = RenderGlobal.class.getDeclaredField("celeritas$renderer");
                celeritas$renderer.setAccessible(true);
                CeleritasWorldRenderer cwr = (CeleritasWorldRenderer) celeritas$renderer.get(rgProxy);
                cwr.scheduleRebuildForBlockArea(minX, minY, minZ, maxX, maxY, maxZ, false);
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Inject(method = "markBlockForUpdate", at = @At(value = "TAIL"))
    public void markBlockForUpdate(int x, int y, int z, CallbackInfo ci) {
        this.markBlocksForUpdateSubworlds(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1);
    }

    @Inject(method = "markBlockForRenderUpdate", at = @At(value = "TAIL"))
    public void markBlockForRenderUpdate(int x, int y, int z, CallbackInfo ci) {
        this.markBlocksForUpdateSubworlds(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1);
    }

    @Inject(method = "markBlockRangeForRenderUpdate", at = @At(value = "TAIL"))
    public void markBlockRangeForRenderUpdate(int minX, int minY, int minZ, int maxX, int maxY, int maxZ,
        CallbackInfo ci) {
        this.markBlocksForUpdateSubworlds(minX - 1, minY - 1, minZ - 1, maxX + 1, maxY + 1, maxZ + 1);
    }

    @WrapOperation(
        method = "clipRenderersByFrustum",
        remap = true,
        at = @At(
            value = "INVOKE",
            target = "Lorg/embeddedt/embeddium/impl/render/viewport/ViewportProvider;sodium$createViewport()Lorg/embeddedt/embeddium/impl/render/viewport/Viewport;",
            remap = false))
    private Viewport setRenderWorld(ViewportProvider instance, Operation<Viewport> original) {
        return ((IMixinViewport) (Object) original.call(instance)).setWorld(theWorld);
    }

}
