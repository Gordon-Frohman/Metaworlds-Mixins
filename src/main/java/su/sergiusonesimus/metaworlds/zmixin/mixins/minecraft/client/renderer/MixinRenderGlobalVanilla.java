package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.client.renderer;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.EntitySorter;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderList;
import net.minecraft.client.renderer.RenderSorter;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.util.RenderDistanceSorter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.lwjgl.opengl.ARBOcclusionQuery;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import com.llamalad7.mixinextras.sugar.Local;

import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.client.multiplayer.SubWorldClient;
import su.sergiusonesimus.metaworlds.util.OrientedBB;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.client.renderer.IMixinRenderGlobalVanilla;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.client.renderer.IMixinRenderList;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.util.IMixinAxisAlignedBB;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

@Mixin(RenderGlobal.class)
public class MixinRenderGlobalVanilla implements IMixinRenderGlobalVanilla {

    private List<WorldRenderer> worldRenderersList = new ArrayList<WorldRenderer>();

    private List<WorldRenderer> sortedWorldRenderersList = new ArrayList<WorldRenderer>();

    private Map<Integer, WorldRenderer> worldRenderersMap = new HashMap<Integer, WorldRenderer>();

    @Shadow(remap = true)
    public Map<Integer, DestroyBlockProgress> damagedBlocks;

    @Shadow(remap = true)
    private Minecraft mc;

    @Shadow(remap = true)
    private WorldClient theWorld;

    @Shadow(remap = true)
    private boolean occlusionEnabled;

    @Shadow(remap = true)
    private IntBuffer glOcclusionQueryBase;

    @Shadow(remap = true)
    private List<WorldRenderer> worldRenderersToUpdate;

    @Shadow(remap = true)
    private int renderChunksWide;

    @Shadow(remap = true)
    private int renderChunksTall;

    @Shadow(remap = true)
    private int renderChunksDeep;

    @Shadow(remap = true)
    private List<TileEntity> tileEntities;

    @Shadow(remap = true)
    private int glRenderListBase;

    @Shadow(remap = true)
    private int renderDistanceChunks;

    @Shadow(remap = true)
    private int minBlockX;

    @Shadow(remap = true)
    private int minBlockY;

    @Shadow(remap = true)
    private int minBlockZ;

    @Shadow(remap = true)
    private int maxBlockX;

    @Shadow(remap = true)
    private int maxBlockY;

    @Shadow(remap = true)
    private int maxBlockZ;

    @Shadow(remap = true)
    private int countEntitiesTotal;

    @Shadow(remap = true)
    private boolean displayListEntitiesDirty;

    @Shadow(remap = true)
    private int displayListEntities;

    @Shadow(remap = true)
    private int worldRenderersCheckIndex;

    @Shadow(remap = true)
    private double prevSortX;

    @Shadow(remap = true)
    private double prevSortY;

    @Shadow(remap = true)
    private double prevSortZ;

    @Shadow(remap = true)
    private int prevChunkSortX;

    @Shadow(remap = true)
    private int prevChunkSortY;

    @Shadow(remap = true)
    private int prevChunkSortZ;

    @Shadow(remap = true)
    private double prevRenderSortX;

    @Shadow(remap = true)
    private double prevRenderSortY;

    @Shadow(remap = true)
    private double prevRenderSortZ;

    @Shadow(remap = true)
    public IntBuffer occlusionResult;

    @Shadow(remap = true)
    private int cloudTickCounter;

    @Shadow(remap = true)
    public List<WorldRenderer> glRenderLists;

    @Shadow(remap = true)
    public RenderList[] allRenderLists;

    @Shadow(remap = true)
    private int frustumCheckOffset;

    @Shadow(remap = true)
    public RenderBlocks renderBlocksRg;

    @Shadow(remap = true)
    public IIcon[] destroyBlockIcons;

    @Shadow(remap = true)
    protected void renderAllRenderLists(int par3, double par4) {}

    @Shadow(remap = true)
    protected void onStaticEntitiesChanged() {}

    @Shadow(remap = true)
    protected void rebuildDisplayListEntities() {}

    public List<WorldRenderer> getSortedWorldRenderersList() {
        return this.sortedWorldRenderersList;
    }

    public void setSortedWorldRenderersList(List<WorldRenderer> sortedWorldRenderersList) {
        this.sortedWorldRenderersList = sortedWorldRenderersList;
    }

    public Map<Integer, WorldRenderer> getWorldRenderersMap() {
        return this.worldRenderersMap;
    }

    public void setWorldRenderersMap(Map<Integer, WorldRenderer> worldRenderersMap) {
        this.worldRenderersMap = worldRenderersMap;
    }

    public List<WorldRenderer> getWorldRenderersList() {
        return this.worldRenderersList;
    }

    public void setWorldRenderersList(List<WorldRenderer> worldRenderersList) {
        this.worldRenderersList = worldRenderersList;
    }

