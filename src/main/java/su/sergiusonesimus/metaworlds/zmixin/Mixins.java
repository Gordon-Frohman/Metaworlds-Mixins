package su.sergiusonesimus.metaworlds.zmixin;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class Mixins implements IMixinConfigPlugin {

    public static final int angelicaPatchPriority = 2001;
    public static final int fmlPatchPriority = 1000;

    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        List<String> mixins = new ArrayList<String>();
        if (isClassLoaded("com.gtnewhorizons.angelica.AngelicaMod")) {
            mixins.add("angelica.MixinRenderGlobal");
            mixins.add("angelica.MixinEffectRenderer");
        }
        if (isClassLoaded("chylex.hee.HardcoreEnderExpansion")) {
            mixins.add("hee.MixinPlayerDataHandler");
        }
        if (isClassLoaded("codechicken.core.launch.CodeChickenCorePlugin")) {
            mixins.add("codechickenlib.MixinPacketCustom");
            mixins.add("codechickenlib.MixinRayTracer");
            mixins.add("codechickenlib.MixinVector3");
        }
        if (isClassLoaded("codechicken.multipart.minecraft.MinecraftMultipartMod")) {
            mixins.add("forgemultipart.MixinPlacementGrid");
            mixins.add("forgemultipart.MixinMicroblockRender");
            mixins.add("forgemultipart.MixinMicroblockPlacement");
            mixins.add("forgemultipart.MixinTileMultipart");
            mixins.add("forgemultipart.MixinMultipartSPH");
            mixins.add("forgemultipart.MixinMultipartCPH");
            mixins.add("forgemultipart.MixinMultipartSPH$$anonfun$onTickEnd");
            mixins.add("forgemultipart.MixinMultipartSPH$$anonfun$onTickEnd$2");
            mixins.add("forgemultipart.MixinMultipartSPH$$anonfun$onTickEnd$5");
            mixins.add("forgemultipart.MixinIconHitEffects");
        }
        if (isClassLoaded("com.creativemd.littletiles.LTTags")) {
            // Looking for tags class, since looking for LittleTiles class causes error for some reason
            mixins.add("littletiles.MixinLittleTileBlockPos");
            mixins.add("littletiles.MixinPreviewRenderer");
            mixins.add("littletiles.MixinItemBlockTiles");
            mixins.add("littletiles.MixinLittlePlacePacket");
            mixins.add("littletiles.MixinBlockTile");
            mixins.add("littletiles.MixinLittleBlockPacket");
            mixins.add("littletiles.MixinPlacementHelper");
        }
        return mixins;
    }

    private boolean isClassLoaded(String className) {
        try {
            if (Class.forName(className) != null) {
                return true;
            }
        } catch (ClassNotFoundException e) {}
        return false;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

}
