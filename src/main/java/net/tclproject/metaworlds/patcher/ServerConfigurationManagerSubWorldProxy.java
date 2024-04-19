package net.tclproject.metaworlds.patcher;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.world.WorldServer;

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
}
