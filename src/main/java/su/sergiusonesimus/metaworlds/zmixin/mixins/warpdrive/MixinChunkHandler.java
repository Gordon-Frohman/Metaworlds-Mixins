package su.sergiusonesimus.metaworlds.zmixin.mixins.warpdrive;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cr0s.warpdrive.Commons;
import cr0s.warpdrive.LocalProfiler;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.ChunkData;
import cr0s.warpdrive.event.ChunkHandler;
import su.sergiusonesimus.metaworlds.integrations.warpdrive.SubworldChunkHandler;
import su.sergiusonesimus.metaworlds.zmixin.MixinPriorities;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

@Mixin(value = ChunkHandler.class, priority = MixinPriorities.WARPDRIVE)
public class MixinChunkHandler {

    @Shadow(remap = false)
    private static Map<Integer, Map<Long, ChunkData>> registryClient;

    @Shadow(remap = false)
    private static Map<Integer, Map<Long, ChunkData>> registryServer;

    @Shadow(remap = false)
    private static long CHUNK_HANDLER_UNLOADED_CHUNK_MAX_AGE_MS;

    @Shadow(remap = false)
    public static long delayLogging;

    // TODO

    @Shadow(remap = false)
    private static void updateTickLoopStep(final World world, final Map<Long, ChunkData> mapRegistryItems,
        final ChunkData chunkData) {}

    @Shadow(remap = false)
    private static ChunkData getChunkData(final boolean isRemote, final int dimensionId, final int x, final int y,
        final int z) {
        return null;
    }

    @Shadow(remap = false)
    private static ChunkData getChunkData(final boolean isRemote, final int dimensionId, final int xChunk,
        final int zChunk, final boolean doCreate) {
        return null;
    }

    /* event catchers */
    @SubscribeEvent
    @Overwrite(remap = false)
    public void onLoadWorld(final WorldEvent.Load event) {
        if (event.world.isRemote || event.world.provider.dimensionId == 0) {
            if (WarpDriveConfig.LOGGING_CHUNK_HANDLER) {
                WarpDrive.logger.info(
                    String.format(
                        "%s subworld %d in dimension %s load.",
                        event.world.isRemote ? "Client" : "Server",
                        ((IMixinWorld) event.world).getSubWorldID(),
                        event.world.provider.getDimensionName()));
            }
        }

        if (!event.world.isRemote && event.world.provider.dimensionId == 0) {
            // load star map
            final String filename = String.format(
                "%s/%s.dat",
                event.world.getSaveHandler()
                    .getWorldDirectory()
                    .getPath(),
                WarpDrive.MODID);
            final NBTTagCompound tagCompound = Commons.readNBTFromFile(filename);
            WarpDrive.starMap.readFromNBT(tagCompound);
        }
    }

    // new chunks aren't loaded
    @Overwrite(remap = false)
    public static void onGenerated(final World world, final int chunkX, final int chunkZ) {
        int subworldId = ((IMixinWorld) world).getSubWorldID();
        if (WarpDriveConfig.LOGGING_CHUNK_HANDLER) {
            WarpDrive.logger.info(
                String.format(
                    "%s subworld %d in dimension %s chunk [%d, %d] generating",
                    world.isRemote ? "Client" : "Server",
                    subworldId,
                    world.provider.getDimensionName(),
                    chunkX,
                    chunkZ));
        }

        final ChunkData chunkData = subworldId == 0
            ? getChunkData(world.isRemote, world.provider.dimensionId, chunkX, chunkZ, true)
            : SubworldChunkHandler
                .getChunkData(world.isRemote, world.provider.dimensionId, subworldId, chunkX, chunkZ, true);
        assert (chunkData != null);
        // (world can load a non-generated chunk, or the chunk be regenerated, so we reset only as needed)
        if (!chunkData.isLoaded()) {
            chunkData.load(new NBTTagCompound());
        }
    }

