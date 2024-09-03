package su.sergiusonesimus.metaworlds.core;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;

import su.sergiusonesimus.metaworlds.core.client.SubWorldClient;
import su.sergiusonesimus.metaworlds.patcher.SubWorldFactory;

public class SubWorldClientFactory implements SubWorldFactory {

    public World CreateSubWorld(World parentWorld, int newSubWorldID) {
        return new SubWorldClient(
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
    }
}
