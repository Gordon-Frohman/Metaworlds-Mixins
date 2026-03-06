package su.sergiusonesimus.metaworlds.zmixin.mixins.warpdrive;

import java.util.Iterator;
import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.CameraRegistryItem;
import cr0s.warpdrive.data.CamerasRegistry;
import cr0s.warpdrive.data.EnumCameraType;
import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.warpdrive.IMixinCameraRegistryItem;

@Mixin(CamerasRegistry.class)
public class MixinCamerasRegistry {

    @Shadow(remap = false)
    private LinkedList<CameraRegistryItem> registry;

    @Overwrite(remap = false)
    public CameraRegistryItem getCameraByVideoChannel(World world, int videoChannel) {
        if (world == null) {
            return null;
        }
        CameraRegistryItem cam;
        for (final Iterator<CameraRegistryItem> it = registry.iterator(); it.hasNext();) {
            cam = it.next();
            if (cam.videoChannel == videoChannel && cam.dimensionId == world.provider.dimensionId) {
                if (isCamAlive(world, cam)) {
                    return cam;
                } else {
                    if (WarpDriveConfig.LOGGING_VIDEO_CHANNEL) {
                        int subworldId = ((IMixinCameraRegistryItem) cam).getSubworldId();
                        WarpDrive.logger.info(
                            "Removing 'dead' camera in subworld " + subworldId
                                + " at "
                                + cam.position.chunkPosX
                                + ", "
                                + cam.position.chunkPosY
                                + ", "
                                + cam.position.chunkPosZ
                                + " (while searching)");
                    }
                    it.remove();
                }
            }
        }

        // not found => dump registry
        printRegistry(world);
        return null;
    }

    @Overwrite(remap = false)
    private CameraRegistryItem getCamByPosition(World world, ChunkPosition position) {
        for (final CameraRegistryItem cam : registry) {
            if (cam.position.chunkPosX == position.chunkPosX && cam.position.chunkPosY == position.chunkPosY
                && cam.position.chunkPosZ == position.chunkPosZ
                && cam.dimensionId == world.provider.dimensionId
                && ((IMixinCameraRegistryItem) cam).getSubworldId() == ((IMixinWorld) world).getSubWorldID()) {
                return cam;
            }
        }

        return null;
    }

    @Overwrite(remap = false)
    private static boolean isCamAlive(World world, CameraRegistryItem cam) {
        if (world.provider.dimensionId != cam.dimensionId) {
            WarpDrive.logger
                .error("Inconsistent worldObj with camera " + world.provider.dimensionId + " vs " + cam.dimensionId);
            return false;
        }

        int subworldId = ((IMixinCameraRegistryItem) cam).getSubworldId();
        World subworld = ((IMixinWorld) ((IMixinWorld) world).getParentWorld()).getSubWorld(subworldId);

        if (!subworld.getChunkFromBlockCoords(cam.position.chunkPosX, cam.position.chunkPosZ).isChunkLoaded) {
            if (WarpDriveConfig.LOGGING_VIDEO_CHANNEL) {
                WarpDrive.logger.info(
                    "Reporting an 'unloaded' camera in subworld " + subworldId
                        + " of dimension "
                        + cam.dimensionId
                        + " at "
                        + cam.position.chunkPosX
                        + ", "
                        + cam.position.chunkPosY
                        + ", "
                        + cam.position.chunkPosZ);
            }
            return false;
        }
        final Block block = subworld.getBlock(cam.position.chunkPosX, cam.position.chunkPosY, cam.position.chunkPosZ);
        if (block != WarpDrive.blockCamera && block != WarpDrive.blockLaserCamera) {
            if (WarpDriveConfig.LOGGING_VIDEO_CHANNEL) {
                WarpDrive.logger.info(
                    "Reporting a 'dead' camera in subworld " + subworldId
                        + " of dimension "
                        + cam.dimensionId
                        + " at "
                        + cam.position.chunkPosX
                        + ", "
                        + cam.position.chunkPosY
                        + ", "
                        + cam.position.chunkPosZ);
            }
            return false;
        }

        return true;
    }

