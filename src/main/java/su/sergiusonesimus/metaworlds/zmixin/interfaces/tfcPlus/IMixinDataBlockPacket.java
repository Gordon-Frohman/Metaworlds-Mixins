package su.sergiusonesimus.metaworlds.zmixin.interfaces.tfcPlus;

import com.dunk.tfc.Handlers.Network.DataBlockPacket;

public interface IMixinDataBlockPacket {

    int getSubworldId();

    DataBlockPacket setSubworldId(int id);

}
