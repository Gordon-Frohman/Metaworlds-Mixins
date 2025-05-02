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
        try {
            Class.forName("ganymedes01.etfuturum.EtFuturum");
        } catch (ClassNotFoundException e) {
            mixins.add("fixes.MixinFMLLog");
            mixins.add("fixes.MixinFMLRelaunchLog");
        }
        try {
            if (Class.forName("com.gtnewhorizons.angelica.AngelicaMod") != null) {
                mixins.add("angelica.MixinRenderGlobal");
                mixins.add("angelica.MixinEffectRenderer");
            }
        } catch (ClassNotFoundException e) {}
        return mixins;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

}
