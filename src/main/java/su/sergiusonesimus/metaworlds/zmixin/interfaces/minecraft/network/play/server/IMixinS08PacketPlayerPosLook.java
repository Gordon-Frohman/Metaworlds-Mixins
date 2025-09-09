package su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.network.play.server;

import net.minecraft.network.play.server.S08PacketPlayerPosLook;

public interface IMixinS08PacketPlayerPosLook {

    public int getSubWorldBelowFeetID();

    public int getSubWorldBelowFeetType();

    public S08PacketPlayerPosLook setSubWorldBelowFeetID(int ID);

    public S08PacketPlayerPosLook setSubWorldBelowFeetType(int type);

}
