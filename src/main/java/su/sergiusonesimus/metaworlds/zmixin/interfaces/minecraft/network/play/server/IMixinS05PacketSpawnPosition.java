package su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.network.play.server;

import net.minecraft.network.play.server.S05PacketSpawnPosition;

public interface IMixinS05PacketSpawnPosition {

    int getSpawnWorldID();

    S05PacketSpawnPosition setSpawnWorldID(int ID);

}