    @Overwrite(remap = false)
    private void removeDeadCams(World world) {
        if (world instanceof SubWorld subworld) {
            removeDeadCams(subworld.getParentWorld());
        } else {
            // LocalProfiler.start("CamRegistry Removing dead cameras");

            CameraRegistryItem cam;
            for (final Iterator<CameraRegistryItem> it = registry.iterator(); it.hasNext();) {
                cam = it.next();
                if (!isCamAlive(world, cam)) {
                    if (WarpDriveConfig.LOGGING_VIDEO_CHANNEL) {
                        int subworldId = ((IMixinCameraRegistryItem) cam).getSubworldId();
                        WarpDrive.logger.info(
                            "Removing 'dead' camera in subworld " + subworldId
                                + " of dimension "
                                + cam.dimensionId
                                + " at "
                                + cam.position.chunkPosX
                                + ", "
                                + cam.position.chunkPosY
                                + ", "
                                + cam.position.chunkPosZ);
                    }
                    it.remove();
                }
            }

            // LocalProfiler.stop();
        }
    }

    @Overwrite(remap = false)
    public void removeFromRegistry(World world, ChunkPosition position) {
        final CameraRegistryItem cam = getCamByPosition(world, position);
        if (cam != null) {
            if (WarpDriveConfig.LOGGING_VIDEO_CHANNEL) {
                int subworldId = ((IMixinCameraRegistryItem) cam).getSubworldId();
                WarpDrive.logger.info(
                    "Removing camera by request in subworld " + subworldId
                        + " of dimension "
                        + cam.dimensionId
                        + " at "
                        + cam.position.chunkPosX
                        + ", "
                        + cam.position.chunkPosY
                        + ", "
                        + cam.position.chunkPosZ);
            }
            registry.remove(cam);
        }
    }

    @Overwrite(remap = false)
    public void updateInRegistry(World world, ChunkPosition position, int videoChannel, EnumCameraType enumCameraType) {
        final CameraRegistryItem cam = new CameraRegistryItem(world, position, videoChannel, enumCameraType);
        removeDeadCams(world);
        int subworldId = ((IMixinCameraRegistryItem) cam).getSubworldId();

        if (isCamAlive(world, cam)) {
            final CameraRegistryItem existingCam = getCamByPosition(world, cam.position);
            if (existingCam == null) {
                if (WarpDriveConfig.LOGGING_VIDEO_CHANNEL) {
                    WarpDrive.logger.info(
                        "Adding 'live' camera in subworld " + subworldId
                            + " at "
                            + cam.position.chunkPosX
                            + ", "
                            + cam.position.chunkPosY
                            + ", "
                            + cam.position.chunkPosZ
                            + " with video channel '"
                            + cam.videoChannel
                            + "'");
                }
                registry.add(cam);
            } else if (existingCam.videoChannel != cam.videoChannel) {
                if (WarpDriveConfig.LOGGING_VIDEO_CHANNEL) {
                    WarpDrive.logger.info(
                        "Updating 'live' camera in subworld " + subworldId
                            + " at "
                            + cam.position.chunkPosX
                            + ", "
                            + cam.position.chunkPosY
                            + ", "
                            + cam.position.chunkPosZ
                            + " from video channel '"
                            + existingCam.videoChannel
                            + "' to video channel '"
                            + cam.videoChannel
                            + "'");
                }
                existingCam.videoChannel = cam.videoChannel;
            }
        } else {
            if (WarpDriveConfig.LOGGING_VIDEO_CHANNEL) {
                WarpDrive.logger.info(
                    "Unable to update 'dead' camera in subworld " + subworldId
                        + " at "
                        + cam.position.chunkPosX
                        + ", "
                        + cam.position.chunkPosY
                        + ", "
                        + cam.position.chunkPosZ);
            }
        }
    }

    @Overwrite(remap = false)
    public void printRegistry(final World world) {
        if (world == null) {
            return;
        }
        WarpDrive.logger.info("Cameras registry for dimension " + world.provider.dimensionId + ":");

        for (final CameraRegistryItem cam : registry) {
            int subworldId = ((IMixinCameraRegistryItem) cam).getSubworldId();
            WarpDrive.logger.info(
                "- " + cam.videoChannel
                    + " - subworld "
                    + subworldId
                    + " ("
                    + cam.position.chunkPosX
                    + ", "
                    + cam.position.chunkPosY
                    + ", "
                    + cam.position.chunkPosZ
                    + ")");
        }
    }
}
