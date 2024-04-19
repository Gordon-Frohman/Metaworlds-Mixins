package net.tclproject.metaworlds.miniature;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import net.tclproject.metaworlds.api.RecipeConfig;
import net.tclproject.metaworlds.api.RecipeConfig.RecipePlaceHolderDef;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(
    modid = "metaworldsminiaturemod",
    name = "MetaworldsMiniatureMod",
    version = "0.995",
    dependencies = "required-after:mwcore")
public class MetaworldsMiniatureMod {

    public static final String MODID = "metaworldsminiaturemod";
    public static final String VERSION = "0.995";
    public static Block miniaturizerBlock;
    public static RecipeConfig miniaturizerBlockConfig;
    public static Block scaleNormalizerBlock;
    public static RecipeConfig scaleNormalizerBlockConfig;
    public static Block supersizerBlock;
    public static RecipeConfig supersizerBlockConfig;
    public static Item emptyWorldBottleItem;
    public static RecipeConfig emptyWorldBottleItemConfig;
    public static Item bottledWorldItem;
    protected Configuration config;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        this.config = new Configuration(event.getSuggestedConfigurationFile());
        miniaturizerBlock = (new BlockMiniaturizer()).setHardness(0.5F)
            .setStepSound(Block.soundTypeWood)
            .setBlockName("subWorldMiniaturizer");
        miniaturizerBlock.setBlockTextureName("metaworldsminiaturemod:" + miniaturizerBlock.getUnlocalizedName());
        scaleNormalizerBlock = (new BlockScaleNormalizer()).setHardness(0.5F)
            .setStepSound(Block.soundTypeWood)
            .setBlockName("subWorldScaleNormalizer");
        scaleNormalizerBlock.setBlockTextureName("metaworldsminiaturemod:" + scaleNormalizerBlock.getUnlocalizedName());
        supersizerBlock = (new BlockSupersizer()).setHardness(0.5F)
            .setStepSound(Block.soundTypeWood)
            .setBlockName("subWorldSupersizer");
        supersizerBlock.setBlockTextureName("metaworldsminiaturemod:" + supersizerBlock.getUnlocalizedName());
        emptyWorldBottleItem = (new ItemEmptyWorldBottle()).setUnlocalizedName("emptyWorldBottle");
        emptyWorldBottleItem.setTextureName("potion_bottle_empty");
        bottledWorldItem = (new ItemBottledWorld()).setUnlocalizedName("bottledWorld");
        bottledWorldItem.setTextureName("metaworldsminiaturemod:" + bottledWorldItem.getUnlocalizedName());
        this.config.load();
        miniaturizerBlockConfig = new RecipeConfig(
            this.config,
            "miniaturizerBlock",
            new ItemStack(miniaturizerBlock, 1, 0),
            false,
            new String[] { "G", "", "" },
            new RecipePlaceHolderDef[] { new RecipePlaceHolderDef('G', Blocks.glass.getUnlocalizedName()) });
        scaleNormalizerBlockConfig = new RecipeConfig(
            this.config,
            "scaleNormalizerBlock",
            new ItemStack(scaleNormalizerBlock, 1, 0),
            false,
            new String[] { "GGG", "GGG", "GGG" },
            new RecipePlaceHolderDef[] { new RecipePlaceHolderDef('G', Blocks.gravel.getUnlocalizedName()) });
        supersizerBlockConfig = new RecipeConfig(
            this.config,
            "supersizerBlock",
            new ItemStack(supersizerBlock, 1, 0),
            false,
            new String[] { "GGG", "GGG", "GGG" },
            new RecipePlaceHolderDef[] { new RecipePlaceHolderDef('G', Blocks.glass.getUnlocalizedName()) });
        emptyWorldBottleItemConfig = new RecipeConfig(
            this.config,
            "emptyWorldBottleItem",
            new ItemStack(emptyWorldBottleItem, 1, 0),
            true,
            new String[] { "GGG", "G G", "GGG" },
            new RecipePlaceHolderDef[] { new RecipePlaceHolderDef('G', Blocks.glass_pane.getUnlocalizedName()) });
        this.config.save();
        GameRegistry.registerBlock(miniaturizerBlock, "subWorldMiniaturizer");
        GameRegistry.registerBlock(scaleNormalizerBlock, "subWorldScaleNormalizer");
        GameRegistry.registerBlock(supersizerBlock, "subWorldSupersizer");
        GameRegistry.registerItem(emptyWorldBottleItem, "emptyWorldBottle");
        GameRegistry.registerItem(bottledWorldItem, "bottledWorld");
        miniaturizerBlockConfig.addRecipeToGameRegistry();
        scaleNormalizerBlockConfig.addRecipeToGameRegistry();
        supersizerBlockConfig.addRecipeToGameRegistry();
        emptyWorldBottleItemConfig.addRecipeToGameRegistry();
    }

    @EventHandler
    public void load(FMLInitializationEvent event) {}

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {}
}
