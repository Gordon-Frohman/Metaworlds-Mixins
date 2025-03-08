package su.sergiusonesimus.metaworlds.zmixin.interfaces.network.play.server;

import net.minecraft.network.play.server.S08PacketPlayerPosLook;

public interface IMixinS08PacketPlayerPosLook {

    public int getSubWorldBelowFeetID();

    public S08PacketPlayerPosLook setSubWorldBelowFeetID(int ID);

}
