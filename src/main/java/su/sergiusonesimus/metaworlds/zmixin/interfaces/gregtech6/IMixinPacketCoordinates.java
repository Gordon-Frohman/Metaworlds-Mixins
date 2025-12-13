package su.sergiusonesimus.metaworlds.zmixin.interfaces.gregtech6;

import gregapi.network.packets.PacketCoordinates;

public interface IMixinPacketCoordinates {

    PacketCoordinates setSubworldId(int id);

    int getSubworldId();

}
