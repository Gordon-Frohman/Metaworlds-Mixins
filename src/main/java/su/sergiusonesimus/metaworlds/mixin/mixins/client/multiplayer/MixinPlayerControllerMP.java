package su.sergiusonesimus.metaworlds.mixin.mixins.client.multiplayer;

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
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import su.sergiusonesimus.metaworlds.api.IMixinEntity;
import su.sergiusonesimus.metaworlds.api.IMixinWorld;
import su.sergiusonesimus.metaworlds.compat.CompatUtil;
import su.sergiusonesimus.metaworlds.mixin.interfaces.client.multiplayer.IMixinPlayerControllerMP;
import net.minecraft.client.entity.EntityClientPlayerMP;

@Mixin(PlayerControllerMP.class)
public abstract class MixinPlayerControllerMP implements IMixinPlayerControllerMP {

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
                IMixinPlayerControllerMP.clickBlockCreative(this.mc, (PlayerControllerMP)(Object)this, par1, par2, par3, par4, par5World);
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
            IMixinPlayerControllerMP.clickBlockCreative(this.mc, (PlayerControllerMP)(Object)this, par1, par2, par3, par4, par5World);
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

    private boolean sameToolAndBlock(int par1, int par2, int par3, World par4World)
    {
        ItemStack itemstack = this.mc.thePlayer.getHeldItem();
        boolean flag = this.currentItemHittingBlock == null && itemstack == null;

        if (this.currentItemHittingBlock != null && itemstack != null)
        {
            flag = itemstack.getItem() == this.currentItemHittingBlock.getItem() && ItemStack.areItemStackTagsEqual(itemstack, this.currentItemHittingBlock) && (itemstack.isItemStackDamageable() || itemstack.getItemDamage() == this.currentItemHittingBlock.getItemDamage());
        }

        return par1 == this.currentBlockX && par2 == this.currentBlockY && par3 == this.currentblockZ && ((IMixinWorld)par4World).getSubWorldID() == this.currentBlockSubWorldID && flag;
    }

    /**
     * Handles a players right click. Args: player, world, x, y, z, side, hitVec
     */
    @Overwrite
    public boolean onPlayerRightClick(EntityPlayer par1EntityPlayer, World par2World, ItemStack par3ItemStack, int par4, int par5, int par6, int par7, Vec3 par8Vec3)
    {
        this.syncCurrentPlayItem();
        Vec3 transformedVec = ((IMixinWorld)par2World).transformToLocal(par8Vec3);
        float f = (float)transformedVec.xCoord - (float)par4;
        float f1 = (float)transformedVec.yCoord - (float)par5;
        float f2 = (float)transformedVec.zCoord - (float)par6;
        boolean flag = false;

        if (par3ItemStack != null &&
            par3ItemStack.getItem() != null &&
            par3ItemStack.getItem().onItemUseFirst(par3ItemStack, par1EntityPlayer, par2World, par4, par5, par6, par7, f, f1, f2))
        {
                return true;
        }

        if (!par1EntityPlayer.isSneaking() || par1EntityPlayer.getHeldItem() == null || par1EntityPlayer.getHeldItem().getItem().doesSneakBypassUse(par2World, par4, par5, par6, par1EntityPlayer))
        {
            flag = par2World.getBlock(par4, par5, par6).onBlockActivated(par2World, par4, par5, par6, par1EntityPlayer, par7, f, f1, f2);
        }

        if (!flag && par3ItemStack != null && par3ItemStack.getItem() instanceof ItemBlock)
        {
            ItemBlock itemblock = (ItemBlock)par3ItemStack.getItem();

            if (!itemblock.func_150936_a(par2World, par4, par5, par6, par7, par1EntityPlayer, par3ItemStack))
            {
                return false;
            }
        }

        NetHandlerPlayClient netHandler = ((EntityClientPlayerMP)((IMixinEntity)par1EntityPlayer).getProxyPlayer(par2World)).sendQueue;
        netHandler.addToSendQueue(new C08PacketPlayerBlockPlacement(par4, par5, par6, par7, par1EntityPlayer.inventory.getCurrentItem(), f, f1, f2));

        if (flag)
        {
            return true;
        }
        else if (par3ItemStack == null)
        {
            return false;
        }
        else if (this.currentGameType.isCreative())
        {
            int j1 = par3ItemStack.getItemDamage();
            int i1 = par3ItemStack.stackSize;
            boolean flag1 = par3ItemStack.tryPlaceItemIntoWorld(par1EntityPlayer, par2World, par4, par5, par6, par7, f, f1, f2);
            par3ItemStack.setItemDamage(j1);
            par3ItemStack.stackSize = i1;
            return flag1;
        }
        else
        {
            if (!par3ItemStack.tryPlaceItemIntoWorld(par1EntityPlayer, par2World, par4, par5, par6, par7, f, f1, f2))
            {
                return false;
            }
            if (par3ItemStack.stackSize <= 0)
            {
                MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(par1EntityPlayer, par3ItemStack));
            }
            return true;
        }
    }

    /**
     * Send packet to server - player is interacting with another entity (left click)
     */
    @Overwrite
    public boolean interactWithEntitySendPacket(EntityPlayer p_78768_1_, Entity p_78768_2_)
    {
        this.syncCurrentPlayItem();
        ((EntityClientPlayerMP)p_78768_1_).sendQueue.addToSendQueue(new C02PacketUseEntity(p_78768_2_, C02PacketUseEntity.Action.INTERACT));
        return p_78768_1_.interactWith(p_78768_2_);
    }

}
