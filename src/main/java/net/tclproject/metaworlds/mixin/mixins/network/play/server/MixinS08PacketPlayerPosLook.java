package net.tclproject.metaworlds.mixin.mixins.network.play.server;

import java.io.IOException;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(S08PacketPlayerPosLook.class)
public abstract class MixinS08PacketPlayerPosLook implements IMixinS08PacketPlayerPosLook {

    private int subWorldBelowFeetID;

    public int getSubWorldBelowFeetID() {
        return subWorldBelowFeetID;
    }

    public S08PacketPlayerPosLook setSubWorldBelowFeetID(int ID) {
        subWorldBelowFeetID = ID;
        return (S08PacketPlayerPosLook) (Object) this;
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    @Inject(method = "readPacketData", at = @At("TAIL"))
    public void readPacketData(PacketBuffer data, CallbackInfo ci) throws IOException {
        this.subWorldBelowFeetID = data.readInt();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    @Inject(method = "writePacketData", at = @At("TAIL"))
    public void writePacketData(PacketBuffer data, CallbackInfo ci) throws IOException {
        data.writeInt(this.subWorldBelowFeetID);
    }

}
