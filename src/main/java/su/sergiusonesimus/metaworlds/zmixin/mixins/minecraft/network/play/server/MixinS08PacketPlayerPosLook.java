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
public class MixinS08PacketPlayerPosLook implements IMixinS08PacketPlayerPosLook {

    @Shadow(remap = true)
    private double field_148939_c;

    // TODO

    private int subWorldBelowFeetID;

    private int subWorldBelowFeetType;

    protected double subWorldPosX;
    protected double subWorldPosY;
    protected double subWorldPosZ;

    public int getSubWorldBelowFeetID() {
        return subWorldBelowFeetID;
    }

    public int getSubWorldBelowFeetType() {
        return subWorldBelowFeetType;
    }

    public IMixinS08PacketPlayerPosLook setSubWorldBelowFeetID(int ID) {
        subWorldBelowFeetID = ID;
        return this;
    }

    public IMixinS08PacketPlayerPosLook setSubWorldBelowFeetType(int type) {
        subWorldBelowFeetType = type;
        return this;
    }

    @Override
    public IMixinS08PacketPlayerPosLook setSubWorldXPosition(double newX) {
        this.subWorldPosX = newX;
        return this;
    }

    @Override
    public IMixinS08PacketPlayerPosLook setSubWorldYPosition(double newY) {
        this.subWorldPosY = newY;
        return this;
    }

    @Override
    public IMixinS08PacketPlayerPosLook setSubWorldZPosition(double newZ) {
        this.subWorldPosZ = newZ;
        return this;
    }

    @Override
    public double getSubWorldXPosition() {
        return this.subWorldPosX;
    }

    @Override
    public double getSubWorldYPosition() {
        return this.subWorldPosY;
    }

    @Override
    public double getSubWorldZPosition() {
        return this.subWorldPosZ;
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    @Inject(method = "readPacketData", at = @At("TAIL"))
    public void readPacketData(PacketBuffer data, CallbackInfo ci) throws IOException {
        this.subWorldBelowFeetID = data.readInt();
        this.subWorldBelowFeetType = data.readInt();
        this.subWorldPosX = data.readDouble();
        this.subWorldPosY = data.readDouble();
        this.subWorldPosZ = data.readDouble();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    @Inject(method = "writePacketData", at = @At("TAIL"))
    public void writePacketData(PacketBuffer data, CallbackInfo ci) throws IOException {
        data.writeInt(this.subWorldBelowFeetID);
        data.writeInt(this.subWorldBelowFeetType);
        data.writeDouble(this.subWorldPosX);
        data.writeDouble(this.subWorldPosY);
        data.writeDouble(this.subWorldPosZ);
    }

}