    public WorldRenderer createWorldRenderer(World targetWorld, int chunkIndexX, int chunkIndexY, int chunkIndexZ) {
        if (targetWorld != this.theWorld) {
            return ((IMixinRenderGlobalVanilla) ((SubWorldClient) targetWorld).getRenderGlobal())
                .createWorldRenderer(targetWorld, chunkIndexX, chunkIndexY, chunkIndexZ);
        }
        int curLocalRendererIndex = (chunkIndexZ * this.renderChunksTall + chunkIndexY) * this.renderChunksWide
            + chunkIndexX;
        int curRendererIndex = ((((IMixinWorld) targetWorld).getSubWorldID() * this.renderChunksDeep + chunkIndexZ)
            * this.renderChunksTall + chunkIndexY) * this.renderChunksWide + chunkIndexX;

        WorldRenderer curRenderer = new WorldRenderer(
            targetWorld,
            this.tileEntities,
            chunkIndexX * 16,
            chunkIndexY * 16,
            chunkIndexZ * 16,
            this.glRenderListBase + curLocalRendererIndex * 3);
        this.worldRenderersMap.put(curRendererIndex, curRenderer);
        this.worldRenderersList.add(curRenderer);
        if (this.occlusionEnabled) {
            curRenderer.glOcclusionQuery = this.glOcclusionQueryBase.get(curLocalRendererIndex);
        }
        curRenderer.isWaitingOnOcclusionQuery = false;
        curRenderer.isVisible = true;
        curRenderer.isInFrustum = true;
        curRenderer.chunkIndex = curRendererIndex;
        curRenderer.markDirty();
        this.sortedWorldRenderersList.add(curRenderer);
        this.worldRenderersToUpdate.add(curRenderer);
        return curRenderer;
    }

    public void removeWorldRenderer(World targetWorld, int chunkIndexX, int chunkIndexY, int chunkIndexZ) {
        int curRendererIndex = ((((IMixinWorld) targetWorld).getSubWorldID() * this.renderChunksDeep + chunkIndexZ)
            * this.renderChunksTall + chunkIndexY) * this.renderChunksWide + chunkIndexX;
        WorldRenderer curRenderer = this.worldRenderersMap.remove(curRendererIndex);
        if (curRenderer != null) {
            this.worldRenderersList.remove(curRenderer);
            this.worldRenderersToUpdate.remove(curRenderer);
            this.sortedWorldRenderersList.remove(curRenderer);
        }
    }

