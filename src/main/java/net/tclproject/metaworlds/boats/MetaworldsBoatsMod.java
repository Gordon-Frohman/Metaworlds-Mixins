package net.tclproject.metaworlds.boats;

import net.minecraft.block.Block;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = MetaworldsBoatsMod.MODID, version = MetaworldsBoatsMod.VERSION, name = "MetaWorlds Boats")
public class MetaworldsBoatsMod {

    public static final String MODID = "metaworldsboatsmod";
    public static final String VERSION = "0.995";
    public static Block floatingWoodBlock;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        floatingWoodBlock = (new BlockFloatingWood()).setHardness(3.0F)
            .setResistance(15.0F)
            .setStepSound(Block.soundTypeWood)
            .setBlockName("floatingWoodBlock");
        floatingWoodBlock.setBlockTextureName("planks_oak");
        GameRegistry.registerBlock(floatingWoodBlock, "floatingWoodBlock");
    }

    @EventHandler
    public void load(FMLInitializationEvent event) {}
}
