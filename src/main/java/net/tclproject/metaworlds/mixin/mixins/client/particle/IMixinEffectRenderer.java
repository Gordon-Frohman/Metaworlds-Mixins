package net.tclproject.metaworlds.mixin.mixins.client.particle;

import net.minecraft.world.World;

public interface IMixinEffectRenderer {
	
	public void addBlockHitEffects(int par1, int par2, int par3, int par4, World parWorldObj);

}
