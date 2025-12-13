package su.sergiusonesimus.metaworlds.zmixin.mixins.gregtech6;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import gregapi.network.packets.PacketCoordinates;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.gregtech6.IMixinPacketCoordinates;

@Mixin(PacketCoordinates.class)
public class MixinPacketCoordinates implements IMixinPacketCoordinates {

    protected int subworldID = 0;

    @Override
    public PacketCoordinates setSubworldId(int id) {
        subworldID = id;
        return (PacketCoordinates) (Object) this;
    }

    @Override
    public int getSubworldId() {
        return subworldID;
    }

    @WrapOperation(
        method = "encode",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lgregapi/network/packets/PacketCoordinates;encode2(Lcom/google/common/io/ByteArrayDataOutput;)Lcom/google/common/io/ByteArrayDataOutput;"))
    public ByteArrayDataOutput encodeSubworldId(PacketCoordinates instance, ByteArrayDataOutput rOut,
        Operation<ByteArrayDataOutput> original) {
        rOut.writeInt(subworldID);
        return original.call(instance, rOut);
    }

    @WrapOperation(
        method = "decode",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lgregapi/network/packets/PacketCoordinates;decode2(IIILcom/google/common/io/ByteArrayDataInput;)Lgregapi/network/packets/PacketCoordinates;"))
    public PacketCoordinates decodeSubworldId(PacketCoordinates instance, int aX, int aY, int aZ,
        ByteArrayDataInput aData, Operation<PacketCoordinates> original) {
        int subworldID = aData.readInt();
        PacketCoordinates result = original.call(instance, aX, aY, aZ, aData);
        ((IMixinPacketCoordinates) result).setSubworldId(subworldID);
        return result;
    }

}
