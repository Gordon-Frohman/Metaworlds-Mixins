package net.tclproject.metaworlds.mixin.interfaces.client.particles;

import net.minecraft.world.World;

public interface IMixinEffectRenderer {
	
	public void addBlockHitEffects(int par1, int par2, int par3, int par4, World parWorldObj);

}
