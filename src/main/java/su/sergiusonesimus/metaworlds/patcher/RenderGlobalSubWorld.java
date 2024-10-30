package su.sergiusonesimus.metaworlds.patcher;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemRecord;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.lwjgl.opengl.ARBOcclusionQuery;

import su.sergiusonesimus.metaworlds.api.IMixinWorld;
import su.sergiusonesimus.metaworlds.mixin.interfaces.client.renderer.IMixinRenderGlobal;

public class RenderGlobalSubWorld extends RenderGlobal {

    private RenderGlobal parentRenderGlobal;
    // private World worldObj;

    public RenderGlobalSubWorld(Minecraft par1Minecraft, RenderGlobal origRenderGlobal) {
        super(null);

        this.mc = par1Minecraft;
        this.renderEngine = par1Minecraft.getTextureManager();

        this.worldRenderersToUpdate = ((IMixinRenderGlobal) origRenderGlobal).getWorldRenderersToUpdate();
        ((IMixinRenderGlobal) this)
            .setSortedWorldRenderersList(((IMixinRenderGlobal) origRenderGlobal).getSortedWorldRenderersList());
        ((IMixinRenderGlobal) this)
            .setWorldRenderersMap(((IMixinRenderGlobal) origRenderGlobal).getWorldRenderersMap());
        ((IMixinRenderGlobal) this)
            .setWorldRenderersList(((IMixinRenderGlobal) origRenderGlobal).getWorldRenderersList());

        this.renderChunksWide = ((IMixinRenderGlobal) origRenderGlobal).getRenderChunksWide();
        this.renderChunksTall = ((IMixinRenderGlobal) origRenderGlobal).getRenderChunksTall();
        this.renderChunksDeep = ((IMixinRenderGlobal) origRenderGlobal).getRenderChunksDeep();

        this.occlusionEnabled = origRenderGlobal.occlusionEnabled;

        byte b0 = 34;
        byte b1 = 16;
        this.glRenderListBase = GLAllocation.generateDisplayLists(b0 * b0 * b1 * 3);

        if (this.occlusionEnabled) {
            this.occlusionResult.clear();
            this.glOcclusionQueryBase = GLAllocation.createDirectIntBuffer(b0 * b0 * b1);
            this.glOcclusionQueryBase.clear();
            this.glOcclusionQueryBase.position(0);
            this.glOcclusionQueryBase.limit(b0 * b0 * b1);
            ARBOcclusionQuery.glGenQueriesARB(this.glOcclusionQueryBase);
        }

        this.theWorld = par1Minecraft.theWorld;

        // this.worldObj = par1;
        this.parentRenderGlobal = origRenderGlobal;
    }

    public void onWorldRemove() {
        this.deleteAllDisplayLists();

        if (((IMixinRenderGlobal) this).getOcclusionEnabled()) {
            ARBOcclusionQuery.glDeleteQueriesARB(((IMixinRenderGlobal) this).getOcclusionQueryBase());
        }
    }

    /**
     * On the client, re-renders the block. On the server, sends the block to the client (which will re-render it),
     * including the tile entity description packet if applicable. Args: x, y, z
     */
    @Override
    public void markBlockForUpdate(int par1, int par2, int par3) {
        parentRenderGlobal.markBlockForUpdate(par1, par2, par3);
    }

    /**
     * On the client, re-renders this block. On the server, does nothing. Used for lighting updates.
     */
    @Override
    public void markBlockForRenderUpdate(int par1, int par2, int par3) {
        parentRenderGlobal.markBlockForRenderUpdate(par1, par2, par3);
    }

    /**
     * On the client, re-renders all blocks in this range, inclusive. On the server, does nothing. Args: min x, min y,
     * min z, max x, max y, max z
     */
    @Override
    public void markBlockRangeForRenderUpdate(int par1, int par2, int par3, int par4, int par5, int par6) {
        parentRenderGlobal.markBlockRangeForRenderUpdate(par1, par2, par3, par4, par5, par6);
    }

