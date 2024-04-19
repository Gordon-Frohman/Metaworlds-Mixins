package net.tclproject.metaworlds.mixins.client.multiplayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.tclproject.metaworlds.compat.CompatUtil;
import net.tclproject.metaworlds.api.IMixinEntity;
import net.tclproject.metaworlds.api.IMixinWorld;

@Mixin(PlayerControllerMP.class)
public abstract class MixinPlayerControllerMP implements IMixinPlayerControllerMP {

    @Shadow(remap = true)
    private int currentBlockSubWorldID;

    @Shadow(remap = true)
    private Minecraft mc;

    @Shadow(remap = true)
    private WorldSettings.GameType currentGameType;

    @Shadow(remap = true)
    private int currentBlockX = -1;

    @Shadow(remap = true)
    private int currentBlockY = -1;

    @Shadow(remap = true)
    private int currentblockZ = -1;

    @Shadow(remap = true)
    private int blockHitDelay;

    @Shadow(remap = true)
    private boolean isHittingBlock;

    @Shadow(remap = true)
    private ItemStack currentItemHittingBlock;

    @Shadow(remap = true)
    private float curBlockDamageMP;

    @Shadow(remap = true)
    private float stepSoundTickCounter;
    
    //TODO

    @Shadow(remap = true)
    protected abstract boolean sameToolAndBlock(int par1, int par2, int par3, World par4World);

    @Shadow(remap = true)
    protected abstract void syncCurrentPlayItem();

    /**
     * Called when a player completes the destruction of a block
     */
    public boolean onPlayerDestroyBlock(int par1, int par2, int par3, int par4, World par5World) {
        ItemStack stack = mc.thePlayer.getCurrentEquippedItem();
        if (stack != null && stack.getItem() != null
            && stack.getItem()
                .onBlockStartBreak(stack, par1, par2, par3, mc.thePlayer)) {
            return false;
        }

        if (this.currentGameType.isAdventure()
            && !CompatUtil.isCurrentToolAdventureModeExempt(this.mc.thePlayer, par1, par2, par3, par5World)) {
            return false;
        } else if (this.currentGameType.isCreative() && this.mc.thePlayer.getHeldItem() != null
            && this.mc.thePlayer.getHeldItem()
                .getItem() instanceof ItemSword) {
                    return false;
                } else {
                    WorldClient worldclient = (WorldClient) par5World;
                    Block block = worldclient.getBlock(par1, par2, par3);

                    if (block.getMaterial() == Material.air) {
                        return false;
                    } else {
                        worldclient.playAuxSFX(
                            2001,
                            par1,
                            par2,
                            par3,
                            Block.getIdFromBlock(block) + (worldclient.getBlockMetadata(par1, par2, par3) << 12));
                        int i1 = worldclient.getBlockMetadata(par1, par2, par3);
                        boolean flag = block.removedByPlayer(worldclient, mc.thePlayer, par1, par2, par3);

                        if (flag) {
                            block.onBlockDestroyedByPlayer(worldclient, par1, par2, par3, i1);
                        }

                        this.currentBlockY = -1;

                        if (!this.currentGameType.isCreative()) {
                            ItemStack itemstack = this.mc.thePlayer.getCurrentEquippedItem();

                            if (itemstack != null) {
                                itemstack.func_150999_a(worldclient, block, par1, par2, par3, this.mc.thePlayer);

                                if (itemstack.stackSize == 0) {
                                    this.mc.thePlayer.destroyCurrentEquippedItem();
                                }
                            }
                        }

                        return flag;
                    }
                }
    }

    /**
     * Called by Minecraft class when the player is hitting a block with an item. Args: x, y, z, side
     */
    public void clickBlock(int par1, int par2, int par3, int par4, World par5World) {
        if (!this.currentGameType.isAdventure()
            || CompatUtil.isCurrentToolAdventureModeExempt(this.mc.thePlayer, par1, par2, par3, par5World)) {
            if (this.currentGameType.isCreative()) {
                NetHandlerPlayClient netHandler = ((EntityClientPlayerMP) ((IMixinEntity)(Object)this.mc.thePlayer).getProxyPlayer(par5World)).sendQueue;
                netHandler.addToSendQueue(new C07PacketPlayerDigging(0, par1, par2, par3, par4));
                ((IMixinPlayerControllerMP)this).clickBlockCreative(this.mc, (PlayerControllerMP)(Object)this, par1, par2, par3, par4, par5World);
                this.blockHitDelay = 5;
            } else if (!this.isHittingBlock || !this.sameToolAndBlock(par1, par2, par3, par5World)) {
                if (this.isHittingBlock) {
                    NetHandlerPlayClient netHandler = ((EntityClientPlayerMP) ((IMixinEntity)(Object)this.mc.thePlayer).getProxyPlayer(this.currentBlockSubWorldID)).sendQueue;
                    netHandler.addToSendQueue(
                        new C07PacketPlayerDigging(
                            1,
                            this.currentBlockX,
                            this.currentBlockY,
                            this.currentblockZ,
                            par4));
                }

                NetHandlerPlayClient netHandler = ((EntityClientPlayerMP) ((IMixinEntity)(Object)this.mc.thePlayer).getProxyPlayer(par5World)).sendQueue;
                netHandler.addToSendQueue(new C07PacketPlayerDigging(0, par1, par2, par3, par4));
                Block block = par5World.getBlock(par1, par2, par3);
                boolean flag = block.getMaterial() != Material.air;

                if (flag && this.curBlockDamageMP == 0.0F) {
                    block.onBlockClicked(par5World, par1, par2, par3, this.mc.thePlayer);
                }

                if (flag
                    && block.getPlayerRelativeBlockHardness(this.mc.thePlayer, par5World, par1, par2, par3) >= 1.0F) {
                    this.onPlayerDestroyBlock(par1, par2, par3, par4, par5World);
                } else {
                    this.isHittingBlock = true;
                    this.currentBlockX = par1;
                    this.currentBlockY = par2;
                    this.currentblockZ = par3;
                    this.currentBlockSubWorldID = ((IMixinWorld)par5World).getSubWorldID();
                    this.currentItemHittingBlock = this.mc.thePlayer.getHeldItem();
                    this.curBlockDamageMP = 0.0F;
                    this.stepSoundTickCounter = 0.0F;
                    par5World.destroyBlockInWorldPartially(
                        this.mc.thePlayer.getEntityId(),
                        this.currentBlockX,
                        this.currentBlockY,
                        this.currentblockZ,
                        (int) (this.curBlockDamageMP * 10.0F) - 1);
                }
            }
        }
    }

