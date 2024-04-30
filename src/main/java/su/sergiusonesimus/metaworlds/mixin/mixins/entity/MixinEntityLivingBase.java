package su.sergiusonesimus.metaworlds.mixin.mixins.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.entity.EntityLivingBase;
import su.sergiusonesimus.metaworlds.api.IMixinEntityLivingBase;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends MixinEntity implements IMixinEntityLivingBase {
	
	@Shadow(remap = true)
	public boolean isOnLadder() { return false; }

}