    // (server side only)
    @SubscribeEvent
    @Overwrite(remap = false)
    public void onLoadChunkData(final ChunkDataEvent.Load event) {
        int subworldId = ((IMixinWorld) event.world).getSubWorldID();
        if (WarpDriveConfig.LOGGING_CHUNK_HANDLER) {
            WarpDrive.logger.info(
                String.format(
                    "%s subworld %d in dimension %s chunk %s loading data (1)",
                    event.world.isRemote ? "Client" : "Server",
                    subworldId,
                    event.world.provider.getDimensionName(),
                    event.getChunk()
                        .getChunkCoordIntPair()));
        }

        final ChunkData chunkData = subworldId == 0
            ? getChunkData(
                event.world.isRemote,
                event.world.provider.dimensionId,
                event.getChunk().xPosition,
                event.getChunk().zPosition,
                true)
            : SubworldChunkHandler.getChunkData(
                event.world.isRemote,
                event.world.provider.dimensionId,
                subworldId,
                event.getChunk().xPosition,
                event.getChunk().zPosition,
                true);
        assert (chunkData != null);
        chunkData.load(event.getData());
    }

    // (called after data loading, or before a late generation, or on client side)
    @SubscribeEvent
    @Overwrite(remap = false)
    public void onLoadChunk(final ChunkEvent.Load event) {
        int subworldId = ((IMixinWorld) event.world).getSubWorldID();
        if (WarpDriveConfig.LOGGING_CHUNK_HANDLER) {
            WarpDrive.logger.info(
                String.format(
                    "%s subworld %d in dimension %s chunk %s loaded (2)",
                    event.world.isRemote ? "Client" : "Server",
                    subworldId,
                    event.world.provider.getDimensionName(),
                    event.getChunk()
                        .getChunkCoordIntPair()));
        }

        final ChunkData chunkData = subworldId == 0
            ? getChunkData(
                event.world.isRemote,
                event.world.provider.dimensionId,
                event.getChunk().xPosition,
                event.getChunk().zPosition,
                true)
            : SubworldChunkHandler.getChunkData(
                event.world.isRemote,
                event.world.provider.dimensionId,
                subworldId,
                event.getChunk().xPosition,
                event.getChunk().zPosition,
                true);
        assert (chunkData != null);
        if (!chunkData.isLoaded()) {
            chunkData.load(new NBTTagCompound());
        }
    }

    // (server side only)
    // not called when chunk wasn't changed since last save?
    @SubscribeEvent
    @Overwrite(remap = false)
    public void onSaveChunkData(final ChunkDataEvent.Save event) {
        int subworldId = ((IMixinWorld) event.world).getSubWorldID();
        if (WarpDriveConfig.LOGGING_CHUNK_HANDLER) {
            WarpDrive.logger.info(
                String.format(
                    "%s subworld %d in dimension %s chunk %s save data",
                    event.world.isRemote ? "Client" : "Server",
                    subworldId,
                    event.world.provider.getDimensionName(),
                    event.getChunk()
                        .getChunkCoordIntPair()));
        }
        final ChunkData chunkData = subworldId == 0
            ? getChunkData(
                event.world.isRemote,
                event.world.provider.dimensionId,
                event.getChunk().xPosition,
                event.getChunk().zPosition,
                false)
            : SubworldChunkHandler.getChunkData(
                event.world.isRemote,
                event.world.provider.dimensionId,
                subworldId,
                event.getChunk().xPosition,
                event.getChunk().zPosition,
                false);
        if (chunkData != null) {
            chunkData.save(event.getData());
        } else if (WarpDriveConfig.LOGGING_CHUNK_HANDLER) {
            WarpDrive.logger.error(
                String.format(
                    "%s subworld %d in dimension %s chunk %s is saving data without loading it first!",
                    event.world.isRemote ? "Client" : "Server",
                    subworldId,
                    event.world.provider.getDimensionName(),
                    event.getChunk()
                        .getChunkCoordIntPair()));
        }
    }

    // (server side only)
    @SubscribeEvent
    @Overwrite(remap = false)
    public void onSaveWorld(final WorldEvent.Save event) {
        if (event.world.provider.dimensionId != 0) {
            return;
        }
        int subworldId = ((IMixinWorld) event.world).getSubWorldID();
        if (WarpDriveConfig.LOGGING_CHUNK_HANDLER) {
            WarpDrive.logger.info(
                String.format(
                    "%s subworld %d in dimension %s saved.",
                    event.world.isRemote ? "Client" : "Server",
                    subworldId,
                    event.world.provider.getDimensionName()));
        }

        if (event.world.isRemote) {
            return;
        }

        // save star map
        final String filename = String.format(
            "%s/%s.dat",
            event.world.getSaveHandler()
                .getWorldDirectory()
                .getPath(),
            WarpDrive.MODID);
        final NBTTagCompound tagCompound = new NBTTagCompound();
        WarpDrive.starMap.writeToNBT(tagCompound);
        Commons.writeNBTToFile(filename, tagCompound);
    }

