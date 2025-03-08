package su.sergiusonesimus.metaworlds.world;

import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;

public class SubWorldServerFactory implements SubWorldFactory {

    public World CreateSubWorld(World parentWorld, int newSubWorldID) {
        SubWorldServer.global_newSubWorldID = newSubWorldID;
        return new SubWorldServer(
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
}
