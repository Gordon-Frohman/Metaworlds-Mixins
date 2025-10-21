package su.sergiusonesimus.metaworlds.zmixin.mixins.forgemultipart;

import java.util.Iterator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.chunk.Chunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import codechicken.lib.packet.PacketCustom;
import su.sergiusonesimus.metaworlds.integrations.ForgeMultipartIntegration;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

@Mixin(targets = "codechicken.multipart.handler.MultipartSPH$")
public class MixinMultipartSPH {

    private int storedSubworldID;

    @Inject(method = "getDescPacket", remap = false, at = @At(value = "HEAD"))
    public void storeSubworldID(Chunk chunk, final Iterator<TileEntity> it, CallbackInfoReturnable<PacketCustom> cir) {
        storedSubworldID = ((IMixinWorld) chunk.worldObj).getSubWorldID();
    }

    @WrapOperation(
        method = "getDescPacket",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lcodechicken/lib/packet/PacketCustom;compress()Lcodechicken/lib/packet/PacketCustom;"))
    public PacketCustom writeSubworldID(PacketCustom packet, Operation<PacketCustom> original) {
        return original.call(packet)
            .writeInt(storedSubworldID);
    }

    @WrapOperation(
        method = { "onChunkWatch", "onChunkUnWatch" },
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;getEntityId()I"),
        remap = false)
    public int getSubworldSpecificEntityId(EntityPlayer player, Operation<Integer> original) {
        return ForgeMultipartIntegration.getSubworldSpecificEntityId(player);
    }

}