    /**
     * Loads all the renderers and sets up the basic settings usage
     * 
     * @author Sergius Onesimus
     * @reason Too complex to be modified without Overwrite
     */
    @SuppressWarnings("unchecked")
    @Overwrite
    public void loadRenderers() {
        if (this.theWorld != null) {
            Blocks.leaves.setGraphicsLevel(this.mc.gameSettings.fancyGraphics);
            Blocks.leaves2.setGraphicsLevel(this.mc.gameSettings.fancyGraphics);
            this.renderDistanceChunks = this.mc.gameSettings.renderDistanceChunks;

            for (Map.Entry<Integer, WorldRenderer> curRendererEntry : this.worldRenderersMap.entrySet()) {
                curRendererEntry.getValue()
                    .stopRendering();
            }

            int i;
            i = this.renderDistanceChunks * 2 + 1;
            this.renderChunksWide = i;
            this.renderChunksTall = 16;
            this.renderChunksDeep = i;

            this.worldRenderersMap.clear();
            this.worldRenderersList.clear();
            this.sortedWorldRenderersList.clear();

            this.minBlockX = 0;
            this.minBlockY = 0;
            this.minBlockZ = 0;
            this.maxBlockX = this.renderChunksWide;
            this.maxBlockY = this.renderChunksTall;
            this.maxBlockZ = this.renderChunksDeep;
            int l;

            for (l = 0; l < this.worldRenderersToUpdate.size(); ++l) {
                ((WorldRenderer) this.worldRenderersToUpdate.get(l)).needsUpdate = false;
            }

            this.worldRenderersToUpdate.clear();
            this.tileEntities.clear();
            this.onStaticEntitiesChanged();

            for (World curWorld : ((IMixinWorld) this.theWorld).getWorlds()) {
                for (l = 0; l < this.renderChunksWide; ++l) {
                    for (int i1 = 0; i1 < this.renderChunksTall; ++i1) {

                        for (int j1 = 0; j1 < this.renderChunksDeep; ++j1) {

                            if (((IMixinWorld) curWorld).isSubWorld()) {
                                SubWorld curSubWorld = (SubWorld) curWorld;
                                if (curSubWorld.getMaxX() < l * 16 || curSubWorld.getMinX() > (l + 1) * 16
                                    || curSubWorld.getMaxY() < i1 * 16
                                    || curSubWorld.getMinY() > (i1 + 1) * 16
                                    || curSubWorld.getMaxZ() < j1 * 16
                                    || curSubWorld.getMinZ() > (j1 + 1) * 16) {
                                    this.removeWorldRenderer(curWorld, l, i1, j1);
                                    continue;
                                }
                            }
                            this.createWorldRenderer(curWorld, l, i1, j1);
                        }
                    }
                }
            }

            if (this.theWorld != null) {
                EntityLivingBase entitylivingbase = this.mc.renderViewEntity;

                if (entitylivingbase != null) {
                    this.markRenderersForNewPosition(
                        entitylivingbase.posX,
                        entitylivingbase.posY,
                        entitylivingbase.posZ);
                    Collections.sort(this.sortedWorldRenderersList, new EntitySorter(entitylivingbase));
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void loadRenderersForNewSubWorld(int subWorldId) {
        if (this.theWorld != null) {
            int i;
            i = this.renderDistanceChunks * 2 + 1;

            this.renderChunksWide = i;
            this.renderChunksTall = 16;
            this.renderChunksDeep = i;
            this.minBlockX = 0;
            this.minBlockY = 0;
            this.minBlockZ = 0;
            this.maxBlockX = this.renderChunksWide;
            this.maxBlockY = this.renderChunksTall;

            this.maxBlockZ = this.renderChunksDeep;
            int var4;
            int curSubWorldID = subWorldId;
            World curWorld = ((IMixinWorld) this.theWorld).getSubWorld(curSubWorldID);

            for (var4 = 0; var4 < this.renderChunksWide; ++var4) {
                for (int var5 = 0; var5 < this.renderChunksTall; ++var5) {
                    for (int var6 = 0; var6 < this.renderChunksDeep; ++var6) {
                        if (curWorld instanceof SubWorld) {
                            SubWorld curSubWorld = (SubWorld) curWorld;
                            if (curSubWorld.getMaxX() < var4 * 16 || curSubWorld.getMinX() > (var4 + 1) * 16
                                || curSubWorld.getMaxY() < var5 * 16
                                || curSubWorld.getMinY() > (var5 + 1) * 16
                                || curSubWorld.getMaxZ() < var6 * 16
                                || curSubWorld.getMinZ() > (var6 + 1) * 16) {
                                this.removeWorldRenderer(curWorld, var4, var5, var6);
                                continue;
                            }
                        }
                        this.createWorldRenderer(curWorld, var4, var5, var6);
                    }
                }
            }

            if (this.theWorld != null) {
                EntityLivingBase entitylivingbase = this.mc.renderViewEntity;
                if (entitylivingbase != null) {
                    this.markRenderersForNewPositionSingle(
                        entitylivingbase.posX,
                        entitylivingbase.posY,
                        entitylivingbase.posZ,
                        curSubWorldID);
                    Collections.sort(this.sortedWorldRenderersList, new EntitySorter(entitylivingbase));

                }
            }
        }
    }

    public void unloadRenderersForSubWorld(int subWorldId) {
        Iterator<Map.Entry<Integer, WorldRenderer>> iter = this.worldRenderersMap.entrySet()
            .iterator();
        while (iter.hasNext()) {
            WorldRenderer curRenderer = iter.next()
                .getValue();
            if (((IMixinWorld) curRenderer.worldObj).getSubWorldID() == subWorldId) {
                iter.remove();
                this.worldRenderersList.remove(curRenderer);
                this.worldRenderersToUpdate.remove(curRenderer);
                this.sortedWorldRenderersList.remove(curRenderer);
            }
        }
    }

    @Inject(
        method = "renderEntities",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endSection()V", shift = Shift.AFTER),
        locals = LocalCapture.CAPTURE_FAILSOFT)
    private void injectRenderEntities(EntityLivingBase player, ICamera camera, float partialTicks, CallbackInfo ci,
        @Local(name = "pass") int pass, @Local(name = "d0") double globalX, @Local(name = "d1") double globalY,
        @Local(name = "d2") double globalZ, @Local(name = "d3") double interpolatedX,
        @Local(name = "d4") double interpolatedY, @Local(name = "d5") double interpolatedZ) {
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
            interpolatedX = entitylivingbase1.lastTickPosX
                + (entitylivingbase1.posX - entitylivingbase1.lastTickPosX) * (double) partialTicks;
            interpolatedY = entitylivingbase1.lastTickPosY
                + (entitylivingbase1.posY - entitylivingbase1.lastTickPosY) * (double) partialTicks;
            interpolatedZ = entitylivingbase1.lastTickPosZ
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
                if (entity.isInRangeToRender3d(transformedPos.xCoord, transformedPos.yCoord, transformedPos.zCoord)) {
                    RenderManager.instance.renderEntitySimple(entity, partialTicks);
                }
            }

            this.theWorld.theProfiler.endStartSection("entities");

            for (i = 0; i < list.size(); ++i) {
                entity = (Entity) list.get(i);
                if (!entity.shouldRenderInPass(pass)) continue;
                boolean flag = entity
                    .isInRangeToRender3d(transformedPos.xCoord, transformedPos.yCoord, transformedPos.zCoord)
                    && (entity.ignoreFrustumCheck || camera.isBoundingBoxInFrustum(
                        ((IMixinAxisAlignedBB) entity.boundingBox).getTransformedToGlobalBoundingBox(entity.worldObj))
                        || entity.riddenByEntity == this.mc.thePlayer);

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
                    RenderManager.instance.renderEntitySimple(entity, partialTicks);
                }
            }

            this.theWorld.theProfiler.endStartSection("blockentities");
            RenderHelper.enableStandardItemLighting();

            for (i = 0; i < curClientWorld.mc.renderGlobal.tileEntities.size(); ++i) {
                TileEntity tile = (TileEntity) curClientWorld.mc.renderGlobal.tileEntities.get(i);
                if (tile.shouldRenderInPass(pass) && camera.isBoundingBoxInFrustum(
                    tile.getWorldObj() == null ? tile.getRenderBoundingBox()
                        : ((IMixinAxisAlignedBB) tile.getRenderBoundingBox())
                            .getTransformedToGlobalBoundingBox(tile.getWorldObj()))) {
                    TileEntityRendererDispatcher.instance.renderTileEntity(tile, partialTicks);
                }
            }

            this.mc.entityRenderer.disableLightmap((double) partialTicks);
            this.theWorld.theProfiler.endSection();
        }
    }

    /**
     * Goes through all the renderers setting new positions on them and those that have their position changed are
     * adding to be updated
     */
    public void markRenderersForNewPosition(double par1, double par2, double par3) {
        for (World curWorld : ((IMixinWorld) this.theWorld).getWorlds()) {
            this.markRenderersForNewPositionSingle(par1, par2, par3, ((IMixinWorld) curWorld).getSubWorldID());
        }
    }

    public void markRenderersForNewPositionSubworlds(double par1, double par2, double par3) {
        for (World curWorld : ((IMixinWorld) this.theWorld).getSubWorlds()) {
            this.markRenderersForNewPositionSingle(par1, par2, par3, ((IMixinWorld) curWorld).getSubWorldID());
        }
    }

    @Inject(method = "markRenderersForNewPosition(III)V", at = @At(value = "HEAD"), cancellable = true)
    private void markRenderersForNewPosition(int par1, int par2, int par3, CallbackInfo ci) {
        markRenderersForNewPosition((double) par1, (double) par2, (double) par3);
        ci.cancel();
    }

    public void markRenderersForNewPositionSingle(double par1d, double par2d, double par3d, int subWorldID) {
        World curWorld = ((IMixinWorld) this.theWorld).getSubWorld(subWorldID);
        Vec3 tranformedVec = ((IMixinWorld) curWorld).transformToLocal((double) par1d, (double) par2d, (double) par3d);
        int par1 = MathHelper.floor_double(tranformedVec.xCoord);
        MathHelper.floor_double(tranformedVec.yCoord);
        int par3 = MathHelper.floor_double(tranformedVec.zCoord);
        par1 -= 8;
        par3 -= 8;
        this.minBlockX = Integer.MAX_VALUE;
        this.minBlockY = Integer.MAX_VALUE;
        this.minBlockZ = Integer.MAX_VALUE;
        this.maxBlockX = Integer.MIN_VALUE;
        this.maxBlockY = Integer.MIN_VALUE;
        this.maxBlockZ = Integer.MIN_VALUE;
        int l = this.renderChunksWide * 16;
        int i1 = l / 2;

        for (int j1 = 0; j1 < this.renderChunksWide; ++j1) {
            int k1 = j1 * 16;
            int l1 = k1 + i1 - par1;

            if (l1 < 0) {
                l1 -= l - 1;
            }

            l1 /= l;
            k1 -= l1 * l;

            if (k1 < this.minBlockX) {
                this.minBlockX = k1;
            }

            if (k1 > this.maxBlockX) {
                this.maxBlockX = k1;
            }

            for (int i2 = 0; i2 < this.renderChunksDeep; ++i2) {
                int j2 = i2 * 16;
                int k2 = j2 + i1 - par3;

                if (k2 < 0) {
                    k2 -= l - 1;
                }

                k2 /= l;
                j2 -= k2 * l;

                if (j2 < this.minBlockZ) {
                    this.minBlockZ = j2;
                }

                if (j2 > this.maxBlockZ) {
                    this.maxBlockZ = j2;
                }

                for (int l2 = 0; l2 < this.renderChunksTall; ++l2) {
                    int i3 = l2 * 16;

                    if (i3 < this.minBlockY) {
                        this.minBlockY = i3;
                    }

                    if (i3 > this.maxBlockY) {
                        this.maxBlockY = i3;
                    }

                    int curRendererIndex = ((subWorldID * this.renderChunksDeep + i2) * this.renderChunksTall + l2)
                        * this.renderChunksWide + j1;
                    WorldRenderer worldrenderer = this.worldRenderersMap.get(curRendererIndex);
                    if (((IMixinWorld) curWorld).isSubWorld()) {
                        SubWorld curSubWorld = (SubWorld) curWorld;
                        if (curSubWorld.getMaxX() < k1 || curSubWorld.getMinX() > (k1 + 16)
                            || curSubWorld.getMaxY() < i3
                            || curSubWorld.getMinY() > (i3 + 16)
                            || curSubWorld.getMaxZ() < j2
                            || curSubWorld.getMinZ() > (j2 + 16)) {
                            this.removeWorldRenderer(curWorld, j1, l2, i2);
                            continue;
                        }
                    }
                    if (worldrenderer == null) worldrenderer = this.createWorldRenderer(curWorld, j1, l2, i2);
                    boolean flag = worldrenderer.needsUpdate;
                    worldrenderer.setPosition(k1, i3, j2);

                    if (!flag && worldrenderer.needsUpdate) {
                        this.worldRenderersToUpdate.add(worldrenderer);
                    }
                }
            }
        }
    }

    /**
     * Sorts all renderers based on the passed in entity. Args: entityLiving, renderPass, partialTickTime
     * 
     * @author Sergius Onesimus
     * @reason Too complex to be modified without Overwrite
     */
    @SuppressWarnings({ "unchecked", "unused" })
    @Overwrite
    public int sortAndRender(EntityLivingBase par1EntityLivingBase, int par2, double par3) {
        this.theWorld.theProfiler.startSection("sortchunks");

        if (!this.worldRenderersList.isEmpty()) for (int j = 0; j < 10; ++j) {
            this.worldRenderersCheckIndex = (this.worldRenderersCheckIndex + 1) % this.worldRenderersList.size();
            WorldRenderer worldrenderer = this.worldRenderersList.get(this.worldRenderersCheckIndex);

            if (worldrenderer.needsUpdate && !this.worldRenderersToUpdate.contains(worldrenderer)) {
                this.worldRenderersToUpdate.add(worldrenderer);
            }
        }

        if (this.mc.gameSettings.renderDistanceChunks != this.renderDistanceChunks) {
            this.loadRenderers();
        }

        if (par2 == 0) {}

        double d9 = par1EntityLivingBase.lastTickPosX
            + (par1EntityLivingBase.posX - par1EntityLivingBase.lastTickPosX) * par3;
        double d1 = par1EntityLivingBase.lastTickPosY
            + (par1EntityLivingBase.posY - par1EntityLivingBase.lastTickPosY) * par3;
        double d2 = par1EntityLivingBase.lastTickPosZ
            + (par1EntityLivingBase.posZ - par1EntityLivingBase.lastTickPosZ) * par3;
        double d3 = par1EntityLivingBase.posX - this.prevSortX;
        double d4 = par1EntityLivingBase.posY - this.prevSortY;
        double d5 = par1EntityLivingBase.posZ - this.prevSortZ;

        if (this.prevChunkSortX != par1EntityLivingBase.chunkCoordX
            || this.prevChunkSortY != par1EntityLivingBase.chunkCoordY
            || this.prevChunkSortZ != par1EntityLivingBase.chunkCoordZ
            || d3 * d3 + d4 * d4 + d5 * d5 > 16.0D) {
            this.prevSortX = par1EntityLivingBase.posX;
            this.prevSortY = par1EntityLivingBase.posY;
            this.prevSortZ = par1EntityLivingBase.posZ;
            this.prevChunkSortX = par1EntityLivingBase.chunkCoordX;
            this.prevChunkSortY = par1EntityLivingBase.chunkCoordY;
            this.prevChunkSortZ = par1EntityLivingBase.chunkCoordZ;
            this.markRenderersForNewPosition(
                par1EntityLivingBase.posX,
                par1EntityLivingBase.posY,
                par1EntityLivingBase.posZ);
            Collections.sort(this.sortedWorldRenderersList, new EntitySorter(par1EntityLivingBase));
        } else {
            boolean rendererPositionsChanged = false;
            for (World curSubWorld : ((IMixinWorld) this.theWorld).getSubWorlds()) {
                if (((SubWorldClient) curSubWorld).rendererUpdateRequired()) {
                    rendererPositionsChanged = true;
                    this.markRenderersForNewPositionSingle(
                        this.prevSortX,
                        this.prevSortY,
                        this.prevSortZ,
                        ((IMixinWorld) curSubWorld).getSubWorldID());
                    ((SubWorldClient) curSubWorld).markRendererUpdateDone();
                }
            }
            if (rendererPositionsChanged)
                Collections.sort(this.sortedWorldRenderersList, new EntitySorter(par1EntityLivingBase));
        }

        double d6 = par1EntityLivingBase.posX - this.prevRenderSortX;
        double d7 = par1EntityLivingBase.posY - this.prevRenderSortY;
        double d8 = par1EntityLivingBase.posZ - this.prevRenderSortZ;
        int k;

        if (d6 * d6 + d7 * d7 + d8 * d8 > 1.0D) {
            this.prevRenderSortX = par1EntityLivingBase.posX;
            this.prevRenderSortY = par1EntityLivingBase.posY;
            this.prevRenderSortZ = par1EntityLivingBase.posZ;

            for (k = 0; k < 27; ++k) {
                this.sortedWorldRenderersList.get(k)
                    .updateRendererSort(par1EntityLivingBase);
            }
        }

        RenderHelper.disableStandardItemLighting();
        byte b1 = 0;

        if (this.occlusionEnabled && this.mc.gameSettings.advancedOpengl
            && !this.mc.gameSettings.anaglyph
            && par2 == 0) {
            int index = 0;
            for (WorldRenderer curRenderer : this.sortedWorldRenderersList) {
                if (curRenderer.distanceToEntitySquared(par1EntityLivingBase) > 675.0f)/*
                                                                                        * 3 * (16/2 + 6)Ġ = 588... let's
                                                                                        * add some space
                                                                                        */
                    break;
                index++;
            }

            byte b0 = 0;
            int l = Math.max(16, index);// 16;
            this.checkOcclusionQueryResult(b0, l);

            for (int i1 = b0; i1 < l; ++i1) {
                this.sortedWorldRenderersList.get(i1).isVisible = true;
            }

            this.theWorld.theProfiler.endStartSection("render");
            k = b1 + this.renderSortedRenderers(b0, l, par2, par3);

            do {
                this.theWorld.theProfiler.endStartSection("occ");
                int l1 = l;
                l *= 2;

                if (l > this.sortedWorldRenderersList.size()) {
                    l = this.sortedWorldRenderersList.size();
                }

                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glDisable(GL11.GL_ALPHA_TEST);
                GL11.glDisable(GL11.GL_FOG);
                GL11.glColorMask(false, false, false, false);
                GL11.glDepthMask(false);
                this.theWorld.theProfiler.startSection("check");
                this.checkOcclusionQueryResult(l1, l);
                this.theWorld.theProfiler.endSection();
                GL11.glPushMatrix();
                float f9 = 0.0F;
                float f = 0.0F;
                float f1 = 0.0F;

                for (int j1 = l1; j1 < l; ++j1) {
                    WorldRenderer curRenderer = this.sortedWorldRenderersList.get(j1);

                    if (curRenderer.skipAllRenderPasses()) {
                        curRenderer.isInFrustum = false;
                    } else {
                        if (!curRenderer.isInFrustum) {
                            curRenderer.isVisible = true;
                        }

                        if (curRenderer.isInFrustum && !curRenderer.isWaitingOnOcclusionQuery) {
                            float f2 = MathHelper.sqrt_float(curRenderer.distanceToEntitySquared(par1EntityLivingBase));
                            int k1 = (int) (1.0F + f2 / 128.0F);

                            if (this.cloudTickCounter % k1 == j1 % k1) {
                                WorldRenderer worldrenderer1 = curRenderer;
                                float f3 = (float) ((double) worldrenderer1.posXMinus - d9);
                                float f4 = (float) ((double) worldrenderer1.posYMinus - d1);
                                float f5 = (float) ((double) worldrenderer1.posZMinus - d2);
                                GL11.glPushMatrix();
                                GL11.glTranslatef((float) -d9, (float) -d1, (float) -d2);
                                if (((IMixinWorld) worldrenderer1.worldObj).isSubWorld()) {
                                    GL11.glMultMatrix(
                                        ((SubWorld) worldrenderer1.worldObj).getTransformToGlobalMatrixDirectBuffer());
                                }
                                GL11.glTranslatef(
                                    (float) worldrenderer1.posXMinus,
                                    (float) worldrenderer1.posYMinus,
                                    (float) worldrenderer1.posZMinus);

                                this.theWorld.theProfiler.startSection("bb");
                                ARBOcclusionQuery.glBeginQueryARB(
                                    ARBOcclusionQuery.GL_SAMPLES_PASSED_ARB,
                                    curRenderer.glOcclusionQuery);
                                curRenderer.callOcclusionQueryList();
                                ARBOcclusionQuery.glEndQueryARB(ARBOcclusionQuery.GL_SAMPLES_PASSED_ARB);
                                this.theWorld.theProfiler.endSection();
                                curRenderer.isWaitingOnOcclusionQuery = true;
                                GL11.glPopMatrix();
                            }
                        }
                    }
                }

                GL11.glPopMatrix();

                if (this.mc.gameSettings.anaglyph) {
                    if (EntityRenderer.anaglyphField == 0) {
                        GL11.glColorMask(false, true, true, true);
                    } else {
                        GL11.glColorMask(true, false, false, true);
                    }
                } else {
                    GL11.glColorMask(true, true, true, true);
                }

                GL11.glDepthMask(true);
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glEnable(GL11.GL_ALPHA_TEST);
                GL11.glEnable(GL11.GL_FOG);
                this.theWorld.theProfiler.endStartSection("render");
                k += this.renderSortedRenderers(l1, l, par2, par3);
            } while (l < this.sortedWorldRenderersList.size());
        } else {
            this.theWorld.theProfiler.endStartSection("render");
            k = b1 + this.renderSortedRenderers(0, this.sortedWorldRenderersList.size(), par2, par3);
        }

        this.theWorld.theProfiler.endSection();
        return k;
    }

    // checkOcclusionQueryResult
    /**
     * @author Sergius Onesimus
     * @reason Too complex to be modified without Overwrite
     */
    @Overwrite
    public void checkOcclusionQueryResult(int p_72720_1_, int p_72720_2_) {
        for (int k = p_72720_1_; k < p_72720_2_; ++k) {
            WorldRenderer curRenderer = this.sortedWorldRenderersList.get(k);
            if (curRenderer.isWaitingOnOcclusionQuery) {
                this.occlusionResult.clear();
                ARBOcclusionQuery.glGetQueryObjectuARB(
                    curRenderer.glOcclusionQuery,
                    ARBOcclusionQuery.GL_QUERY_RESULT_AVAILABLE_ARB,
                    this.occlusionResult);

                if (this.occlusionResult.get(0) != 0) {
                    curRenderer.isWaitingOnOcclusionQuery = false;
                    this.occlusionResult.clear();
                    ARBOcclusionQuery.glGetQueryObjectuARB(
                        curRenderer.glOcclusionQuery,
                        ARBOcclusionQuery.GL_QUERY_RESULT_ARB,
                        this.occlusionResult);
                    curRenderer.isVisible = this.occlusionResult.get(0) != 0;
                }
            }
        }
    }

    // renderSortedRenderers

    /**
     * Renders the sorted renders for the specified render pass. Args: startRenderer, numRenderers, renderPass,
     * partialTickTime
     * 
     * @author Sergius Onesimus
     * @reason Too complex to be modified without Overwrite
     */
    @SuppressWarnings("unchecked")
    @Overwrite
    public int renderSortedRenderers(int par1, int par2, int par3, double par4) {
        this.glRenderLists.clear();
        int l = 0;
        int i1 = par1;
        int j1 = par2;
        byte b0 = 1;

        if (par3 == 1) {
            i1 = this.sortedWorldRenderersList.size() - 1 - par1;
            j1 = this.sortedWorldRenderersList.size() - 1 - par2;
            b0 = -1;
        }

        for (int k1 = i1; k1 != j1; k1 += b0) {
            WorldRenderer curRenderer = this.sortedWorldRenderersList.get(k1);
            if (par3 == 0) {
                if (curRenderer.skipRenderPass[par3]) {} else if (!curRenderer.isInFrustum) {} else
                    if (this.occlusionEnabled && !curRenderer.isVisible) {} else {}
            }

            if (!curRenderer.skipRenderPass[par3] && curRenderer.isInFrustum
                && (!this.occlusionEnabled || curRenderer.isVisible)) {
                int l1 = curRenderer.getGLCallListForPass(par3);

                if (l1 >= 0) {
                    this.glRenderLists.add(curRenderer);
                    ++l;
                }
            }
        }

        EntityLivingBase entitylivingbase = this.mc.renderViewEntity;
        double d3 = entitylivingbase.lastTickPosX + (entitylivingbase.posX - entitylivingbase.lastTickPosX) * par4;
        double d1 = entitylivingbase.lastTickPosY + (entitylivingbase.posY - entitylivingbase.lastTickPosY) * par4;
        double d2 = entitylivingbase.lastTickPosZ + (entitylivingbase.posZ - entitylivingbase.lastTickPosZ) * par4;
        int i2 = 0;
        int j2;

        for (j2 = 0; j2 < this.allRenderLists.length; ++j2) {
            this.allRenderLists[j2].resetList();
        }

        int k2;
        int l2;

        for (j2 = 0; j2 < this.glRenderLists.size(); ++j2) {
            WorldRenderer worldrenderer = (WorldRenderer) this.glRenderLists.get(j2);
            k2 = -1;

            int subWorldID = ((IMixinWorld) worldrenderer.worldObj).getSubWorldID();
            DoubleBuffer customTransformation = null;
            if (((IMixinWorld) worldrenderer.worldObj).isSubWorld())
                customTransformation = ((SubWorld) worldrenderer.worldObj).getTransformToGlobalMatrixDirectBuffer();

            for (l2 = 0; l2 < i2; ++l2) {
                if (((IMixinRenderList) this.allRenderLists[l2]).rendersChunk(
                    worldrenderer.posXMinus,
                    worldrenderer.posYMinus,
                    worldrenderer.posZMinus,
                    subWorldID)) {
                    k2 = l2;
                }
            }

            if (k2 < 0) {
                k2 = i2++;
                ((IMixinRenderList) this.allRenderLists[k2]).setupRenderList(
                    worldrenderer.posXMinus,
                    worldrenderer.posYMinus,
                    worldrenderer.posZMinus,
                    d3,
                    d1,
                    d2,
                    subWorldID,
                    customTransformation);
            }

            this.allRenderLists[k2].addGLRenderList(worldrenderer.getGLCallListForPass(par3));
        }

        j2 = MathHelper.floor_double(d3);
        int i3 = MathHelper.floor_double(d2);
        k2 = j2 - (j2 & 1023);
        l2 = i3 - (i3 & 1023);
        Arrays.sort(this.allRenderLists, new RenderDistanceSorter(k2, l2));
        this.renderAllRenderLists(par3, par4);
        return l;
    }

    /**
     * Updates some of the renderers sorted by distance from the player
     * 
     * @author Sergius Onesimus
     * @reason Too complex to be modified without Overwrite
     */
    @SuppressWarnings("unchecked")
    @Overwrite
    public boolean updateRenderers(EntityLivingBase p_72716_1_, boolean p_72716_2_) {
        byte b0 = 2;
        RenderSorter rendersorter = new RenderSorter(p_72716_1_);
        WorldRenderer[] aworldrenderer = new WorldRenderer[b0];
        ArrayList<WorldRenderer> arraylist = null;
        int i = this.worldRenderersToUpdate.size();
        int j = 0;
        this.theWorld.theProfiler.startSection("nearChunksSearch");
        int k;
        WorldRenderer worldrenderer;
        int l;
        int i1;
        label136:

        for (k = 0; k < i; ++k) {
            worldrenderer = (WorldRenderer) this.worldRenderersToUpdate.get(k);

            if (worldrenderer != null) {
                if (!p_72716_2_) {
                    if (worldrenderer.distanceToEntitySquared(p_72716_1_) > 272.0F) {
                        for (l = 0; l < b0 && (aworldrenderer[l] == null
                            || rendersorter.compare(aworldrenderer[l], worldrenderer) <= 0); ++l) {
                            ;
                        }

                        --l;

                        if (l > 0) {
                            i1 = l;

                            while (true) {
                                --i1;

                                if (i1 == 0) {
                                    aworldrenderer[l] = worldrenderer;
                                    continue label136;
                                }

                                aworldrenderer[i1 - 1] = aworldrenderer[i1];
                            }
                        }

                        continue;
                    }
                } else if (!worldrenderer.isInFrustum) {
                    continue;
                }

                if (arraylist == null) {
                    arraylist = new ArrayList<WorldRenderer>();
                }

                ++j;
                arraylist.add(worldrenderer);
                this.worldRenderersToUpdate.set(k, null);
            }
        }

        this.theWorld.theProfiler.endSection();
        this.theWorld.theProfiler.startSection("sort");

        if (arraylist != null) {
            if (arraylist.size() > 1) {
                Collections.sort(arraylist, rendersorter);
            }

            for (k = arraylist.size() - 1; k >= 0; --k) {
                worldrenderer = (WorldRenderer) arraylist.get(k);
                worldrenderer.updateRenderer(p_72716_1_);
                worldrenderer.needsUpdate = false;
            }
        }

        this.theWorld.theProfiler.endSection();
        k = 0;
        this.theWorld.theProfiler.startSection("rebuild");
        int k1;

        for (k1 = b0 - 1; k1 >= 0; --k1) {
            WorldRenderer worldrenderer2 = aworldrenderer[k1];

            if (worldrenderer2 != null) {
                if (!worldrenderer2.isInFrustum && worldrenderer2.isInitialized && k1 != b0 - 1) {
                    aworldrenderer[k1] = null;
                    aworldrenderer[0] = null;
                    break;
                }

                aworldrenderer[k1].updateRenderer(p_72716_1_);
                aworldrenderer[k1].needsUpdate = false;
                ++k;
            }
        }

        this.theWorld.theProfiler.endSection();
        this.theWorld.theProfiler.startSection("cleanup");
        k1 = 0;
        l = 0;

        for (i1 = this.worldRenderersToUpdate.size(); k1 != i1; ++k1) {
            WorldRenderer worldrenderer1 = (WorldRenderer) this.worldRenderersToUpdate.get(k1);

            if (worldrenderer1 != null) {
                boolean flag1 = false;

                for (int j1 = 0; j1 < b0 && !flag1; ++j1) {
                    if (worldrenderer1 == aworldrenderer[j1]) {
                        flag1 = true;
                    }
                }

                if (!flag1) {
                    if (l != k1) {
                        this.worldRenderersToUpdate.set(l, worldrenderer1);
                    }

                    ++l;
                }
            }
        }

        this.theWorld.theProfiler.endSection();
        this.theWorld.theProfiler.startSection("trim");

        while (true) {
            --k1;

            if (k1 < l) {
                this.theWorld.theProfiler.endSection();
                return i == j + k;
            }

            this.worldRenderersToUpdate.remove(k1);
        }
    }

    /**
     * Marks the blocks in the given range for update
     * 
     * @author Sergius Onesimus
     * @reason Too complex to be modified without Overwrite
     */
    @Overwrite
    public void markBlocksForUpdate(int p_72725_1_, int p_72725_2_, int p_72725_3_, int p_72725_4_, int p_72725_5_,
        int p_72725_6_) {
        int k1 = MathHelper.bucketInt(p_72725_1_, 16);
        int l1 = MathHelper.bucketInt(p_72725_2_, 16);
        int i2 = MathHelper.bucketInt(p_72725_3_, 16);
        int j2 = MathHelper.bucketInt(p_72725_4_, 16);
        int k2 = MathHelper.bucketInt(p_72725_5_, 16);
        int l2 = MathHelper.bucketInt(p_72725_6_, 16);

        for (World curWorld : ((IMixinWorld) this.theWorld).getWorlds()) {
            for (int i3 = k1; i3 <= j2; ++i3) {
                int j3 = i3 % this.renderChunksWide;

                if (j3 < 0) {
                    j3 += this.renderChunksWide;
                }

                for (int k3 = l1; k3 <= k2; ++k3) {
                    int l3 = k3 % this.renderChunksTall;

                    if (l3 < 0) {
                        l3 += this.renderChunksTall;
                    }

                    for (int i4 = i2; i4 <= l2; ++i4) {
                        int j4 = i4 % this.renderChunksDeep;

                        if (j4 < 0) {
                            j4 += this.renderChunksDeep;
                        }

                        int k4 = ((((IMixinWorld) curWorld).getSubWorldID() * this.renderChunksDeep + j4)
                            * this.renderChunksTall + l3) * this.renderChunksWide + j3;
                        WorldRenderer worldrenderer = this.worldRenderersMap.get(k4);

                        if (worldrenderer != null && !worldrenderer.needsUpdate) {
                            this.worldRenderersToUpdate.add(worldrenderer);
                            worldrenderer.markDirty();
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks all renderers that previously weren't in the frustum and 1/16th of those that previously were in the
     * frustum for frustum clipping Args: frustum, partialTickTime
     * 
     * @author Sergius Onesimus
     * @reason Using maps instead of arrays, might as well overwrite the whole method
     */
    @Overwrite
    public void clipRenderersByFrustum(ICamera p_72729_1_, float p_72729_2_) {
        for (Map.Entry<Integer, WorldRenderer> curRendererEntry : this.worldRenderersMap.entrySet()) {
            WorldRenderer curRenderer = curRendererEntry.getValue();
            if (!curRenderer.skipAllRenderPasses()
                && (!curRenderer.isInFrustum || (curRendererEntry.getKey() + this.frustumCheckOffset & 15) == 0)) {
                curRenderer.updateInFrustum(p_72729_1_);
            }
        }

        ++this.frustumCheckOffset;
    }

    // drawOutlinedBoundingBox

    private static AxisAlignedBB storedAABB;
    private static OrientedBB storedOBB;

    @Inject(method = "drawOutlinedBoundingBox", at = @At(value = "HEAD"))
    private static void storeVariables(AxisAlignedBB aabb, int color, CallbackInfo ci) {
        storedAABB = aabb;
        storedOBB = ((IMixinAxisAlignedBB) aabb).getOrientedBB();
    }

    @ModifyArgs(
        method = "drawOutlinedBoundingBox",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/Tessellator;addVertex(DDD)V"))
    private static void modifyAddVertex(Args args) {
        double x = args.get(0);
        double y = args.get(1);
        double z = args.get(2);
        int vertexIndex;
        if (y == storedAABB.minY) {
            if (z == storedAABB.minZ) {
                if (x == storedAABB.minX) {
                    vertexIndex = 0;
                } else {
                    vertexIndex = 1;
                }
            } else {
                if (x == storedAABB.minX) {
                    vertexIndex = 2;
                } else {
                    vertexIndex = 3;
                }
            }
        } else {
            if (z == storedAABB.minZ) {
                if (x == storedAABB.minX) {
                    vertexIndex = 4;
                } else {
                    vertexIndex = 5;
                }
            } else {
                if (x == storedAABB.minX) {
                    vertexIndex = 6;
                } else {
                    vertexIndex = 7;
                }
            }
        }
        args.set(0, storedOBB.getX(vertexIndex));
        args.set(1, storedOBB.getY(vertexIndex));
        args.set(2, storedOBB.getZ(vertexIndex));
    }

}
