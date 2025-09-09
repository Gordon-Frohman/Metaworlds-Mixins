package su.sergiusonesimus.metaworlds.world;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;

import su.sergiusonesimus.metaworlds.client.multiplayer.SubWorldClient;

public class SubWorldFactory {

    public static SubWorldFactory instance = new SubWorldFactory();

    public World CreateSubWorld(World parentWorld, int newSubWorldID) {
        World subWorld;
        if (parentWorld.isRemote) {
            subWorld = new SubWorldClient(
                (WorldClient) parentWorld,
                newSubWorldID,
                ((WorldClient) parentWorld).sendQueue,
                new WorldSettings(
                    0L,
                    parentWorld.getWorldInfo()
                        .getGameType(),
                    false,
                    parentWorld.getWorldInfo()
                        .isHardcoreModeEnabled(),
                    parentWorld.getWorldInfo()
                        .getTerrainType()),
                ((WorldClient) parentWorld).provider.dimensionId,
                parentWorld.difficultySetting,
                parentWorld.theProfiler);
        } else {
            SubWorldServer.global_newSubWorldID = newSubWorldID;
            subWorld = new SubWorldServer(
                (WorldServer) parentWorld,
                newSubWorldID,
                ((WorldServer) parentWorld).func_73046_m(),
                parentWorld.getSaveHandler(),
                parentWorld.getWorldInfo()
                    .getWorldName(),
                parentWorld.provider.dimensionId,
                new WorldSettings(
                    0L,
                    parentWorld.getWorldInfo()
                        .getGameType(),
                    false,
                    parentWorld.getWorldInfo()
                        .isHardcoreModeEnabled(),
                    parentWorld.getWorldInfo()
                        .getTerrainType()),
                parentWorld.theProfiler);
        }
        return subWorld;
    }
}
