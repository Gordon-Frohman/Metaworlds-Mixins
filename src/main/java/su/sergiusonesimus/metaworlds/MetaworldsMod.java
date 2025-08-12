package su.sergiusonesimus.metaworlds;

import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.relauncher.Side;
import su.sergiusonesimus.metaworlds.admin.MwAdminGuiHandler;
import su.sergiusonesimus.metaworlds.admin.SubWorldImportProgressUpdater;
import su.sergiusonesimus.metaworlds.api.SubWorldTypeManager;
import su.sergiusonesimus.metaworlds.block.MetaworldsBlocks;
import su.sergiusonesimus.metaworlds.command.server.CommandMWAdmin;
import su.sergiusonesimus.metaworlds.command.server.CommandTPWorlds;
import su.sergiusonesimus.metaworlds.controls.SubWorldControllerKeyHandler;
import su.sergiusonesimus.metaworlds.entity.EntitySubWorldController;
import su.sergiusonesimus.metaworlds.item.MetaworldsItems;
import su.sergiusonesimus.metaworlds.network.MetaMagicNetwork;
import su.sergiusonesimus.metaworlds.network.play.client.CSubWorldProxyPacket;
import su.sergiusonesimus.metaworlds.network.play.server.SSubWorldProxyPacket;
import su.sergiusonesimus.metaworlds.serverlist.ServerListButtonAdder;
import su.sergiusonesimus.metaworlds.util.BlockVolatilityMap;
import su.sergiusonesimus.metaworlds.util.RotationHelper;

@Mod(modid = MetaworldsMod.MODID, name = "MetaWorlds (Mixins Version)")
public class MetaworldsMod {

    @Instance("MetaworldsMod")
    public static MetaworldsMod instance;
    public static final String MODID = "metaworlds";

    public GeneralPacketPipeline networkHandler;
    public static final String CHANNEL = "metaworlds";
    public static final String CHANNEL2 = "metaworldsController";

    @SidedProxy(
        clientSide = "su.sergiusonesimus.metaworlds.ClientProxy",
        serverSide = "su.sergiusonesimus.metaworlds.ServerProxy")
    public static CommonProxy proxy;

    public MetaworldsMod() {
        instance = this;
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        FMLCommonHandler.instance()
            .bus()
            .register(new EventHookContainer());
        MinecraftForge.EVENT_BUS.register(new EventHookContainer());

        MetaworldsBlocks.registerBlocks();

        MetaworldsItems.registerItems();

        EntityRegistry.registerModEntity(
            EntitySubWorldController.class,
            "EntitySubWorldController2",
            EntityRegistry.findGlobalUniqueEntityId(),
            this,
            80,
            3,
            true);

        SubWorldTypeManager.registerSubWorldType(SubWorldTypeManager.SUBWORLD_TYPE_DEFAULT);
        SubWorldTypeManager.registerSubWorldType(SubWorldTypeManager.SUBWORLD_TYPE_BOAT);
    }

    @EventHandler
    public void load(FMLInitializationEvent event) {
        MWCorePlayerTracker mwcpt = new MWCorePlayerTracker();
        FMLCommonHandler.instance()
            .bus()
            .register(mwcpt);
        MinecraftForge.EVENT_BUS.register(mwcpt);

        PlayerTickHandler playerTickHandler = new PlayerTickHandler();
        FMLCommonHandler.instance()
            .bus()
            .register(playerTickHandler);
        MinecraftForge.EVENT_BUS.register(playerTickHandler);

        if (event.getSide()
            .isClient()) {
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

        MetaMagicNetwork.registerPackets();

        networkHandler = new GeneralPacketPipeline();
        networkHandler.initialize(CHANNEL2);
        networkHandler.addDiscriminator(251, CSubWorldProxyPacket.class);
        networkHandler.addDiscriminator(250, SSubWorldProxyPacket.class);

        NetworkRegistry.INSTANCE.registerGuiHandler("metaworlds", new MwAdminGuiHandler());
        SubWorldImportProgressUpdater swipu = new SubWorldImportProgressUpdater();
        FMLCommonHandler.instance()
            .bus()
            .register(swipu);
        MinecraftForge.EVENT_BUS.register(swipu);

        // Controls

        if (event.getSide()
            .isClient()) {
            SubWorldControllerKeyHandler sckh = new SubWorldControllerKeyHandler();
            FMLCommonHandler.instance()
                .bus()
                .register(sckh);
            MinecraftForge.EVENT_BUS.register(sckh);
        }

        MWControlEventListener mwcListener = new MWControlEventListener();
        FMLCommonHandler.instance()
            .bus()
            .register(mwcListener);
        MinecraftForge.EVENT_BUS.register(mwcListener);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        RotationHelper.init();
        BlockVolatilityMap.init();
    }

    @EventHandler
    public void serverStart(FMLServerStartingEvent event) {
        MinecraftServer server = MinecraftServer.getServer();
        ICommandManager command = server.getCommandManager();
        ServerCommandManager manager = (ServerCommandManager) command;
        manager.registerCommand(new CommandTPWorlds());
        manager.registerCommand(new CommandMWAdmin());
    }

    @EventHandler
    public void onServerStopping(FMLServerStoppingEvent event) {
        if (FMLCommonHandler.instance()
            .getSide() == Side.CLIENT) {
            // Do stuff only for Single Player / integrated server
            MinecraftServer mc = FMLClientHandler.instance()
                .getServer();
            String allNames[] = mc.getAllUsernames()
                .clone();
            for (int i = 0; i < allNames.length; i++) {
                EntityPlayerMP player = MinecraftServer.getServer()
                    .getConfigurationManager()
                    .func_152612_a(allNames[i]);
                MWCorePlayerTracker.savePlayerData(player);
            }
        } else {
            // Do stuff only for dedicated server *shutdown*, for individual players logging out hook
            // PlayerLoggedOutEvent in an EventBus subscription instead
        }
    }

    public static void breakpoint() {
        int x = 0;
    }
}
