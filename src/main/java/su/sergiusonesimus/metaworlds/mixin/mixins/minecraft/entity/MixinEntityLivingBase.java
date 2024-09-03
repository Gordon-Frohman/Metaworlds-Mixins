package su.sergiusonesimus.metaworlds.mixin.mixins.minecraft.entity;

import net.minecraft.entity.EntityLivingBase;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import su.sergiusonesimus.metaworlds.api.IMixinEntityLivingBase;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends MixinEntity implements IMixinEntityLivingBase {

    @Shadow(remap = true)
    public boolean isOnLadder() {
        return false;
    }

}
