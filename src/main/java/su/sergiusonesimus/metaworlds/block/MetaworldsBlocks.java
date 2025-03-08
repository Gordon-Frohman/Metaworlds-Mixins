package su.sergiusonesimus.metaworlds.block;

import net.minecraft.block.Block;

import cpw.mods.fml.common.registry.GameRegistry;

public class MetaworldsBlocks {

    public static Block floatingWoodBlock;
    public static Block subWorldAligner;
    public static Block subWorldController;
    public static Block flipSubWorldBlock;
    public static Block blankSubWorldCreator;
    public static Block contagiousSubWorldCreator;
    public static Block subWorldReintegrator;
    public static Block dummyBlock;
    public static Block hingeJointBlock;
    public static Block miniaturizerBlock;
    public static Block scaleNormalizerBlock;
    public static Block supersizerBlock;

    public static void registerBlocks() {
        floatingWoodBlock = new BlockFloatingWood().setHardness(3.0F)
            .setResistance(15.0F)
            .setStepSound(Block.soundTypeWood)
            .setBlockName("floatingWoodBlock")
            .setBlockTextureName("planks_oak");
        subWorldAligner = new BlockSubWorldAligner().setHardness(0.5F)
            .setStepSound(Block.soundTypeWood)
            .setBlockName("subWorldAligner")
            .setBlockTextureName("metaworlds:tile.subWorldAligner");
        subWorldController = new BlockSubWorldController().setHardness(0.5F)
            .setStepSound(Block.soundTypeStone)
            .setBlockName("subWorldController")
            .setBlockTextureName("metaworlds:tile.subWorldController");
        flipSubWorldBlock = new BlockFlipSubWorld().setHardness(0.5F)
            .setStepSound(Block.soundTypeWood)
            .setBlockName("flipSubWorldBlock")
            .setBlockTextureName("planks_oak");
        blankSubWorldCreator = new BlockBlankSubWorldCreator().setHardness(3.0F)
            .setResistance(15.0F)
            .setStepSound(Block.soundTypeWood)
            .setBlockName("blankSubWorldCreator")
            .setBlockTextureName("metaworlds:tile.blankSubWorldCreator");
        contagiousSubWorldCreator = new BlockContagiousSubWorldCreator().setHardness(3.0F)
            .setResistance(15.0F)
            .setStepSound(Block.soundTypeGravel)
            .setBlockName("contagiousSubWorldCreator")
            .setBlockTextureName("metaworlds:tile.contagiousSubWorldCreator");
        subWorldReintegrator = new BlockSubWorldReintegrator().setHardness(3.0F)
            .setResistance(15.0F)
            .setStepSound(Block.soundTypeWood)
            .setBlockName("subWorldReintegrator")
            .setBlockTextureName("planks_oak");
        dummyBlock = new BlockDummyReobfTracker();
        ((BlockDummyReobfTracker) dummyBlock).initialize();
        hingeJointBlock = new BlockHingeJoint().setHardness(0.5F)
            .setStepSound(Block.soundTypeWood)
            .setBlockName("subWorldHingeJoint")
            .setBlockTextureName("planks_oak");
        miniaturizerBlock = new BlockMiniaturizer().setHardness(0.5F)
            .setStepSound(Block.soundTypeWood)
            .setBlockName("subWorldMiniaturizer")
            .setBlockTextureName("metaworlds:tile.subWorldMiniaturizer");
        scaleNormalizerBlock = new BlockScaleNormalizer().setHardness(0.5F)
            .setStepSound(Block.soundTypeWood)
            .setBlockName("subWorldScaleNormalizer")
            .setBlockTextureName("metaworlds:tile.subWorldScaleNormalizer");
        supersizerBlock = new BlockSupersizer().setHardness(0.5F)
            .setStepSound(Block.soundTypeWood)
            .setBlockName("subWorldSupersizer")
            .setBlockTextureName("metaworlds:tile.subWorldSupersizer");

        GameRegistry.registerBlock(floatingWoodBlock, "floatingWoodBlock");
        GameRegistry.registerBlock(subWorldAligner, "subWorldAligner");
        GameRegistry.registerBlock(subWorldController, "subWorldController");
        GameRegistry.registerBlock(flipSubWorldBlock, "flipSubWorldBlock");
        GameRegistry.registerBlock(blankSubWorldCreator, "blankSubWorldCreator");
        GameRegistry.registerBlock(contagiousSubWorldCreator, "contagiousSubWorldCreator");
        GameRegistry.registerBlock(subWorldReintegrator, "subWorldReintegrator");
        GameRegistry.registerBlock(hingeJointBlock, "subWorldHingeJoint");
        GameRegistry.registerBlock(miniaturizerBlock, "subWorldMiniaturizer");
        GameRegistry.registerBlock(scaleNormalizerBlock, "subWorldScaleNormalizer");
        GameRegistry.registerBlock(supersizerBlock, "subWorldSupersizer");
    }

}
