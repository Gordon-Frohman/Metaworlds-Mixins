package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.client.renderer;

import net.minecraft.client.renderer.DestroyBlockProgress;

import org.spongepowered.asm.mixin.Mixin;

import su.sergiusonesimus.metaworlds.zmixin.interfaces.client.renderer.IMixinDestroyBlockProgress;

@Mixin(DestroyBlockProgress.class)
public class MixinDestroyBlockProgress implements IMixinDestroyBlockProgress {

    private int partialBlockSubWorldId;

    public DestroyBlockProgress setPartialBlockSubWorldId(int partialBlockSubWorldId) {
        this.partialBlockSubWorldId = partialBlockSubWorldId;
        return (DestroyBlockProgress) (Object) this;
    }

    public int getPartialBlockSubWorldID() {
        return this.partialBlockSubWorldId;
    }

}
