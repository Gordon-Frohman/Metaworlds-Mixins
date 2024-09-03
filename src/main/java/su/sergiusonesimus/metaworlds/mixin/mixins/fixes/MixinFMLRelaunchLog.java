package su.sergiusonesimus.metaworlds.mixin.mixins.fixes;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.FMLRelaunchLog;
import su.sergiusonesimus.metaworlds.core.MetaworldsMod;

@Mixin(FMLRelaunchLog.class)
public abstract class MixinFMLRelaunchLog {

    @Overwrite(remap = false)
    public static void log(String targetLog, Level level, String format, Object... data) {
        LogManager.getLogger(targetLog)
            .log(level, String.format(format, data));
        if (format.startsWith("Detected ongoing potential memory lea")) {
            FMLLog.severe(
                "Let me interject for a moment. " + MetaworldsMod.CHANNEL2
                    + " isn't actually causing any memory leaks, but forge is unfortunately complaining due to the way some packets are handled. You can safely ignore this.",
                new Object[] { 0 });
        }
    }

}
