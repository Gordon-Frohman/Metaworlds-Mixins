package su.sergiusonesimus.metaworlds.zmixin.mixins.tfcPlus;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.dunk.tfc.Handlers.Network.DataBlockPacket;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.tfcPlus.IMixinDataBlockPacket;

@Mixin(DataBlockPacket.class)
public class MixinDataBlockPacket implements IMixinDataBlockPacket {

    private int subworldID = 0;

    @Override
    public int getSubworldId() {
        return subworldID;
    }

    @Override
    public DataBlockPacket setSubworldId(int id) {
        subworldID = id;
        return (DataBlockPacket) (Object) this;
    }

    @Inject(
        method = "encodeInto",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/PacketBuffer;writeInt(I)Lio/netty/buffer/ByteBuf;",
            remap = true,
            ordinal = 1,
            shift = Shift.AFTER),
        locals = LocalCapture.CAPTURE_FAILHARD)
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer, CallbackInfo ci,
        @Local(name = "pb") PacketBuffer pb) {
        pb.writeInt(subworldID);
    }

    @Inject(
        method = "decodeInto",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/PacketBuffer;readInt()I",
            remap = true,
            ordinal = 1,
            shift = Shift.AFTER),
        locals = LocalCapture.CAPTURE_FAILHARD)
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer, CallbackInfo ci,
        @Local(name = "pb") PacketBuffer pb) {
        subworldID = pb.readInt();
    }

    @WrapOperation(
        method = { "handleClientSide", "handleServerSide" },
        remap = false,
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/entity/player/EntityPlayer;worldObj:Lnet/minecraft/world/World;",
            remap = true))
    public World getSubworld(EntityPlayer player, Operation<World> original) {
        World playerWorld = original.call(player);
        World subworld = ((IMixinWorld) playerWorld).getSubWorld(subworldID);
        return subworld == null ? playerWorld : subworld;
    }

}
