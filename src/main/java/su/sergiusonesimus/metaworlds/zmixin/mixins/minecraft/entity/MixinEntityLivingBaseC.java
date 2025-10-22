package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.entity;

import net.minecraft.entity.EntityLivingBase;

import org.spongepowered.asm.mixin.Mixin;

import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.IMixinEntityLivingBaseC;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBaseC extends MixinEntityC implements IMixinEntityLivingBaseC {

}
