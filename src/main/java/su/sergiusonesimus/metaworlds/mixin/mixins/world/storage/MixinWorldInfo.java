package su.sergiusonesimus.metaworlds.mixin.mixins.world.storage;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.primitives.Ints;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import su.sergiusonesimus.metaworlds.api.IMixinWorld;
import su.sergiusonesimus.metaworlds.api.IMixinWorldInfo;
import su.sergiusonesimus.metaworlds.api.IMixinWorldIntermediate;
import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.core.SubWorldServerFactory;
import su.sergiusonesimus.metaworlds.patcher.SubWorldInfoHolder;

@Mixin(WorldInfo.class)
public class MixinWorldInfo implements IMixinWorldInfo {
	
	private Map<Integer, Set<Integer>> subWorldIDsByDimension = new TreeMap<Integer, Set<Integer>>();
	private Map<Integer, SubWorldInfoHolder> subWorldInfoByID = new HashMap<Integer, SubWorldInfoHolder>();
	private int nextSubWorldID = 1;//For creating new subWorlds
	
    @Inject(method = "<init>(Lnet/minecraft/nbt/NBTTagCompound;)V", at = @At("TAIL"))
    public void WorldInfo(NBTTagCompound par1NBTTagCompound, CallbackInfo ci) {
    	if (par1NBTTagCompound.hasKey("NextSubWorldID"))
    		{
    		    this.nextSubWorldID = par1NBTTagCompound.getInteger("NextSubWorldID");
    		}
    		
    		if (par1NBTTagCompound.hasKey("SubWorldIDsByDimension"))
    		{
    		    NBTTagList subWorldIDslist = par1NBTTagCompound.getTagList("SubWorldIDsByDimension", 10);
    		    for (int j = 0; j < subWorldIDslist.tagCount(); ++j)
    		    {
    		        NBTTagCompound dimensionData = (NBTTagCompound)subWorldIDslist.getCompoundTagAt(j);
    		        int dimensionID = dimensionData.getInteger("DimID");
    		        
    		        int[] subWorldIDsArray = dimensionData.getIntArray("SubWorldIDs");
    		        this.subWorldIDsByDimension.put(dimensionID, new HashSet<Integer>(Ints.asList(subWorldIDsArray)));
    		    }
    		}
    		
    		if (par1NBTTagCompound.hasKey("SubWorldInfos"))
    		{
    		    NBTTagList subWorldslist = par1NBTTagCompound.getTagList("SubWorldInfos", 10);
    		    for (int j = 0; j < subWorldslist.tagCount(); ++j)
    		    {
    		        NBTTagCompound subWorldData = (NBTTagCompound)subWorldslist.getCompoundTagAt(j);
    		        
    		        SubWorldInfoHolder curInfoHolder = new SubWorldInfoHolder(subWorldData);
    		        
    		        subWorldInfoByID.put(curInfoHolder.subWorldId, curInfoHolder);
    		    }
    		}
    }
	
    @Inject(method = "updateTagCompound(Lnet/minecraft/nbt/NBTTagCompound;Lnet/minecraft/nbt/NBTTagCompound;)V", at = @At("TAIL"))
    public void updateTagCompound(NBTTagCompound par1NBTTagCompound, NBTTagCompound par2NBTTagCompound, CallbackInfo ci) {
    	//SubWorlds
    	par1NBTTagCompound.setInteger("NextSubWorldID", this.nextSubWorldID);
    	
    	NBTTagList subWorldIDslist = new NBTTagList();
    	for (WorldServer curDimensionWorld : DimensionManager.getWorlds())
    	{
    	    NBTTagCompound dimensionData = new NBTTagCompound();
    	    
    	    dimensionData.setInteger("DimID", ((IMixinWorldIntermediate) curDimensionWorld).getDimension());
    	    dimensionData.setIntArray("SubWorldIDs", ArrayUtils.toPrimitive(((IMixinWorld) curDimensionWorld).getSubWorldsMap().keySet().toArray(new Integer[0])));
    	    
    	    subWorldIDslist.appendTag(dimensionData);
    	    
    	    for (World curSubWorld : ((IMixinWorld) curDimensionWorld).getSubWorlds())
    	    {
    	        SubWorldInfoHolder curSubWorldInfo = new SubWorldInfoHolder((SubWorld)curSubWorld);
    	        this.subWorldInfoByID.put(((IMixinWorld) curSubWorld).getSubWorldID(), curSubWorldInfo);
    	    }
    	}
    	par1NBTTagCompound.setTag("SubWorldIDsByDimension", subWorldIDslist);
    	
    	
    	
    	NBTTagList subWorldslist = new NBTTagList(); 
    	{
    	    for (SubWorldInfoHolder curHolder : this.subWorldInfoByID.values())
    	    {
    	        NBTTagCompound subWorldData = new NBTTagCompound();
    	        
    	        curHolder.writeToNBT(subWorldData);
    	        
    	        subWorldslist.appendTag(subWorldData);
    	    }
    	}
    	par1NBTTagCompound.setTag("SubWorldInfos", subWorldslist);
    }
	
	//Returns next free ID for creating subWorlds
	public int getNextSubWorldID()
	{
	    return this.nextSubWorldID++;
	}
	
	public Collection<Integer> getSubWorldIDs(int dimId)
	{
	    return this.subWorldIDsByDimension.get(dimId);
	}
	
	public void updateSubWorldInfo(SubWorld subWorldToUpdate)
	{
	    this.subWorldInfoByID.put(subWorldToUpdate.getSubWorldID(), new SubWorldInfoHolder(subWorldToUpdate));
	}
	
	public void updateSubWorldInfo(SubWorldInfoHolder newInfoHolder)
	{
	    this.subWorldInfoByID.put(newInfoHolder.subWorldId, newInfoHolder);
	}
	
	public SubWorldInfoHolder getSubWorldInfo(int subWorldId)
	{
	    return this.subWorldInfoByID.get(subWorldId);
	}
	
	public Collection<SubWorldInfoHolder> getSubWorldInfos()
	{
	    return this.subWorldInfoByID.values();
	}

}
