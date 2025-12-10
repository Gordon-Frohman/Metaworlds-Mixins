package su.sergiusonesimus.metaworlds.zmixin.mixins.angelica;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.EntitySorter;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderList;
import net.minecraft.client.renderer.RenderSorter;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.util.RenderDistanceSorter;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import org.lwjgl.opengl.ARBOcclusionQuery;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.client.multiplayer.SubWorldClient;
import su.sergiusonesimus.metaworlds.zmixin.MixinPriorities;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.client.renderer.IMixinRenderList;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

@Mixin(value = RenderGlobal.class, priority = MixinPriorities.ANGELICA)
public class MixinRenderGlobal {

    @Shadow(remap = false)
    private List<WorldRenderer> worldRenderersList;

    @Shadow(remap = false)
    private List<WorldRenderer> sortedWorldRenderersList;

    @Shadow(remap = false)
    private Map<Integer, WorldRenderer> worldRenderersMap;

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
    private List<WorldRenderer> worldRenderersToUpdate;

    @Shadow(remap = true)
    private int renderChunksWide;

    @Shadow(remap = true)
    private int renderChunksTall;

    @Shadow(remap = true)
    private int renderChunksDeep;

    @SuppressWarnings("rawtypes")
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
    public List<WorldRenderer> glRenderLists;

    @Shadow(remap = true)
    public RenderList[] allRenderLists;

    @Shadow(remap = true)
    private int frustumCheckOffset;

    @Shadow(remap = true)
    public RenderBlocks renderBlocksRg;

    @Shadow(remap = true)
    public IIcon[] destroyBlockIcons;

    // TODO

    @Shadow(remap = false)
    public void markRenderersForNewPositionSingle(double par1d, double par2d, double par3d, int subWorldID) {}

    @Shadow(remap = false)
    public WorldRenderer createWorldRenderer(World targetWorld, int chunkIndexX, int chunkIndexY, int chunkIndexZ) {
        return null;
    }

    @Shadow(remap = false)
    public void removeWorldRenderer(World targetWorld, int chunkIndexX, int chunkIndexY, int chunkIndexZ) {};

    @Shadow(remap = false)
    public void markRenderersForNewPositionSubworlds(double par1, double pass, double partialTicks) {}

    @Shadow(remap = true)
    private void renderStars() {}

    @Shadow(remap = true)
    protected void onStaticEntitiesChanged() {};

    // These methods are now overwritten by Angelica
    // To fix rendering issues, we re-overwrite them, leaving only subworlds renderers

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