    @SubscribeEvent
    @Overwrite(remap = false)
    public void onUnloadWorld(final WorldEvent.Unload event) {
        int subworldId = ((IMixinWorld) event.world).getSubWorldID();
        if (WarpDriveConfig.LOGGING_CHUNK_HANDLER) {
            WarpDrive.logger.info(
                String.format(
                    "%s subworld %d in dimension %s unload",
                    event.world.isRemote ? "Client" : "Server",
                    subworldId,
                    event.world.provider.getDimensionName()));
        }

        // get dimension data
        LocalProfiler.updateCallStat("onUnloadWorld");
        final Map<Long, ChunkData> mapRegistryItems;
        if (subworldId == 0) {
            final Map<Integer, Map<Long, ChunkData>> registry = event.world.isRemote ? registryClient : registryServer;
            mapRegistryItems = registry.get(event.world.provider.dimensionId);
        } else {
            mapRegistryItems = SubworldChunkHandler
                .getChunkDataForSubworld(event.world.isRemote, event.world.provider.dimensionId, subworldId);
        }
        if (mapRegistryItems != null) {
            // unload chunks during shutdown
            for (final ChunkData chunkData : mapRegistryItems.values()) {
                if (chunkData.isLoaded()) {
                    chunkData.unload();
                }
            }
        }

        // @TODO unload star map
    }

    // (not called when closing SSP game)
    @SubscribeEvent
    @Overwrite(remap = false)
    public void onUnloadChunk(final ChunkEvent.Unload event) {
        int subworldId = ((IMixinWorld) event.world).getSubWorldID();
        if (WarpDriveConfig.LOGGING_CHUNK_HANDLER) {
            WarpDrive.logger.info(
                String.format(
                    "%s subworld %d in dimension %s chunk %s unload",
                    event.world.isRemote ? "Client" : "Server",
                    subworldId,
                    event.world.provider.getDimensionName(),
                    event.getChunk()
                        .getChunkCoordIntPair()));
        }

        final ChunkData chunkData = subworldId == 0
            ? getChunkData(
                event.world.isRemote,
                event.world.provider.dimensionId,
                event.getChunk().xPosition,
                event.getChunk().zPosition,
                false)
            : SubworldChunkHandler.getChunkData(
                event.world.isRemote,
                event.world.provider.dimensionId,
                subworldId,
                event.getChunk().xPosition,
                event.getChunk().zPosition,
                false);
        if (chunkData != null) {
            chunkData.unload();
        } else if (WarpDriveConfig.LOGGING_CHUNK_HANDLER) {
            WarpDrive.logger.error(
                String.format(
                    "%s subworld %d in dimension %s chunk %s is unloading without loading it first!",
                    event.world.isRemote ? "Client" : "Server",
                    subworldId,
                    event.world.provider.getDimensionName(),
                    event.getChunk()
                        .getChunkCoordIntPair()));
        }
    }

    @Overwrite(remap = false)
    public static void onBlockUpdated(final World world, final int x, final int y, final int z) {
        if (!world.isRemote) {
            final ChunkData chunkData = getChunkData(world, x, y, z);
            if (chunkData != null) {
                chunkData.onBlockUpdated(x, y, z);
            } else {
                if (WarpDriveConfig.LOGGING_WORLD_GENERATION) {
                    WarpDrive.logger.error(
                        String.format(
                            "%s subworld %d in dimension %s block updating at (%d %d %d), while chunk isn't loaded!",
                            world.isRemote ? "Client" : "Server",
                            ((IMixinWorld) world).getSubWorldID(),
                            world.provider.getDimensionName(),
                            x,
                            y,
                            z));
                    Commons.dumpAllThreads();
                }
            }
        }
    }

