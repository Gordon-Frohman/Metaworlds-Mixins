package net.tclproject.metaworlds.mixin.mixins.client.renderer;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.EntityAuraFX;
import net.minecraft.client.particle.EntityBlockDustFX;
import net.minecraft.client.particle.EntityBreakingFX;
import net.minecraft.client.particle.EntityBubbleFX;
import net.minecraft.client.particle.EntityCloudFX;
import net.minecraft.client.particle.EntityCritFX;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.client.particle.EntityDropParticleFX;
import net.minecraft.client.particle.EntityEnchantmentTableParticleFX;
import net.minecraft.client.particle.EntityExplodeFX;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.particle.EntityFireworkSparkFX;
import net.minecraft.client.particle.EntityFishWakeFX;
import net.minecraft.client.particle.EntityFlameFX;
import net.minecraft.client.particle.EntityFootStepFX;
import net.minecraft.client.particle.EntityHeartFX;
import net.minecraft.client.particle.EntityHugeExplodeFX;
import net.minecraft.client.particle.EntityLargeExplodeFX;
import net.minecraft.client.particle.EntityLavaFX;
import net.minecraft.client.particle.EntityNoteFX;
import net.minecraft.client.particle.EntityPortalFX;
import net.minecraft.client.particle.EntityReddustFX;
import net.minecraft.client.particle.EntitySmokeFX;
import net.minecraft.client.particle.EntitySnowShovelFX;
import net.minecraft.client.particle.EntitySpellParticleFX;
import net.minecraft.client.particle.EntitySplashFX;
import net.minecraft.client.particle.EntitySuspendFX;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderList;
import net.minecraft.client.renderer.RenderSorter;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.util.RenderDistanceSorter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.EntitySorter;
import net.tclproject.metaworlds.api.IMixinEntity;
import net.tclproject.metaworlds.api.IMixinWorld;
import net.tclproject.metaworlds.api.SubWorld;
import net.tclproject.metaworlds.core.client.SubWorldClient;
import net.tclproject.metaworlds.mixin.interfaces.client.multiplayer.IMixinWorldClient;
import net.tclproject.metaworlds.mixin.interfaces.client.renderer.IMixinDestroyBlockProgress;
import net.tclproject.metaworlds.mixin.interfaces.client.renderer.IMixinRenderGlobal;
import net.tclproject.metaworlds.mixin.interfaces.client.renderer.IMixinRenderList;
import net.tclproject.metaworlds.mixin.interfaces.client.renderer.IMixinWorldRenderer;
import net.tclproject.metaworlds.mixin.interfaces.util.IMixinAxisAlignedBB;
import net.tclproject.metaworlds.mixin.interfaces.util.IMixinMovingObjectPosition;
import net.tclproject.metaworlds.patcher.OrientedBB;

import org.lwjgl.opengl.ARBOcclusionQuery;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RenderGlobal.class, priority = 800)
public abstract class MixinRenderGlobal implements IMixinRenderGlobal {

    private List<WorldRenderer> worldRenderersList = new ArrayList<WorldRenderer>();

    private List<WorldRenderer> sortedWorldRenderersList = new ArrayList<WorldRenderer>();

    private Map<Integer, WorldRenderer> worldRenderersMap = new HashMap<Integer, WorldRenderer>();

    @Shadow(remap = true)
    public Map damagedBlocks;

    @Shadow(remap = true)
    private Minecraft mc;

    @Shadow(remap = true)
    private TextureManager renderEngine;

    @Shadow(remap = true)
    private WorldClient theWorld;

    @Shadow(remap = true)
    private boolean occlusionEnabled;

    @Shadow(remap = true)
    private IntBuffer glOcclusionQueryBase;

    @Shadow(remap = true)
    private List worldRenderersToUpdate;

    @Shadow(remap = true)
    private int renderChunksWide;

    @Shadow(remap = true)
    private int renderChunksTall;

    @Shadow(remap = true)
    private int renderChunksDeep;

    @Shadow(remap = true)
    private List tileEntities;

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
    private int renderEntitiesStartupCounter;

    @Shadow(remap = true)
    private int countEntitiesTotal;

    @Shadow(remap = true)
    private int countEntitiesRendered;

    @Shadow(remap = true)
    private int countEntitiesHidden;

    @Shadow(remap = true)
    private boolean displayListEntitiesDirty;

    @Shadow(remap = true)
    private int displayListEntities;

    @Shadow(remap = true)
    private int worldRenderersCheckIndex;

    @Shadow(remap = true)
    private int renderersLoaded;

    @Shadow(remap = true)
    private int dummyRenderInt;

    @Shadow(remap = true)
    private int renderersBeingClipped;

    @Shadow(remap = true)
    private int renderersBeingOccluded;

    @Shadow(remap = true)
    private int renderersBeingRendered;

    @Shadow(remap = true)
    private int renderersSkippingRenderPass;

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
    public List glRenderLists;

    @Shadow(remap = true)
    public RenderList[] allRenderLists;

    @Shadow(remap = true)
    private int frustumCheckOffset;
	
    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderGlobal;mc:Lnet/minecraft/client/Minecraft;"), method = "<init>")
	// To get an empty RenderGlobal variable
	private void init(Minecraft mc, CallbackInfo info) {
		if(mc == null)
			return;
	}

