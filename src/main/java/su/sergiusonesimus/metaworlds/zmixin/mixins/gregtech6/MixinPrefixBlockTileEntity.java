package su.sergiusonesimus.metaworlds.zmixin.mixins.gregtech6;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import gregapi.block.prefixblock.PrefixBlockTileEntity;
import gregapi.network.INetworkHandler;
import gregapi.network.IPacket;
import gregapi.network.packets.PacketCoordinates;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.gregtech6.IMixinPacketCoordinates;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

@Mixin(PrefixBlockTileEntity.class)
public class MixinPrefixBlockTileEntity extends MixinTileEntityBase01Root {

    @WrapOperation(
        method = { "getDescriptionPacket", "onScheduledUpdate", "onAdjacentBlockChange" },
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lgregapi/network/INetworkHandler;sendToAllPlayersInRange(Lgregapi/network/IPacket;Lnet/minecraft/world/World;Lnet/minecraft/util/ChunkCoordinates;)V"))
    public void sendToAllPlayersInRange(INetworkHandler instance, IPacket aPacket, World aWorld,
        ChunkCoordinates aCoords, Operation<Void> original) {
        if (aPacket instanceof PacketCoordinates cPacket) {
            aPacket = ((IMixinPacketCoordinates) cPacket).setSubworldId(((IMixinWorld) aWorld).getSubWorldID());
        }
        original.call(instance, aPacket, aWorld, aCoords);
    }

    @WrapOperation(
        method = "sendUpdateToPlayer",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lgregapi/network/INetworkHandler;sendToPlayer(Lgregapi/network/IPacket;Lnet/minecraft/entity/player/EntityPlayerMP;)V"))
    public void sendUpdateToPlayer(INetworkHandler instance, IPacket aPacket, EntityPlayerMP aPlayer,
        Operation<Void> original) {
        if (aPacket instanceof PacketCoordinates cPacket) {
            aPacket = ((IMixinPacketCoordinates) cPacket)
                .setSubworldId(((IMixinWorld) this.getWorldObj()).getSubWorldID());
        }
        original.call(instance, aPacket, aPlayer);
    }

}
