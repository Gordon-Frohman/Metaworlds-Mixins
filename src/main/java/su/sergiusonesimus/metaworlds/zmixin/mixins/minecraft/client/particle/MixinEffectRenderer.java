package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.client.particle;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.client.entity.EntityClientPlayerMPSubWorldProxy;
import su.sergiusonesimus.metaworlds.entity.player.EntityPlayerProxy;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.client.particles.IMixinEffectRenderer;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.IMixinEntity;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.util.IMixinMovingObjectPosition;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

@Mixin(EffectRenderer.class)
public abstract class MixinEffectRenderer implements IMixinEffectRenderer {

    @Shadow(remap = true)
    protected World worldObj;

    @Shadow(remap = true)
    private Random rand;

    @Shadow(remap = true)
    private List[] fxLayers = new List[4];

    @Shadow(remap = true)
    private TextureManager renderer;

    @Shadow(remap = true)
    private static ResourceLocation particleTextures;

    // TODO

    @Shadow(remap = true)
    public abstract void addEffect(EntityFX p_78873_1_);

    @Inject(method = "updateEffects", at = @At("TAIL"))
    public void updateEffects(CallbackInfo info) {
        if (!((IMixinWorld) this.worldObj).isSubWorld()) {
            Entity globalPlayer = Minecraft.getMinecraft().thePlayer;
            for (EntityPlayerProxy curPlayer : ((IMixinEntity) globalPlayer).getPlayerProxyMap()
                .values()) {
                EntityClientPlayerMPSubWorldProxy curPlayerProxy = (EntityClientPlayerMPSubWorldProxy) curPlayer;
                curPlayerProxy.getMinecraft().effectRenderer.updateEffects();
            }
        }
    }

