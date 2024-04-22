package net.tclproject.metaworlds.mixin.mixins.server;

import java.io.File;
import java.net.Proxy;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.network.NetworkSystem;
import net.minecraft.network.play.server.S03PacketTimeUpdate;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.ReportedException;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.storage.AnvilSaveConverter;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraftforge.common.DimensionManager;
import net.tclproject.metaworlds.api.IMixinWorld;

import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;

import cpw.mods.fml.common.FMLCommonHandler;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer {

    @Shadow(remap = true)
    public File anvilFile;

    @Shadow(remap = true)
    public ISaveFormat anvilConverterForAnvilFile;

    @Shadow(remap = true)
    public ICommandManager commandManager;

    @Shadow(remap = true)
    public Proxy serverProxy;

    @Shadow(remap = true)
    public NetworkSystem field_147144_o;

    @Shadow(remap = true)
    public YggdrasilAuthenticationService field_152364_T;

    @Shadow(remap = true)
    public MinecraftSessionService field_147143_S;

    @Shadow(remap = true)
    public long field_147142_T;

    @Shadow(remap = true)
    public GameProfileRepository field_152365_W;

    @Shadow(remap = true)
    public PlayerProfileCache field_152366_X;

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
    
    // To get an empty MinecraftServer variable
    // Firstly - disable the original constructor
    
	@Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/server/MinecraftServer;field_152366_X:Lnet/minecraft/server/management/PlayerProfileCache;", opcode = Opcodes.PUTFIELD))
	private void disableField_152366_X(MinecraftServer minecraftServer, PlayerProfileCache value) {
	    // Do nothing
	}
    
	@Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/server/MinecraftServer;mcServer:Lnet/minecraft/server/MinecraftServer;", opcode = Opcodes.PUTSTATIC))
	private void disableMcServer(MinecraftServer value) {
	    // Do nothing
	}
    
	@Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/server/MinecraftServer;serverProxy:Ljava/net/Proxy;", opcode = Opcodes.PUTFIELD))
	private void disableServerProxy(MinecraftServer minecraftServer, Proxy value) {
	    // Do nothing
	}
    
	@Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/server/MinecraftServer;anvilFile:Ljava/io/File;", opcode = Opcodes.PUTFIELD))
	private void disableAnvilFile(MinecraftServer minecraftServer, File value) {
	    // Do nothing
	}
    
	@Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/server/MinecraftServer;field_147144_o:Lnet/minecraft/network/NetworkSystem;", opcode = Opcodes.PUTFIELD))
	private void disableField_147144_o(MinecraftServer minecraftServer, NetworkSystem value) {
	    // Do nothing
	}
    
	@Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/server/MinecraftServer;commandManager:Lnet/minecraft/command/ICommandManager;", opcode = Opcodes.PUTFIELD))
	private void disableCommandManager(MinecraftServer minecraftServer, ICommandManager value) {
	    // Do nothing
	}
    
	@Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/server/MinecraftServer;anvilConverterForAnvilFile:Lnet/minecraft/world/storage/ISaveFormat;", opcode = Opcodes.PUTFIELD))
	private void disableAnvilConverterForAnvilFile(MinecraftServer minecraftServer, ISaveFormat value) {
	    // Do nothing
	}
	
	@Redirect(method = "<init>", at = @At(value = "NEW", target = "Lnet/minecraft/world/chunk/storage/AnvilSaveConverter;"))
	private AnvilSaveConverter anvilSaveConverterPlaceholder(File file) {
	  return (AnvilSaveConverter)MinecraftServer.mcServer.anvilConverterForAnvilFile;
	}
    
	@Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/server/MinecraftServer;field_152364_T:Lcom/mojang/authlib/yggdrasil/YggdrasilAuthenticationService;", opcode = Opcodes.PUTFIELD))
	private void disableField_152364_T(MinecraftServer minecraftServer, YggdrasilAuthenticationService value) {
	    // Do nothing
	}
	
	@Redirect(method = "<init>", at = @At(value = "NEW", target = "Lcom/mojang/authlib/yggdrasil/YggdrasilAuthenticationService;"))
	private YggdrasilAuthenticationService yggdrasilAuthenticationServicePlaceholder(Proxy proxy, String clientToken) {
	  return MinecraftServer.mcServer.field_152364_T;
	}
    
	@Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/server/MinecraftServer;field_147143_S:Lcom/mojang/authlib/minecraft/MinecraftSessionService;", opcode = Opcodes.PUTFIELD))
	private void disableField_147143_S(MinecraftServer minecraftServer, MinecraftSessionService value) {
	    // Do nothing
	}
	
	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/authlib/yggdrasil/YggdrasilAuthenticationService;createMinecraftSessionService()Lcom/mojang/authlib/minecraft/MinecraftSessionService;"))
	public MinecraftSessionService disableCreateMinecraftSessionService(YggdrasilAuthenticationService yggdrasilAuthenticationService) {
		// Do nothing, yet
		return null;
	}
    
	@Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/server/MinecraftServer;field_152365_W:Lcom/mojang/authlib/GameProfileRepository;", opcode = Opcodes.PUTFIELD))
	private void disableField_152365_W(MinecraftServer minecraftServer, GameProfileRepository value) {
	    // Do nothing
	}
	
	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/authlib/yggdrasil/YggdrasilAuthenticationService;createProfileRepository()Lcom/mojang/authlib/GameProfileRepository;"))
	public GameProfileRepository disableCreateProfileRepository(YggdrasilAuthenticationService yggdrasilAuthenticationService) {
		// Do nothing, yet
		return null;
	}
	
	// And secondly - set up a new constructor

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/server/MinecraftServer;field_152366_X:Lnet/minecraft/server/management/PlayerProfileCache;"), method = "<init>")
	private void init(File workDir, Proxy proxy, CallbackInfo info) {
		if(workDir == null && proxy == null) {
			// No fields should be set here at all
		}
		else {
	        this.field_152366_X = new PlayerProfileCache((MinecraftServer)(Object)this, MinecraftServer.field_152367_a);
	        MinecraftServer.mcServer = (MinecraftServer)(Object)this;
	        this.serverProxy = proxy;
	        this.anvilFile = workDir;
	        this.field_147144_o = new NetworkSystem((MinecraftServer)(Object)this);
	        this.commandManager = new ServerCommandManager();
	        this.anvilConverterForAnvilFile = new AnvilSaveConverter(workDir);
	        this.field_152364_T = new YggdrasilAuthenticationService(proxy, UUID.randomUUID().toString());
	        this.field_147143_S = this.field_152364_T.createMinecraftSessionService();
	        this.field_152365_W = this.field_152364_T.createProfileRepository();
		}
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
