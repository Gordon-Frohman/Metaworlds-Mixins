package su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.client.renderer;

import net.minecraft.client.renderer.DestroyBlockProgress;

public interface IMixinDestroyBlockProgress {

    public DestroyBlockProgress setPartialBlockSubWorldId(int partialBlockSubWorldId);

    public int getPartialBlockSubWorldID();

}
