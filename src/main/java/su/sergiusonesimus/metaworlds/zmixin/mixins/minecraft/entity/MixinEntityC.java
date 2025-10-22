package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.entity;

import net.minecraft.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Entity.class)
public class MixinEntityC {

    @Shadow(remap = true)
    public void setAngles(float par1, float par2) {}

}
