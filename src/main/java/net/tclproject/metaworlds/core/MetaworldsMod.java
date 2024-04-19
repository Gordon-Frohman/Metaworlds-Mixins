package net.tclproject.metaworlds.core;

import java.io.File;
import java.io.PrintStream;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.EntitySorter;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderList;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBucket;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S14PacketEntity;
import net.minecraft.network.play.server.S18PacketEntityTeleport;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.MinecraftForge;
import net.tclproject.metaworlds.admin.MwAdminGuiHandler;
import net.tclproject.metaworlds.admin.SubWorldImportProgressUpdater;
import net.tclproject.metaworlds.boats.MetaworldsBoatsMod;
import net.tclproject.metaworlds.serverlist.ServerListButtonAdder;
import net.tclproject.mysteriumlib.network.MetaMagicNetwork;

import org.apache.logging.log4j.Level;
import org.objectweb.asm.util.ASMifier;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
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
    public static final String CHANNEL2 = "metaworldscontrolscaptainmod";

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
