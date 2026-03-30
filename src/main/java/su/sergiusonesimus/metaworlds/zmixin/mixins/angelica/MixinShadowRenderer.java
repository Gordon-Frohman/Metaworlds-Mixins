package su.sergiusonesimus.metaworlds.zmixin.mixins.angelica;

import java.util.ArrayList;
import java.util.List;

import net.coderbot.iris.pipeline.ShadowRenderer;
import net.coderbot.iris.shadows.frustum.FrustumHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.gtnewhorizons.angelica.compat.mojang.Camera;
import com.gtnewhorizons.angelica.glsm.GLStateManager;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.client.entity.EntityClientPlayerMPSubWorldProxy;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.IMixinEntity;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.tileentity.IMixinTileEntity;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

@Mixin(ShadowRenderer.class)
public class MixinShadowRenderer {

    @Shadow(remap = false)
    private FrustumHolder terrainFrustumHolder;

    @Shadow(remap = false)
    public static Matrix4f MODELVIEW;

    @Inject(
        method = "renderShadows",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/RenderGlobal;clipRenderersByFrustum(Lnet/minecraft/client/renderer/culling/ICamera;F)V",
            remap = true,
            shift = Shift.AFTER),
        locals = LocalCapture.CAPTURE_FAILHARD)
    public void clipRenderersForSubworlds(EntityRenderer levelRenderer, Camera playerCamera, CallbackInfo ci,
        @Local(name = "mc") Minecraft mc, @Share("playerCamera") LocalRef<Camera> sharedPlayerCamera) {
        sharedPlayerCamera.set(playerCamera);
        Vector3d cameraPos = playerCamera.getPos();
        EntityPlayer player = mc.thePlayer;
        for (World world : ((IMixinWorld) mc.theWorld).getSubWorlds()) {
            EntityClientPlayerMPSubWorldProxy proxy = (EntityClientPlayerMPSubWorldProxy) ((IMixinEntity) player)
                .getProxyPlayer(world);

            Vec3 localPos = ((IMixinWorld) world).transformToLocal(cameraPos.x, cameraPos.y, cameraPos.z);
            terrainFrustumHolder.getFrustum()
                .setPosition(localPos.xCoord, localPos.yCoord, localPos.zCoord);

            proxy.mc.renderGlobal
                .clipRenderersByFrustum(terrainFrustumHolder.getFrustum(), playerCamera.getPartialTicks());
        }

        terrainFrustumHolder.getFrustum()
            .setPosition(cameraPos.x, cameraPos.y, cameraPos.z);
    }

    @WrapOperation(
        method = "renderShadows",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/RenderGlobal;sortAndRender(Lnet/minecraft/entity/EntityLivingBase;ID)I",
            remap = true))
    public int sortAndRenderForSubworlds(RenderGlobal instance, EntityLivingBase entitylivingbase, int pass,
        double partialTicks, Operation<Integer> original, @Share("playerCamera") LocalRef<Camera> playerCamera) {
        int result = original.call(instance, entitylivingbase, pass, partialTicks);
        if (entitylivingbase instanceof EntityPlayer player) {
            Minecraft mc = Minecraft.getMinecraft();
            Vector3d cameraPos = playerCamera.get()
                .getPos();
            Matrix4f modelView = new Matrix4f(MODELVIEW);
            for (World world : ((IMixinWorld) mc.theWorld).getSubWorlds()) {
                EntityClientPlayerMPSubWorldProxy proxy = (EntityClientPlayerMPSubWorldProxy) ((IMixinEntity) mc.thePlayer)
                    .getProxyPlayer(world);
                SubWorld subworld = (SubWorld) world;

                MODELVIEW.translate(
                    (float) subworld.getTranslationX(),
                    (float) subworld.getTranslationY(),
                    (float) subworld.getTranslationZ());

                MODELVIEW.translate(
                    (float) subworld.getCenterX(),
                    (float) subworld.getCenterY(),
                    (float) subworld.getCenterZ());
                MODELVIEW.translate((float) -cameraPos.x, (float) -cameraPos.y, (float) -cameraPos.z);

                MODELVIEW.rotate((float) (subworld.getRotationYaw() % 360D / 180D * Math.PI), 0.0F, 1.0F, 0.0F);
                MODELVIEW.rotate((float) (subworld.getRotationPitch() % 360D / 180D * Math.PI), 0.0F, 0.0F, 1.0F);
                MODELVIEW.rotate((float) (subworld.getRotationRoll() % 360D / 180D * Math.PI), 1.0F, 0.0F, 0.0F);
                MODELVIEW.scale((float) subworld.getScaling());

                MODELVIEW.translate(
                    (float) -subworld.getCenterX(),
                    (float) -subworld.getCenterY(),
                    (float) -subworld.getCenterZ());
                MODELVIEW.translate((float) cameraPos.x, (float) cameraPos.y, (float) cameraPos.z);

                original.call(proxy.mc.renderGlobal, player, pass, partialTicks);

                MODELVIEW.set(modelView);
            }
        }

        return result;
    }

    @WrapOperation(
        method = "renderEntities",
        remap = false,
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/multiplayer/WorldClient;loadedEntityList:Ljava/util/List;",
            remap = true))
    public List<Entity> getAllEntities(WorldClient instance, Operation<List<Entity>> original) {
        List<Entity> result = new ArrayList<Entity>();

        result.addAll(original.call(instance));
        for (World currentWorld : ((IMixinWorld) instance).getSubWorlds()) {
            for (Entity entity : original.call(currentWorld)) {
                if (entity instanceof EntityPlayer) continue;
                result.add(entity);
            }
        }

        return result;
    }

    // renderTileEntity

    @WrapOperation(
        method = "renderTileEntity",
        remap = false,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntity;getDistanceFrom(DDD)D"))
    private double redirectGetDistanceFromGlobal(TileEntity instance, double x, double y, double z,
        Operation<Double> original) {
        return ((IMixinTileEntity) instance).getDistanceFromGlobal(x, y, z);
    }

    @Inject(
        method = "renderTileEntity",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lcom/gtnewhorizons/angelica/glsm/GLStateManager;glColor4f(FFFF)V",
            remap = false,
            shift = Shift.AFTER))
    private void injectRenderTileEntity1(TileEntity tile, double cameraX, double cameraY, double cameraZ,
        float partialTicks, CallbackInfo ci) {
        GLStateManager.glPushMatrix();
        if (tile.hasWorldObj() && ((IMixinWorld) tile.getWorldObj()).isSubWorld()) {
            GLStateManager.glTranslated(
                -TileEntityRendererDispatcher.staticPlayerX,
                -TileEntityRendererDispatcher.staticPlayerY,
                -TileEntityRendererDispatcher.staticPlayerZ);

            SubWorld parentSubWorld = (SubWorld) tile.getWorldObj();
            GLStateManager.glMultMatrix(parentSubWorld.getTransformToGlobalMatrixDirectBuffer());

            GLStateManager.glTranslated(
                TileEntityRendererDispatcher.staticPlayerX,
                TileEntityRendererDispatcher.staticPlayerY,
                TileEntityRendererDispatcher.staticPlayerZ);
        }
    }

    @Inject(
        method = "renderTileEntity",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher;renderTileEntityAt(Lnet/minecraft/tileentity/TileEntity;DDDF)V",
            remap = true,
            shift = Shift.AFTER))
    private void injectRenderTileEntity2(TileEntity tile, double cameraX, double cameraY, double cameraZ,
        float partialTicks, CallbackInfo ci) {
        GLStateManager.glPopMatrix();
    }

}
