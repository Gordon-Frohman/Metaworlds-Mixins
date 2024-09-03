package su.sergiusonesimus.metaworlds.creators.blank;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import su.sergiusonesimus.metaworlds.api.RecipeConfig;
import su.sergiusonesimus.metaworlds.api.RecipeConfig.RecipePlaceHolderDef;

@Mod(
    modid = "metaworldsblankcreatormod",
    name = "MetaworldsBlankCreatorMod",
    version = "0.995",
    dependencies = "required-after:mwcore")
public class MetaworldsBlankCreatorMod {

    public static final String MODID = "metaworldsblankcreatormod";
    public static final String VERSION = "0.995";
    public static RecipeConfig blankSubWorldCreatorConfig;
    public static Block blankSubWorldCreator;
    protected Configuration config;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        this.config = new Configuration(event.getSuggestedConfigurationFile());
        blankSubWorldCreator = (new BlockBlankSubWorldCreator()).setHardness(3.0F)
            .setResistance(15.0F)
            .setStepSound(Block.soundTypeStone)
            .setBlockName("blankSubWorldCreator")
            .setCreativeTab(CreativeTabs.tabBlock);
        blankSubWorldCreator.setBlockTextureName("metaworlds:" + blankSubWorldCreator.getUnlocalizedName());
        this.config.load();
        blankSubWorldCreatorConfig = new RecipeConfig(
            this.config,
            "blankSubWorldCreator",
            new ItemStack(blankSubWorldCreator, 1, 0),
            false,
            new String[] { "CCC", "CCC", "CCC" },
            new RecipePlaceHolderDef[] { new RecipePlaceHolderDef('C', Blocks.cobblestone.getUnlocalizedName()) });
        this.config.save();
        GameRegistry.registerBlock(blankSubWorldCreator, "blankSubWorldCreator");
        blankSubWorldCreatorConfig.addRecipeToGameRegistry();
    }

    @EventHandler
    public void load(FMLInitializationEvent event) {}

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {}
}
