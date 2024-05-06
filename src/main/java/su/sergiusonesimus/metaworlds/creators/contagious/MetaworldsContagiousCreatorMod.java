package su.sergiusonesimus.metaworlds.creators.contagious;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import su.sergiusonesimus.metaworlds.api.RecipeConfig;
import su.sergiusonesimus.metaworlds.api.RecipeConfig.RecipePlaceHolderDef;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(
    modid = "metaworldscontagiouscreatormod",
    name = "MetaworldsContagiousCreatorMod",
    version = "0.995",
    dependencies = "required-after:mwcore")
public class MetaworldsContagiousCreatorMod {

    public static final String MODID = "metaworldscontagiouscreatormod";
    public static final String VERSION = "0.995";
    public static RecipeConfig contagiousSubWorldCreatorConfig;
    public static Block contagiousSubWorldCreator;
    public static Block dummyBlock;
    protected Configuration config;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        this.config = new Configuration(event.getSuggestedConfigurationFile());
        contagiousSubWorldCreator = (new BlockContagiousSubWorldCreator()).setHardness(3.0F)
            .setResistance(15.0F)
            .setStepSound(Block.soundTypeStone)
            .setBlockName("contagiousSubWorldCreator")
            .setCreativeTab(CreativeTabs.tabBlock);
        contagiousSubWorldCreator
            .setBlockTextureName("metaworlds:" + contagiousSubWorldCreator.getUnlocalizedName());
        dummyBlock = new BlockDummyReobfTracker();
        ((BlockDummyReobfTracker) dummyBlock).initialize();
        this.config.load();
        contagiousSubWorldCreatorConfig = new RecipeConfig(
            this.config,
            "contagiousSubWorldCreator",
            new ItemStack(contagiousSubWorldCreator, 1, 0),
            false,
            new String[] { "DDD", "DDD", "DDD" },
            new RecipePlaceHolderDef[] { new RecipePlaceHolderDef('D', Blocks.dirt.getUnlocalizedName()) });
        this.config.save();
        GameRegistry.registerBlock(contagiousSubWorldCreator, "contagiousSubWorldCreator");
        contagiousSubWorldCreatorConfig.addRecipeToGameRegistry();
    }

    @EventHandler
    public void load(FMLInitializationEvent event) {}

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {}
}
