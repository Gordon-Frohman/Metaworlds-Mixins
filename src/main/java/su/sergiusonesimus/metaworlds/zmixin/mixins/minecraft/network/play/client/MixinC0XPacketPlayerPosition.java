package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.network.play.client;

import java.io.IOException;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition;
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = { C04PacketPlayerPosition.class, C06PacketPlayerPosLook.class })
public class MixinC0XPacketPlayerPosition extends MixinC03PacketPlayer {

    /**
     * Reads the raw packet data from the data stream.
     */
    @Inject(method = "readPacketData", at = @At("TAIL"))
    public void readPacketData(PacketBuffer data, CallbackInfo ci) throws IOException {
        this.subWorldPosX = data.readDouble();
        this.subWorldPosY = data.readDouble();
        this.subWorldPosZ = data.readDouble();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    @Inject(method = "writePacketData", at = @At("TAIL"))
    public void writePacketData(PacketBuffer data, CallbackInfo ci) throws IOException {
        data.writeDouble(this.subWorldPosX);
        data.writeDouble(this.subWorldPosY);
        data.writeDouble(this.subWorldPosZ);
    }

}
