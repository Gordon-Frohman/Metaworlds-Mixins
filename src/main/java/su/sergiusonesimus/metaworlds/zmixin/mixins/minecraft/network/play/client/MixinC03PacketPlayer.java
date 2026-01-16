package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.network.play.client;

import java.io.IOException;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C03PacketPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.network.play.client.IMixinC03PacketPlayer;

@Mixin(C03PacketPlayer.class)
public class MixinC03PacketPlayer implements IMixinC03PacketPlayer {

    protected int subWorldBelowFeetID;
    protected byte tractionLoss;
    protected boolean losingTraction;
    protected double subWorldPosX;
    protected double subWorldPosY;
    protected double subWorldPosZ;

    @Shadow(remap = true)
    private double field_149479_a;

    @Shadow(remap = true)
    private double field_149477_b;

    @Shadow(remap = true)
    private double field_149478_c;

    @Shadow(remap = true)
    private double field_149475_d;

    public int getSubWorldBelowFeetId() {
        return this.subWorldBelowFeetID;
    }

    public byte getTractionLoss() {
        return this.tractionLoss;
    }

    public boolean getLosingTraction() {
        return this.losingTraction;
    }

    public IMixinC03PacketPlayer setSubWorldBelowFeetId(int ID) {
        subWorldBelowFeetID = ID;
        return this;
    }

    public IMixinC03PacketPlayer setTractionLoss(byte TL) {
        tractionLoss = TL;
        return this;
    }

    public IMixinC03PacketPlayer setLosingTraction(boolean LT) {
        losingTraction = LT;
        return this;
    }

    public IMixinC03PacketPlayer setXPosition(double newX) {
        this.field_149479_a = newX;
        return this;
    }

    public IMixinC03PacketPlayer setYPosition(double newY) {
        this.field_149477_b = newY;
        return this;
    }

    public IMixinC03PacketPlayer setZPosition(double newZ) {
        this.field_149478_c = newZ;
        return this;
    }

    public IMixinC03PacketPlayer setStance(double newStance) {
        this.field_149475_d = newStance;
        return this;
    }

    public IMixinC03PacketPlayer setSubWorldXPosition(double newX) {
        this.subWorldPosX = newX;
        return this;
    }

    public IMixinC03PacketPlayer setSubWorldYPosition(double newY) {
        this.subWorldPosY = newY;
        return this;
    }

    public IMixinC03PacketPlayer setSubWorldZPosition(double newZ) {
        this.subWorldPosZ = newZ;
        return this;
    }

    public double getSubWorldXPosition() {
        return this.subWorldPosX;
    }

    public double getSubWorldYPosition() {
        return this.subWorldPosY;
    }

    public double getSubWorldZPosition() {
        return this.subWorldPosZ;
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    @Inject(method = "readPacketData", at = @At("TAIL"))
    public void readPacketData(PacketBuffer data, CallbackInfo ci) throws IOException {
        this.subWorldBelowFeetID = data.readInt();
        this.tractionLoss = data.readByte();
        this.losingTraction = data.readBoolean();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    @Inject(method = "writePacketData", at = @At("TAIL"))
    public void writePacketData(PacketBuffer data, CallbackInfo ci) throws IOException {
        data.writeInt(this.subWorldBelowFeetID);
        data.writeByte(this.tractionLoss);
        data.writeBoolean(this.losingTraction);
    }

}
