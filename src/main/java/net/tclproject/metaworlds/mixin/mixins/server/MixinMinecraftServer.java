package net.tclproject.metaworlds.mixin.mixins.server;

import java.io.File;
import java.net.Proxy;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import net.minecraft.crash.CrashReport;
import net.minecraft.network.NetworkSystem;
import net.minecraft.network.play.server.S03PacketTimeUpdate;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.ReportedException;
import net.minecraft.util.Session;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.tclproject.metaworlds.api.IMixinWorld;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Multimap;

import cpw.mods.fml.common.FMLCommonHandler;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer {

    @Shadow(remap = true)
    public Profiler theProfiler;

    @Shadow(remap = true)
    private int tickCounter;

    @Shadow(remap = true)
    private ServerConfigurationManager serverConfigManager;

    @Shadow(remap = true)
    public Hashtable<Integer, long[]> worldTickTimes;

    @Shadow(remap = true)
    private List tickables;

    @Shadow(remap = true)
    abstract boolean getAllowNether();

    @Shadow(remap = true)
    public abstract NetworkSystem func_147137_ag();

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/server/MinecraftServer;field_152366_X:Lnet/minecraft/server/management/PlayerProfileCache;"), method = "<init>")
	// To get an empty MinecraftServer variable
	private void init(File workDir, Proxy proxy, CallbackInfo info) {
		if(workDir == null && proxy == null)
			return;
	}

    @Overwrite
    public void updateTimeLightAndEntities() {
        this.theProfiler.startSection("levels");
        net.minecraftforge.common.chunkio.ChunkIOExecutor.tick();
        int i;

        Integer[] ids = DimensionManager.getIDs(this.tickCounter % 200 == 0);
        for (int x = 0; x < ids.length; x++) {
            int id = ids[x];
            long j = System.nanoTime();

            if (id == 0 || this.getAllowNether()) {
            	Collection<World> worlds = ((IMixinWorld) DimensionManager.getWorld(id)).getWorlds();
            	if (worlds != null)
	                for (World curWorld : worlds) {
	                    WorldServer worldserver = (WorldServer) curWorld;// DimensionManager.getWorld(id);
	                    this.theProfiler.startSection(
	                        worldserver.getWorldInfo()
	                            .getWorldName());
	                    this.theProfiler.startSection("pools");
	                    this.theProfiler.endSection();
	
	                    if (this.tickCounter % 20 == 0) {
	                        this.theProfiler.startSection("timeSync");
	                        this.serverConfigManager.sendPacketToAllPlayersInDimension(
	                            new S03PacketTimeUpdate(
	                                worldserver.getTotalWorldTime(),
	                                worldserver.getWorldTime(),
	                                worldserver.getGameRules()
	                                    .getGameRuleBooleanValue("doDaylightCycle")),
	                            worldserver.provider.dimensionId);
	                        this.theProfiler.endSection();
	                    }
	
	                    this.theProfiler.startSection("tick");
	                    FMLCommonHandler.instance()
	                        .onPreWorldTick(worldserver);
	                    CrashReport crashreport;
	
	                    try {
	                        worldserver.tick();
	                    } catch (Throwable throwable1) {
	                        crashreport = CrashReport.makeCrashReport(throwable1, "Exception ticking world");
	                        worldserver.addWorldInfoToCrashReport(crashreport);
	                        throw new ReportedException(crashreport);
	                    }
	
	                    try {
	                        worldserver.updateEntities();
	                    } catch (Throwable throwable) {
	                        crashreport = CrashReport.makeCrashReport(throwable, "Exception ticking world entities");
	                        worldserver.addWorldInfoToCrashReport(crashreport);
	                        throw new ReportedException(crashreport);
	                    }
	
	                    FMLCommonHandler.instance()
	                        .onPostWorldTick(worldserver);
	                    this.theProfiler.endSection();
	                    this.theProfiler.startSection("tracker");
	                    worldserver.getEntityTracker()
	                        .updateTrackedEntities();
	                    this.theProfiler.endSection();
	                    this.theProfiler.endSection();
	                }
            }

            worldTickTimes.get(id)[this.tickCounter % 100] = System.nanoTime() - j;
        }

        this.theProfiler.endStartSection("dim_unloading");
        DimensionManager.unloadWorlds(worldTickTimes);
        this.theProfiler.endStartSection("connection");
        this.func_147137_ag()
            .networkTick();
        this.theProfiler.endStartSection("players");
        this.serverConfigManager.sendPlayerInfoToAllPlayers();
        this.theProfiler.endStartSection("tickables");

        for (i = 0; i < this.tickables.size(); ++i) {
            ((IUpdatePlayerListBox) this.tickables.get(i)).update();
        }

        this.theProfiler.endSection();
    }

}
