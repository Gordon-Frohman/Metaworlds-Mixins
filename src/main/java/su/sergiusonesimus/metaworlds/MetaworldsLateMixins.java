package su.sergiusonesimus.metaworlds;

import java.util.List;
import java.util.Set;

import com.gtnewhorizon.gtnhmixins.ILateMixinLoader;
import com.gtnewhorizon.gtnhmixins.LateMixin;

import su.sergiusonesimus.metaworlds.zmixin.Mixins;

@LateMixin
public class MetaworldsLateMixins implements ILateMixinLoader {

    @Override
    public String getMixinConfig() {
        return "mixins.metaworlds.late.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedMods) {
        return Mixins.getLateMixins(loadedMods);
    }
}
