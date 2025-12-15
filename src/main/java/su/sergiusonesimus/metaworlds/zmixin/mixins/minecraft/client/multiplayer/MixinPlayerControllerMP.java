package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.client.multiplayer;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;

import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import su.sergiusonesimus.metaworlds.compat.CompatUtil;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.client.multiplayer.IMixinPlayerControllerMP;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.IMixinEntity;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

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

    // TODO

    @Shadow(remap = true)
    protected abstract void syncCurrentPlayItem();

    /**
     * Called when a player completes the destruction of a block
     */
    @SuppressWarnings("deprecation")
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
                NetHandlerPlayClient netHandler = ((EntityClientPlayerMP) ((IMixinEntity) (Object) this.mc.thePlayer)
                    .getProxyPlayer(par5World)).sendQueue;
                netHandler.addToSendQueue(new C07PacketPlayerDigging(0, par1, par2, par3, par4));
                IMixinPlayerControllerMP
                    .clickBlockCreative(this.mc, (PlayerControllerMP) (Object) this, par1, par2, par3, par4, par5World);
                this.blockHitDelay = 5;
            } else if (!this.isHittingBlock || !this.sameToolAndBlock(par1, par2, par3, par5World)) {
                if (this.isHittingBlock) {
                    NetHandlerPlayClient netHandler = ((EntityClientPlayerMP) ((IMixinEntity) (Object) this.mc.thePlayer)
                        .getProxyPlayer(this.currentBlockSubWorldID)).sendQueue;
                    netHandler.addToSendQueue(
                        new C07PacketPlayerDigging(
                            1,
                            this.currentBlockX,
                            this.currentBlockY,
                            this.currentblockZ,
                            par4));
                }

                NetHandlerPlayClient netHandler = ((EntityClientPlayerMP) ((IMixinEntity) (Object) this.mc.thePlayer)
                    .getProxyPlayer(par5World)).sendQueue;
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
                    this.currentBlockSubWorldID = ((IMixinWorld) par5World).getSubWorldID();
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

    // Class constructor

    @WrapOperation(
        method = "resetBlockRemoving",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;netClientHandler:Lnet/minecraft/client/network/NetHandlerPlayClient;",
            opcode = Opcodes.GETFIELD))
    private NetHandlerPlayClient getNetClientHandlerInit(PlayerControllerMP instance,
        Operation<NetHandlerPlayClient> original) {
        EntityClientPlayerMP proxy = ((EntityClientPlayerMP) ((IMixinEntity) (Object) this.mc.thePlayer)
            .getProxyPlayer(this.currentBlockSubWorldID));
        return proxy == null ? null : proxy.sendQueue;
    }

    @WrapOperation(
        method = "resetBlockRemoving",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/network/NetHandlerPlayClient;addToSendQueue(Lnet/minecraft/network/Packet;)V"))
    private void addToSendQueue(NetHandlerPlayClient instance, Packet packet, Operation<Void> original) {
        if (instance != null) original.call(instance, packet);
    }

    @WrapOperation(
        method = "resetBlockRemoving",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;theWorld:Lnet/minecraft/client/multiplayer/WorldClient;",
            opcode = Opcodes.GETFIELD))
    private WorldClient getTheWorld(Minecraft instance, Operation<WorldClient> original) {
        return (WorldClient) ((IMixinWorld) instance.theWorld).getSubWorld(this.currentBlockSubWorldID);
    }

    @WrapOperation(
        method = "resetBlockRemoving",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/WorldClient;destroyBlockInWorldPartially(IIIII)V"))
    private void destroyBlockInWorldPartially(WorldClient instance, int p_147443_1_, int x, int y, int z,
        int blockDamage, Operation<Void> original) {
        if (instance != null) original.call(instance, p_147443_1_, x, y, z, blockDamage);
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
            NetHandlerPlayClient netHandler = ((EntityClientPlayerMP) ((IMixinEntity) (Object) this.mc.thePlayer)
                .getProxyPlayer(par5World)).sendQueue;
            netHandler.addToSendQueue(new C07PacketPlayerDigging(0, par1, par2, par3, par4));
            IMixinPlayerControllerMP
                .clickBlockCreative(this.mc, (PlayerControllerMP) (Object) this, par1, par2, par3, par4, par5World);
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
                    NetHandlerPlayClient netHandler = ((EntityClientPlayerMP) ((IMixinEntity) (Object) this.mc.thePlayer)
                        .getProxyPlayer(par5World)).sendQueue;
                    netHandler.addToSendQueue(new C07PacketPlayerDigging(2, par1, par2, par3, par4));
                    this.onPlayerDestroyBlock(par1, par2, par3, par4, par5World);
                    this.curBlockDamageMP = 0.0F;
                    this.stepSoundTickCounter = 0.0F;
                    this.blockHitDelay = 5;
                }

                ((IMixinWorld) this.mc.theWorld).getSubWorld(this.currentBlockSubWorldID)
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

    private boolean sameToolAndBlock(int par1, int par2, int par3, World par4World) {
        ItemStack itemstack = this.mc.thePlayer.getHeldItem();
        boolean flag = this.currentItemHittingBlock == null && itemstack == null;

        if (this.currentItemHittingBlock != null && itemstack != null) {
            flag = itemstack.getItem() == this.currentItemHittingBlock.getItem()
                && ItemStack.areItemStackTagsEqual(itemstack, this.currentItemHittingBlock)
                && (itemstack.isItemStackDamageable()
                    || itemstack.getItemDamage() == this.currentItemHittingBlock.getItemDamage());
        }

        return par1 == this.currentBlockX && par2 == this.currentBlockY
            && par3 == this.currentblockZ
            && ((IMixinWorld) par4World).getSubWorldID() == this.currentBlockSubWorldID
            && flag;
    }

    // onPlayerRightClick

    Vec3 transformedVec;
    World storedWorld;

    @Inject(method = "onPlayerRightClick", at = { @At(value = "HEAD") })
    private void injectOnPlayerRightClick(EntityPlayer player, World worldIn, ItemStack itemStackIn, int x, int y,
        int z, int side, Vec3 hitVector, CallbackInfoReturnable<Boolean> ci) {
        transformedVec = ((IMixinWorld) worldIn).transformToLocal(hitVector);
        storedWorld = worldIn;
    }

    @WrapOperation(
        method = "onPlayerRightClick",
        at = @At(value = "FIELD", target = "Lnet/minecraft/util/Vec3;xCoord:D", opcode = Opcodes.GETFIELD))
    private double getXcoord(Vec3 instance, Operation<Double> original) {
        return transformedVec.xCoord;
    }

    @WrapOperation(
        method = "onPlayerRightClick",
        at = @At(value = "FIELD", target = "Lnet/minecraft/util/Vec3;yCoord:D", opcode = Opcodes.GETFIELD))
    private double getYcoord(Vec3 instance, Operation<Double> original) {
        return transformedVec.yCoord;
    }

    @WrapOperation(
        method = "onPlayerRightClick",
        at = @At(value = "FIELD", target = "Lnet/minecraft/util/Vec3;zCoord:D", opcode = Opcodes.GETFIELD))
    private double getZcoord(Vec3 instance, Operation<Double> original) {
        return transformedVec.zCoord;
    }

    @WrapOperation(
        method = "onPlayerRightClick",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;netClientHandler:Lnet/minecraft/client/network/NetHandlerPlayClient;",
            opcode = Opcodes.GETFIELD))
    private NetHandlerPlayClient getNetClientHandlerOnPlayerRightClick(PlayerControllerMP instance,
        Operation<NetHandlerPlayClient> original) {
        return ((EntityClientPlayerMP) ((IMixinEntity) (Object) this.mc.thePlayer)
            .getProxyPlayer(storedWorld)).sendQueue;
    }

    // attackEntity, interactWithEntitySendPacket

    private EntityPlayer storedPlayer;

    @Inject(method = { "attackEntity", "interactWithEntitySendPacket" }, at = @At(value = "HEAD"))
    private void storePlayer(EntityPlayer player, Entity targetEntity, CallbackInfo ci) {
        storedPlayer = player;
    }

    @WrapOperation(
        method = { "attackEntity", "interactWithEntitySendPacket" },
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;netClientHandler:Lnet/minecraft/client/network/NetHandlerPlayClient;",
            opcode = Opcodes.GETFIELD))
    private NetHandlerPlayClient getNetClientHandlerAttackEntity(PlayerControllerMP instance,
        Operation<NetHandlerPlayClient> original) {
        return ((EntityClientPlayerMP) storedPlayer).sendQueue;
    }

}
