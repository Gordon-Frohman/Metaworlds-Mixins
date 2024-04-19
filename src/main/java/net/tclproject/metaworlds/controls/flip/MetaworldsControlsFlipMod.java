package net.tclproject.metaworlds.controls.flip;

import net.minecraft.block.Block;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(
    modid = "metaworldscontrolsflipmod",
    name = "MetaworldsControlsFlipMod",
    version = "0.995",
    dependencies = "required-after:mwcore")
public class MetaworldsControlsFlipMod {

    public static final String MODID = "metaworldscontrolsflipmod";
    public static final String VERSION = "0.995";
    public static Block flipSubWorldBlock;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        flipSubWorldBlock = (new BlockFlipSubWorld()).setHardness(0.5F)
            .setStepSound(Block.soundTypeStone)
            .setBlockName("flipSubWorldBlock");
        flipSubWorldBlock.setBlockTextureName("planks_oak");
        GameRegistry.registerBlock(flipSubWorldBlock, "flipSubWorldBlock");
    }

    @EventHandler
    public void load(FMLInitializationEvent event) {}
}
