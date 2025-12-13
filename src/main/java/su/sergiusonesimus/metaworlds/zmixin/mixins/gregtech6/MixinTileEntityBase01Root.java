package su.sergiusonesimus.metaworlds.zmixin.mixins.gregtech6;

import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import gregapi.network.INetworkHandler;
import gregapi.network.IPacket;
import gregapi.network.packets.PacketCoordinates;
import gregapi.tileentity.base.TileEntityBase01Root;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.gregtech6.IMixinPacketCoordinates;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;
import su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.tileentity.MixinTileEntity;

@Mixin(TileEntityBase01Root.class)
public class MixinTileEntityBase01Root extends MixinTileEntity {

    @WrapOperation(
        method = { "setError", "sendBlockEvent" },
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

}
