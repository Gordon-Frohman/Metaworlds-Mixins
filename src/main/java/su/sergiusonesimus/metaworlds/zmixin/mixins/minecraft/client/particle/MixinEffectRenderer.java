package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.client.particle;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.MovingObjectPosition;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.client.entity.EntityClientPlayerMPSubWorldProxy;
import su.sergiusonesimus.metaworlds.entity.player.EntityPlayerProxy;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.client.particles.IMixinEffectRenderer;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.entity.IMixinEntity;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.util.IMixinMovingObjectPosition;

@Mixin(EffectRenderer.class)
public abstract class MixinEffectRenderer implements IMixinEffectRenderer {

    @Shadow(remap = true)
    protected World worldObj;

    @Shadow(remap = true)
    private List[] fxLayers = new List[4];

    @Shadow(remap = true)
    private TextureManager renderer;

    @Shadow(remap = true)
    private static ResourceLocation particleTextures;

    @Shadow(remap = true)
    private Random rand;

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

    @Inject(
        method = "renderParticles",
        at = { @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/particle/EntityFX;interpPosZ:D",
            opcode = Opcodes.PUTSTATIC,
            shift = At.Shift.AFTER) },
        locals = LocalCapture.CAPTURE_FAILSOFT)
    public void injectRenderParticles1(Entity entity, float f, CallbackInfo ci, float f1, float f2, float f3, float f4,
        float f5) {
        if (((IMixinWorld) this.worldObj).isSubWorld()) {
            Vec3 transformedViewDir = ((IMixinWorld) this.worldObj).rotateToLocal(f3, f5, f4);
            f3 = (float) transformedViewDir.xCoord;
            f5 = (float) transformedViewDir.yCoord;
            f4 = (float) transformedViewDir.zCoord;
            float planarMagSq = f3 * f3 + f4 * f4;
            if (planarMagSq > 0.0f) {
                float planarMag = (float) Math.sqrt(planarMagSq);
                if (entity.rotationPitch < 0.0f) planarMag = -planarMag;
                f1 = f4 / planarMag;
                f2 = -f3 / planarMag;
            }
        }
    }

    @Inject(
        method = "renderParticles",
        at = { @At(
            value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glAlphaFunc(IF)V",
            opcode = Opcodes.INVOKESTATIC,
            shift = At.Shift.AFTER) })
    public void injectRenderParticles2(Entity entity, float f, CallbackInfo ci) {
        if (((IMixinWorld) this.worldObj).isSubWorld()) {
            GL11.glPushMatrix();
            GL11.glTranslated(-EntityFX.interpPosX, -EntityFX.interpPosY, -EntityFX.interpPosZ);
            SubWorld parentSubWorld = (SubWorld) this.worldObj;
            GL11.glMultMatrix(parentSubWorld.getTransformToGlobalMatrixDirectBuffer());
            GL11.glTranslated(EntityFX.interpPosX, EntityFX.interpPosY, EntityFX.interpPosZ);
        }
    }

    @Inject(
        method = "renderParticles",
        at = { @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/Tessellator;draw()I",
            shift = At.Shift.AFTER) })
    public void injectRenderParticles3(Entity entity, float f, CallbackInfo ci) {
        if (((IMixinWorld) this.worldObj).isSubWorld()) {
            GL11.glPopMatrix();
        }
    }

    @Inject(method = "renderParticles", at = { @At(value = "TAIL") })
    public void injectRenderParticles4(Entity entity, float f, CallbackInfo ci) {
        if (!((IMixinWorld) this.worldObj).isSubWorld()) {
            for (EntityPlayerProxy curPlayer : ((IMixinEntity) entity).getPlayerProxyMap()
                .values()) {
                EntityClientPlayerMPSubWorldProxy curPlayerProxy = (EntityClientPlayerMPSubWorldProxy) curPlayer;
                curPlayerProxy.getMinecraft().effectRenderer.renderParticles(entity, f);
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

    @Overwrite(remap = false)
    public void addBlockHitEffects(int x, int y, int z, MovingObjectPosition target) {
        Block block = ((IMixinMovingObjectPosition) target).getWorld()
            .getBlock(x, y, z);
        if (block != null && !block
            .addHitEffects(((IMixinMovingObjectPosition) target).getWorld(), target, (EffectRenderer) (Object) this)) {
            ((IMixinEffectRenderer) this)
                .addBlockHitEffects(x, y, z, target.sideHit, ((IMixinMovingObjectPosition) target).getWorld());
        }
    }

    @Overwrite(remap = false)
    // addBlockDestroyEffects
    public void func_147215_a(int p_147215_1_, int p_147215_2_, int p_147215_3_, Block p_147215_4_, int p_147215_5_) {
        if (!p_147215_4_.isAir(worldObj, p_147215_1_, p_147215_2_, p_147215_3_) && !p_147215_4_.addDestroyEffects(
            worldObj,
            p_147215_1_,
            p_147215_2_,
            p_147215_3_,
            p_147215_5_,
            (EffectRenderer) (Object) this)) {
            byte b0 = 4;

            for (int i1 = 0; i1 < b0; ++i1) {
                for (int j1 = 0; j1 < b0; ++j1) {
                    for (int k1 = 0; k1 < b0; ++k1) {
                        double d0 = (double) p_147215_1_ + ((double) i1 + 0.5D) / (double) b0;
                        double d1 = (double) p_147215_2_ + ((double) j1 + 0.5D) / (double) b0;
                        double d2 = (double) p_147215_3_ + ((double) k1 + 0.5D) / (double) b0;
                        if (worldObj instanceof SubWorld) {
                            World parentWorld = ((SubWorld) this.worldObj).getParentWorld();
                            Vec3 globalCoords = ((IMixinWorld) this.worldObj)
                                .transformToGlobal(Vec3.createVectorHelper(d0, d1, d2));
                            Minecraft.getMinecraft().effectRenderer.addEffect(
                                (new EntityDiggingFX(
                                    parentWorld,
                                    globalCoords.xCoord,
                                    globalCoords.yCoord,
                                    globalCoords.zCoord,
                                    d0 - (double) p_147215_1_ - 0.5D,
                                    d1 - (double) p_147215_2_ - 0.5D,
                                    d2 - (double) p_147215_3_ - 0.5D,
                                    p_147215_4_,
                                    p_147215_5_)).applyColourMultiplier(p_147215_1_, p_147215_2_, p_147215_3_));
                        } else {
                            this.addEffect(
                                (new EntityDiggingFX(
                                    this.worldObj,
                                    d0,
                                    d1,
                                    d2,
                                    d0 - (double) p_147215_1_ - 0.5D,
                                    d1 - (double) p_147215_2_ - 0.5D,
                                    d2 - (double) p_147215_3_ - 0.5D,
                                    p_147215_4_,
                                    p_147215_5_)).applyColourMultiplier(p_147215_1_, p_147215_2_, p_147215_3_));
                        }
                    }
                }
            }
        }
    }

}
