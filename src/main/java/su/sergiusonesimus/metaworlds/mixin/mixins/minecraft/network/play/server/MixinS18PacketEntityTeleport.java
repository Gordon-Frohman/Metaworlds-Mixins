package su.sergiusonesimus.metaworlds.mixin.mixins.minecraft.network.play.server;

import java.io.IOException;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S14PacketEntity;
import net.minecraft.network.play.server.S18PacketEntityTeleport;
import su.sergiusonesimus.metaworlds.api.IMixinEntity;
import su.sergiusonesimus.metaworlds.api.IMixinWorld;
import su.sergiusonesimus.metaworlds.mixin.interfaces.network.play.server.IMixinS18PacketEntityTeleport;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(S18PacketEntityTeleport.class)
public class MixinS18PacketEntityTeleport implements IMixinS18PacketEntityTeleport {

    /** subWorld specific */
    public int subWorldId;
    public byte tractionLoss;
    public boolean losingTraction;
    public byte sendSubWorldPosFlag;
    public int xPosOnSubWorld;
    public int yPosOnSubWorld;
    public int zPosOnSubWorld;

    public int getSubWorldBelowFeetId() {
        return this.subWorldId;
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

    public int getXPosOnSubWorld()
    {
        return this.xPosOnSubWorld;
    }

    public int getYPosOnSubWorld()
    {
        return this.yPosOnSubWorld;
    }

    public int getZPosOnSubWorld()
    {
        return this.zPosOnSubWorld;
    }

    public IMixinS18PacketEntityTeleport setSubWorldId(int ID) {
        subWorldId = ID;
        return this;
    }

    public IMixinS18PacketEntityTeleport setTractionLoss(byte TL) {
        tractionLoss = TL;
        return this;
    }

    public IMixinS18PacketEntityTeleport setLosingTraction(boolean LT) {
        losingTraction = LT;
        return this;
    }

    public IMixinS18PacketEntityTeleport setSendSubWorldPosFlag(byte SSPF) {
        sendSubWorldPosFlag = SSPF;
        return this;
    }

    public IMixinS18PacketEntityTeleport setXPosOnSubWorld(int XPDS) {
        xPosOnSubWorld = XPDS;
        return this;
    }

    public IMixinS18PacketEntityTeleport setYPosOnSubWorld(int YPDS) {
        yPosOnSubWorld = YPDS;
        return this;
    }

    public IMixinS18PacketEntityTeleport setZPosOnSubWorld(int ZPDS) {
        zPosOnSubWorld = ZPDS;
        return this;
    }

    @Inject(method = "<init>(Lnet/minecraft/entity/Entity;)V", at = @At("TAIL"))
    public void S18PacketEntityTeleport(Entity entity, CallbackInfo ci) {
        this.subWorldId = ((IMixinWorld) ((IMixinEntity) entity).getWorldBelowFeet()).getSubWorldID();
        this.tractionLoss = ((IMixinEntity) entity).getTractionLossTicks();
        this.losingTraction = ((IMixinEntity) entity).isLosingTraction();
        this.sendSubWorldPosFlag = 0;
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    @Inject(method = "readPacketData", at = @At("TAIL"))
    public void readPacketData(PacketBuffer data, CallbackInfo ci) throws IOException {
        this.subWorldId = data.readUnsignedShort();
        this.tractionLoss = data.readByte();
        this.losingTraction = data.readBoolean();
        this.sendSubWorldPosFlag = data.readByte();
        if (this.sendSubWorldPosFlag != 0) {
            this.xPosOnSubWorld = data.readInt();
            this.yPosOnSubWorld = data.readInt();
            this.zPosOnSubWorld = data.readInt();
        }
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    @Inject(method = "writePacketData", at = @At("TAIL"))
    public void writePacketData(PacketBuffer data, CallbackInfo ci) throws IOException {
        data.writeShort(this.subWorldId);
        data.writeByte(this.tractionLoss);
        data.writeBoolean(this.losingTraction);
        data.writeByte(this.sendSubWorldPosFlag);
        if (this.sendSubWorldPosFlag != 0) {
            data.writeInt(this.xPosOnSubWorld);
            data.writeInt(this.yPosOnSubWorld);
            data.writeInt(this.zPosOnSubWorld);
        }
    }

}