    /**
     * Plays the specified sound. Arg: soundName, x, y, z, volume, pitch
     */
    @Override
    public void playSound(String par1Str, double par2, double par4, double par6, float par8, float par9) {
        parentRenderGlobal.playSound(par1Str, par2, par4, par6, par8, par9);
    }

    /**
     * Plays sound to all near players except the player reference given
     */
    @Override
    public void playSoundToNearExcept(EntityPlayer par1EntityPlayer, String par2Str, double par3, double par5,
        double par7, float par9, float par10) {
        parentRenderGlobal.playSoundToNearExcept(par1EntityPlayer, par2Str, par3, par5, par7, par9, par10);
    }

    /**
     * Spawns a particle. Arg: particleType, x, y, z, velX, velY, velZ
     */
    @Override
    public void spawnParticle(String par1Str, double par2, double par4, double par6, double par8, double par10,
        double par12) {
        super.spawnParticle(par1Str, par2, par4, par6, par8, par10, par12);
        /*
         * WorldClient origWorld = parentRenderGlobal.getWorld();
         * parentRenderGlobal.setWorld(this.getWorld();
         * parentRenderGlobal.spawnParticle(par1Str, par2, par4, par6, par8, par10, par12);
         * parentRenderGlobal.setWorld(origWorld);
         */
    }

    /**
     * Called on all IWorldAccesses when an entity is created or loaded. On client worlds, starts downloading any
     * necessary textures. On server worlds, adds the entity to the entity tracker.
     */
    @Override
    public void onEntityCreate(Entity par1Entity) {
        parentRenderGlobal.onEntityCreate(par1Entity);
    }

    /**
     * Called on all IWorldAccesses when an entity is unloaded or destroyed. On client worlds, releases any downloaded
     * textures. On server worlds, removes the entity from the entity tracker.
     */
    @Override
    public void onEntityDestroy(Entity par1Entity) {
        parentRenderGlobal.onEntityDestroy(par1Entity);
    }

    /**
     * Plays the specified record. Arg: recordName, x, y, z
     */
    @Override
    public void playRecord(String par1Str, int par2, int par3, int par4) {
        parentRenderGlobal.playRecord(par1Str, par2, par3, par4);
    }

    @Override
    public void broadcastSound(int par1, int par2, int par3, int par4, int par5) {
        Vec3 globalCoords = ((IMixinWorld) this.theWorld).transformToGlobal(Vec3.createVectorHelper(par2, par3, par4));
        parentRenderGlobal.broadcastSound(
            par1,
            (int) globalCoords.xCoord,
            (int) globalCoords.yCoord,
            (int) globalCoords.zCoord,
            par5);
    }

