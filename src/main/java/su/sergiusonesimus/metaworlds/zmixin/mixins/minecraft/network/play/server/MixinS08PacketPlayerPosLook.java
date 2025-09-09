package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.network.play.server;

import java.io.IOException;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.network.play.server.IMixinS08PacketPlayerPosLook;

@Mixin(S08PacketPlayerPosLook.class)
public abstract class MixinS08PacketPlayerPosLook implements IMixinS08PacketPlayerPosLook {

    @Shadow(remap = true)
    private double field_148939_c;

    // TODO

    private int subWorldBelowFeetID;

    private int subWorldBelowFeetType;

    public int getSubWorldBelowFeetID() {
        return subWorldBelowFeetID;
    }

    public int getSubWorldBelowFeetType() {
        return subWorldBelowFeetType;
    }

    public S08PacketPlayerPosLook setSubWorldBelowFeetID(int ID) {
        subWorldBelowFeetID = ID;
        return (S08PacketPlayerPosLook) (Object) this;
    }

    public S08PacketPlayerPosLook setSubWorldBelowFeetType(int type) {
        subWorldBelowFeetType = type;
        return (S08PacketPlayerPosLook) (Object) this;
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    @Inject(method = "readPacketData", at = @At("TAIL"))
    public void readPacketData(PacketBuffer data, CallbackInfo ci) throws IOException {
        this.subWorldBelowFeetID = data.readInt();
        this.subWorldBelowFeetType = data.readInt();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    @Inject(method = "writePacketData", at = @At("TAIL"))
    public void writePacketData(PacketBuffer data, CallbackInfo ci) throws IOException {
        data.writeInt(this.subWorldBelowFeetID);
        data.writeInt(this.subWorldBelowFeetType);
    }

}
