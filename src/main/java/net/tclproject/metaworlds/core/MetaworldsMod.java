package net.tclproject.metaworlds.core;

import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.tclproject.metaworlds.admin.MwAdminGuiHandler;
import net.tclproject.metaworlds.admin.SubWorldImportProgressUpdater;
import net.tclproject.metaworlds.boats.MetaworldsBoatsMod;
import net.tclproject.metaworlds.serverlist.ServerListButtonAdder;
import net.tclproject.mysteriumlib.network.MetaMagicNetwork;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;

@Mod(modid = "mwcore", version = MetaworldsBoatsMod.VERSION, name = "MetaWorlds Core")
public class MetaworldsMod {

    public static final String CHANNEL = "mwcore";
    @Instance("MetaworldsMod")
    public static MetaworldsMod instance;

    public GeneralPacketPipeline networkHandler;
    public static final String CHANNEL2 = "mwcaptainmod";
    
    public MetaworldsMod() {
    	instance = this;
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    	// Moved it to MixinWorldClient
//        if (event.getSide()
//            .isClient()) {
//            WorldClient.subWorldFactory = new SubWorldClientFactory();
//        }

    	// Again, moved to MixinWorldServer
//        WorldServer.subWorldFactory = new SubWorldServerFactory();
//        FMLCommonHandler.instance()
//            .bus()
//            .register(new EventHookContainer());
        MinecraftForge.EVENT_BUS.register(new EventHookContainer());
    }

    @EventHandler
    public void load(FMLInitializationEvent event) {
        FMLCommonHandler.instance()
            .bus()
            .register(new MWCorePlayerTracker());
        FMLCommonHandler.instance()
            .bus()
            .register(new PlayerTickHandler());
        MinecraftForge.EVENT_BUS.register(new MWCorePlayerTracker());
        MinecraftForge.EVENT_BUS.register(new PlayerTickHandler());
        if (event.getSide()
            .isClient()) {
            FMLCommonHandler.instance()
                .bus()
                .register(new SubWorldClientPreTickHandler());
            FMLCommonHandler.instance()
                .bus()
                .register(new ServerListButtonAdder());
            MinecraftForge.EVENT_BUS.register(new SubWorldClientPreTickHandler());
            MinecraftForge.EVENT_BUS.register(new ServerListButtonAdder());
        }

        MetaMagicNetwork.registerPackets();

        networkHandler = new GeneralPacketPipeline();
        networkHandler.initialize(CHANNEL2);
        networkHandler.addDiscriminator(251, CSubWorldProxyPacket.class);
        networkHandler.addDiscriminator(250, SSubWorldProxyPacket.class);

        NetworkRegistry.INSTANCE.registerGuiHandler("mwcore", new MwAdminGuiHandler());
        FMLCommonHandler.instance()
            .bus()
            .register(new SubWorldImportProgressUpdater());
        MinecraftForge.EVENT_BUS.register(new SubWorldImportProgressUpdater());
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {

    }

    @EventHandler
    public void serverStart(FMLServerStartingEvent event) {
        MinecraftServer server = MinecraftServer.getServer();
        ICommandManager command = server.getCommandManager();
        ServerCommandManager manager = (ServerCommandManager) command;
        manager.registerCommand(new CommandTPWorlds());
        manager.registerCommand(new CommandMWAdmin());
    }
}
