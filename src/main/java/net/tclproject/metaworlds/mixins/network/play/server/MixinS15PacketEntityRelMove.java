package net.tclproject.metaworlds.mixins.network.play.server;

import java.io.IOException;

import net.minecraft.network.PacketBuffer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.network.play.server.S14PacketEntity$S15PacketEntityRelMove")
public abstract class MixinS15PacketEntityRelMove {

    @Shadow(remap = true)
    private byte sendSubWorldPosFlag;

    @Shadow(remap = true)
    private byte xPosDiffOnSubWorld;

    @Shadow(remap = true)
    private byte yPosDiffOnSubWorld;

    @Shadow(remap = true)
    private byte zPosDiffOnSubWorld;

    /**
     * Reads the raw packet data from the data stream.
     */
    @Inject(method = "readPacketData", at = @At("TAIL"))
    public void readPacketData(PacketBuffer data, CallbackInfo ci) throws IOException {
        this.sendSubWorldPosFlag = data.readByte();
        if (this.sendSubWorldPosFlag != 0) {
            this.xPosDiffOnSubWorld = data.readByte();
            this.yPosDiffOnSubWorld = data.readByte();
            this.zPosDiffOnSubWorld = data.readByte();
        }
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    @Inject(method = "writePacketData", at = @At("TAIL"))
    public void writePacketData(PacketBuffer data, CallbackInfo ci) throws IOException {
        data.writeByte(this.sendSubWorldPosFlag);
        if (this.sendSubWorldPosFlag != 0) {
            data.writeByte(this.xPosDiffOnSubWorld);
            data.writeByte(this.yPosDiffOnSubWorld);
            data.writeByte(this.zPosDiffOnSubWorld);
        }
    }

}
