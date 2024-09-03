package su.sergiusonesimus.metaworlds.mixin.mixins.minecraft.client.renderer;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.client.renderer.culling.Frustrum;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import su.sergiusonesimus.metaworlds.api.IMixinWorld;
import su.sergiusonesimus.metaworlds.mixin.interfaces.util.IMixinAxisAlignedBB;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {

    @Shadow(remap = true)
    private Minecraft mc;

    @Shadow(remap = true)
    private Entity pointedEntity;

    @Shadow(remap = true)
    private boolean lightmapUpdateNeeded;

    @Shadow(remap = true)
    public static int anaglyphField;

    @Shadow(remap = true)
    public int debugViewDirection;

    @Shadow(remap = true)
    private double cameraZoom;

    // TODO

    @Shadow(remap = true)
    protected abstract void renderHand(float p_78476_1_, int p_78476_2_);

    @Shadow(remap = true)
    protected abstract void renderRainSnow(float p_78474_1_);

    @Shadow(remap = true)
    public abstract void enableLightmap(double p_78463_1_);

    @Shadow(remap = true)
    public abstract void disableLightmap(double p_78483_1_);

    @Shadow(remap = true)
    protected abstract void renderCloudsCheck(RenderGlobal p_82829_1_, float p_82829_2_);

    @Shadow(remap = true)
    protected abstract void setupFog(int p_78468_1_, float p_78468_2_);

    @Shadow(remap = true)
    protected abstract void updateFogColor(float p_78466_1_);

    @Shadow(remap = true)
    protected abstract void updateLightmap(float p_78472_1_);

    @Shadow(remap = true)
    protected abstract void setupCameraTransform(float p_78479_1_, int p_78479_2_);

    /**
     * Finds what block or object the mouse is over at the specified partial tick time. Args: partialTickTime
     */
    @Overwrite
    public void getMouseOver(float p_78473_1_) {
        if (this.mc.renderViewEntity != null) {
            if (this.mc.theWorld != null) {
                this.mc.pointedEntity = null;
                double d0 = (double) this.mc.playerController.getBlockReachDistance();
                this.mc.objectMouseOver = this.mc.renderViewEntity.rayTrace(d0, p_78473_1_);
                double d1 = d0;
                Vec3 vec3 = this.mc.renderViewEntity.getPosition(p_78473_1_);

                if (this.mc.playerController.extendedReach()) {
                    d0 = 6.0D;
                    d1 = 6.0D;
                } else {
                    if (d0 > 3.0D) {
                        d1 = 3.0D;
                    }

                    d0 = d1;
                }

                if (this.mc.objectMouseOver != null) {
                    d1 = this.mc.objectMouseOver.hitVec.distanceTo(vec3);
                }

                Vec3 vec31 = this.mc.renderViewEntity.getLook(p_78473_1_);
                Vec3 vec32 = vec3.addVector(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0);
                this.pointedEntity = null;
                Vec3 vec33 = null;
                float f1 = 1.0F;
                List list = this.mc.theWorld.getEntitiesWithinAABBExcludingEntity(
                    this.mc.renderViewEntity,
                    this.mc.renderViewEntity.boundingBox
                        .addCoord(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0)
                        .expand((double) f1, (double) f1, (double) f1));
                double d2 = d1;

                for (int i = 0; i < list.size(); ++i) {
                    Entity entity = (Entity) list.get(i);

                    if (entity.canBeCollidedWith()) {
                        float f2 = entity.getCollisionBorderSize();
                        AxisAlignedBB axisalignedbb = ((IMixinAxisAlignedBB) entity.boundingBox
                            .expand((double) f2, (double) f2, (double) f2))
                                .getTransformedToGlobalBoundingBox(entity.worldObj);
                        MovingObjectPosition movingobjectposition = ((IMixinAxisAlignedBB) axisalignedbb)
                            .calculateIntercept(vec3, vec32, entity.worldObj);

                        if (axisalignedbb.isVecInside(vec3)) {
                            if (0.0D < d2 || d2 == 0.0D) {
                                this.pointedEntity = entity;
                                vec33 = movingobjectposition == null ? vec3 : movingobjectposition.hitVec;
                                d2 = 0.0D;
                            }
                        } else if (movingobjectposition != null) {
                            double d3 = vec3.distanceTo(movingobjectposition.hitVec);

                            if (d3 < d2 || d2 == 0.0D) {
                                if (entity == this.mc.renderViewEntity.ridingEntity && !entity.canRiderInteract()) {
                                    if (d2 == 0.0D) {
                                        this.pointedEntity = entity;
                                        vec33 = movingobjectposition.hitVec;
                                    }
                                } else {
                                    this.pointedEntity = entity;
                                    vec33 = movingobjectposition.hitVec;
                                    d2 = d3;
                                }
                            }
                        }
                    }
                }

                if (this.pointedEntity != null && (d2 < d1 || this.mc.objectMouseOver == null)) {
                    this.mc.objectMouseOver = new MovingObjectPosition(this.pointedEntity, vec33);

                    if (this.pointedEntity instanceof EntityLivingBase
                        || this.pointedEntity instanceof EntityItemFrame) {
                        this.mc.pointedEntity = this.pointedEntity;
                    }
                }
            }
        }
    }

    @Overwrite
    public void renderWorld(float p_78471_1_, long p_78471_2_) {
        this.mc.mcProfiler.startSection("lightTex");

        if (this.lightmapUpdateNeeded) {
            this.updateLightmap(p_78471_1_);
        }

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.5F);

        if (this.mc.renderViewEntity == null) {
            this.mc.renderViewEntity = this.mc.thePlayer;
        }

        this.mc.mcProfiler.endStartSection("pick");
        for (World curWorld : ((IMixinWorld) this.mc.theWorld).getSubWorlds())
            ((IMixinWorld) curWorld).doTickPartial(p_78471_1_);

        this.getMouseOver(p_78471_1_);
        EntityLivingBase entitylivingbase = this.mc.renderViewEntity;
        RenderGlobal renderglobal = this.mc.renderGlobal;
        EffectRenderer effectrenderer = this.mc.effectRenderer;
        double d0 = entitylivingbase.lastTickPosX
            + (entitylivingbase.posX - entitylivingbase.lastTickPosX) * (double) p_78471_1_;
        double d1 = entitylivingbase.lastTickPosY
            + (entitylivingbase.posY - entitylivingbase.lastTickPosY) * (double) p_78471_1_;
        double d2 = entitylivingbase.lastTickPosZ
            + (entitylivingbase.posZ - entitylivingbase.lastTickPosZ) * (double) p_78471_1_;
        this.mc.mcProfiler.endStartSection("center");

        for (int j = 0; j < 2; ++j) {
            if (this.mc.gameSettings.anaglyph) {
                anaglyphField = j;

                if (anaglyphField == 0) {
                    GL11.glColorMask(false, true, true, false);
                } else {
                    GL11.glColorMask(true, false, false, false);
                }
            }

            this.mc.mcProfiler.endStartSection("clear");
            GL11.glViewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
            this.updateFogColor(p_78471_1_);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
            GL11.glEnable(GL11.GL_CULL_FACE);
            this.mc.mcProfiler.endStartSection("camera");
            this.setupCameraTransform(p_78471_1_, j);
            ActiveRenderInfo.updateRenderInfo(this.mc.thePlayer, this.mc.gameSettings.thirdPersonView == 2);
            this.mc.mcProfiler.endStartSection("frustrum");
            ClippingHelperImpl.getInstance();

            if (this.mc.gameSettings.renderDistanceChunks >= 4) {
                this.setupFog(-1, p_78471_1_);
                this.mc.mcProfiler.endStartSection("sky");
                renderglobal.renderSky(p_78471_1_);
            }

            GL11.glEnable(GL11.GL_FOG);
            this.setupFog(1, p_78471_1_);

            if (this.mc.gameSettings.ambientOcclusion != 0) {
                GL11.glShadeModel(GL11.GL_SMOOTH);
            }

            this.mc.mcProfiler.endStartSection("culling");
            Frustrum frustrum = new Frustrum();
            frustrum.setPosition(d0, d1, d2);
            this.mc.renderGlobal.clipRenderersByFrustum(frustrum, p_78471_1_);

            if (j == 0) {
                this.mc.mcProfiler.endStartSection("updatechunks");

                while (!this.mc.renderGlobal.updateRenderers(entitylivingbase, false) && p_78471_2_ != 0L) {
                    long k = p_78471_2_ - System.nanoTime();

                    if (k < 0L || k > 1000000000L) {
                        break;
                    }
                }
            }

            if (entitylivingbase.posY < 128.0D) {
                this.renderCloudsCheck(renderglobal, p_78471_1_);
            }

            this.mc.mcProfiler.endStartSection("prepareterrain");
            this.setupFog(0, p_78471_1_);
            GL11.glEnable(GL11.GL_FOG);
            this.mc.getTextureManager()
                .bindTexture(TextureMap.locationBlocksTexture);
            RenderHelper.disableStandardItemLighting();
            this.mc.mcProfiler.endStartSection("terrain");
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glPushMatrix();
            renderglobal.sortAndRender(entitylivingbase, 0, (double) p_78471_1_);
            GL11.glShadeModel(GL11.GL_FLAT);
            GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
            EntityPlayer entityplayer;

            if (this.debugViewDirection == 0) {
                GL11.glMatrixMode(GL11.GL_MODELVIEW);
                GL11.glPopMatrix();
                GL11.glPushMatrix();
                RenderHelper.enableStandardItemLighting();
                this.mc.mcProfiler.endStartSection("entities");
                net.minecraftforge.client.ForgeHooksClient.setRenderPass(0);
                renderglobal.renderEntities(entitylivingbase, frustrum, p_78471_1_);
                net.minecraftforge.client.ForgeHooksClient.setRenderPass(0);
                // ToDo: Try and figure out how to make particles render sorted correctly.. {They render behind water}
                RenderHelper.disableStandardItemLighting();
                this.disableLightmap((double) p_78471_1_);
                GL11.glMatrixMode(GL11.GL_MODELVIEW);
                GL11.glPopMatrix();
                GL11.glPushMatrix();

                if (this.mc.objectMouseOver != null && entitylivingbase.isInsideOfMaterial(Material.water)
                    && entitylivingbase instanceof EntityPlayer
                    && !this.mc.gameSettings.hideGUI) {
                    entityplayer = (EntityPlayer) entitylivingbase;
                    GL11.glDisable(GL11.GL_ALPHA_TEST);
                    this.mc.mcProfiler.endStartSection("outline");
                    if (!ForgeHooksClient.onDrawBlockHighlight(
                        renderglobal,
                        entityplayer,
                        mc.objectMouseOver,
                        0,
                        entityplayer.inventory.getCurrentItem(),
                        p_78471_1_)) {
                        renderglobal.drawSelectionBox(entityplayer, this.mc.objectMouseOver, 0, p_78471_1_);
                    }
                    GL11.glEnable(GL11.GL_ALPHA_TEST);
                }
            }

            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glPopMatrix();

            if (this.cameraZoom == 1.0D && entitylivingbase instanceof EntityPlayer
                && !this.mc.gameSettings.hideGUI
                && this.mc.objectMouseOver != null
                && !entitylivingbase.isInsideOfMaterial(Material.water)) {
                entityplayer = (EntityPlayer) entitylivingbase;
                GL11.glDisable(GL11.GL_ALPHA_TEST);
                this.mc.mcProfiler.endStartSection("outline");
                if (!ForgeHooksClient.onDrawBlockHighlight(
                    renderglobal,
                    entityplayer,
                    mc.objectMouseOver,
                    0,
                    entityplayer.inventory.getCurrentItem(),
                    p_78471_1_)) {
                    renderglobal.drawSelectionBox(entityplayer, this.mc.objectMouseOver, 0, p_78471_1_);
                }
                GL11.glEnable(GL11.GL_ALPHA_TEST);
            }

            this.mc.mcProfiler.endStartSection("destroyProgress");
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 1, 1, 0);
            renderglobal.drawBlockDamageTexture(Tessellator.instance, entitylivingbase, p_78471_1_);
            GL11.glDisable(GL11.GL_BLEND);

            if (this.debugViewDirection == 0) {
                this.enableLightmap((double) p_78471_1_);
                this.mc.mcProfiler.endStartSection("litParticles");
                effectrenderer.renderLitParticles(entitylivingbase, p_78471_1_);
                RenderHelper.disableStandardItemLighting();
                this.setupFog(0, p_78471_1_);
                this.mc.mcProfiler.endStartSection("particles");
                effectrenderer.renderParticles(entitylivingbase, p_78471_1_);
                this.disableLightmap((double) p_78471_1_);
            }

            GL11.glDepthMask(false);
            GL11.glEnable(GL11.GL_CULL_FACE);
            this.mc.mcProfiler.endStartSection("weather");
            this.renderRainSnow(p_78471_1_);
            GL11.glDepthMask(true);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_CULL_FACE);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
            this.setupFog(0, p_78471_1_);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glDepthMask(false);
            this.mc.getTextureManager()
                .bindTexture(TextureMap.locationBlocksTexture);

            if (this.mc.gameSettings.fancyGraphics) {
                this.mc.mcProfiler.endStartSection("water");

                if (this.mc.gameSettings.ambientOcclusion != 0) {
                    GL11.glShadeModel(GL11.GL_SMOOTH);
                }

                GL11.glEnable(GL11.GL_BLEND);
                OpenGlHelper.glBlendFunc(770, 771, 1, 0);

                if (this.mc.gameSettings.anaglyph) {
                    if (anaglyphField == 0) {
                        GL11.glColorMask(false, true, true, true);
                    } else {
                        GL11.glColorMask(true, false, false, true);
                    }

                    renderglobal.sortAndRender(entitylivingbase, 1, (double) p_78471_1_);
                } else {
                    renderglobal.sortAndRender(entitylivingbase, 1, (double) p_78471_1_);
                }

                GL11.glDisable(GL11.GL_BLEND);
                GL11.glShadeModel(GL11.GL_FLAT);
            } else {
                this.mc.mcProfiler.endStartSection("water");
                renderglobal.sortAndRender(entitylivingbase, 1, (double) p_78471_1_);
            }

            if (this.debugViewDirection == 0) // Only render if render pass 0 happens as well.
            {
                RenderHelper.enableStandardItemLighting();
                this.mc.mcProfiler.endStartSection("entities");
                ForgeHooksClient.setRenderPass(1);
                renderglobal.renderEntities(entitylivingbase, frustrum, p_78471_1_);
                ForgeHooksClient.setRenderPass(-1);
                RenderHelper.disableStandardItemLighting();
            }

            GL11.glDepthMask(true);
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_FOG);

            if (entitylivingbase.posY >= 128.0D) {
                this.mc.mcProfiler.endStartSection("aboveClouds");
                this.renderCloudsCheck(renderglobal, p_78471_1_);
            }

            this.mc.mcProfiler.endStartSection("FRenderLast");
            ForgeHooksClient.dispatchRenderLast(renderglobal, p_78471_1_);

            this.mc.mcProfiler.endStartSection("hand");

            if (!ForgeHooksClient.renderFirstPersonHand(renderglobal, p_78471_1_, j) && this.cameraZoom == 1.0D) {
                GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
                this.renderHand(p_78471_1_, j);
            }

            if (!this.mc.gameSettings.anaglyph) {
                this.mc.mcProfiler.endSection();
                return;
            }
        }

        GL11.glColorMask(true, true, true, false);
        this.mc.mcProfiler.endSection();
    }

}
