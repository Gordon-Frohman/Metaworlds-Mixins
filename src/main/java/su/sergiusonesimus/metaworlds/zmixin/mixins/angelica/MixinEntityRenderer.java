package su.sergiusonesimus.metaworlds.zmixin.mixins.angelica;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.Frustrum;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.gtnewhorizons.angelica.compat.mojang.Camera;
import com.gtnewhorizons.angelica.rendering.RenderingState;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalDoubleRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;

import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.client.entity.EntityClientPlayerMPSubWorldProxy;
import su.sergiusonesimus.metaworlds.integrations.AngelicaIntegration;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.IMixinEntity;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    @Shadow(remap = true)
    private Minecraft mc;

    // TODO

    @Shadow(remap = true)
    private void setupCameraTransform(float p_78479_1_, int p_78479_2_) {}

    @Inject(
        method = "renderWorld",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/RenderGlobal;clipRenderersByFrustum(Lnet/minecraft/client/renderer/culling/ICamera;F)V",
            shift = Shift.AFTER),
        locals = LocalCapture.CAPTURE_FAILHARD)
    public void clipRenderersForSubworlds(float partialTicks, long totalTime, CallbackInfo ci,
        @Local(name = "entitylivingbase") EntityLivingBase entitylivingbase, @Local(name = "d0") double interpolatedX,
        @Local(name = "d1") double interpolatedY, @Local(name = "d2") double interpolatedZ,
        @Share("interpolatedX") LocalDoubleRef sharedInterpolatedX,
        @Share("interpolatedY") LocalDoubleRef sharedInterpolatedY,
        @Share("interpolatedZ") LocalDoubleRef sharedInterpolatedZ,
        @Share("partialTicks") LocalFloatRef sharedPartialTicks) {
        sharedInterpolatedX.set(interpolatedX);
        sharedInterpolatedY.set(interpolatedY);
        sharedInterpolatedZ.set(interpolatedZ);
        sharedPartialTicks.set(partialTicks);
        if (entitylivingbase instanceof EntityPlayer player) {
            for (World world : ((IMixinWorld) mc.theWorld).getSubWorlds()) {
                EntityClientPlayerMPSubWorldProxy proxy = (EntityClientPlayerMPSubWorldProxy) ((IMixinEntity) player)
                    .getProxyPlayer(world);

                Frustrum frustrum = new Frustrum();
                Vec3 localPos = ((IMixinWorld) world).transformToLocal(interpolatedX, interpolatedY, interpolatedZ);
                frustrum.setPosition(localPos.xCoord, localPos.yCoord, localPos.zCoord);

                proxy.mc.renderGlobal.clipRenderersByFrustum(frustrum, partialTicks);
            }
        }
    }

    @WrapOperation(
        method = "renderWorld",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/RenderGlobal;sortAndRender(Lnet/minecraft/entity/EntityLivingBase;ID)I"))
    public int sortAndRenderForSubworlds(RenderGlobal instance, EntityLivingBase entitylivingbase, int pass,
        double partialTicks, Operation<Integer> original, @Share("interpolatedX") LocalDoubleRef interpolatedX,
        @Share("interpolatedY") LocalDoubleRef interpolatedY, @Share("interpolatedZ") LocalDoubleRef interpolatedZ,
        @Share("partialTicks") LocalFloatRef sharedPartialTicks) {
        AngelicaIntegration.currentWorld = mc.theWorld;
        int result = original.call(instance, entitylivingbase, pass, partialTicks);
        if (entitylivingbase instanceof EntityPlayer player) {
            for (World world : ((IMixinWorld) mc.theWorld).getSubWorlds()) {
                EntityClientPlayerMPSubWorldProxy proxy = (EntityClientPlayerMPSubWorldProxy) ((IMixinEntity) player)
                    .getProxyPlayer(world);
                SubWorld subworld = (SubWorld) world;

                GL11.glPushMatrix();

                GL11.glTranslated(subworld.getTranslationX(), subworld.getTranslationY(), subworld.getTranslationZ());

                GL11.glTranslated(subworld.getCenterX(), subworld.getCenterY(), subworld.getCenterZ());
                GL11.glTranslated(-interpolatedX.get(), -interpolatedY.get(), -interpolatedZ.get());

                GL11.glRotated(subworld.getRotationYaw() % 360D, 0.0D, 1.0D, 0.0D);
                GL11.glRotated(subworld.getRotationPitch() % 360D, 0.0D, 0.0D, 1.0D);
                GL11.glRotated(subworld.getRotationRoll() % 360D, 1.0D, 0.0D, 0.0D);

                double scale = subworld.getScaling();
                GL11.glScaled(scale, scale, scale);

                GL11.glTranslated(-subworld.getCenterX(), -subworld.getCenterY(), -subworld.getCenterZ());
                GL11.glTranslated(interpolatedX.get(), interpolatedY.get(), interpolatedZ.get());

                ActiveRenderInfo.updateRenderInfo(player, mc.gameSettings.thirdPersonView == 2);
                Camera.INSTANCE.update(player, sharedPartialTicks.get());
                RenderingState.INSTANCE.setCameraPosition(
                    Camera.INSTANCE.getEntityPos().x,
                    Camera.INSTANCE.getEntityPos().y,
                    Camera.INSTANCE.getEntityPos().z);

                AngelicaIntegration.currentWorld = world;
                original.call(proxy.mc.renderGlobal, player, pass, partialTicks);

                GL11.glPopMatrix();
            }

            ActiveRenderInfo.updateRenderInfo((EntityPlayer) entitylivingbase, mc.gameSettings.thirdPersonView == 2);
            Camera.INSTANCE.update(entitylivingbase, sharedPartialTicks.get());
            RenderingState.INSTANCE.setCameraPosition(
                Camera.INSTANCE.getEntityPos().x,
                Camera.INSTANCE.getEntityPos().y,
                Camera.INSTANCE.getEntityPos().z);
        }

        return result;
    }

}