    /**
     * Renders all current particles. Args player, partialTickTime
     * 
     * @author Sergius Onesimus
     * @reason Water and outline boxes render messes up otherwise
     */
    @Overwrite
    public void renderParticles(Entity p_78874_1_, float p_78874_2_) {
        float f1 = ActiveRenderInfo.rotationX;
        float f2 = ActiveRenderInfo.rotationZ;
        float f3 = ActiveRenderInfo.rotationYZ;
        float f4 = ActiveRenderInfo.rotationXY;
        float f5 = ActiveRenderInfo.rotationXZ;
        EntityFX.interpPosX = p_78874_1_.lastTickPosX
            + (p_78874_1_.posX - p_78874_1_.lastTickPosX) * (double) p_78874_2_;
        EntityFX.interpPosY = p_78874_1_.lastTickPosY
            + (p_78874_1_.posY - p_78874_1_.lastTickPosY) * (double) p_78874_2_;
        EntityFX.interpPosZ = p_78874_1_.lastTickPosZ
            + (p_78874_1_.posZ - p_78874_1_.lastTickPosZ) * (double) p_78874_2_;
        if (((IMixinWorld) this.worldObj).isSubWorld()) {
            Vec3 transformedViewDir = ((IMixinWorld) this.worldObj).rotateToLocal(f3, f5, f4);
            f3 = (float) transformedViewDir.xCoord;
            f5 = (float) transformedViewDir.yCoord;
            f4 = (float) transformedViewDir.zCoord;
            float planarMagSq = f3 * f3 + f4 * f4;
            if (planarMagSq > 0.0f) {
                float planarMag = (float) Math.sqrt(planarMagSq);
                if (p_78874_1_.rotationPitch < 0.0f) planarMag = -planarMag;
                f1 = f4 / planarMag;
                f2 = -f3 / planarMag;
            }
        }

        for (int k = 0; k < 3; ++k) {
            final int i = k;

            if (!this.fxLayers[i].isEmpty()) {
                switch (i) {
                    case 0:
                    default:
                        this.renderer.bindTexture(particleTextures);
                        break;
                    case 1:
                        this.renderer.bindTexture(TextureMap.locationBlocksTexture);
                        break;
                    case 2:
                        this.renderer.bindTexture(TextureMap.locationItemsTexture);
                }

                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                GL11.glDepthMask(false);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glAlphaFunc(GL11.GL_GREATER, 0.003921569F);

                if (((IMixinWorld) this.worldObj).isSubWorld()) {
                    GL11.glPushMatrix();
                    GL11.glTranslated(-EntityFX.interpPosX, -EntityFX.interpPosY, -EntityFX.interpPosZ);
                    SubWorld parentSubWorld = (SubWorld) this.worldObj;
                    GL11.glMultMatrix(parentSubWorld.getTransformToGlobalMatrixDirectBuffer());
                    GL11.glTranslated(EntityFX.interpPosX, EntityFX.interpPosY, EntityFX.interpPosZ);
                }

                Tessellator tessellator = Tessellator.instance;
                tessellator.startDrawingQuads();

                for (int j = 0; j < this.fxLayers[i].size(); ++j) {
                    final EntityFX entityfx = (EntityFX) this.fxLayers[i].get(j);
                    if (entityfx == null) continue;
                    tessellator.setBrightness(entityfx.getBrightnessForRender(p_78874_2_));

                    try {
                        entityfx.renderParticle(tessellator, p_78874_2_, f1, f5, f2, f3, f4);
                    } catch (Throwable throwable) {
                        CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering Particle");
                        CrashReportCategory crashreportcategory = crashreport.makeCategory("Particle being rendered");
                        crashreportcategory.addCrashSectionCallable("Particle", new Callable() {

                            public String call() {
                                return entityfx.toString();
                            }
                        });
                        crashreportcategory.addCrashSectionCallable("Particle Type", new Callable() {

                            public String call() {
                                return i == 0 ? "MISC_TEXTURE"
                                    : (i == 1 ? "TERRAIN_TEXTURE"
                                        : (i == 2 ? "ITEM_TEXTURE"
                                            : (i == 3 ? "ENTITY_PARTICLE_TEXTURE" : "Unknown - " + i)));
                            }
                        });
                        throw new ReportedException(crashreport);
                    }
                }

                tessellator.draw();

                if (((IMixinWorld) this.worldObj).isSubWorld()) {
                    GL11.glPopMatrix();
                }

                GL11.glDisable(GL11.GL_BLEND);
                GL11.glDepthMask(true);
                GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
            }
        }

        if (!((IMixinWorld) this.worldObj).isSubWorld()) {
            for (EntityPlayerProxy curPlayer : ((IMixinEntity) p_78874_1_).getPlayerProxyMap()
                .values()) {
                EntityClientPlayerMPSubWorldProxy curPlayerProxy = (EntityClientPlayerMPSubWorldProxy) curPlayer;
                curPlayerProxy.getMinecraft().effectRenderer.renderParticles(p_78874_1_, p_78874_2_);
            }
        }
    }

    /**
     * Adds block hit particles for the specified block. Args: x, y, z, sideHit
     */
    public void addBlockHitEffects(int par1, int par2, int par3, int par4, World parWorldObj) {
        Block block = parWorldObj.getBlock(par1, par2, par3);

        if (block.getMaterial() != Material.air) {
            float f = 0.1F;
            double d0 = (double) par1
                + this.rand.nextDouble()
                    * (block.getBlockBoundsMaxX() - block.getBlockBoundsMinX() - (double) (f * 2.0F))
                + (double) f
                + block.getBlockBoundsMinX();
            double d1 = (double) par2
                + this.rand.nextDouble()
                    * (block.getBlockBoundsMaxY() - block.getBlockBoundsMinY() - (double) (f * 2.0F))
                + (double) f
                + block.getBlockBoundsMinY();
            double d2 = (double) par3
                + this.rand.nextDouble()
                    * (block.getBlockBoundsMaxZ() - block.getBlockBoundsMinZ() - (double) (f * 2.0F))
                + (double) f
                + block.getBlockBoundsMinZ();

            if (par4 == 0) {
                d1 = (double) par2 + block.getBlockBoundsMinY() - (double) f;
            }

            if (par4 == 1) {
                d1 = (double) par2 + block.getBlockBoundsMaxY() + (double) f;
            }

            if (par4 == 2) {
                d2 = (double) par3 + block.getBlockBoundsMinZ() - (double) f;
            }

            if (par4 == 3) {
                d2 = (double) par3 + block.getBlockBoundsMaxZ() + (double) f;
            }

            if (par4 == 4) {
                d0 = (double) par1 + block.getBlockBoundsMinX() - (double) f;
            }

            if (par4 == 5) {
                d0 = (double) par1 + block.getBlockBoundsMaxX() + (double) f;
            }

            this.addEffect(
                (new EntityDiggingFX(
                    parWorldObj,
                    d0,
                    d1,
                    d2,
                    0.0D,
                    0.0D,
                    0.0D,
                    block,
                    parWorldObj.getBlockMetadata(par1, par2, par3))).applyColourMultiplier(par1, par2, par3)
                        .multiplyVelocity(0.2F)
                        .multipleParticleScaleBy(0.6F));
        }
    }

