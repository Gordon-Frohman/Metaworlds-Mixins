package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.client.renderer;

import java.nio.IntBuffer;
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
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderList;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.util.OrientedBB;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.client.renderer.IMixinDestroyBlockProgress;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.client.renderer.IMixinRenderGlobal;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.IMixinEntity;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.util.IMixinAxisAlignedBB;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.util.IMixinMovingObjectPosition;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

@Mixin(value = RenderGlobal.class)
public class MixinRenderGlobal implements IMixinRenderGlobal {

    @Shadow(remap = true)
    public Map<Integer, DestroyBlockProgress> damagedBlocks;

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

    @Shadow(remap = true)
    public IntBuffer occlusionResult;

    @Shadow(remap = true)
    private int cloudTickCounter;

    @Shadow(remap = true)
    public List<WorldRenderer> glRenderLists;

    @Shadow(remap = true)
    public RenderList[] allRenderLists;

    @Shadow(remap = true)
    public RenderBlocks renderBlocksRg;

    @Shadow(remap = true)
    public IIcon[] destroyBlockIcons;

    // To get an empty RenderGlobal variable

    private boolean generateEmpty = false;

    @Inject(
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/renderer/RenderGlobal;mc:Lnet/minecraft/client/Minecraft;"),
        method = "<init>")
    private void init(Minecraft mc, CallbackInfo ci) {
        if (mc == null) generateEmpty = true;
    }

