package su.sergiusonesimus.metaworlds.zmixin.mixins.angelica;

import java.lang.reflect.Field;
import java.util.List;

import net.coderbot.iris.Iris;
import net.coderbot.iris.layer.GbufferPrograms;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.coderbot.iris.uniforms.EntityIdHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.embeddedt.embeddium.impl.render.viewport.Viewport;
import org.embeddedt.embeddium.impl.render.viewport.ViewportProvider;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.gtnewhorizons.angelica.render.SelectionBoxRenderer;
import com.gtnewhorizons.angelica.rendering.celeritas.CeleritasWorldRenderer;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.client.entity.EntityClientPlayerMPSubWorldProxy;
import su.sergiusonesimus.metaworlds.integrations.AngelicaIntegration;
import su.sergiusonesimus.metaworlds.util.OrientedBB;
import su.sergiusonesimus.metaworlds.zmixin.MixinPriorities;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.angelica.IMixinViewport;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.IMixinEntity;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.util.IMixinAxisAlignedBB;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

@Mixin(value = RenderGlobal.class, priority = MixinPriorities.ANGELICA)
public class MixinRenderGlobal {

    @Shadow(remap = true)
    private WorldClient theWorld;

    @Shadow(remap = true)
    public Minecraft mc;

    @Shadow(remap = true)
    public boolean displayListEntitiesDirty;

    @Shadow(remap = true)
    public int displayListEntities;

    @Shadow(remap = true)
    public int countEntitiesTotal;

    @Shadow(remap = false)
    private CeleritasWorldRenderer celeritas$renderer;

    @Shadow(remap = false)
    private static int[] sodium$entityItemCount;

    @Shadow(remap = false)
    private static int sodium$itemRenderDist;

    // TODO

    @Shadow(remap = true)
    public void rebuildDisplayListEntities() {}

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

