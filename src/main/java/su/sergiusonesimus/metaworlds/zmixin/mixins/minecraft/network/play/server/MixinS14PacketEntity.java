package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.network.play.server;

import java.io.IOException;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S14PacketEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.network.play.server.IMixinS14PacketEntity;

@Mixin(S14PacketEntity.class)
public abstract class MixinS14PacketEntity implements IMixinS14PacketEntity {

    /** subWorld specific */
    protected int subWorldBelowFeetId;
    protected byte tractionLoss;
    protected boolean losingTraction;
    protected byte sendSubWorldPosFlag;
    protected byte xPosDiffOnSubWorld;
    protected byte yPosDiffOnSubWorld;
    protected byte zPosDiffOnSubWorld;

    public int getSubWorldBelowFeetId() {
        return this.subWorldBelowFeetId;
    }

    public byte getTractionLoss() {
        return this.tractionLoss;
    }

    public boolean getLosingTraction() {
        return this.losingTraction;
    }

    public byte getSendSubWorldPosFlag() {
        return this.sendSubWorldPosFlag;
    }

    public byte getXPosDiffOnSubWorld() {
        return this.xPosDiffOnSubWorld;
    }

    public byte getYPosDiffOnSubWorld() {
        return this.yPosDiffOnSubWorld;
    }

    public byte getZPosDiffOnSubWorld() {
        return this.zPosDiffOnSubWorld;
    }

    public IMixinS14PacketEntity setSubWorldBelowFeetID(int ID) {
        subWorldBelowFeetId = ID;
        return this;
    }

    public IMixinS14PacketEntity setTractionLoss(byte TL) {
        tractionLoss = TL;
        return this;
    }

    public IMixinS14PacketEntity setLosingTraction(boolean LT) {
        losingTraction = LT;
        return this;
    }

    public IMixinS14PacketEntity setSendSubWorldPosFlag(byte SSPF) {
        sendSubWorldPosFlag = SSPF;
        return this;
    }

    public IMixinS14PacketEntity setXPosDiffOnSubWorld(byte XPDS) {
        xPosDiffOnSubWorld = XPDS;
        return this;
    }

    public IMixinS14PacketEntity setYPosDiffOnSubWorld(byte YPDS) {
        yPosDiffOnSubWorld = YPDS;
        return this;
    }

    public IMixinS14PacketEntity setZPosDiffOnSubWorld(byte ZPDS) {
        zPosDiffOnSubWorld = ZPDS;
        return this;
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    @Inject(method = "readPacketData", at = @At("TAIL"))
    public void readPacketData(PacketBuffer data, CallbackInfo ci) throws IOException {
        this.subWorldBelowFeetId = data.readInt();
        this.tractionLoss = data.readByte();
        this.losingTraction = data.readBoolean();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    @Inject(method = "writePacketData", at = @At("TAIL"))
    public void writePacketData(PacketBuffer data, CallbackInfo ci) throws IOException {
        data.writeInt(this.subWorldBelowFeetId);
        data.writeByte(this.tractionLoss);
        data.writeBoolean(this.losingTraction);
    }

}
