package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.network.play.server;

import java.io.IOException;

import net.minecraft.network.PacketBuffer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.network.play.server.IMixinS14PacketEntity;

@Mixin(targets = "net.minecraft.network.play.server.S14PacketEntity$S17PacketEntityLookMove")
public abstract class MixinS17PacketEntityLookMove {

    /**
     * Reads the raw packet data from the data stream.
     */
    @Inject(method = "readPacketData", at = @At("TAIL"))
    public void readPacketData(PacketBuffer data, CallbackInfo ci) throws IOException {
        ((IMixinS14PacketEntity) this).setSendSubWorldPosFlag(data.readByte());
        if (((IMixinS14PacketEntity) this).getSendSubWorldPosFlag() != 0) {
            ((IMixinS14PacketEntity) this).setXPosDiffOnSubWorld(data.readByte());
            ((IMixinS14PacketEntity) this).setYPosDiffOnSubWorld(data.readByte());
            ((IMixinS14PacketEntity) this).setZPosDiffOnSubWorld(data.readByte());
        }
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    @Inject(method = "writePacketData", at = @At("TAIL"))
    public void writePacketData(PacketBuffer data, CallbackInfo ci) throws IOException {
        data.writeByte(((IMixinS14PacketEntity) this).getSendSubWorldPosFlag());
        if (((IMixinS14PacketEntity) this).getSendSubWorldPosFlag() != 0) {
            data.writeByte(((IMixinS14PacketEntity) this).getXPosDiffOnSubWorld());
            data.writeByte(((IMixinS14PacketEntity) this).getYPosDiffOnSubWorld());
            data.writeByte(((IMixinS14PacketEntity) this).getZPosDiffOnSubWorld());
        }
    }

}
