package su.sergiusonesimus.metaworlds.zmixin.interfaces.tfc;

import com.bioxx.tfc.Handlers.Network.DataBlockPacket;

public interface IMixinDataBlockPacket {

    int getSubworldId();

    DataBlockPacket setSubworldId(int id);

}
