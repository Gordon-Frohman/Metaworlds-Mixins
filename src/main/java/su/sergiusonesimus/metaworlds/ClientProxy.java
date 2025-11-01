package su.sergiusonesimus.metaworlds;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.common.FMLCommonHandler;
import su.sergiusonesimus.metaworlds.client.multiplayer.SubWorldClient;
import su.sergiusonesimus.metaworlds.serverlist.ServerListButtonAdder;

public class ClientProxy extends CommonProxy {

    public World createSubWorld(World parentWorld, int newSubWorldID) {
        World subWorld = null;
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
            subWorld = super.createSubWorld(parentWorld, newSubWorldID);
        }
        return subWorld;
    }

    public void onLoad() {
        SubWorldClientPreTickHandler swcpth = new SubWorldClientPreTickHandler();
        FMLCommonHandler.instance()
            .bus()
            .register(swcpth);
        MinecraftForge.EVENT_BUS.register(swcpth);

        ServerListButtonAdder slba = new ServerListButtonAdder();
        FMLCommonHandler.instance()
            .bus()
            .register(slba);
        MinecraftForge.EVENT_BUS.register(slba);
    }

    public World getMainWorld() {
        return Minecraft.getMinecraft().theWorld;
    }

}