    public RenderGlobal setMC(Minecraft par1Minecraft) {
        this.mc = par1Minecraft;
        this.renderEngine = par1Minecraft.getTextureManager();
        this.theWorld = par1Minecraft.theWorld;
        return (RenderGlobal) (Object) this;
    }

    public boolean getOcclusionEnabled() {
        return this.occlusionEnabled;
    }

    public IntBuffer getOcclusionQueryBase() {
        return this.glOcclusionQueryBase;
    }

    public List getWorldRenderersToUpdate() {
        return this.worldRenderersToUpdate;
    }

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

    public int getRenderChunksWide() {
        return this.renderChunksWide;
    }

    public int getRenderChunksTall() {
        return this.renderChunksTall;
    }

    public int getRenderChunksDeep() {
        return this.renderChunksDeep;
    }

    public WorldRenderer createWorldRenderer(World targetWorld, int chunkIndexX, int chunkIndexY, int chunkIndexZ) {
        if (targetWorld != this.theWorld) {
            return ((IMixinRenderGlobal) ((SubWorldClient) targetWorld).getRenderGlobal())
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
     */
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

            int j = 0;
            int k = 0;
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

            this.renderEntitiesStartupCounter = 2;
        }
    }

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
            // this.renderEntitiesStartupCounter = 2;
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

    /**
     * Renders all entities within range and within the frustrum. Args: pos, frustrum, partialTickTime
     */
    @Overwrite
    public void renderEntities(EntityLivingBase p_147589_1_, ICamera p_147589_2_, float p_147589_3_) {
        int pass = MinecraftForgeClient.getRenderPass();
        if (this.renderEntitiesStartupCounter > 0) {
            if (pass > 0) return;
            --this.renderEntitiesStartupCounter;
        } else {
            double d0 = p_147589_1_.prevPosX + (p_147589_1_.posX - p_147589_1_.prevPosX) * (double) p_147589_3_;
            double d1 = p_147589_1_.prevPosY + (p_147589_1_.posY - p_147589_1_.prevPosY) * (double) p_147589_3_;
            double d2 = p_147589_1_.prevPosZ + (p_147589_1_.posZ - p_147589_1_.prevPosZ) * (double) p_147589_3_;

            if (pass == 0) // no indentation to shrink patch
            {
                this.countEntitiesTotal = 0;
                this.countEntitiesRendered = 0;
                this.countEntitiesHidden = 0;
            }

            for (World curWorld : ((IMixinWorld) this.theWorld).getWorlds()) {
                WorldClient curClientWorld = (WorldClient) curWorld;
                Vec3 transformedPos = ((IMixinWorld) curWorld).transformToLocal(d0, d1, d2);
                this.theWorld.theProfiler.startSection("prepare");
                TileEntityRendererDispatcher.instance.cacheActiveRenderInfo(
                    curClientWorld,
                    this.mc.getTextureManager(),
                    this.mc.fontRenderer,
                    this.mc.renderViewEntity,
                    p_147589_3_);
                RenderManager.instance.cacheActiveRenderInfo(
                    curClientWorld,
                    this.mc.getTextureManager(),
                    this.mc.fontRenderer,
                    this.mc.renderViewEntity,
                    this.mc.pointedEntity,
                    this.mc.gameSettings,
                    p_147589_3_);

                EntityLivingBase entitylivingbase1 = this.mc.renderViewEntity;
                double d3 = entitylivingbase1.lastTickPosX
                    + (entitylivingbase1.posX - entitylivingbase1.lastTickPosX) * (double) p_147589_3_;
                double d4 = entitylivingbase1.lastTickPosY
                    + (entitylivingbase1.posY - entitylivingbase1.lastTickPosY) * (double) p_147589_3_;
                double d5 = entitylivingbase1.lastTickPosZ
                    + (entitylivingbase1.posZ - entitylivingbase1.lastTickPosZ) * (double) p_147589_3_;
                TileEntityRendererDispatcher.staticPlayerX = d3;
                TileEntityRendererDispatcher.staticPlayerY = d4;
                TileEntityRendererDispatcher.staticPlayerZ = d5;
                this.theWorld.theProfiler.endStartSection("staticentities");

                if (this.displayListEntitiesDirty) {
                    RenderManager.renderPosX = 0.0D;
                    RenderManager.renderPosY = 0.0D;
                    RenderManager.renderPosZ = 0.0D;
                    this.rebuildDisplayListEntities();
                }

                GL11.glMatrixMode(GL11.GL_MODELVIEW);
                GL11.glPushMatrix();
                GL11.glTranslated(-d3, -d4, -d5);
                GL11.glCallList(this.displayListEntities);
                GL11.glPopMatrix();
                RenderManager.renderPosX = d3;
                RenderManager.renderPosY = d4;
                RenderManager.renderPosZ = d5;
                this.mc.entityRenderer.enableLightmap((double) p_147589_3_);
                this.theWorld.theProfiler.endStartSection("global");
                List list = curWorld.getLoadedEntityList();
                if (pass == 0) // no indentation for smaller patch size
                {
                    this.countEntitiesTotal = list.size();
                }
                int i;
                Entity entity;

                for (i = 0; i < curWorld.weatherEffects.size(); ++i) {
                    entity = (Entity) curWorld.weatherEffects.get(i);
                    if (!entity.shouldRenderInPass(pass)) continue;
                    ++this.countEntitiesRendered;

                    if (entity
                        .isInRangeToRender3d(transformedPos.xCoord, transformedPos.yCoord, transformedPos.zCoord)) {
                        RenderManager.instance.renderEntitySimple(entity, p_147589_3_);
                    }
                }

                this.theWorld.theProfiler.endStartSection("entities");

                for (i = 0; i < list.size(); ++i) {
                    entity = (Entity) list.get(i);
                    if (!entity.shouldRenderInPass(pass)) continue;
                    boolean flag = entity
                        .isInRangeToRender3d(transformedPos.xCoord, transformedPos.yCoord, transformedPos.zCoord)
                        && (entity.ignoreFrustumCheck
                            || p_147589_2_.isBoundingBoxInFrustum(
                                ((IMixinAxisAlignedBB) entity.boundingBox)
                                    .getTransformedToGlobalBoundingBox(entity.worldObj))
                            || entity.riddenByEntity == this.mc.thePlayer);

                    if (!flag && entity instanceof EntityLiving) {
                        EntityLiving entityliving = (EntityLiving) entity;

                        if (entityliving.getLeashed() && entityliving.getLeashedToEntity() != null) {
                            Entity entity1 = entityliving.getLeashedToEntity();
                            flag = p_147589_2_.isBoundingBoxInFrustum(
                                ((IMixinAxisAlignedBB) entity1.boundingBox)
                                    .getTransformedToGlobalBoundingBox(entity.worldObj));
                        }
                    }

                    if (flag
                        && (entity != this.mc.renderViewEntity || this.mc.gameSettings.thirdPersonView != 0
                            || this.mc.renderViewEntity.isPlayerSleeping())
                        && curWorld.blockExists(
                            MathHelper.floor_double(entity.posX),
                            0,
                            MathHelper.floor_double(entity.posZ))) {
                        ++this.countEntitiesRendered;
                        RenderManager.instance.renderEntitySimple(entity, p_147589_3_);
                    }
                }

                this.theWorld.theProfiler.endStartSection("blockentities");
                RenderHelper.enableStandardItemLighting();

                for (i = 0; i
                    < ((IMixinWorldClient) curClientWorld).getMinecraft().renderGlobal.tileEntities.size(); ++i) {
                    TileEntity tile = (TileEntity) ((IMixinWorldClient) curClientWorld)
                        .getMinecraft().renderGlobal.tileEntities.get(i);
                    if (tile.shouldRenderInPass(pass) && p_147589_2_.isBoundingBoxInFrustum(
                        tile.getWorldObj() == null ? tile.getRenderBoundingBox()
                            : ((IMixinAxisAlignedBB) tile.getRenderBoundingBox())
                                .getTransformedToGlobalBoundingBox(tile.getWorldObj()))) {
                        TileEntityRendererDispatcher.instance.renderTileEntity(tile, p_147589_3_);
                    }
                }

                this.mc.entityRenderer.disableLightmap((double) p_147589_3_);
                this.theWorld.theProfiler.endSection();
            }
        }
    }

