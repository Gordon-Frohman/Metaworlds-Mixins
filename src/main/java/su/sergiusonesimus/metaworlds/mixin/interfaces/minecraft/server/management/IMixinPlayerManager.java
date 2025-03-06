package su.sergiusonesimus.metaworlds.mixin.interfaces.minecraft.server.management;

import java.util.List;

import net.minecraft.world.ChunkCoordIntPair;

public interface IMixinPlayerManager {

    public void addWatchableChunks(List<ChunkCoordIntPair> chunksToAdd);

    public void removeWatchableChunks(List<ChunkCoordIntPair> chunksToRemove);

    public List getPlayers();
}
