package net.tclproject.metaworlds;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = TestMod.MODID, version = TestMod.VERSION, name = "MetaWorlds Test Mod")
public class TestMod {

    public static TestMod INSTANCE;
    public static final String MODID = "mwtestmod";
    public static final String VERSION = "1.0a";

    @SidedProxy(
        clientSide = "net.tclproject.metaworlds.ClientProxy",
        serverSide = "net.tclproject.metaworlds.ServerProxy")
    public static CommonProxy proxy;

    @EventHandler
    public void load(FMLPreInitializationEvent event) {
        INSTANCE = this;
    }
}