    @Shadow(remap = true)
    protected abstract void rebuildDisplayListEntities();

    /**
     * Goes through all the renderers setting new positions on them and those that have their position changed are
     * adding to be updated
     */
    public void markRenderersForNewPosition(double par1, double par2, double par3) {
        for (World curWorld : ((IMixinWorld) this.theWorld).getWorlds()) {
            this.markRenderersForNewPositionSingle(par1, par2, par3, ((IMixinWorld) curWorld).getSubWorldID());

        }
    }

    public void markRenderersForNewPositionSingle(double par1d, double par2d, double par3d, int subWorldID) {
        World curWorld = ((IMixinWorld) this.theWorld).getSubWorld(subWorldID);
        Vec3 tranformedVec = ((IMixinWorld) curWorld).transformToLocal((double) par1d, (double) par2d, (double) par3d);
        int par1 = MathHelper.floor_double(tranformedVec.xCoord);
        int par2 = MathHelper.floor_double(tranformedVec.yCoord);
        int par3 = MathHelper.floor_double(tranformedVec.zCoord);
        par1 -= 8;
        par2 -= 8;
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
     */
    @Overwrite
    public int sortAndRender(EntityLivingBase par1EntityLivingBase, int par2, double par3) {
        this.theWorld.theProfiler.startSection("sortchunks");

        for (int j = 0; j < 10; ++j) {
            this.worldRenderersCheckIndex = (this.worldRenderersCheckIndex + 1) % this.worldRenderersList.size();
            WorldRenderer worldrenderer = this.worldRenderersList.get(this.worldRenderersCheckIndex);

            if (worldrenderer.needsUpdate && !this.worldRenderersToUpdate.contains(worldrenderer)) {
                this.worldRenderersToUpdate.add(worldrenderer);
            }
        }

        if (this.mc.gameSettings.renderDistanceChunks != this.renderDistanceChunks) {
            this.loadRenderers();
        }

        if (par2 == 0) {
            this.renderersLoaded = 0;
            this.dummyRenderInt = 0;
            this.renderersBeingClipped = 0;
            this.renderersBeingOccluded = 0;
            this.renderersBeingRendered = 0;
            this.renderersSkippingRenderPass = 0;
        }

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
                                float f6 = f3 - f9;
                                float f7 = f4 - f;
                                float f8 = f5 - f1;

                                GL11.glPushMatrix();
                                /*
                                 * if (f6 != 0.0F || f7 != 0.0F || f8 != 0.0F)
                                 * {
                                 * GL11.glTranslatef(f6, f7, f8);
                                 * f9 += f6;
                                 * f += f7;
                                 * f1 += f8;
                                 * }
                                 */
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

    @Overwrite
    private void checkOcclusionQueryResult(int p_72720_1_, int p_72720_2_) {
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

    /**
     * Renders the sorted renders for the specified render pass. Args: startRenderer, numRenderers, renderPass,
     * partialTickTime
     */
    @Overwrite
    private int renderSortedRenderers(int par1, int par2, int par3, double par4) {
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
                ++this.renderersLoaded;

                if (curRenderer.skipRenderPass[par3]) {
                    ++this.renderersSkippingRenderPass;
                } else if (!curRenderer.isInFrustum) {
                    ++this.renderersBeingClipped;
                } else if (this.occlusionEnabled && !curRenderer.isVisible) {
                    ++this.renderersBeingOccluded;
                } else {
                    ++this.renderersBeingRendered;
                }
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
     */
    @Overwrite
    public boolean updateRenderers(EntityLivingBase p_72716_1_, boolean p_72716_2_) {
        byte b0 = 2;
        RenderSorter rendersorter = new RenderSorter(p_72716_1_);
        WorldRenderer[] aworldrenderer = new WorldRenderer[b0];
        ArrayList arraylist = null;
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
                    arraylist = new ArrayList();
                }

                ++j;
                arraylist.add(worldrenderer);
                this.worldRenderersToUpdate.set(k, (Object) null);
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
                if (!worldrenderer2.isInFrustum && ((IMixinWorldRenderer)worldrenderer2).isInitialized() && k1 != b0 - 1) {
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
     * Draws the selection box for the player. Args: entityPlayer, rayTraceHit, i, itemStack, partialTickTime
     */
    @Overwrite
    public void drawSelectionBox(EntityPlayer p_72731_1_, MovingObjectPosition par2MovingObjectPosition, int p_72731_3_,
        float p_72731_4_) {
        if (p_72731_3_ == 0 && par2MovingObjectPosition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.4F);
            GL11.glLineWidth(2.0F);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDepthMask(false);
            float f1 = 0.002F;
            Block block = ((IMixinMovingObjectPosition) par2MovingObjectPosition).getWorld()
                .getBlock(
                    par2MovingObjectPosition.blockX,
                    par2MovingObjectPosition.blockY,
                    par2MovingObjectPosition.blockZ);

            if (block.getMaterial() != Material.air) {
                block.setBlockBoundsBasedOnState(
                    ((IMixinMovingObjectPosition) par2MovingObjectPosition).getWorld(),
                    par2MovingObjectPosition.blockX,
                    par2MovingObjectPosition.blockY,
                    par2MovingObjectPosition.blockZ);
                double d0 = p_72731_1_.lastTickPosX + (p_72731_1_.posX - p_72731_1_.lastTickPosX) * (double) p_72731_4_;
                double d1 = p_72731_1_.lastTickPosY + (p_72731_1_.posY - p_72731_1_.lastTickPosY) * (double) p_72731_4_;
                double d2 = p_72731_1_.lastTickPosZ + (p_72731_1_.posZ - p_72731_1_.lastTickPosZ) * (double) p_72731_4_;
                drawOutlinedBoundingBox(
                    ((IMixinAxisAlignedBB) block
                        .getSelectedBoundingBoxFromPool(
                            ((IMixinMovingObjectPosition) par2MovingObjectPosition).getWorld(),
                            par2MovingObjectPosition.blockX,
                            par2MovingObjectPosition.blockY,
                            par2MovingObjectPosition.blockZ)
                        .expand((double) f1, (double) f1, (double) f1))
                            .getTransformedToGlobalBoundingBox(
                                ((IMixinMovingObjectPosition) par2MovingObjectPosition).getWorld())
                            .offset(-d0, -d1, -d2),
                    -1);
            }

            GL11.glDepthMask(true);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_BLEND);
        }
    }

    /**
     * Draws lines for the edges of the bounding box.
     */
    @Overwrite
    public static void drawOutlinedBoundingBox(AxisAlignedBB p_147590_0_, int p_147590_1_) {
        OrientedBB par1OrientedBB = ((IMixinAxisAlignedBB) p_147590_0_).getOrientedBB();
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawing(3);

        if (p_147590_1_ != -1) {
            tessellator.setColorOpaque_I(p_147590_1_);
        }

        tessellator.addVertex(par1OrientedBB.getX(0), par1OrientedBB.getY(0), par1OrientedBB.getZ(0));
        tessellator.addVertex(par1OrientedBB.getX(1), par1OrientedBB.getY(1), par1OrientedBB.getZ(1));
        tessellator.addVertex(par1OrientedBB.getX(3), par1OrientedBB.getY(3), par1OrientedBB.getZ(3));
        tessellator.addVertex(par1OrientedBB.getX(2), par1OrientedBB.getY(2), par1OrientedBB.getZ(2));
        tessellator.addVertex(par1OrientedBB.getX(0), par1OrientedBB.getY(0), par1OrientedBB.getZ(0));
        tessellator.draw();
        tessellator.startDrawing(3);

        if (p_147590_1_ != -1) {
            tessellator.setColorOpaque_I(p_147590_1_);
        }

        tessellator.addVertex(par1OrientedBB.getX(4), par1OrientedBB.getY(4), par1OrientedBB.getZ(4));
        tessellator.addVertex(par1OrientedBB.getX(5), par1OrientedBB.getY(5), par1OrientedBB.getZ(5));
        tessellator.addVertex(par1OrientedBB.getX(7), par1OrientedBB.getY(7), par1OrientedBB.getZ(7));
        tessellator.addVertex(par1OrientedBB.getX(6), par1OrientedBB.getY(6), par1OrientedBB.getZ(6));
        tessellator.addVertex(par1OrientedBB.getX(4), par1OrientedBB.getY(4), par1OrientedBB.getZ(4));
        tessellator.draw();
        tessellator.startDrawing(1);

        if (p_147590_1_ != -1) {
            tessellator.setColorOpaque_I(p_147590_1_);
        }

        tessellator.addVertex(par1OrientedBB.getX(0), par1OrientedBB.getY(0), par1OrientedBB.getZ(0));
        tessellator.addVertex(par1OrientedBB.getX(4), par1OrientedBB.getY(4), par1OrientedBB.getZ(4));
        tessellator.addVertex(par1OrientedBB.getX(1), par1OrientedBB.getY(1), par1OrientedBB.getZ(1));
        tessellator.addVertex(par1OrientedBB.getX(5), par1OrientedBB.getY(5), par1OrientedBB.getZ(5));
        tessellator.addVertex(par1OrientedBB.getX(2), par1OrientedBB.getY(2), par1OrientedBB.getZ(2));
        tessellator.addVertex(par1OrientedBB.getX(6), par1OrientedBB.getY(6), par1OrientedBB.getZ(6));
        tessellator.addVertex(par1OrientedBB.getX(3), par1OrientedBB.getY(3), par1OrientedBB.getZ(3));
        tessellator.addVertex(par1OrientedBB.getX(7), par1OrientedBB.getY(7), par1OrientedBB.getZ(7));
        tessellator.draw();
    }

    /**
     * Marks the blocks in the given range for update
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
     */
    @Overwrite
    public void clipRenderersByFrustum(ICamera p_72729_1_, float p_72729_2_) {
        for (Map.Entry<Integer, WorldRenderer> curRendererEntry : this.worldRenderersMap.entrySet()) {
            WorldRenderer curRenderer = curRendererEntry.getValue();
            if (!curRenderer.skipAllRenderPasses() && /* curRenderer.isInitialized() && */ (!curRenderer.isInFrustum
                || (curRendererEntry.getKey() + this.frustumCheckOffset & 15) == 0)) {
                curRenderer.updateInFrustum(p_72729_1_);
            }
        }
        /*
         * for (int i = 0; i < this.worldRenderers.length; ++i)
         * {
         * if (!this.worldRenderers[i].skipAllRenderPasses() && (!this.worldRenderers[i].isInFrustum || (i +
         * this.frustumCheckOffset & 15) == 0))
         * {
         * this.worldRenderers[i].updateInFrustum(p_72729_1_);
         * }
         * }
         */

        ++this.frustumCheckOffset;
    }

    /**
     * Spawns a particle. Arg: particleType, x, y, z, velX, velY, velZ
     */
    @Overwrite
    public EntityFX doSpawnParticle(String par1Str, double p_72726_2_, double p_72726_4_, double p_72726_6_,
        double p_72726_8_, double p_72726_10_, double p_72726_12_) {
        if (this.mc != null && this.mc.renderViewEntity != null && this.mc.effectRenderer != null) {
            int i = this.mc.gameSettings.particleSetting;

            if (i == 1 && this.theWorld.rand.nextInt(3) == 0) {
                i = 2;
            }

            Vec3 transformedPos = ((IMixinEntity) this.mc.renderViewEntity).getLocalPos(this.theWorld);
            double d6 = transformedPos.xCoord - p_72726_2_;
            double d7 = transformedPos.yCoord - p_72726_4_;
            double d8 = transformedPos.zCoord - p_72726_6_;
            EntityFX entityfx = null;

            if (par1Str.equals("hugeexplosion")) {
                this.mc.effectRenderer.addEffect(
                    entityfx = new EntityHugeExplodeFX(
                        this.theWorld,
                        p_72726_2_,
                        p_72726_4_,
                        p_72726_6_,
                        p_72726_8_,
                        p_72726_10_,
                        p_72726_12_));
            } else if (par1Str.equals("largeexplode")) {
                this.mc.effectRenderer.addEffect(
                    entityfx = new EntityLargeExplodeFX(
                        this.renderEngine,
                        this.theWorld,
                        p_72726_2_,
                        p_72726_4_,
                        p_72726_6_,
                        p_72726_8_,
                        p_72726_10_,
                        p_72726_12_));
            } else if (par1Str.equals("fireworksSpark")) {
                this.mc.effectRenderer.addEffect(
                    entityfx = new EntityFireworkSparkFX(
                        this.theWorld,
                        p_72726_2_,
                        p_72726_4_,
                        p_72726_6_,
                        p_72726_8_,
                        p_72726_10_,
                        p_72726_12_,
                        this.mc.effectRenderer));
            }

            if (entityfx != null) {
                return (EntityFX) entityfx;
            } else {
                double d9 = 16.0D;

                if (d6 * d6 + d7 * d7 + d8 * d8 > d9 * d9) {
                    return null;
                } else if (i > 1) {
                    return null;
                } else {
                    if (par1Str.equals("bubble")) {
                        entityfx = new EntityBubbleFX(
                            this.theWorld,
                            p_72726_2_,
                            p_72726_4_,
                            p_72726_6_,
                            p_72726_8_,
                            p_72726_10_,
                            p_72726_12_);
                    } else if (par1Str.equals("suspended")) {
                        entityfx = new EntitySuspendFX(
                            this.theWorld,
                            p_72726_2_,
                            p_72726_4_,
                            p_72726_6_,
                            p_72726_8_,
                            p_72726_10_,
                            p_72726_12_);
                    } else if (par1Str.equals("depthsuspend")) {
                        entityfx = new EntityAuraFX(
                            this.theWorld,
                            p_72726_2_,
                            p_72726_4_,
                            p_72726_6_,
                            p_72726_8_,
                            p_72726_10_,
                            p_72726_12_);
                    } else if (par1Str.equals("townaura")) {
                        entityfx = new EntityAuraFX(
                            this.theWorld,
                            p_72726_2_,
                            p_72726_4_,
                            p_72726_6_,
                            p_72726_8_,
                            p_72726_10_,
                            p_72726_12_);
                    } else if (par1Str.equals("crit")) {
                        entityfx = new EntityCritFX(
                            this.theWorld,
                            p_72726_2_,
                            p_72726_4_,
                            p_72726_6_,
                            p_72726_8_,
                            p_72726_10_,
                            p_72726_12_);
                    } else if (par1Str.equals("magicCrit")) {
                        entityfx = new EntityCritFX(
                            this.theWorld,
                            p_72726_2_,
                            p_72726_4_,
                            p_72726_6_,
                            p_72726_8_,
                            p_72726_10_,
                            p_72726_12_);
                        ((EntityFX) entityfx).setRBGColorF(
                            ((EntityFX) entityfx).getRedColorF() * 0.3F,
                            ((EntityFX) entityfx).getGreenColorF() * 0.8F,
                            ((EntityFX) entityfx).getBlueColorF());
                        ((EntityFX) entityfx).nextTextureIndexX();
                    } else if (par1Str.equals("smoke")) {
                        entityfx = new EntitySmokeFX(
                            this.theWorld,
                            p_72726_2_,
                            p_72726_4_,
                            p_72726_6_,
                            p_72726_8_,
                            p_72726_10_,
                            p_72726_12_);
                    } else if (par1Str.equals("mobSpell")) {
                        entityfx = new EntitySpellParticleFX(
                            this.theWorld,
                            p_72726_2_,
                            p_72726_4_,
                            p_72726_6_,
                            0.0D,
                            0.0D,
                            0.0D);
                        ((EntityFX) entityfx)
                            .setRBGColorF((float) p_72726_8_, (float) p_72726_10_, (float) p_72726_12_);
                    } else if (par1Str.equals("mobSpellAmbient")) {
                        entityfx = new EntitySpellParticleFX(
                            this.theWorld,
                            p_72726_2_,
                            p_72726_4_,
                            p_72726_6_,
                            0.0D,
                            0.0D,
                            0.0D);
                        ((EntityFX) entityfx).setAlphaF(0.15F);
                        ((EntityFX) entityfx)
                            .setRBGColorF((float) p_72726_8_, (float) p_72726_10_, (float) p_72726_12_);
                    } else if (par1Str.equals("spell")) {
                        entityfx = new EntitySpellParticleFX(
                            this.theWorld,
                            p_72726_2_,
                            p_72726_4_,
                            p_72726_6_,
                            p_72726_8_,
                            p_72726_10_,
                            p_72726_12_);
                    } else if (par1Str.equals("instantSpell")) {
                        entityfx = new EntitySpellParticleFX(
                            this.theWorld,
                            p_72726_2_,
                            p_72726_4_,
                            p_72726_6_,
                            p_72726_8_,
                            p_72726_10_,
                            p_72726_12_);
                        ((EntitySpellParticleFX) entityfx).setBaseSpellTextureIndex(144);
                    } else if (par1Str.equals("witchMagic")) {
                        entityfx = new EntitySpellParticleFX(
                            this.theWorld,
                            p_72726_2_,
                            p_72726_4_,
                            p_72726_6_,
                            p_72726_8_,
                            p_72726_10_,
                            p_72726_12_);
                        ((EntitySpellParticleFX) entityfx).setBaseSpellTextureIndex(144);
                        float f = this.theWorld.rand.nextFloat() * 0.5F + 0.35F;
                        ((EntityFX) entityfx).setRBGColorF(1.0F * f, 0.0F * f, 1.0F * f);
                    } else if (par1Str.equals("note")) {
                        entityfx = new EntityNoteFX(
                            this.theWorld,
                            p_72726_2_,
                            p_72726_4_,
                            p_72726_6_,
                            p_72726_8_,
                            p_72726_10_,
                            p_72726_12_);
                    } else if (par1Str.equals("portal")) {
                        entityfx = new EntityPortalFX(
                            this.theWorld,
                            p_72726_2_,
                            p_72726_4_,
                            p_72726_6_,
                            p_72726_8_,
                            p_72726_10_,
                            p_72726_12_);
                    } else if (par1Str.equals("enchantmenttable")) {
                        entityfx = new EntityEnchantmentTableParticleFX(
                            this.theWorld,
                            p_72726_2_,
                            p_72726_4_,
                            p_72726_6_,
                            p_72726_8_,
                            p_72726_10_,
                            p_72726_12_);
                    } else if (par1Str.equals("explode")) {
                        entityfx = new EntityExplodeFX(
                            this.theWorld,
                            p_72726_2_,
                            p_72726_4_,
                            p_72726_6_,
                            p_72726_8_,
                            p_72726_10_,
                            p_72726_12_);
                    } else if (par1Str.equals("flame")) {
                        entityfx = new EntityFlameFX(
                            this.theWorld,
                            p_72726_2_,
                            p_72726_4_,
                            p_72726_6_,
                            p_72726_8_,
                            p_72726_10_,
                            p_72726_12_);
                    } else if (par1Str.equals("lava")) {
                        entityfx = new EntityLavaFX(this.theWorld, p_72726_2_, p_72726_4_, p_72726_6_);
                    } else if (par1Str.equals("footstep")) {
                        entityfx = new EntityFootStepFX(
                            this.renderEngine,
                            this.theWorld,
                            p_72726_2_,
                            p_72726_4_,
                            p_72726_6_);
                    } else if (par1Str.equals("splash")) {
                        entityfx = new EntitySplashFX(
                            this.theWorld,
                            p_72726_2_,
                            p_72726_4_,
                            p_72726_6_,
                            p_72726_8_,
                            p_72726_10_,
                            p_72726_12_);
                    } else if (par1Str.equals("wake")) {
                        entityfx = new EntityFishWakeFX(
                            this.theWorld,
                            p_72726_2_,
                            p_72726_4_,
                            p_72726_6_,
                            p_72726_8_,
                            p_72726_10_,
                            p_72726_12_);
                    } else if (par1Str.equals("largesmoke")) {
                        entityfx = new EntitySmokeFX(
                            this.theWorld,
                            p_72726_2_,
                            p_72726_4_,
                            p_72726_6_,
                            p_72726_8_,
                            p_72726_10_,
                            p_72726_12_,
                            2.5F);
                    } else if (par1Str.equals("cloud")) {
                        entityfx = new EntityCloudFX(
                            this.theWorld,
                            p_72726_2_,
                            p_72726_4_,
                            p_72726_6_,
                            p_72726_8_,
                            p_72726_10_,
                            p_72726_12_);
                    } else if (par1Str.equals("reddust")) {
                        entityfx = new EntityReddustFX(
                            this.theWorld,
                            p_72726_2_,
                            p_72726_4_,
                            p_72726_6_,
                            (float) p_72726_8_,
                            (float) p_72726_10_,
                            (float) p_72726_12_);
                    } else if (par1Str.equals("snowballpoof")) {
                        entityfx = new EntityBreakingFX(
                            this.theWorld,
                            p_72726_2_,
                            p_72726_4_,
                            p_72726_6_,
                            Items.snowball);
                    } else if (par1Str.equals("dripWater")) {
                        entityfx = new EntityDropParticleFX(
                            this.theWorld,
                            p_72726_2_,
                            p_72726_4_,
                            p_72726_6_,
                            Material.water);
                    } else if (par1Str.equals("dripLava")) {
                        entityfx = new EntityDropParticleFX(
                            this.theWorld,
                            p_72726_2_,
                            p_72726_4_,
                            p_72726_6_,
                            Material.lava);
                    } else if (par1Str.equals("snowshovel")) {
                        entityfx = new EntitySnowShovelFX(
                            this.theWorld,
                            p_72726_2_,
                            p_72726_4_,
                            p_72726_6_,
                            p_72726_8_,
                            p_72726_10_,
                            p_72726_12_);
                    } else if (par1Str.equals("slime")) {
                        entityfx = new EntityBreakingFX(
                            this.theWorld,
                            p_72726_2_,
                            p_72726_4_,
                            p_72726_6_,
                            Items.slime_ball);
                    } else if (par1Str.equals("heart")) {
                        entityfx = new EntityHeartFX(
                            this.theWorld,
                            p_72726_2_,
                            p_72726_4_,
                            p_72726_6_,
                            p_72726_8_,
                            p_72726_10_,
                            p_72726_12_);
                    } else if (par1Str.equals("angryVillager")) {
                        entityfx = new EntityHeartFX(
                            this.theWorld,
                            p_72726_2_,
                            p_72726_4_ + 0.5D,
                            p_72726_6_,
                            p_72726_8_,
                            p_72726_10_,
                            p_72726_12_);
                        ((EntityFX) entityfx).setParticleTextureIndex(81);
                        ((EntityFX) entityfx).setRBGColorF(1.0F, 1.0F, 1.0F);
                    } else if (par1Str.equals("happyVillager")) {
                        entityfx = new EntityAuraFX(
                            this.theWorld,
                            p_72726_2_,
                            p_72726_4_,
                            p_72726_6_,
                            p_72726_8_,
                            p_72726_10_,
                            p_72726_12_);
                        ((EntityFX) entityfx).setParticleTextureIndex(82);
                        ((EntityFX) entityfx).setRBGColorF(1.0F, 1.0F, 1.0F);
                    } else {
                        int k;
                        String[] astring;

                        if (par1Str.startsWith("iconcrack_")) {
                            astring = par1Str.split("_", 3);
                            int j = Integer.parseInt(astring[1]);

                            if (astring.length > 2) {
                                k = Integer.parseInt(astring[2]);
                                entityfx = new EntityBreakingFX(
                                    this.theWorld,
                                    p_72726_2_,
                                    p_72726_4_,
                                    p_72726_6_,
                                    p_72726_8_,
                                    p_72726_10_,
                                    p_72726_12_,
                                    Item.getItemById(j),
                                    k);
                            } else {
                                entityfx = new EntityBreakingFX(
                                    this.theWorld,
                                    p_72726_2_,
                                    p_72726_4_,
                                    p_72726_6_,
                                    p_72726_8_,
                                    p_72726_10_,
                                    p_72726_12_,
                                    Item.getItemById(j),
                                    0);
                            }
                        } else {
                            Block block;

                            if (par1Str.startsWith("blockcrack_")) {
                                astring = par1Str.split("_", 3);
                                block = Block.getBlockById(Integer.parseInt(astring[1]));
                                k = Integer.parseInt(astring[2]);
                                entityfx = (new EntityDiggingFX(
                                    this.theWorld,
                                    p_72726_2_,
                                    p_72726_4_,
                                    p_72726_6_,
                                    p_72726_8_,
                                    p_72726_10_,
                                    p_72726_12_,
                                    block,
                                    k)).applyRenderColor(k);
                            } else if (par1Str.startsWith("blockdust_")) {
                                astring = par1Str.split("_", 3);
                                block = Block.getBlockById(Integer.parseInt(astring[1]));
                                k = Integer.parseInt(astring[2]);
                                entityfx = (new EntityBlockDustFX(
                                    this.theWorld,
                                    p_72726_2_,
                                    p_72726_4_,
                                    p_72726_6_,
                                    p_72726_8_,
                                    p_72726_10_,
                                    p_72726_12_,
                                    block,
                                    k)).applyRenderColor(k);
                            }
                        }
                    }

                    if (entityfx != null) {
                        this.mc.effectRenderer.addEffect((EntityFX) entityfx);
                    }

                    return (EntityFX) entityfx;
                }
            }
        } else {
            return null;
        }
    }

    public WorldClient getWorld() {
        return this.theWorld;
    }

    public void setWorld(WorldClient newWorld) {
        this.theWorld = newWorld;
    }

    @Shadow(remap = true)
    protected abstract void renderAllRenderLists(int par3, double par4);

    @Shadow(remap = true)
    protected abstract void onStaticEntitiesChanged();
    
    public void destroyBlockPartially(int p_147587_1_, int p_147587_2_, int p_147587_3_, int p_147587_4_, int p_147587_5_, int subWorldId)
    {
        if (p_147587_5_ >= 0 && p_147587_5_ < 10)
        {
            DestroyBlockProgress destroyblockprogress = (DestroyBlockProgress)this.damagedBlocks.get(Integer.valueOf(p_147587_1_));

            if (destroyblockprogress == null || destroyblockprogress.getPartialBlockX() != p_147587_2_ || destroyblockprogress.getPartialBlockY() != p_147587_3_ || destroyblockprogress.getPartialBlockZ() != p_147587_4_)
            {
            	destroyblockprogress = ((IMixinDestroyBlockProgress)new DestroyBlockProgress(p_147587_1_, p_147587_2_, p_147587_3_, p_147587_4_)).setPartialBlockSubWorldId(subWorldId);
                this.damagedBlocks.put(Integer.valueOf(p_147587_1_), destroyblockprogress);
            }

            destroyblockprogress.setPartialBlockDamage(p_147587_5_);
            destroyblockprogress.setCloudUpdateTick(this.cloudTickCounter);
        }
        else
        {
            this.damagedBlocks.remove(Integer.valueOf(p_147587_1_));
        }
    }

}
