package su.sergiusonesimus.metaworlds.api;

import java.util.List;

import net.minecraft.world.ChunkCoordIntPair;

public interface PlayerManagerSuperClass {

    public void addWatchableChunks(List<ChunkCoordIntPair> chunksToAdd);

    public void removeWatchableChunks(List<ChunkCoordIntPair> chunksToRemove);

    public List getPlayers();
}