            for (World curWorld : ((IMixinWorld) this.theWorld).getSubWorlds()) {
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
                    this.markRenderersForNewPositionSubworlds(
                        entitylivingbase.posX,
                        entitylivingbase.posY,
                        entitylivingbase.posZ);
                    Collections.sort(this.sortedWorldRenderersList, new EntitySorter(entitylivingbase));
                }
            }

            this.renderEntitiesStartupCounter = 2;
        }
    }

    @WrapOperation(
        method = "sortAndRender",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/EntityRenderer;disableLightmap(D)V"))
    private void sortAndRender(EntityRenderer entityRenderer, double partialTicks, Operation<Void> original) {
        // We will disable it later
    }

    @SuppressWarnings({ "unused", "unchecked" })
    @Inject(method = "sortAndRender", at = @At("RETURN"), cancellable = true)
    private void sortAndRender(EntityLivingBase entity, int pass, double partialTicks,
        CallbackInfoReturnable<Integer> ci) {
        this.theWorld.theProfiler.startSection("sortchunks");

        if (this.worldRenderersList.size() > 0) for (int j = 0; j < 10; ++j) {
            this.worldRenderersCheckIndex = (this.worldRenderersCheckIndex + 1) % this.worldRenderersList.size();
            WorldRenderer worldrenderer = this.worldRenderersList.get(this.worldRenderersCheckIndex);

            if (worldrenderer.needsUpdate && !this.worldRenderersToUpdate.contains(worldrenderer)) {
                this.worldRenderersToUpdate.add(worldrenderer);
            }
        }

        if (pass == 0) {
            this.renderersLoaded = 0;
            this.dummyRenderInt = 0;
            this.renderersBeingClipped = 0;
            this.renderersBeingOccluded = 0;
            this.renderersBeingRendered = 0;
            this.renderersSkippingRenderPass = 0;
        }

        double d9 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
        double d1 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
        double d2 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;
        double d3 = entity.posX - this.prevSortX;
        double d4 = entity.posY - this.prevSortY;
        double d5 = entity.posZ - this.prevSortZ;

        if (this.prevChunkSortX != entity.chunkCoordX || this.prevChunkSortY != entity.chunkCoordY
            || this.prevChunkSortZ != entity.chunkCoordZ
            || d3 * d3 + d4 * d4 + d5 * d5 > 16.0D) {
            this.prevSortX = entity.posX;
            this.prevSortY = entity.posY;
            this.prevSortZ = entity.posZ;
            this.prevChunkSortX = entity.chunkCoordX;
            this.prevChunkSortY = entity.chunkCoordY;
            this.prevChunkSortZ = entity.chunkCoordZ;
            this.markRenderersForNewPositionSubworlds(entity.posX, entity.posY, entity.posZ);
            Collections.sort(this.sortedWorldRenderersList, new EntitySorter(entity));
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
            if (rendererPositionsChanged) Collections.sort(this.sortedWorldRenderersList, new EntitySorter(entity));
        }

        double d6 = entity.posX - this.prevRenderSortX;
        double d7 = entity.posY - this.prevRenderSortY;
        double d8 = entity.posZ - this.prevRenderSortZ;
        int k;

        if (d6 * d6 + d7 * d7 + d8 * d8 > 1.0D) {
            this.prevRenderSortX = entity.posX;
            this.prevRenderSortY = entity.posY;
            this.prevRenderSortZ = entity.posZ;

            if (this.sortedWorldRenderersList.size() > 0) for (k = 0; k < 27; ++k) {
                if (k >= this.sortedWorldRenderersList.size()) break;
                this.sortedWorldRenderersList.get(k)
                    .updateRendererSort(entity);
            }
        }

        RenderHelper.disableStandardItemLighting();
        byte b1 = 0;

        if (this.occlusionEnabled && this.mc.gameSettings.advancedOpengl
            && !this.mc.gameSettings.anaglyph
            && pass == 0) {
            int index = 0;
            for (WorldRenderer curRenderer : this.sortedWorldRenderersList) {
                if (curRenderer.distanceToEntitySquared(entity) > 675.0f)/*
                                                                          * 3 * (16/2 + 6)Ġ = 588... let's
                                                                          * add some space
                                                                          */
                    break;
                index++;
            }

            byte b0 = 0;
            int l = Math.max(16, index);// 16;
            this.checkOcclusionQueryResult(b0, l);

            for (int i1 = b0; i1 < l && i1 < this.sortedWorldRenderersList.size(); ++i1) {
                this.sortedWorldRenderersList.get(i1).isVisible = true;
            }

            this.theWorld.theProfiler.endStartSection("render");
            k = b1 + this.renderSortedRenderers(b0, l, pass, partialTicks);

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

                for (int j1 = l1; j1 < l && j1 < this.sortedWorldRenderersList.size(); ++j1) {
                    WorldRenderer curRenderer = this.sortedWorldRenderersList.get(j1);

                    if (curRenderer.skipAllRenderPasses()) {
                        curRenderer.isInFrustum = false;
                    } else {
                        if (!curRenderer.isInFrustum) {
                            curRenderer.isVisible = true;
                        }

                        if (curRenderer.isInFrustum && !curRenderer.isWaitingOnOcclusionQuery) {
                            float f2 = MathHelper.sqrt_float(curRenderer.distanceToEntitySquared(entity));
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
                k += this.renderSortedRenderers(l1, l, pass, partialTicks);
            } while (l < this.sortedWorldRenderersList.size());
        } else {
            this.theWorld.theProfiler.endStartSection("render");
            k = b1 + this.renderSortedRenderers(0, this.sortedWorldRenderersList.size(), pass, partialTicks);
        }

        this.mc.entityRenderer.disableLightmap(partialTicks);
        this.theWorld.theProfiler.endSection();
        ci.setReturnValue(k);
        ci.cancel();
    }

    @Overwrite
    public void checkOcclusionQueryResult(int p_72720_1_, int p_72720_2_) {
        for (int k = p_72720_1_; k < p_72720_2_ && k < this.sortedWorldRenderersList.size(); ++k) {
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

    @SuppressWarnings("unchecked")
    @Inject(method = "updateRenderers", at = @At(value = "RETURN"), cancellable = true)
    public void updateRenderers(EntityLivingBase p_72716_1_, boolean p_72716_2_, CallbackInfoReturnable<Boolean> ci) {
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
                ci.setReturnValue(i == j + k);
                ci.cancel();
                break;
            }

            this.worldRenderersToUpdate.remove(k1);
        }
    }

    @Inject(method = "markBlocksForUpdate", at = @At(value = "TAIL"))
    public void markBlocksForUpdate(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, CallbackInfo ci) {
        markBlocksForUpdateSubworlds(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public void markBlocksForUpdateSubworlds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        int k1 = MathHelper.bucketInt(minX, 16);
        int l1 = MathHelper.bucketInt(minY, 16);
        int i2 = MathHelper.bucketInt(minZ, 16);
        int j2 = MathHelper.bucketInt(maxX, 16);
        int k2 = MathHelper.bucketInt(maxY, 16);
        int l2 = MathHelper.bucketInt(maxZ, 16);

        for (World curWorld : ((IMixinWorld) this.theWorld).getSubWorlds()) {
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

    @Inject(method = "clipRenderersByFrustum", at = @At(value = "TAIL"))
    public void clipRenderersByFrustum(ICamera p_72729_1_, float p_72729_2_, CallbackInfo ci) {
        for (Map.Entry<Integer, WorldRenderer> curRendererEntry : this.worldRenderersMap.entrySet()) {
            WorldRenderer curRenderer = curRendererEntry.getValue();
            if (!curRenderer.skipAllRenderPasses()
                && (!curRenderer.isInFrustum || (curRendererEntry.getKey() + this.frustumCheckOffset & 15) == 0)) {
                curRenderer.updateInFrustum(p_72729_1_);
            }
        }

        ++this.frustumCheckOffset;
    }

    /**
     * Render all render lists
     */
    @Overwrite
    public void renderAllRenderLists(int p_72733_1_, double p_72733_2_) {
        for (int j = 0; j < this.allRenderLists.length; ++j) {
            this.allRenderLists[j].callLists();
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

}
