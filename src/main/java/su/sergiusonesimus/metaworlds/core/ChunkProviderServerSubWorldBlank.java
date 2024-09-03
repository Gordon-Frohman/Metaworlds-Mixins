package su.sergiusonesimus.metaworlds.core;

import java.util.List;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;

import su.sergiusonesimus.metaworlds.patcher.ChunkSubWorld;

public class ChunkProviderServerSubWorldBlank extends ChunkProviderServer {

    public ChunkProviderServerSubWorldBlank(WorldServer par1WorldServer, IChunkLoader par2IChunkLoader,
        IChunkProvider par3IChunkProvider) {
        super(par1WorldServer, par2IChunkLoader, par3IChunkProvider);
    }

    public void populate(IChunkProvider par1IChunkProvider, int par2, int par3) {}

    public Chunk provideChunk(int par1, int par2) {
        return new ChunkSubWorld(this.worldObj, par1, par2);
    }

    public boolean unloadQueuedChunks() {
        return false;
    }

    public List getPossibleCreatures(EnumCreatureType par1EnumCreatureType, int par2, int par3, int par4) {
        return null;
    }
}
