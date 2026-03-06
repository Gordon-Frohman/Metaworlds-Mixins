package su.sergiusonesimus.metaworlds.integrations.warpdrive;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.LocalProfiler;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.ChunkData;

public class SubworldChunkHandler {

    public static World currentWorld;

    // persistent properties
    // Maps' keys now contain both dimension and subworld id instead of just dimension
    private static final Map<Long, Map<Long, ChunkData>> registryClient = new HashMap<>(32);
    private static final Map<Long, Map<Long, ChunkData>> registryServer = new HashMap<>(32);

    /**
     * Return null if chunk isn't already generated or loaded
     */
    public static ChunkData getChunkData(final boolean isRemote, final int dimensionId, final int subworldId,
        final int x, final int y, final int z) {
        assert (y >= -1 && y <= 256); // includes 1 block tolerance for mirroring
        return getChunkData(isRemote, dimensionId, subworldId, x >> 4, z >> 4, false);
    }

    public static ChunkData getChunkData(final boolean isRemote, final int dimensionId, final int subworldId,
        final int xChunk, final int zChunk, final boolean doCreate) {
        // get dimension data
        LocalProfiler.updateCallStat("getChunkData");
        final Map<Long, ChunkData> mapRegistryItems = getChunkDataForSubworld(
            isRemote,
            dimensionId,
            subworldId,
            doCreate);
        // (lambda expressions are forcing synchronisation, so we don't use them here)
        // noinspection Java8MapApi
        if (mapRegistryItems == null) return null;
        // get chunk data
        final long index = ChunkCoordIntPair.chunkXZ2Int(xChunk, zChunk);
        ChunkData chunkData = mapRegistryItems.get(index);
        // (lambda expressions are forcing synchronisation, so we don't use them here)
        // noinspection Java8MapApi
        if (chunkData == null) {
            if (!doCreate) {
                if (WarpDriveConfig.LOGGING_CHUNK_HANDLER) {
                    WarpDrive.logger.info(
                        String.format(
                            "getChunkData(%s, %d, %d, %d, %d, false) returning null",
                            isRemote,
                            dimensionId,
                            subworldId,
                            xChunk,
                            zChunk));
                }
                return null;
            }
            chunkData = new ChunkData(xChunk, zChunk);
            if (WarpDriveConfig.LOGGING_CHUNK_HANDLER) {
                WarpDrive.logger.info(
                    String.format(
                        "%s subworld %d in DIM%d chunk %s is being added to the registry",
                        isRemote ? "Client" : "Server",
                        subworldId,
                        dimensionId,
                        chunkData.getChunkCoords()));
            }
            if (Commons.isSafeThread()) {
                mapRegistryItems.put(index, chunkData);
            } else {
                WarpDrive.logger.error(
                    String.format(
                        "%s subworld %d in DIM%d chunk %s is being added to the registry outside main thread!",
                        isRemote ? "Client" : "Server",
                        subworldId,
                        dimensionId,
                        chunkData.getChunkCoords()));
                Commons.dumpAllThreads();
                mapRegistryItems.put(index, chunkData);
            }
        }
        return chunkData;
    }

    public static Map<Long, ChunkData> getChunkDataForSubworld(boolean isRemote, int dimension, Integer subworld) {
        return getChunkDataForSubworld(isRemote, dimension, subworld, false);
    }

    private static Map<Long, ChunkData> getChunkDataForSubworld(boolean isRemote, int dimension, Integer subworld,
        boolean doCreate) {
        Map<Long, Map<Long, ChunkData>> registry = isRemote ? registryClient : registryServer;
        Long key = ((long) subworld << 32) | (dimension & 0xffffffffL);
        Map<Long, ChunkData> result = registry.get(key);
        if (result == null && doCreate) {
            result = new LinkedHashMap<>(2048);
            registry.put(key, result);
        }
        return result;
    }
}
