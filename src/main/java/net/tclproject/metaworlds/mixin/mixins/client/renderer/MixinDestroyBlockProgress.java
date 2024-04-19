package net.tclproject.metaworlds.mixin.mixins.client.renderer;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.renderer.DestroyBlockProgress;

@Mixin(DestroyBlockProgress.class)
public class MixinDestroyBlockProgress implements IMixinDestroyBlockProgress {
	
    private int partialBlockSubWorldId;
    
    public DestroyBlockProgress setPartialBlockSubWorldId(int partialBlockSubWorldId) {
    	this.partialBlockSubWorldId = partialBlockSubWorldId;
    	return (DestroyBlockProgress)(Object)this;
    }

    public int getPartialBlockSubWorldID() {
        return this.partialBlockSubWorldId;
    }

}