    /**
     * Plays a pre-canned sound effect along with potentially auxiliary data-driven one-shot behaviour (particles, etc).
     */
    @Override
    public void playAuxSFX(EntityPlayer player, int sfxID, int x, int y, int z, int p_72706_6_) {
        Vec3 globalCoords = ((IMixinWorld) this.theWorld).transformToGlobal(Vec3.createVectorHelper(x, y, z));
        Vec3 globalCoordsMiddle = ((IMixinWorld) this.theWorld)
            .transformToGlobal(Vec3.createVectorHelper(x + 0.5D, y + 0.5D, z + 0.5D));
        World parentWorld = ((IMixinWorld) this.theWorld).getParentWorld();

        Random random = this.theWorld.rand;
        Block block = null;
        double d0;
        double d1;
        double d2;
        String s;
        int k1;
        double d4;
        double d5;
        double d6;
        double d7;
        int l2;
        double d13;

        switch (sfxID) {
            case 1000:
                parentWorld.playSound(
                    globalCoords.xCoord,
                    globalCoords.yCoord,
                    globalCoords.zCoord,
                    "random.click",
                    1.0F,
                    1.0F,
                    false);
                break;
            case 1001:
                parentWorld.playSound(
                    globalCoords.xCoord,
                    globalCoords.yCoord,
                    globalCoords.zCoord,
                    "random.click",
                    1.0F,
                    1.2F,
                    false);
                break;
            case 1002:
                parentWorld.playSound(
                    globalCoords.xCoord,
                    globalCoords.yCoord,
                    globalCoords.zCoord,
                    "random.bow",
                    1.0F,
                    1.2F,
                    false);
                break;
            case 1003:
                if (Math.random() < 0.5D) {
                    parentWorld.playSound(
                        globalCoordsMiddle.xCoord,
                        globalCoordsMiddle.yCoord,
                        globalCoordsMiddle.zCoord,
                        "random.door_open",
                        1.0F,
                        this.theWorld.rand.nextFloat() * 0.1F + 0.9F,
                        false);
                } else {
                    parentWorld.playSound(
                        globalCoordsMiddle.xCoord,
                        globalCoordsMiddle.yCoord,
                        globalCoordsMiddle.zCoord,
                        "random.door_close",
                        1.0F,
                        this.theWorld.rand.nextFloat() * 0.1F + 0.9F,
                        false);
                }

                break;
            case 1004:
                parentWorld.playSound(
                    (double) ((float) globalCoords.xCoord + 0.5F),
                    (double) ((float) globalCoords.yCoord + 0.5F),
                    (double) ((float) globalCoords.zCoord + 0.5F),
                    "random.fizz",
                    0.5F,
                    2.6F + (random.nextFloat() - random.nextFloat()) * 0.8F,
                    false);
                break;
            case 1005:
                // TODO A lot to be done heres
                if (Item.getItemById(p_72706_6_) instanceof ItemRecord) {
                    this.theWorld
                        .playRecord("records." + ((ItemRecord) Item.getItemById(p_72706_6_)).recordName, x, y, z);
                } else {
                    this.theWorld.playRecord((String) null, x, y, z);
                }

                break;
            case 1007:
                // Probably won't ever be called from a subworld
                parentWorld.playSound(
                    globalCoordsMiddle.xCoord,
                    globalCoordsMiddle.yCoord,
                    globalCoordsMiddle.zCoord,
                    "mob.ghast.charge",
                    10.0F,
                    (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F,
                    false);
                break;
            case 1008:
                // Probably won't ever be called from a subworld
                parentWorld.playSound(
                    globalCoordsMiddle.xCoord,
                    globalCoordsMiddle.yCoord,
                    globalCoordsMiddle.zCoord,
                    "mob.ghast.fireball",
                    10.0F,
                    (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F,
                    false);
                break;
            case 1009:
                // Probably won't ever be called from a subworld
                parentWorld.playSound(
                    globalCoordsMiddle.xCoord,
                    globalCoordsMiddle.yCoord,
                    globalCoordsMiddle.zCoord,
                    "mob.ghast.fireball",
                    2.0F,
                    (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F,
                    false);
                break;
            case 1010:
                // Probably won't ever be called from a subworld
                parentWorld.playSound(
                    globalCoordsMiddle.xCoord,
                    globalCoordsMiddle.yCoord,
                    globalCoordsMiddle.zCoord,
                    "mob.zombie.wood",
                    2.0F,
                    (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F,
                    false);
                break;
            case 1011:
                // Probably won't ever be called from a subworld
                parentWorld.playSound(
                    globalCoordsMiddle.xCoord,
                    globalCoordsMiddle.yCoord,
                    globalCoordsMiddle.zCoord,
                    "mob.zombie.metal",
                    2.0F,
                    (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F,
                    false);
                break;
            case 1012:
                // Probably won't ever be called from a subworld
                parentWorld.playSound(
                    globalCoordsMiddle.xCoord,
                    globalCoordsMiddle.yCoord,
                    globalCoordsMiddle.zCoord,
                    "mob.zombie.woodbreak",
                    2.0F,
                    (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F,
                    false);
                break;
            case 1014:
                // Probably won't ever be called from a subworld
                parentWorld.playSound(
                    globalCoordsMiddle.xCoord,
                    globalCoordsMiddle.yCoord,
                    globalCoordsMiddle.zCoord,
                    "mob.wither.shoot",
                    2.0F,
                    (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F,
                    false);
                break;
            case 1015:
                // Probably won't ever be called from a subworld
                parentWorld.playSound(
                    globalCoordsMiddle.xCoord,
                    globalCoordsMiddle.yCoord,
                    globalCoordsMiddle.zCoord,
                    "mob.bat.takeoff",
                    0.05F,
                    (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F,
                    false);
                break;
            case 1016:
                // Probably won't ever be called from a subworld
                parentWorld.playSound(
                    globalCoordsMiddle.xCoord,
                    globalCoordsMiddle.yCoord,
                    globalCoordsMiddle.zCoord,
                    "mob.zombie.infect",
                    2.0F,
                    (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F,
                    false);
                break;
            case 1017:
                // Probably won't ever be called from a subworld
                parentWorld.playSound(
                    globalCoordsMiddle.xCoord,
                    globalCoordsMiddle.yCoord,
                    globalCoordsMiddle.zCoord,
                    "mob.zombie.unfect",
                    2.0F,
                    (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F,
                    false);
                break;
            case 1020:
                parentWorld.playSound(
                    (double) ((float) globalCoords.xCoord + 0.5F),
                    (double) ((float) globalCoords.yCoord + 0.5F),
                    (double) ((float) globalCoords.zCoord + 0.5F),
                    "random.anvil_break",
                    1.0F,
                    this.theWorld.rand.nextFloat() * 0.1F + 0.9F,
                    false);
                break;
            case 1021:
                parentWorld.playSound(
                    (double) ((float) globalCoords.xCoord + 0.5F),
                    (double) ((float) globalCoords.yCoord + 0.5F),
                    (double) ((float) globalCoords.zCoord + 0.5F),
                    "random.anvil_use",
                    1.0F,
                    this.theWorld.rand.nextFloat() * 0.1F + 0.9F,
                    false);
                break;
            case 1022:
                parentWorld.playSound(
                    (double) ((float) globalCoords.xCoord + 0.5F),
                    (double) ((float) globalCoords.yCoord + 0.5F),
                    (double) ((float) globalCoords.zCoord + 0.5F),
                    "random.anvil_land",
                    0.3F,
                    this.theWorld.rand.nextFloat() * 0.1F + 0.9F,
                    false);
                break;
            case 2000:
                // Dispensers and droppers' smoke
                int j2 = p_72706_6_ % 3 - 1;
                int j1 = p_72706_6_ / 3 % 3 - 1;
                d1 = x + (double) j2 * 0.6D + 0.5D;
                d2 = y + 0.5D;
                double d9 = z + (double) j1 * 0.6D + 0.5D;

                for (int k2 = 0; k2 < 10; ++k2) {
                    double d11 = random.nextDouble() * 0.2D + 0.01D;
                    double d12 = d1 + (double) j2 * 0.01D + (random.nextDouble() - 0.5D) * (double) j1 * 0.5D;
                    d4 = d2 + (random.nextDouble() - 0.5D) * 0.5D;
                    d13 = d9 + (double) j1 * 0.01D + (random.nextDouble() - 0.5D) * (double) j2 * 0.5D;
                    d5 = (double) j2 * d11 + random.nextGaussian() * 0.01D;
                    d6 = -0.03D + random.nextGaussian() * 0.01D;
                    d7 = (double) j1 * d11 + random.nextGaussian() * 0.01D;
                    Vec3 newGlobalCoords = ((IMixinWorld) this.theWorld)
                        .transformToGlobal(Vec3.createVectorHelper(d12, d4, d13));
                    Vec3 globalVelocity = ((IMixinWorld) this.theWorld)
                        .rotateToGlobal(Vec3.createVectorHelper(d5, d6, d7));
                    parentWorld.spawnParticle(
                        "smoke",
                        newGlobalCoords.xCoord,
                        newGlobalCoords.yCoord,
                        newGlobalCoords.zCoord,
                        globalVelocity.xCoord,
                        globalVelocity.yCoord,
                        globalVelocity.zCoord);
                }

                return;
            case 2001:
                // Block breaking effects
                block = Block.getBlockById(p_72706_6_ & 4095);

                if (block.getMaterial() != Material.air) {
                    this.mc.getSoundHandler()
                        .playSound(
                            new PositionedSoundRecord(
                                new ResourceLocation(block.stepSound.getBreakSound()),
                                (block.stepSound.getVolume() + 1.0F) / 2.0F,
                                block.stepSound.getPitch() * 0.8F,
                                (float) globalCoords.xCoord + 0.5F,
                                (float) globalCoords.yCoord + 0.5F,
                                (float) globalCoords.zCoord + 0.5F));
                }

                this.mc.effectRenderer.addBlockDestroyEffects(x, y, z, block, p_72706_6_ >> 12 & 255);
                break;
            case 2002:
                // Probably won't ever be called from a subworld
                d0 = globalCoords.xCoord;
                d1 = globalCoords.yCoord;
                d2 = globalCoords.zCoord;
                s = "iconcrack_" + Item.getIdFromItem(Items.potionitem) + "_" + p_72706_6_;

                for (k1 = 0; k1 < 8; ++k1) {
                    parentWorld.spawnParticle(
                        s,
                        d0,
                        d1,
                        d2,
                        random.nextGaussian() * 0.15D,
                        random.nextDouble() * 0.2D,
                        random.nextGaussian() * 0.15D);
                }

                k1 = Items.potionitem.getColorFromDamage(p_72706_6_);
                float f = (float) (k1 >> 16 & 255) / 255.0F;
                float f1 = (float) (k1 >> 8 & 255) / 255.0F;
                float f2 = (float) (k1 >> 0 & 255) / 255.0F;
                String s1 = "spell";

                if (Items.potionitem.isEffectInstant(p_72706_6_)) {
                    s1 = "instantSpell";
                }

                for (l2 = 0; l2 < 100; ++l2) {
                    d4 = random.nextDouble() * 4.0D;
                    d13 = random.nextDouble() * Math.PI * 2.0D;
                    d5 = Math.cos(d13) * d4;
                    d6 = 0.01D + random.nextDouble() * 0.5D;
                    d7 = Math.sin(d13) * d4;
                    EntityFX entityfx = this.doSpawnParticle(s1, d0 + d5 * 0.1D, d1 + 0.3D, d2 + d7 * 0.1D, d5, d6, d7);

                    if (entityfx != null) {
                        float f4 = 0.75F + random.nextFloat() * 0.25F;
                        entityfx.setRBGColorF(f * f4, f1 * f4, f2 * f4);
                        entityfx.multiplyVelocity((float) d4);
                    }
                }

                parentWorld.playSound(
                    globalCoordsMiddle.xCoord,
                    globalCoordsMiddle.yCoord,
                    globalCoordsMiddle.zCoord,
                    "game.potion.smash",
                    1.0F,
                    this.theWorld.rand.nextFloat() * 0.1F + 0.9F,
                    false);
                break;
            case 2003:
                // Probably won't ever be called from a subworld
                d0 = globalCoordsMiddle.xCoord;
                d1 = globalCoords.yCoord;
                d2 = globalCoordsMiddle.zCoord;
                s = "iconcrack_" + Item.getIdFromItem(Items.ender_eye);

                for (k1 = 0; k1 < 8; ++k1) {
                    parentWorld.spawnParticle(
                        s,
                        d0,
                        d1,
                        d2,
                        random.nextGaussian() * 0.15D,
                        random.nextDouble() * 0.2D,
                        random.nextGaussian() * 0.15D);
                }

                for (double d10 = 0.0D; d10 < (Math.PI * 2D); d10 += 0.15707963267948966D) {
                    parentWorld.spawnParticle(
                        "portal",
                        d0 + Math.cos(d10) * 5.0D,
                        d1 - 0.4D,
                        d2 + Math.sin(d10) * 5.0D,
                        Math.cos(d10) * -5.0D,
                        0.0D,
                        Math.sin(d10) * -5.0D);
                    parentWorld.spawnParticle(
                        "portal",
                        d0 + Math.cos(d10) * 5.0D,
                        d1 - 0.4D,
                        d2 + Math.sin(d10) * 5.0D,
                        Math.cos(d10) * -7.0D,
                        0.0D,
                        Math.sin(d10) * -7.0D);
                }

                return;
            case 2004:
                // Honestly IDK WTF is this
                for (l2 = 0; l2 < 20; ++l2) {
                    d4 = x + 0.5d + ((double) this.theWorld.rand.nextFloat() - 0.5D) * 2.0D;
                    d13 = y + 0.5d + ((double) this.theWorld.rand.nextFloat() - 0.5D) * 2.0D;
                    d5 = z + 0.5d + ((double) this.theWorld.rand.nextFloat() - 0.5D) * 2.0D;
                    globalCoords = ((IMixinWorld) this.theWorld)
                        .transformToGlobal(Vec3.createVectorHelper(d4, d13, d5));
                    parentWorld.spawnParticle(
                        "smoke",
                        globalCoords.xCoord,
                        globalCoords.yCoord,
                        globalCoords.zCoord,
                        0.0D,
                        0.0D,
                        0.0D);
                    parentWorld.spawnParticle(
                        "flame",
                        globalCoords.xCoord,
                        globalCoords.yCoord,
                        globalCoords.zCoord,
                        0.0D,
                        0.0D,
                        0.0D);
                }

                return;
            case 2005:
                // Probably won't ever be called from a subworld
                ItemDye.func_150918_a(this.theWorld, x, y, z, p_72706_6_);
                break;
            case 2006:
                // Falling on blocks
                block = this.theWorld.getBlock(x, y, z);

                if (block.getMaterial() != Material.air) {
                    double d3 = (double) Math.min(0.2F + (float) p_72706_6_ / 15.0F, 10.0F);

                    if (d3 > 2.5D) {
                        d3 = 2.5D;
                    }

                    int l1 = (int) (150.0D * d3);

                    for (int i2 = 0; i2 < l1; ++i2) {
                        float f3 = MathHelper.randomFloatClamp(random, 0.0F, ((float) Math.PI * 2F));
                        d5 = (double) MathHelper.randomFloatClamp(random, 0.75F, 1.0F);
                        d6 = 0.20000000298023224D + d3 / 100.0D;
                        d7 = (double) (MathHelper.cos(f3) * 0.2F) * d5 * d5 * (d3 + 0.2D);
                        double d8 = (double) (MathHelper.sin(f3) * 0.2F) * d5 * d5 * (d3 + 0.2D);
                        Vec3 newGlobalCoords = ((IMixinWorld) this.theWorld)
                            .transformToGlobal(Vec3.createVectorHelper(x + 0.5F, y + 1.0F, z + 0.5F));
                        Vec3 globalVelocity = ((IMixinWorld) this.theWorld)
                            .rotateToGlobal(Vec3.createVectorHelper(d7, d6, d8));
                        parentWorld.spawnParticle(
                            "blockdust_" + Block.getIdFromBlock(block) + "_" + this.theWorld.getBlockMetadata(x, y, z),
                            newGlobalCoords.xCoord,
                            newGlobalCoords.yCoord,
                            newGlobalCoords.zCoord,
                            globalVelocity.xCoord,
                            globalVelocity.yCoord,
                            globalVelocity.zCoord);
                    }
                }
        }
    }

    /**
     * Starts (or continues) destroying a block with given ID at the given coordinates for the given partially destroyed
     * value
     */
    @Override
    public void destroyBlockPartially(int par1, int par2, int par3, int par4, int par5) {
        ((IMixinRenderGlobal) parentRenderGlobal)
            .destroyBlockPartially(par1, par2, par3, par4, par5, ((IMixinWorld) this.theWorld).getSubWorldID());
    }

    @Override
    public void onStaticEntitiesChanged() {}
}