    @Inject(
        method = "renderEntities",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/profiler/Profiler;startSection(Ljava/lang/String;)V",
            ordinal = 0,
            shift = Shift.BEFORE),
        locals = LocalCapture.CAPTURE_FAILHARD)
    private void injectRenderEntities(EntityLivingBase player, ICamera camera, float partialTicks, CallbackInfo ci,
        @Local(name = "pass") int pass, @Local(name = "d0") double globalX, @Local(name = "d1") double globalY,
        @Local(name = "d2") double globalZ) {
        for (World curWorld : ((IMixinWorld) this.theWorld).getSubWorlds()) {
            WorldClient curClientWorld = (WorldClient) curWorld;
            Vec3 transformedPos = ((IMixinWorld) curWorld).transformToLocal(globalX, globalY, globalZ);
            this.theWorld.theProfiler.startSection("prepare");
            TileEntityRendererDispatcher.instance.cacheActiveRenderInfo(
                curClientWorld,
                this.mc.getTextureManager(),
                this.mc.fontRenderer,
                this.mc.renderViewEntity,
                partialTicks);
            RenderManager.instance.cacheActiveRenderInfo(
                curClientWorld,
                this.mc.getTextureManager(),
                this.mc.fontRenderer,
                this.mc.renderViewEntity,
                this.mc.pointedEntity,
                this.mc.gameSettings,
                partialTicks);

            EntityLivingBase entitylivingbase1 = this.mc.renderViewEntity;
            double interpolatedX = entitylivingbase1.lastTickPosX
                + (entitylivingbase1.posX - entitylivingbase1.lastTickPosX) * (double) partialTicks;
            double interpolatedY = entitylivingbase1.lastTickPosY
                + (entitylivingbase1.posY - entitylivingbase1.lastTickPosY) * (double) partialTicks;
            double interpolatedZ = entitylivingbase1.lastTickPosZ
                + (entitylivingbase1.posZ - entitylivingbase1.lastTickPosZ) * (double) partialTicks;
            TileEntityRendererDispatcher.staticPlayerX = interpolatedX;
            TileEntityRendererDispatcher.staticPlayerY = interpolatedY;
            TileEntityRendererDispatcher.staticPlayerZ = interpolatedZ;
            this.theWorld.theProfiler.endStartSection("staticentities");

            if (this.displayListEntitiesDirty) {
                RenderManager.renderPosX = 0.0D;
                RenderManager.renderPosY = 0.0D;
                RenderManager.renderPosZ = 0.0D;
                this.rebuildDisplayListEntities();
            }

            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glPushMatrix();
            GL11.glTranslated(-interpolatedX, -interpolatedY, -interpolatedZ);
            GL11.glCallList(this.displayListEntities);
            GL11.glPopMatrix();
            RenderManager.renderPosX = interpolatedX;
            RenderManager.renderPosY = interpolatedY;
            RenderManager.renderPosZ = interpolatedZ;
            this.mc.entityRenderer.enableLightmap((double) partialTicks);
            this.theWorld.theProfiler.endStartSection("global");
            List<Entity> list = curWorld.getLoadedEntityList();
            if (pass == 0) // no indentation for smaller patch size
            {
                this.countEntitiesTotal = list.size();
            }
            int i;
            Entity entity;

            for (i = 0; i < curWorld.weatherEffects.size(); ++i) {
                entity = (Entity) curWorld.weatherEffects.get(i);
                if (!entity.shouldRenderInPass(pass)) continue;
                if (entity.isInRangeToRender3d(transformedPos.xCoord, transformedPos.yCoord, transformedPos.zCoord)
                    && this.celeritas$renderer.isEntityVisible(entity)) {
                    int entityId = EntityIdHelper.getEntityId(entity);
                    CapturedRenderingState.INSTANCE.setCurrentEntity(entityId);
                    GbufferPrograms.beginEntities();

                    try {
                        RenderManager.instance.renderEntitySimple(entity, partialTicks);
                    } finally {
                        CapturedRenderingState.INSTANCE.setCurrentEntity(-1);
                        GbufferPrograms.endEntities();
                    }
                }
            }

            this.theWorld.theProfiler.endStartSection("entities");

            for (i = 0; i < list.size(); ++i) {
                entity = (Entity) list.get(i);
                if (!entity.shouldRenderInPass(pass)) continue;
                boolean flag = entity
                    .isInRangeToRender3d(transformedPos.xCoord, transformedPos.yCoord, transformedPos.zCoord)
                    && this.celeritas$renderer.isEntityVisible(entity)
                    && (entity.ignoreFrustumCheck || camera.isBoundingBoxInFrustum(
                        ((IMixinAxisAlignedBB) entity.boundingBox).getTransformedToGlobalBoundingBox(entity.worldObj))
                        || entity.riddenByEntity == this.mc.thePlayer);
                if (flag && entity instanceof EntityItem) {
                    int j = Math.min(
                        (int) (entity.getDistanceSq(transformedPos.xCoord, transformedPos.yCoord, transformedPos.zCoord)
                            / 4.0D),
                        255);
                    sodium$entityItemCount[j]++;
                    flag = j <= sodium$itemRenderDist;
                }

                if (!flag && entity instanceof EntityLiving) {
                    EntityLiving entityliving = (EntityLiving) entity;

                    if (entityliving.getLeashed() && entityliving.getLeashedToEntity() != null) {
                        Entity entity1 = entityliving.getLeashedToEntity();
                        flag = camera.isBoundingBoxInFrustum(
                            ((IMixinAxisAlignedBB) entity1.boundingBox)
                                .getTransformedToGlobalBoundingBox(entity.worldObj));
                    }
                }

                if (flag
                    && (entity != this.mc.renderViewEntity || this.mc.gameSettings.thirdPersonView != 0
                        || this.mc.renderViewEntity.isPlayerSleeping())
                    && curWorld
                        .blockExists(MathHelper.floor_double(entity.posX), 0, MathHelper.floor_double(entity.posZ))) {
                    int entityId = EntityIdHelper.getEntityId(entity);
                    CapturedRenderingState.INSTANCE.setCurrentEntity(entityId);
                    GbufferPrograms.beginEntities();

                    try {
                        RenderManager.instance.renderEntitySimple(entity, partialTicks);
                    } finally {
                        CapturedRenderingState.INSTANCE.setCurrentEntity(-1);
                        GbufferPrograms.endEntities();
                    }
                }
            }

            this.theWorld.theProfiler.endStartSection("blockentities");
            RenderHelper.enableStandardItemLighting();

            if (Iris.enabled) {
                GbufferPrograms.beginBlockEntities();
                GbufferPrograms.setBlockEntityDefaults();
            }

            RenderGlobal rgProxy = ((EntityClientPlayerMPSubWorldProxy) ((IMixinEntity) Minecraft
                .getMinecraft().thePlayer).getProxyPlayer(curWorld)).mc.renderGlobal;
            try {
                Field celeritas$renderer = RenderGlobal.class.getDeclaredField("celeritas$renderer");
                celeritas$renderer.setAccessible(true);
                CeleritasWorldRenderer cwr = (CeleritasWorldRenderer) celeritas$renderer.get(rgProxy);
                cwr.renderBlockEntities(partialTicks);
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }

            if (Iris.enabled) {
                GbufferPrograms.endBlockEntities();
            }

            this.mc.entityRenderer.disableLightmap((double) partialTicks);
            this.theWorld.theProfiler.endSection();
        }
    }

    /**
     * 
     * @author Sergius Onesimus
     * @reason Angelica's optimization broke OBBs render. Using modified vanilla renderer for it
     */
    @WrapOperation(
        method = "drawOutlinedBoundingBox",
        remap = true,
        at = @At(
            value = "INVOKE",
            target = "Lcom/gtnewhorizons/angelica/render/SelectionBoxRenderer;draw(Lnet/minecraft/util/AxisAlignedBB;I)V",
            remap = false))
    private static void drawOrientedBoundingBox(AxisAlignedBB aabb, int color,
        Operation<SelectionBoxRenderer> original) {
        if (aabb instanceof OrientedBB obb) {
            Tessellator tessellator = Tessellator.instance;
            tessellator.startDrawing(3);

            if (color != -1) {
                tessellator.setColorOpaque_I(color);
            }

            tessellator.addVertex(obb.getX(0), obb.getY(0), obb.getZ(0));
            tessellator.addVertex(obb.getX(1), obb.getY(1), obb.getZ(1));
            tessellator.addVertex(obb.getX(3), obb.getY(3), obb.getZ(3));
            tessellator.addVertex(obb.getX(2), obb.getY(2), obb.getZ(2));
            tessellator.addVertex(obb.getX(0), obb.getY(0), obb.getZ(0));
            tessellator.draw();
            tessellator.startDrawing(3);

            if (color != -1) {
                tessellator.setColorOpaque_I(color);
            }

            tessellator.addVertex(obb.getX(4), obb.getY(4), obb.getZ(4));
            tessellator.addVertex(obb.getX(5), obb.getY(5), obb.getZ(5));
            tessellator.addVertex(obb.getX(7), obb.getY(7), obb.getZ(7));
            tessellator.addVertex(obb.getX(6), obb.getY(6), obb.getZ(6));
            tessellator.addVertex(obb.getX(4), obb.getY(4), obb.getZ(4));
            tessellator.draw();
            tessellator.startDrawing(1);

            if (color != -1) {
                tessellator.setColorOpaque_I(color);
            }

            tessellator.addVertex(obb.getX(0), obb.getY(0), obb.getZ(0));
            tessellator.addVertex(obb.getX(4), obb.getY(4), obb.getZ(4));
            tessellator.addVertex(obb.getX(1), obb.getY(1), obb.getZ(1));
            tessellator.addVertex(obb.getX(5), obb.getY(5), obb.getZ(5));
            tessellator.addVertex(obb.getX(3), obb.getY(3), obb.getZ(3));
            tessellator.addVertex(obb.getX(7), obb.getY(7), obb.getZ(7));
            tessellator.addVertex(obb.getX(2), obb.getY(2), obb.getZ(2));
            tessellator.addVertex(obb.getX(6), obb.getY(6), obb.getZ(6));
            tessellator.draw();
        } else original.call(aabb, color);
    }

    @WrapOperation(
        method = "sortAndRender",
        remap = true,
        at = @At(value = "FIELD", target = "Lnet/coderbot/iris/Iris;enabled:Z", remap = false))
    public boolean disableIrisForSubworld(Operation<Boolean> original) {
        return original.call() && !(AngelicaIntegration.currentWorld instanceof SubWorld);
    }

}
