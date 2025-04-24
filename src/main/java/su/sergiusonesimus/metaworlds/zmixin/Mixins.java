package su.sergiusonesimus.metaworlds.zmixin;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class Mixins implements IMixinConfigPlugin {

    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (targetClassName == "net.minecraft.client.renderer.RenderGlobal"
            && mixinClassName == "su.sergiusonesimus.metaworlds.zmixin.mixins.angelica.MixinRenderGlobal") {
            try {
                return Class.forName("me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer") != null;
            } catch (ClassNotFoundException e) {

            }
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        List<String> mixins = new ArrayList<>();
        try {
            if (Class.forName("com.gtnewhorizons.angelica.AngelicaMod") != null) {
                mixins.add("angelica.MixinRenderGlobal");
            }
        } catch (ClassNotFoundException e) {

        }
        return mixins;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

}
