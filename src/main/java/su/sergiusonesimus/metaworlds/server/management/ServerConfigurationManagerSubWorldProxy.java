package su.sergiusonesimus.metaworlds.server.management;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.world.WorldServer;

import su.sergiusonesimus.metaworlds.server.MinecraftServerSubWorldProxy;

public class ServerConfigurationManagerSubWorldProxy extends ServerConfigurationManager {

    protected MinecraftServerSubWorldProxy mcServerProxy;
    protected WorldServer targetSubWorld;

    public ServerConfigurationManagerSubWorldProxy(MinecraftServerSubWorldProxy par1MinecraftServer,
        WorldServer targetWorld) {
        super(par1MinecraftServer);
        this.mcServerProxy = par1MinecraftServer;
        this.targetSubWorld = targetWorld;
        this.playerEntityList = targetWorld.playerEntities;
    }

    public NBTTagCompound getHostPlayerData() {
        return this.mcServerProxy.getRealServer()
            .getConfigurationManager()
            .getHostPlayerData();
    }

    /**
     * Gets the View Distance.
     */
    public int getViewDistance() {
        return this.mcServerProxy.getRealServer()
            .getConfigurationManager()
            .getViewDistance();
    }
}
