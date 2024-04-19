package net.tclproject.metaworlds.joints;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import net.tclproject.metaworlds.api.RecipeConfig;
import net.tclproject.metaworlds.boats.MetaworldsBoatsMod;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = MetaworldsJointsMod.MODID, version = MetaworldsBoatsMod.VERSION, name = "MetaWorlds Joints")
public class MetaworldsJointsMod {

    public static final String MODID = "metaworldsjointsmod";
    public static RecipeConfig hingeJointBlockConfig;
    public static Block hingeJointBlock;
    protected Configuration config;
    @Instance("MetaworldsJointsMod")
    public static MetaworldsJointsMod instance;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        this.config = new Configuration(event.getSuggestedConfigurationFile());
        hingeJointBlock = (new BlockHingeJoint()).setHardness(0.5F)
            .setStepSound(Block.soundTypeWood)
            .setBlockName("subWorldHingeJoint");
        hingeJointBlock.setBlockTextureName("planks_oak");
        this.config.load();
        hingeJointBlockConfig = new RecipeConfig(
            this.config,
            "hingeJointBlock",
            new ItemStack(hingeJointBlock, 1, 0),
            false,
            new String[] { "I", "", "" },
            new RecipeConfig.RecipePlaceHolderDef[] {
                new RecipeConfig.RecipePlaceHolderDef('I', Blocks.iron_block.getUnlocalizedName()) });
        this.config.save();
        GameRegistry.registerBlock(hingeJointBlock, "subWorldHingeJoint");
        hingeJointBlockConfig.addRecipeToGameRegistry();
    }

    @EventHandler
    public void load(FMLInitializationEvent event) {}

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {}
}
