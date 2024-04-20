package net.tclproject.metaworlds.mixin.mixins.world.storage;

import java.util.Collection;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.storage.WorldInfo;
import net.tclproject.metaworlds.api.SubWorld;
import net.tclproject.metaworlds.api.IMixinWorldInfo;
import net.tclproject.metaworlds.patcher.SubWorldInfoHolder;

@Mixin(WorldInfo.class)
public class MixinWorldInfo implements IMixinWorldInfo {

	@Override
	public int getNextSubWorldID() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Collection<Integer> getSubWorldIDs(int dimId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateSubWorldInfo(SubWorld subWorldToUpdate) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateSubWorldInfo(SubWorldInfoHolder newInfoHolder) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public SubWorldInfoHolder getSubWorldInfo(int subWorldId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<SubWorldInfoHolder> getSubWorldInfos() {
		// TODO Auto-generated method stub
		return null;
	}

}
