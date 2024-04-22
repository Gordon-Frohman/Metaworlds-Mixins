package net.tclproject.metaworlds.patcher;

import java.io.IOException;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;

public class MinecraftServerSubWorldProxy extends MinecraftServer {

    MinecraftServer realServer;
    WorldServer targetSubWorld;

    public MinecraftServerSubWorldProxy(MinecraftServer original) {
        super(null, null);
        
    	this.serverProxy = null;
    	this.anvilFile = null;
    	this.field_147144_o = null;
    	this.commandManager = null;
    	this.anvilConverterForAnvilFile = null;
    	this.field_147143_S = null;
    	this.field_152364_T = original.field_152364_T;
    	this.field_152365_W = original.field_152365_W;
    	this.field_152366_X = original.field_152366_X;
    	
        this.realServer = original;
    }

    public void setWorld(WorldServer newWorld) {
        this.targetSubWorld = newWorld;
        this.func_152361_a(new ServerConfigurationManagerSubWorldProxy(this, newWorld));
    }

    public MinecraftServer getRealServer() {
        return this.realServer;
    }

    public ServerConfigurationManager getConfigurationManager() {
        return this.targetSubWorld == null ? this.realServer.getConfigurationManager()
            : super.getConfigurationManager();
    }

    public boolean getCanSpawnAnimals() {
        return this.realServer.getCanSpawnAnimals();
    }

    protected boolean startServer() throws IOException {
        return false;
    }

    public boolean canStructuresSpawn() {
        return false;
    }

    public WorldSettings.GameType getGameType() {
        return this.realServer.getGameType();
    }

    public EnumDifficulty func_147135_j() {
        return this.realServer.func_147135_j();
    }

    public boolean isHardcore() {
        return this.realServer.isHardcore();
    }

    public int getOpPermissionLevel() {
        return this.realServer.getOpPermissionLevel();
    }

    public boolean isDedicatedServer() {
        return this.realServer.isDedicatedServer();
    }

    public boolean isCommandBlockEnabled() {
        return this.realServer.isCommandBlockEnabled();
    }

    public String shareToLAN(WorldSettings.GameType var1, boolean var2) {
        return null;
    }

    @Override
    public boolean func_152363_m() {
        return this.realServer.func_152363_m();
    }
}
