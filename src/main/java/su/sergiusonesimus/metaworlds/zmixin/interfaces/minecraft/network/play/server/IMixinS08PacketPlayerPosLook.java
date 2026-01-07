package su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.network.play.server;

import net.minecraft.util.Vec3;

public interface IMixinS08PacketPlayerPosLook {

    public int getSubWorldBelowFeetID();

    public int getSubWorldBelowFeetType();

    public IMixinS08PacketPlayerPosLook setSubWorldBelowFeetID(int ID);

    public IMixinS08PacketPlayerPosLook setSubWorldBelowFeetType(int type);

    public IMixinS08PacketPlayerPosLook setSubWorldXPosition(double newX);

    public IMixinS08PacketPlayerPosLook setSubWorldYPosition(double newY);

    public IMixinS08PacketPlayerPosLook setSubWorldZPosition(double newZ);

    public double getSubWorldXPosition();

    public double getSubWorldYPosition();

    public double getSubWorldZPosition();

    public default Vec3 getSubWorldPosition() {
        return Vec3.createVectorHelper(getSubWorldXPosition(), getSubWorldYPosition(), getSubWorldZPosition());
    }

}
