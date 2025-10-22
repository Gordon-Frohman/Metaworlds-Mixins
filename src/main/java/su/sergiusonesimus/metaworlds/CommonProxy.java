package su.sergiusonesimus.metaworlds;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;

import su.sergiusonesimus.metaworlds.world.SubWorldServer;

public class CommonProxy {

    public World createSubWorld(World parentWorld, int newSubWorldID) {
        World subWorld = null;
        if (!parentWorld.isRemote) {
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

    public void onLoad() {

    }

    public World getMainWorld() {
        return MinecraftServer.getServer()
            .getEntityWorld();
    }

}
