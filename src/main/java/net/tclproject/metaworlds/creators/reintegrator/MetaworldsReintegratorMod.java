package net.tclproject.metaworlds.creators.reintegrator;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(
    modid = "metaworldsreintegratormod",
    name = "MetaworldsReintegratorMod",
    version = "0.995",
    dependencies = "required-after:mwcore")
public class MetaworldsReintegratorMod {

    public static final String MODID = "metaworldsreintegratormod";
    public static final String VERSION = "0.995";
    public static Block subWorldReintegrator;
    public static Block dummyBlock;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        subWorldReintegrator = (new BlockSubWorldReintegrator()).setHardness(3.0F)
            .setResistance(15.0F)
            .setStepSound(Block.soundTypeStone)
            .setBlockName("subWorldReintegrator")
            .setCreativeTab(CreativeTabs.tabBlock);
        subWorldReintegrator.setBlockTextureName("planks_oak");
        dummyBlock = new BlockDummyReobfTracker();
        ((BlockDummyReobfTracker) dummyBlock).initialize();
        GameRegistry.registerBlock(subWorldReintegrator, "subWorldReintegrator");
    }
}