    @WrapOperation(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/renderer/RenderGlobal;mc:Lnet/minecraft/client/Minecraft;",
            opcode = Opcodes.PUTFIELD))
    private void wrapMc(RenderGlobal instance, Minecraft value, Operation<Void> original) {
        if (!generateEmpty) original.call(instance, value);
    }

    @WrapOperation(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;getTextureManager()Lnet/minecraft/client/renderer/texture/TextureManager;"))
    public TextureManager wrapGetTextureManager(Minecraft instance, Operation<TextureManager> original) {
        if (generateEmpty) return null;
        else return original.call(instance);
    }

    @WrapOperation(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/renderer/RenderGlobal;renderEngine:Lnet/minecraft/client/renderer/texture/TextureManager;",
            opcode = Opcodes.PUTFIELD))
    private void wrapRenderEngine(RenderGlobal instance, TextureManager value, Operation<Void> original) {
        if (!generateEmpty) original.call(instance, value);
    }

    @WrapOperation(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/renderer/RenderGlobal;glRenderListBase:I",
            opcode = Opcodes.PUTFIELD))
    private void wrapGlRenderListBase(RenderGlobal instance, int value, Operation<Void> original) {
        if (!generateEmpty) original.call(instance, value);
    }

    @WrapOperation(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/renderer/RenderGlobal;displayListEntities:I",
            opcode = Opcodes.PUTFIELD))
    private void wrapDisplayListEntities(RenderGlobal instance, int value, Operation<Void> original) {
        if (!generateEmpty) original.call(instance, value);
    }

    @WrapOperation(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/renderer/RenderGlobal;displayListEntitiesDirty:Z",
            opcode = Opcodes.PUTFIELD))
    private void wrapDisplayListEntitiesDirty(RenderGlobal instance, boolean value, Operation<Void> original) {
        if (!generateEmpty) original.call(instance, value);
    }

    @WrapOperation(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/renderer/RenderGlobal;occlusionEnabled:Z",
            opcode = Opcodes.PUTFIELD))
    private void wrapOcclusionEnabled(RenderGlobal instance, boolean value, Operation<Void> original) {
        if (!generateEmpty) original.call(instance, value);
    }

    @WrapOperation(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/renderer/RenderGlobal;occlusionEnabled:Z",
            opcode = Opcodes.GETFIELD))
    private boolean wrapOcclusionEnabled(RenderGlobal instance, Operation<Boolean> original) {
        if (generateEmpty) return false;
        else return original.call(instance);
    }

    @WrapOperation(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/renderer/RenderGlobal;starGLCallList:I",
            opcode = Opcodes.PUTFIELD))
    private void wrapStarGLCallList(RenderGlobal instance, int value, Operation<Void> original) {
        if (!generateEmpty) original.call(instance, value);
    }

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glPushMatrix()V"))
    public void wrapGLPushMatrix(Operation<Void> original) {
        if (!generateEmpty) original.call();
    }

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glNewList(II)V"))
    public void wrapGLNewList(int i1, int i2, Operation<Void> original) {
        if (!generateEmpty) original.call(i1, i2);
    }

    @WrapOperation(
        method = "<init>",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderGlobal;renderStars()V"))
    public void wrapRenderStars(RenderGlobal instance, Operation<Void> original) {
        if (!generateEmpty) original.call(instance);
    }

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glEndList()V"))
    public void wrapGLEndList(Operation<Void> original) {
        if (!generateEmpty) original.call();
    }

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glPopMatrix()V"))
    public void wrapGLPopMatrix(Operation<Void> original) {
        if (!generateEmpty) original.call();
    }

    @WrapOperation(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/renderer/Tessellator;instance:Lnet/minecraft/client/renderer/Tessellator;",
            opcode = Opcodes.GETSTATIC))
    private Tessellator wrapTessellatorInstance(Operation<Tessellator> original) {
        if (generateEmpty) return null;
        else return original.call();
    }

    @WrapOperation(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/renderer/RenderGlobal;glSkyList:I",
            opcode = Opcodes.PUTFIELD))
    private void wrapGLSkyList(RenderGlobal instance, int value, Operation<Void> original) {
        if (!generateEmpty) original.call(instance, value);
    }

    @WrapOperation(
        method = "<init>",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/Tessellator;addVertex(DDD)V"))
    public void wrapTessellatorAddVertex(Tessellator instance, double d0, double d1, double d2,
        Operation<Void> original) {
        if (!generateEmpty) original.call(instance, d0, d1, d2);
    }

    @WrapOperation(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/renderer/RenderGlobal;glSkyList2:I",
            opcode = Opcodes.PUTFIELD))
    private void wrapGLSkyList2(RenderGlobal instance, int value, Operation<Void> original) {
        if (!generateEmpty) original.call(instance, value);
    }

    @WrapOperation(
        method = "<init>",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/Tessellator;startDrawingQuads()V"))
    public void wrapTessellatorStartDrawingQuads(Tessellator instance, Operation<Void> original) {
        if (!generateEmpty) original.call(instance);
    }

    @WrapOperation(
        method = "<init>",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/Tessellator;draw()I"))
    public int wrapTessellatorDraw(Tessellator instance, Operation<Integer> original) {
        if (generateEmpty) return -1;
        else return original.call(instance);
    }

    // Now we should hopefully have an empty RenderGlobal for subworlds

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

    public List<WorldRenderer> getWorldRenderersToUpdate() {
        return this.worldRenderersToUpdate;
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

    // drawBlockDamageTexture

    @Inject(
        method = "drawBlockDamageTexture(Lnet/minecraft/client/renderer/Tessellator;Lnet/minecraft/entity/EntityLivingBase;F)V",
        remap = false,
        at = { @At(
            value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glDisable(I)V",
            ordinal = 0,
            opcode = Opcodes.INVOKESTATIC) },
        locals = LocalCapture.CAPTURE_FAILSOFT)
    public void injectDrawBlockDamageTexture(Tessellator tessellator, EntityLivingBase entity, float p_72717_3_,
        CallbackInfo ci, double d0, double d1, double d2) {
        for (World curSubWorld : ((IMixinWorld) this.theWorld).getSubWorlds()) {
            this.renderBlocksRg.blockAccess = curSubWorld;
            if (((IMixinWorld) curSubWorld).isSubWorld()) {
                GL11.glPushMatrix();
                GL11.glTranslated(-d0, -d1, -d2);

                GL11.glMultMatrix(((SubWorld) curSubWorld).getTransformToGlobalMatrixDirectBuffer());
                GL11.glTranslated(d0, d1, d2);
            }

            tessellator.startDrawingQuads();
            tessellator.setTranslation(-d0, -d1, -d2);
            tessellator.disableColor();
            Iterator<DestroyBlockProgress> iterator = this.damagedBlocks.values()
                .iterator();

            while (iterator.hasNext()) {
                DestroyBlockProgress destroyblockprogress = (DestroyBlockProgress) iterator.next();

                if (((IMixinDestroyBlockProgress) destroyblockprogress).getPartialBlockSubWorldID()
                    != ((IMixinWorld) curSubWorld).getSubWorldID()) continue;

                if (((IMixinEntity) entity).getDistanceSq(
                    destroyblockprogress.getPartialBlockX(),
                    destroyblockprogress.getPartialBlockY(),
                    destroyblockprogress.getPartialBlockZ(),
                    curSubWorld) > 1024.0D) {
                    iterator.remove();
                } else {
                    Block block = curSubWorld.getBlock(
                        destroyblockprogress.getPartialBlockX(),
                        destroyblockprogress.getPartialBlockY(),
                        destroyblockprogress.getPartialBlockZ());

                    if (block.getMaterial() != Material.air) {
                        this.renderBlocksRg.renderBlockUsingTexture(
                            block,
                            destroyblockprogress.getPartialBlockX(),
                            destroyblockprogress.getPartialBlockY(),
                            destroyblockprogress.getPartialBlockZ(),
                            this.destroyBlockIcons[destroyblockprogress.getPartialBlockDamage()]);
                    }
                }
            }

            tessellator.draw();
            tessellator.setTranslation(0.0D, 0.0D, 0.0D);

            if (((IMixinWorld) curSubWorld).isSubWorld()) GL11.glPopMatrix();
        }
        this.renderBlocksRg.blockAccess = this.theWorld;
    }

    // drawSelectionBox

    private MovingObjectPosition storedMovingObjectPosition;

    @Inject(method = "drawSelectionBox", at = @At(value = "HEAD"))
    private void storeMovingObjectPosition(EntityPlayer p_72731_1_, MovingObjectPosition p_72731_2_, int p_72731_3_,
        float p_72731_4_, CallbackInfo ci) {
        storedMovingObjectPosition = p_72731_2_;
    }

    @WrapOperation(
        method = "drawSelectionBox",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/renderer/RenderGlobal;theWorld:Lnet/minecraft/client/multiplayer/WorldClient;",
            opcode = Opcodes.GETFIELD))
    private WorldClient wrapTheWorld(RenderGlobal instance, Operation<WorldClient> original) {
        return (WorldClient) ((IMixinMovingObjectPosition) storedMovingObjectPosition).getWorld();
    }

    @WrapOperation(
        method = "drawSelectionBox",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/AxisAlignedBB;expand(DDD)Lnet/minecraft/util/AxisAlignedBB;"))
    private AxisAlignedBB wrapExpandBB(AxisAlignedBB instance, double x, double y, double z,
        Operation<AxisAlignedBB> original) {
        return ((IMixinAxisAlignedBB) original.call(instance, x, y, z))
            .getTransformedToGlobalBoundingBox(((IMixinMovingObjectPosition) storedMovingObjectPosition).getWorld());
    }

    @WrapOperation(
        method = "drawSelectionBox",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/AxisAlignedBB;getOffsetBoundingBox(DDD)Lnet/minecraft/util/AxisAlignedBB;"))
    private AxisAlignedBB wrapGetOffsetBoundingBox(AxisAlignedBB instance, double x, double y, double z,
        Operation<AxisAlignedBB> original) {
        return instance.offset(x, y, z);
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

    /**
     * Spawns a particle. Arg: particleType, x, y, z, velX, velY, velZ
     * 
     * @author Sergius Onesimus
     * @reason Too complex to be modified without Overwrite
     */
    @Overwrite
    public EntityFX doSpawnParticle(String particleType, double x, double y, double z, double velX, double velY,
        double velZ) {
        if (this.mc != null && this.mc.renderViewEntity != null && this.mc.effectRenderer != null) {
            int i = this.mc.gameSettings.particleSetting;

            if (i == 1 && this.theWorld.rand.nextInt(3) == 0) {
                i = 2;
            }

            Vec3 transformedPos = ((IMixinEntity) this.mc.renderViewEntity).getLocalPos(this.theWorld);
            double d6 = transformedPos.xCoord - x;
            double d7 = transformedPos.yCoord - y;
            double d8 = transformedPos.zCoord - z;
            EntityFX entityfx = null;

            if (particleType.equals("hugeexplosion")) {
                this.mc.effectRenderer
                    .addEffect(entityfx = new EntityHugeExplodeFX(this.theWorld, x, y, z, velX, velY, velZ));
            } else if (particleType.equals("largeexplode")) {
                this.mc.effectRenderer.addEffect(
                    entityfx = new EntityLargeExplodeFX(this.renderEngine, this.theWorld, x, y, z, velX, velY, velZ));
            } else if (particleType.equals("fireworksSpark")) {
                this.mc.effectRenderer.addEffect(
                    entityfx = new EntityFireworkSparkFX(
                        this.theWorld,
                        x,
                        y,
                        z,
                        velX,
                        velY,
                        velZ,
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
                    if (particleType.equals("bubble")) {
                        entityfx = new EntityBubbleFX(this.theWorld, x, y, z, velX, velY, velZ);
                    } else if (particleType.equals("suspended")) {
                        entityfx = new EntitySuspendFX(this.theWorld, x, y, z, velX, velY, velZ);
                    } else if (particleType.equals("depthsuspend")) {
                        entityfx = new EntityAuraFX(this.theWorld, x, y, z, velX, velY, velZ);
                    } else if (particleType.equals("townaura")) {
                        entityfx = new EntityAuraFX(this.theWorld, x, y, z, velX, velY, velZ);
                    } else if (particleType.equals("crit")) {
                        entityfx = new EntityCritFX(this.theWorld, x, y, z, velX, velY, velZ);
                    } else if (particleType.equals("magicCrit")) {
                        entityfx = new EntityCritFX(this.theWorld, x, y, z, velX, velY, velZ);
                        ((EntityFX) entityfx).setRBGColorF(
                            ((EntityFX) entityfx).getRedColorF() * 0.3F,
                            ((EntityFX) entityfx).getGreenColorF() * 0.8F,
                            ((EntityFX) entityfx).getBlueColorF());
                        ((EntityFX) entityfx).nextTextureIndexX();
                    } else if (particleType.equals("smoke")) {
                        entityfx = new EntitySmokeFX(this.theWorld, x, y, z, velX, velY, velZ);
                    } else if (particleType.equals("mobSpell")) {
                        entityfx = new EntitySpellParticleFX(this.theWorld, x, y, z, 0.0D, 0.0D, 0.0D);
                        ((EntityFX) entityfx).setRBGColorF((float) velX, (float) velY, (float) velZ);
                    } else if (particleType.equals("mobSpellAmbient")) {
                        entityfx = new EntitySpellParticleFX(this.theWorld, x, y, z, 0.0D, 0.0D, 0.0D);
                        ((EntityFX) entityfx).setAlphaF(0.15F);
                        ((EntityFX) entityfx).setRBGColorF((float) velX, (float) velY, (float) velZ);
                    } else if (particleType.equals("spell")) {
                        entityfx = new EntitySpellParticleFX(this.theWorld, x, y, z, velX, velY, velZ);
                    } else if (particleType.equals("instantSpell")) {
                        entityfx = new EntitySpellParticleFX(this.theWorld, x, y, z, velX, velY, velZ);
                        ((EntitySpellParticleFX) entityfx).setBaseSpellTextureIndex(144);
                    } else if (particleType.equals("witchMagic")) {
                        entityfx = new EntitySpellParticleFX(this.theWorld, x, y, z, velX, velY, velZ);
                        ((EntitySpellParticleFX) entityfx).setBaseSpellTextureIndex(144);
                        float f = this.theWorld.rand.nextFloat() * 0.5F + 0.35F;
                        ((EntityFX) entityfx).setRBGColorF(1.0F * f, 0.0F * f, 1.0F * f);
                    } else if (particleType.equals("note")) {
                        entityfx = new EntityNoteFX(this.theWorld, x, y, z, velX, velY, velZ);
                    } else if (particleType.equals("portal")) {
                        entityfx = new EntityPortalFX(this.theWorld, x, y, z, velX, velY, velZ);
                    } else if (particleType.equals("enchantmenttable")) {
                        entityfx = new EntityEnchantmentTableParticleFX(this.theWorld, x, y, z, velX, velY, velZ);
                    } else if (particleType.equals("explode")) {
                        entityfx = new EntityExplodeFX(this.theWorld, x, y, z, velX, velY, velZ);
                    } else if (particleType.equals("flame")) {
                        entityfx = new EntityFlameFX(this.theWorld, x, y, z, velX, velY, velZ);
                    } else if (particleType.equals("lava")) {
                        entityfx = new EntityLavaFX(this.theWorld, x, y, z);
                    } else if (particleType.equals("footstep")) {
                        entityfx = new EntityFootStepFX(this.renderEngine, this.theWorld, x, y, z);
                    } else if (particleType.equals("splash")) {
                        entityfx = new EntitySplashFX(this.theWorld, x, y, z, velX, velY, velZ);
                    } else if (particleType.equals("wake")) {
                        entityfx = new EntityFishWakeFX(this.theWorld, x, y, z, velX, velY, velZ);
                    } else if (particleType.equals("largesmoke")) {
                        entityfx = new EntitySmokeFX(this.theWorld, x, y, z, velX, velY, velZ, 2.5F);
                    } else if (particleType.equals("cloud")) {
                        entityfx = new EntityCloudFX(this.theWorld, x, y, z, velX, velY, velZ);
                    } else if (particleType.equals("reddust")) {
                        entityfx = new EntityReddustFX(
                            this.theWorld,
                            x,
                            y,
                            z,
                            (float) velX,
                            (float) velY,
                            (float) velZ);
                    } else if (particleType.equals("snowballpoof")) {
                        entityfx = new EntityBreakingFX(this.theWorld, x, y, z, Items.snowball);
                    } else if (particleType.equals("dripWater")) {
                        entityfx = new EntityDropParticleFX(this.theWorld, x, y, z, Material.water);
                    } else if (particleType.equals("dripLava")) {
                        entityfx = new EntityDropParticleFX(this.theWorld, x, y, z, Material.lava);
                    } else if (particleType.equals("snowshovel")) {
                        entityfx = new EntitySnowShovelFX(this.theWorld, x, y, z, velX, velY, velZ);
                    } else if (particleType.equals("slime")) {
                        entityfx = new EntityBreakingFX(this.theWorld, x, y, z, Items.slime_ball);
                    } else if (particleType.equals("heart")) {
                        entityfx = new EntityHeartFX(this.theWorld, x, y, z, velX, velY, velZ);
                    } else if (particleType.equals("angryVillager")) {
                        entityfx = new EntityHeartFX(this.theWorld, x, y + 0.5D, z, velX, velY, velZ);
                        ((EntityFX) entityfx).setParticleTextureIndex(81);
                        ((EntityFX) entityfx).setRBGColorF(1.0F, 1.0F, 1.0F);
                    } else if (particleType.equals("happyVillager")) {
                        entityfx = new EntityAuraFX(this.theWorld, x, y, z, velX, velY, velZ);
                        ((EntityFX) entityfx).setParticleTextureIndex(82);
                        ((EntityFX) entityfx).setRBGColorF(1.0F, 1.0F, 1.0F);
                    } else {
                        int k;
                        String[] astring;

                        if (particleType.startsWith("iconcrack_")) {
                            astring = particleType.split("_", 3);
                            int j = Integer.parseInt(astring[1]);

                            if (astring.length > 2) {
                                k = Integer.parseInt(astring[2]);
                                entityfx = new EntityBreakingFX(
                                    this.theWorld,
                                    x,
                                    y,
                                    z,
                                    velX,
                                    velY,
                                    velZ,
                                    Item.getItemById(j),
                                    k);
                            } else {
                                entityfx = new EntityBreakingFX(
                                    this.theWorld,
                                    x,
                                    y,
                                    z,
                                    velX,
                                    velY,
                                    velZ,
                                    Item.getItemById(j),
                                    0);
                            }
                        } else {
                            Block block;

                            if (particleType.startsWith("blockcrack_")) {
                                astring = particleType.split("_", 3);
                                block = Block.getBlockById(Integer.parseInt(astring[1]));
                                k = Integer.parseInt(astring[2]);
                                entityfx = (new EntityDiggingFX(this.theWorld, x, y, z, velX, velY, velZ, block, k))
                                    .applyRenderColor(k);
                            } else if (particleType.startsWith("blockdust_")) {
                                astring = particleType.split("_", 3);
                                block = Block.getBlockById(Integer.parseInt(astring[1]));
                                k = Integer.parseInt(astring[2]);
                                entityfx = (new EntityBlockDustFX(this.theWorld, x, y, z, velX, velY, velZ, block, k))
                                    .applyRenderColor(k);
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

    @Inject(method = "destroyBlockPartially", at = @At(value = "HEAD"), cancellable = true)
    private void destroyBlockPartially(int p_147587_1_, int p_147587_2_, int p_147587_3_, int p_147587_4_,
        int p_147587_5_, CallbackInfo ci) {
        destroyBlockPartially(p_147587_1_, p_147587_2_, p_147587_3_, p_147587_4_, p_147587_5_, 0);
        ci.cancel();
    }

    public void destroyBlockPartially(int p_147587_1_, int p_147587_2_, int p_147587_3_, int p_147587_4_,
        int p_147587_5_, int subWorldId) {
        if (p_147587_5_ >= 0 && p_147587_5_ < 10) {
            DestroyBlockProgress destroyblockprogress = (DestroyBlockProgress) this.damagedBlocks
                .get(Integer.valueOf(p_147587_1_));

            if (destroyblockprogress == null || destroyblockprogress.getPartialBlockX() != p_147587_2_
                || destroyblockprogress.getPartialBlockY() != p_147587_3_
                || destroyblockprogress.getPartialBlockZ() != p_147587_4_) {
                destroyblockprogress = ((IMixinDestroyBlockProgress) new DestroyBlockProgress(
                    p_147587_1_,
                    p_147587_2_,
                    p_147587_3_,
                    p_147587_4_)).setPartialBlockSubWorldId(subWorldId);
                this.damagedBlocks.put(Integer.valueOf(p_147587_1_), destroyblockprogress);
            }

            destroyblockprogress.setPartialBlockDamage(p_147587_5_);
            destroyblockprogress.setCloudUpdateTick(this.cloudTickCounter);
        } else {
            this.damagedBlocks.remove(Integer.valueOf(p_147587_1_));
        }
    }

}