    /* internal access */
    /**
     * Return null and spam logs if chunk isn't already generated or loaded
     */
    @Overwrite(remap = false)
    public static ChunkData getChunkData(final World world, final int x, final int y, final int z) {
        int subworldId = ((IMixinWorld) world).getSubWorldID();
        final ChunkData chunkData = subworldId == 0 ? getChunkData(world.isRemote, world.provider.dimensionId, x, y, z)
            : SubworldChunkHandler.getChunkData(world.isRemote, world.provider.dimensionId, subworldId, x, y, z);
        if (chunkData == null) {
            WarpDrive.logger.error(
                String.format(
                    "Trying to get data from an non-loaded chunk in %s subworld %d in dimension %s @ (%d %d %d)",
                    world.isRemote ? "Client" : "Server",
                    subworldId,
                    world.provider.getDimensionName(),
                    x,
                    y,
                    z));
            LocalProfiler.printCallStats();
            Commons.dumpAllThreads();
            assert (false);
        }
        return chunkData;
    }

    /* commons */
    @Overwrite(remap = false)
    public static boolean isLoaded(final World world, final int x, final int y, final int z) {
        int subworldId = ((IMixinWorld) world).getSubWorldID();
        final ChunkData chunkData = subworldId == 0 ? getChunkData(world.isRemote, world.provider.dimensionId, x, y, z)
            : SubworldChunkHandler.getChunkData(world.isRemote, world.provider.dimensionId, subworldId, x, y, z);
        return chunkData != null && chunkData.isLoaded();
    }

    @Overwrite(remap = false)
    private static void updateTick(final World world) {
        // get dimension data
        LocalProfiler.updateCallStat("updateTick");
        int subworldId = ((IMixinWorld) world).getSubWorldID();
        final Map<Long, ChunkData> mapRegistryItems;
        if (subworldId == 0) {
            final Map<Integer, Map<Long, ChunkData>> registry = world.isRemote ? registryClient : registryServer;
            mapRegistryItems = registry.get(world.provider.dimensionId);
        } else {
            mapRegistryItems = SubworldChunkHandler
                .getChunkDataForSubworld(world.isRemote, world.provider.dimensionId, subworldId);
        }
        if (mapRegistryItems == null) {
            return;
        }
        int countLoaded = 0;
        final long timeForRemoval = System.currentTimeMillis() - CHUNK_HANDLER_UNLOADED_CHUNK_MAX_AGE_MS;
        final long timeForThrottle = System.currentTimeMillis() + 200;
        final long sizeBefore = mapRegistryItems.size();

        try {

            for (final Iterator<Entry<Long, ChunkData>> entryIterator = mapRegistryItems.entrySet()
                .iterator(); entryIterator.hasNext();) {
                final Map.Entry<Long, ChunkData> entryChunkData = entryIterator.next();
                final ChunkData chunkData = entryChunkData.getValue();
                // update loaded chunks, remove old unloaded chunks
                if (chunkData.isLoaded()) {
                    countLoaded++;
                    if (System.currentTimeMillis() < timeForThrottle) {
                        updateTickLoopStep(world, mapRegistryItems, entryChunkData.getValue());
                    }
                } else if (chunkData.timeUnloaded < timeForRemoval) {
                    if (WarpDriveConfig.LOGGING_CHUNK_HANDLER) {
                        WarpDrive.logger.info(
                            String.format(
                                "%s subworld %d in dimension %s chunk %s is being removed from updateTick (size is %d)",
                                world.isRemote ? "Client" : "Server",
                                ((IMixinWorld) world).getSubWorldID(),
                                world.provider.getDimensionName(),
                                chunkData.getChunkCoords(),
                                mapRegistryItems.size()));
                    }
                    entryIterator.remove();
                }
            }

        } catch (final ConcurrentModificationException exception) {
            WarpDrive.logger.error(
                String.format(
                    "%s subworld %d in dimension %s had some chunks changed outside main thread? (size %d -> %d)",
                    world.isRemote ? "Client" : "Server",
                    ((IMixinWorld) world).getSubWorldID(),
                    world.provider.getDimensionName(),
                    sizeBefore,
                    mapRegistryItems.size()));
            exception.printStackTrace();
            LocalProfiler.printCallStats();
        }

        if (WarpDriveConfig.LOGGING_CHUNK_HANDLER) {
            if (world.provider.dimensionId == 0) {
                delayLogging = (delayLogging + 1) % 4096;
            }
            if (delayLogging == 1) {
                WarpDrive.logger.info(
                    String.format(
                        "Subworld %d in dimension %d has %d / %d chunks loaded",
                        ((IMixinWorld) world).getSubWorldID(),
                        world.provider.dimensionId,
                        countLoaded,
                        mapRegistryItems.size()));
            }
        }
    }

}
