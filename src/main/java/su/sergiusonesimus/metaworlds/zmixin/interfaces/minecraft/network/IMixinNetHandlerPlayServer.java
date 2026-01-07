package su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.network;

public interface IMixinNetHandlerPlayServer {

    void setPlayerLocation(double x, double y, double z, float yaw, float pitch, int worldBelowFeetId,
        int worldBelowFeetType, double localPosX, double localPosY, double localPosZ);

}
