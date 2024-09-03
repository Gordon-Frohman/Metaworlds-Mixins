package su.sergiusonesimus.metaworlds.controls.alignment;

import net.minecraft.block.Block;
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
    modid = "metaworldscontrolsalignmod",
    name = "MetaworldsControlsAlignMod",
    version = "0.995",
    dependencies = "required-after:mwcore")
public class MetaworldsControlsAlignMod {

    public static final String MODID = "metaworldscontrolsalignmod";
    public static final String VERSION = "0.995";
    public static RecipeConfig subWorldAlignerConfig;
    protected Configuration config;
    public static Block subWorldAligner;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        this.config = new Configuration(event.getSuggestedConfigurationFile());
        subWorldAligner = (new BlockSubWorldAligner()).setHardness(0.5F)
            .setStepSound(Block.soundTypeWood)
            .setBlockName("subWorldAligner");
        subWorldAligner.setBlockTextureName("metaworlds:" + subWorldAligner.getUnlocalizedName());
        this.config.load();
        subWorldAlignerConfig = new RecipeConfig(
            this.config,
            "subWorldAligner",
            new ItemStack(subWorldAligner, 1, 0),
            false,
            new String[] { "SSS", "SSS", "SSS" },
            new RecipePlaceHolderDef[] { new RecipePlaceHolderDef('S', Blocks.sand.getUnlocalizedName()) });
        this.config.save();
        GameRegistry.registerBlock(subWorldAligner, "subWorldAligner");
        subWorldAlignerConfig.addRecipeToGameRegistry();
    }

    @EventHandler
    public void load(FMLInitializationEvent event) {}

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {}
}
