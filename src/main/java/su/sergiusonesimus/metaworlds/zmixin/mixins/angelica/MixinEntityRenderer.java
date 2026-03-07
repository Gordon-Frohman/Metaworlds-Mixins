package su.sergiusonesimus.metaworlds.zmixin.mixins.angelica;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.Frustrum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import su.sergiusonesimus.metaworlds.client.entity.EntityClientPlayerMPSubWorldProxy;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.IMixinEntity;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    private Map<Integer, Frustrum> subworldFrustums = new HashMap<Integer, Frustrum>();

    @Inject(
        method = "renderWorld",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/RenderGlobal;clipRenderersByFrustum(Lnet/minecraft/client/renderer/culling/ICamera;F)V",
            shift = Shift.AFTER),
        locals = LocalCapture.CAPTURE_FAILHARD)
    public void clipRenderersForSubworlds(float partialTicks, long totalTime, CallbackInfo ci, @Local(name = "j") int j,
        @Local(name = "entitylivingbase") EntityLivingBase entitylivingbase, @Local(name = "d0") double d0,
        @Local(name = "d1") double d1, @Local(name = "d2") double d2) {
        if (entitylivingbase instanceof EntityPlayer player) {
            for (World subworld : ((IMixinWorld) Minecraft.getMinecraft().theWorld).getSubWorlds()) {
                EntityClientPlayerMPSubWorldProxy proxy = (EntityClientPlayerMPSubWorldProxy) ((IMixinEntity) player)
                    .getProxyPlayer(subworld);
                Minecraft mc = proxy.mc;

                Frustrum frustrum = new Frustrum();
                Vec3 localPos = ((IMixinWorld) subworld).transformToLocal(d0, d1, d2);
                frustrum.setPosition(localPos.xCoord, localPos.yCoord, localPos.zCoord);
                subworldFrustums.put(((IMixinWorld) subworld).getSubWorldID(), frustrum);

                mc.renderGlobal.clipRenderersByFrustum(frustrum, partialTicks);

                if (j == 0) {
                    mc.mcProfiler.endStartSection("updatechunks");

                    while (!mc.renderGlobal.updateRenderers(proxy, false) && totalTime != 0L) {
                        long k = totalTime - System.nanoTime();

                        if (k < 0L || k > 1000000000L) {
                            break;
                        }
                    }
                }
            }
        }
    }

    @Inject(
        method = "renderWorld",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/RenderGlobal;sortAndRender(Lnet/minecraft/entity/EntityLivingBase;ID)I",
            shift = Shift.AFTER),
        locals = LocalCapture.CAPTURE_FAILHARD)
    public void clipRenderersForSubworlds(float partialTicks, long totalTime, CallbackInfo ci,
        @Local(name = "entitylivingbase") EntityLivingBase entitylivingbase) {
        if (entitylivingbase instanceof EntityPlayer player) {
            for (World subworld : ((IMixinWorld) Minecraft.getMinecraft().theWorld).getSubWorlds()) {
                EntityClientPlayerMPSubWorldProxy proxy = (EntityClientPlayerMPSubWorldProxy) ((IMixinEntity) player)
                    .getProxyPlayer(subworld);
                proxy.mc.renderGlobal.sortAndRender(proxy, 0, (double) partialTicks);
            }
        }
    }

    @WrapOperation(
        method = "renderWorld",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/RenderGlobal;sortAndRender(Lnet/minecraft/entity/EntityLivingBase;ID)I"))
    public int sortAndRenderForSubworlds(RenderGlobal instance, EntityLivingBase entitylivingbase, int pass,
        double partialTicks, Operation<Integer> original) {
        int result = original.call(instance, entitylivingbase, pass, partialTicks);
        if (entitylivingbase instanceof EntityPlayer player) {
            for (World subworld : ((IMixinWorld) Minecraft.getMinecraft().theWorld).getSubWorlds()) {
                EntityClientPlayerMPSubWorldProxy proxy = (EntityClientPlayerMPSubWorldProxy) ((IMixinEntity) player)
                    .getProxyPlayer(subworld);
                original.call(proxy.mc.renderGlobal, proxy, pass, partialTicks);
            }
        }
        return result;
    }

    @WrapOperation(
        method = "renderWorld",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/RenderGlobal;renderEntities(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/client/renderer/culling/ICamera;F)V"))
    public void renderEntitiesForSubworlds(RenderGlobal instance, EntityLivingBase entitylivingbase, ICamera frustum,
        float partialTicks, Operation<Void> original) {
        original.call(instance, entitylivingbase, frustum, partialTicks);
        if (entitylivingbase instanceof EntityPlayer player) {
            for (World subworld : ((IMixinWorld) Minecraft.getMinecraft().theWorld).getSubWorlds()) {
                EntityClientPlayerMPSubWorldProxy proxy = (EntityClientPlayerMPSubWorldProxy) ((IMixinEntity) player)
                    .getProxyPlayer(subworld);
                original.call(
                    proxy.mc.renderGlobal,
                    proxy,
                    subworldFrustums.get(((IMixinWorld) subworld).getSubWorldID()),
                    partialTicks);
            }
        }
    }

}