    // addBlockHitEffects

    private MovingObjectPosition storedTarget;

    @Inject(
        method = "addBlockHitEffects(IIILnet/minecraft/util/MovingObjectPosition;)V",
        remap = false,
        at = { @At(value = "HEAD") })
    private void captureTarget(int x, int y, int z, MovingObjectPosition target, CallbackInfo ci) {
        this.storedTarget = target;
    }

    @Redirect(
        method = "addBlockHitEffects(IIILnet/minecraft/util/MovingObjectPosition;)V",
        remap = false,
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/particle/EffectRenderer;worldObj:Lnet/minecraft/world/World;",
            opcode = Opcodes.GETFIELD))
    private World getWorldObj(EffectRenderer effectRenderer) {
        return ((IMixinMovingObjectPosition) storedTarget).getWorld();
    }

    @Redirect(
        method = "addBlockHitEffects(IIILnet/minecraft/util/MovingObjectPosition;)V",
        remap = false,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/EffectRenderer;addBlockHitEffects(IIII)V"))
    private void addBlockHitEffects(EffectRenderer effectRenderer, int x, int y, int z, int side) {
        ((IMixinEffectRenderer) this)
            .addBlockHitEffects(x, y, z, side, ((IMixinMovingObjectPosition) storedTarget).getWorld());
    }

    // addBlockDestroyEffects

    private double storedD0;
    private double storedD1;
    private double storedD2;
    private int storedX;
    private int storedY;
    private int storedZ;
    private Block storedBlock;
    private int storedMeta;

    @Inject(
        method = "func_147215_a(IIILnet/minecraft/block/Block;I)V",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/particle/EffectRenderer;addEffect(Lnet/minecraft/client/particle/EntityFX;)V"))
    private void storeData(int x, int y, int z, Block block, int meta, CallbackInfo ci, @Local(name = "d0") double d0,
        @Local(name = "d1") double d1, @Local(name = "d2") double d2) {
        storedD0 = d0;
        storedD1 = d1;
        storedD2 = d2;
        storedX = x;
        storedY = y;
        storedZ = z;
        storedBlock = block;
        storedMeta = meta;
    }

    @WrapOperation(
        method = "func_147215_a(IIILnet/minecraft/block/Block;I)V",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/particle/EffectRenderer;addEffect(Lnet/minecraft/client/particle/EntityFX;)V"))
    private void wrapAddEffect(EffectRenderer instance, EntityFX value, Operation<Void> original) {
        if (worldObj instanceof SubWorld) {
            World parentWorld = ((SubWorld) this.worldObj).getParentWorld();
            Vec3 globalCoords = ((IMixinWorld) this.worldObj)
                .transformToGlobal(Vec3.createVectorHelper(storedD0, storedD1, storedD2));
            Minecraft.getMinecraft().effectRenderer.addEffect(
                (new EntityDiggingFX(
                    parentWorld,
                    globalCoords.xCoord,
                    globalCoords.yCoord,
                    globalCoords.zCoord,
                    storedD0 - (double) storedX - 0.5D,
                    storedD1 - (double) storedY - 0.5D,
                    storedD2 - (double) storedZ - 0.5D,
                    storedBlock,
                    storedMeta)).applyColourMultiplier(storedX, storedY, storedZ));
        } else {
            original.call(instance, value);
        }
    }

}
