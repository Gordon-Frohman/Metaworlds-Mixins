package su.sergiusonesimus.metaworlds.client.multiplayer;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import su.sergiusonesimus.metaworlds.world.chunk.ChunkSubWorld;
import su.sergiusonesimus.metaworlds.world.chunk.DirectionalChunk;

public class ChunkProviderClientSubWorld extends ChunkProviderClient {

    public List<DirectionalChunk> directionalChunkListing = new CopyOnWriteArrayList<DirectionalChunk>();

    public ChunkProviderClientSubWorld(World p_i1184_1_) {
        super(p_i1184_1_);
        this.blankChunk = new ChunkSubWorld(this.worldObj, 0, 0);
    }

    /**
     * loads or generates the chunk at the chunk location specified
     */
    public Chunk loadChunk(int p_73158_1_, int p_73158_2_) {
        ChunkSubWorld chunk = new ChunkSubWorld(this.worldObj, p_73158_1_, p_73158_2_);
        this.chunkMapping.add(ChunkCoordIntPair.chunkXZ2Int(p_73158_1_, p_73158_2_), chunk);
        this.chunkListing.add(chunk);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS
            .post(new net.minecraftforge.event.world.ChunkEvent.Load(chunk));
        chunk.isChunkLoaded = true;
        return chunk;
    }

    /**
     * Unloads chunks that are marked to be unloaded. This is not guaranteed to unload every such chunk.
     */
    public boolean unloadQueuedChunks() {
        long i = System.currentTimeMillis();
        Iterator iterator = this.chunkListing.iterator();

        while (iterator.hasNext()) {
            Chunk chunk = (Chunk) iterator.next();
            chunk.func_150804_b(System.currentTimeMillis() - i > 5L);
        }

        iterator = this.directionalChunkListing.iterator();

        while (iterator.hasNext()) {
            DirectionalChunk chunk = (DirectionalChunk) iterator.next();
            chunk.func_150804_b(System.currentTimeMillis() - i > 5L);
        }

        if (System.currentTimeMillis() - i > 100L) {
            logger.info(
                "Warning: Clientside chunk ticking took {} ms",
                new Object[] { Long.valueOf(System.currentTimeMillis() - i) });
        }

        return false;
    }

}