    /**
     * Resets current block damage and field_78778_j
     */
    @Overwrite
    public void resetBlockRemoving() {
        if (this.isHittingBlock) {
            NetHandlerPlayClient netHandler = ((EntityClientPlayerMP) ((IMixinEntity)(Object)this.mc.thePlayer).getProxyPlayer(this.currentBlockSubWorldID)).sendQueue;
            netHandler.addToSendQueue(
                new C07PacketPlayerDigging(1, this.currentBlockX, this.currentBlockY, this.currentblockZ, -1));
        }

        this.isHittingBlock = false;
        this.curBlockDamageMP = 0.0F;
        ((IMixinWorld)this.mc.theWorld).getSubWorld(this.currentBlockSubWorldID)
            .destroyBlockInWorldPartially(
                this.mc.thePlayer.getEntityId(),
                this.currentBlockX,
                this.currentBlockY,
                this.currentblockZ,
                -1);
    }

    /**
     * Called when a player damages a block and updates damage counters
     */
    public void onPlayerDamageBlock(int par1, int par2, int par3, int par4, World par5World) {
        this.syncCurrentPlayItem();

        if (this.blockHitDelay > 0) {
            --this.blockHitDelay;
        } else if (this.currentGameType.isCreative()) {
            this.blockHitDelay = 5;
            NetHandlerPlayClient netHandler = ((EntityClientPlayerMP) ((IMixinEntity)(Object)this.mc.thePlayer).getProxyPlayer(par5World)).sendQueue;
            netHandler.addToSendQueue(new C07PacketPlayerDigging(0, par1, par2, par3, par4));
            ((IMixinPlayerControllerMP)this).clickBlockCreative(this.mc, (PlayerControllerMP)(Object)this, par1, par2, par3, par4, par5World);
        } else {
            if (this.sameToolAndBlock(par1, par2, par3, par5World)) {
                Block block = par5World.getBlock(par1, par2, par3);

                if (block.getMaterial() == Material.air) {
                    this.isHittingBlock = false;
                    return;
                }

                this.curBlockDamageMP += block
                    .getPlayerRelativeBlockHardness(this.mc.thePlayer, par5World, par1, par2, par3);

                if (this.stepSoundTickCounter % 4.0F == 0.0F) {
                    this.mc.getSoundHandler()
                        .playSound(
                            new PositionedSoundRecord(
                                new ResourceLocation(block.stepSound.getStepResourcePath()),
                                (block.stepSound.getVolume() + 1.0F) / 8.0F,
                                block.stepSound.getPitch() * 0.5F,
                                (float) par1 + 0.5F,
                                (float) par2 + 0.5F,
                                (float) par3 + 0.5F));
                }

                ++this.stepSoundTickCounter;

                if (this.curBlockDamageMP >= 1.0F) {
                    this.isHittingBlock = false;
                    NetHandlerPlayClient netHandler = ((EntityClientPlayerMP) ((IMixinEntity)(Object)this.mc.thePlayer).getProxyPlayer(par5World)).sendQueue;
                    netHandler.addToSendQueue(new C07PacketPlayerDigging(2, par1, par2, par3, par4));
                    this.onPlayerDestroyBlock(par1, par2, par3, par4, par5World);
                    this.curBlockDamageMP = 0.0F;
                    this.stepSoundTickCounter = 0.0F;
                    this.blockHitDelay = 5;
                }

                ((IMixinWorld)this.mc.theWorld).getSubWorld(this.currentBlockSubWorldID)
                    .destroyBlockInWorldPartially(
                        this.mc.thePlayer.getEntityId(),
                        this.currentBlockX,
                        this.currentBlockY,
                        this.currentblockZ,
                        (int) (this.curBlockDamageMP * 10.0F) - 1);
            } else {
                this.clickBlock(par1, par2, par3, par4, par5World);
            }
        }
    }

}
