package su.sergiusonesimus.metaworlds.zmixin.mixins.fixes;

import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import cpw.mods.fml.common.FMLLog;
import su.sergiusonesimus.metaworlds.MetaworldsMod;
import su.sergiusonesimus.metaworlds.zmixin.Mixins;

@Mixin(value = FMLLog.class, priority = Mixins.fmlPatchPriority)
public abstract class MixinFMLLog {

    @Shadow(remap = false)
    public static void log(Level level, String format, Object... data) {}

    @Overwrite(remap = false)
    public static void severe(String format, Object... data) {
        log(Level.ERROR, format, data);
        if (format.startsWith("Detected ongoing potential memory lea")) {
            FMLLog.severe(
                "Let me interject for a moment. " + MetaworldsMod.CHANNEL2
                    + " isn't actually causing any memory leaks, but forge is unfortunately complaining due to the way some packets are handled. You can safely ignore this.",
                new Object[] { 0 });
        }
    }

}
