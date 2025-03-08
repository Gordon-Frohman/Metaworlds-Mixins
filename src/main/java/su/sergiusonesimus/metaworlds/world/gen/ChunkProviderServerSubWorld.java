package su.sergiusonesimus.metaworlds.world.gen;

import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;

public class ChunkProviderServerSubWorld extends ChunkProviderServer {

    public ChunkProviderServerSubWorld(WorldServer par1WorldServer, IChunkLoader par2IChunkLoader,
        IChunkProvider par3IChunkProvider) {
        super(par1WorldServer, par2IChunkLoader, par3IChunkProvider);
    }

    public void populate(IChunkProvider par1IChunkProvider, int par2, int par3) {}
}
