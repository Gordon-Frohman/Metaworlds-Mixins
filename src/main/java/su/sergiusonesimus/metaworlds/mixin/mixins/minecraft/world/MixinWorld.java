package su.sergiusonesimus.metaworlds.mixin.mixins.minecraft.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.GameRules;
import net.minecraft.world.IWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import su.sergiusonesimus.metaworlds.api.IMixinWorld;
import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.patcher.UnmodifiableSingleObjPlusCollection;

import org.jblas.DoubleMatrix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mixin(World.class)
public abstract class MixinWorld implements IMixinWorld {

    private final boolean isSubWorld = this instanceof SubWorld;
    public Map<Integer, World> childSubWorlds;
    private UnmodifiableSingleObjPlusCollection<World> allWorlds;

    @Shadow(remap = true)
    public static double MAX_ENTITY_RADIUS;

    @Shadow(remap = true)
    public ArrayList collidingBoundingBoxes;

    @Shadow(remap = true)
    public boolean restoringBlockSnapshots;

    @Shadow(remap = true)
    public List playerEntities;

    @Shadow(remap = true)
    public List loadedEntityList;

    @Shadow(remap = true)
    public WorldInfo worldInfo;

    @Shadow(remap = true)
    protected List worldAccesses;
    
    //TODO

    @Shadow(remap = true)
    public abstract GameRules getGameRules();

    @SideOnly(Side.CLIENT)
    @Shadow(remap = true)
    public abstract void func_82738_a(long p_82738_1_);

    @Shadow(remap = true)
    public abstract void setWorldTime(long time);

    @Shadow(remap = true)
    public abstract long getTotalWorldTime();

    @Shadow(remap = true)
    public MovingObjectPosition func_147447_a(Vec3 par1Vec3, Vec3 par2Vec3, boolean par3, boolean par4, boolean par5) { return null; }

    @Shadow(remap = true)
    public void removePlayerEntityDangerously(Entity par1Entity) {}

    @Shadow(remap = true)
    public void removeEntity(Entity p_72900_1_) {}

    @Shadow(remap = true)
    public boolean spawnEntityInWorld(Entity p_72838_1_) { return false; }

    @Shadow(remap = true)
    public List selectEntitiesWithinAABB(Class par1Class, AxisAlignedBB par2AxisAlignedBB, IEntitySelector par3IEntitySelector) { return null; }

    @Shadow(remap = true)
    public List getEntitiesWithinAABBExcludingEntity(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB, IEntitySelector par3IEntitySelector) { return null; }

    @Shadow(remap = true)
    public boolean isMaterialInBB(AxisAlignedBB par1AxisAlignedBB, Material par2Material) { return false; }

    @Shadow(remap = true)
    public List getCollidingBoundingBoxes(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB) { return null; }

    @Shadow(remap = true)
    public abstract void onEntityRemoved(Entity p_72847_1_);

    @Shadow(remap = true)
    public abstract void onEntityAdded(Entity p_72923_1_);

    @Shadow(remap = true)
    public abstract void updateAllPlayersSleepingFlag();

    @Shadow(remap = true)
    public abstract Chunk getChunkFromChunkCoords(int p_72964_1_, int p_72964_2_);

    @Shadow(remap = true)
    public abstract List getEntitiesWithinAABBExcludingEntity(Entity p_72839_1_, AxisAlignedBB p_72839_2_);

    @Shadow(remap = true)
    public abstract boolean blockExists(int p_72899_1_, int p_72899_2_, int p_72899_3_);

    @Shadow(remap = true)
    public abstract int getBlockMetadata(int p_72805_1_, int p_72805_2_, int p_72805_3_);

    @Shadow(remap = true)
    public abstract Block getBlock(final int p_150810_1_, final int p_150810_2_, final int p_150810_3_);

    @Shadow(remap = true)
    public abstract boolean chunkExists(int p_72916_1_, int p_72916_2_);
	
	public abstract World CreateSubWorld();
	public abstract World CreateSubWorld(int newSubWorldID);

    public Collection<World> getWorlds() {
    	if(this.allWorlds == null)
    		this.allWorlds = new UnmodifiableSingleObjPlusCollection<World>((World) (Object) this, this.getSubWorlds());
        return this.allWorlds;
    }

    public Collection<World> getSubWorlds() {
    	if(this.childSubWorlds == null)
    		childSubWorlds = new TreeMap<Integer, World>();
    	return this.childSubWorlds.values();
    }

    public Map<Integer, World> getSubWorldsMap() {
    	if(this.childSubWorlds == null)
    		childSubWorlds = new TreeMap<Integer, World>();
        return this.childSubWorlds;
    }

    public int getWorldsCount() {
    	if(this.childSubWorlds == null) {
    		childSubWorlds = new TreeMap<Integer, World>();
    		return 0;
    	}
        return this.childSubWorlds.size() + 1;
    }

    public int getSubWorldID() {
        return 0;
    }

    public World getParentWorld() {
        return (World) (Object) this;
    }

    public World getSubWorld(int targetSubWorldID) {
        return targetSubWorldID < 0 ? null
            : (targetSubWorldID == 0 ? (World) (Object) this
                : (World) this.childSubWorlds.get(Integer.valueOf(targetSubWorldID)));
    }

    public boolean isSubWorld() {
        return this.isSubWorld;
    }

    public double getTranslationX() {
        return 0.0D;
    }

    public double getTranslationY() {
        return 0.0D;
    }

    public double getTranslationZ() {
        return 0.0D;
    }

    public double getRotationYaw() {
        return 0.0D;
    }

    public double getRotationPitch() {
        return 0.0D;
    }

    public double getRotationRoll() {
        return 0.0D;
    }

    public double getScaling() {
        return 1.0D;
    }

    public double getCenterX() {
        return 0.0D;
    }

    public double getCenterY() {
        return 0.0D;
    }

    public double getCenterZ() {
        return 0.0D;
    }

    public Vec3 transformToGlobal(Entity localEntity) {
        return this.transformToGlobal(localEntity.posX, localEntity.posY, localEntity.posZ);
    }

    public Vec3 transformToGlobal(Vec3 localVec) {
        return this.transformToGlobal(localVec.xCoord, localVec.yCoord, localVec.zCoord);
    }

    public Vec3 transformToGlobal(double localX, double localY, double localZ) {
        return Vec3.createVectorHelper(localX, localY, localZ);
    }

    public DoubleMatrix transformToGlobal(DoubleMatrix localVectors) {
        return localVectors.dup();
    }

    public DoubleMatrix transformToGlobal(DoubleMatrix localVectors, DoubleMatrix result) {
        result.copy(localVectors);
        return result;
    }

    public Vec3 transformToLocal(Entity globalEntity) {
        return this.transformToLocal(globalEntity.posX, globalEntity.posY, globalEntity.posZ);
    }

    public Vec3 transformToLocal(Vec3 globalVec) {
        return this.transformToLocal(globalVec.xCoord, globalVec.yCoord, globalVec.zCoord);
    }

    public Vec3 transformToLocal(double globalX, double globalY, double globalZ) {
        return Vec3.createVectorHelper(globalX, globalY, globalZ);
    }

    public DoubleMatrix transformToLocal(DoubleMatrix globalVectors) {
        return globalVectors.dup();
    }

    public DoubleMatrix transformToLocal(DoubleMatrix globalVectors, DoubleMatrix result) {
        result.copy(globalVectors);
        return result;
    }

    public Vec3 transformLocalToOther(World targetWorld, Entity localEntity) {
        return this.transformLocalToOther(targetWorld, localEntity.posX, localEntity.posY, localEntity.posZ);
    }

    public Vec3 transformLocalToOther(World targetWorld, Vec3 localVec) {
        return targetWorld == null ? this.transformToLocal(localVec)
            : ((IMixinWorld) targetWorld).transformToLocal(localVec);
    }

    public Vec3 transformLocalToOther(World targetWorld, double localX, double localY, double localZ) {
        return targetWorld == null ? Vec3.createVectorHelper(localX, localY, localZ)
            : ((IMixinWorld) targetWorld).transformToLocal(localX, localY, localZ);
    }

    public DoubleMatrix transformLocalToOther(World targetWorld, DoubleMatrix localVectors) {
        return targetWorld == null ? localVectors.dup() : ((IMixinWorld) targetWorld).transformToLocal(localVectors);
    }

    public DoubleMatrix transformLocalToOther(World targetWorld, DoubleMatrix localVectors, DoubleMatrix result) {
        return targetWorld == null ? result.copy(localVectors)
            : ((IMixinWorld) targetWorld).transformToLocal(localVectors, result);
    }

    public Vec3 transformOtherToLocal(World sourceWorld, Entity otherEntity) {
        return this.transformOtherToLocal(sourceWorld, otherEntity.posX, otherEntity.posY, otherEntity.posZ);
    }

    public Vec3 transformOtherToLocal(World sourceWorld, Vec3 otherVec) {
        return sourceWorld == null ? this.transformToGlobal(otherVec)
            : ((IMixinWorld) sourceWorld).transformToGlobal(otherVec);
    }

    public Vec3 transformOtherToLocal(World sourceWorld, double otherX, double otherY, double otherZ) {
        return sourceWorld == null ? Vec3.createVectorHelper(otherX, otherY, otherZ)
            : ((IMixinWorld) sourceWorld).transformToGlobal(otherX, otherY, otherZ);
    }

    public DoubleMatrix transformOtherToLocal(World sourceWorld, DoubleMatrix otherVectors) {
        return sourceWorld == null ? otherVectors.dup() : ((IMixinWorld) sourceWorld).transformToGlobal(otherVectors);
    }

    public DoubleMatrix transformOtherToLocal(World sourceWorld, DoubleMatrix otherVectors, DoubleMatrix result) {
        return sourceWorld == null ? result.copy(otherVectors)
            : ((IMixinWorld) sourceWorld).transformToGlobal(otherVectors, result);
    }

    public Vec3 rotateToGlobal(Vec3 localVec) {
        return localVec;
    }

    public Vec3 rotateToGlobal(double localX, double localY, double localZ) {
        return Vec3.createVectorHelper(localX, localY, localZ);
    }

    public DoubleMatrix rotateToGlobal(DoubleMatrix localVectors) {
        return localVectors;
    }

    public Vec3 rotateToLocal(Vec3 globalVec) {
        return globalVec;
    }

    public Vec3 rotateToLocal(double globalX, double globalY, double globalZ) {
        return Vec3.createVectorHelper(globalX, globalY, globalZ);
    }

    public DoubleMatrix rotateToLocal(DoubleMatrix globalVectors) {
        return globalVectors;
    }

    public List getCollidingBoundingBoxesGlobal(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB) {
        return this.getCollidingBoundingBoxes(par1Entity, par2AxisAlignedBB);
    }

    public List getEntitiesWithinAABBExcludingEntityLocal(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB) {
    	return this.getEntitiesWithinAABBExcludingEntityLocal(par1Entity, par2AxisAlignedBB, (IEntitySelector)null);
    }

    public abstract List getEntitiesWithinAABBExcludingEntityLocal(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB, IEntitySelector par3IEntitySelector);

    public void doTickPartial(double interpolationFactor) {}

    public boolean isChunkWatchable(int chunkX, int chunkZ) {
        return true;
    }

    public Chunk createNewChunk(int xPos, int zPos) {
        return new Chunk((World) (Object) this, xPos, zPos);
    }
    
    @Inject(method = "<init>", at = @At("TAIL"))
    public void World(ISaveHandler p_i45368_1_, String p_i45368_2_, WorldProvider p_i45368_3_, WorldSettings p_i45368_4_, Profiler p_i45368_5_, CallbackInfo ci) {
        childSubWorlds = new TreeMap<Integer, World>();
        allWorlds = new UnmodifiableSingleObjPlusCollection<World>((World) (Object) this, this.childSubWorlds.values());
    }

    /**
     * Sets the light value either into the sky map or block map depending on if enumSkyBlock is set to sky or block.
     * Args: enumSkyBlock, x, y, z, lightValue
     */
    /*@Overwrite
    public void setLightValue(EnumSkyBlock p_72915_1_, int p_72915_2_, int p_72915_3_, int p_72915_4_, int p_72915_5_)
    {
    	Collection<World> worldsList = ((IMixinWorld)this).isSubWorld()? ((IMixinWorld)((IMixinWorld)this).getParentWorld()).getSubWorlds() : ((IMixinWorld)this).getWorlds();
    	
    	for(World world : worldsList) {
    		Vec3 localCoords = ((IMixinWorld)world).transformToLocal(p_72915_2_, p_72915_3_, p_72915_4_);
	        if (localCoords.xCoord >= -30000000 && localCoords.zCoord >= -30000000 && localCoords.xCoord < 30000000 && localCoords.zCoord < 30000000)
	        {
	            if (localCoords.yCoord >= 0)
	            {
	                if (localCoords.yCoord < 256)
	                {
	                    if (world.chunkExists((int) localCoords.xCoord >> 4, (int) localCoords.zCoord >> 4))
	                    {
	                        Chunk chunk = world.getChunkFromChunkCoords((int) localCoords.xCoord >> 4, (int) localCoords.zCoord >> 4);
	                        chunk.setLightValue(p_72915_1_, (int) localCoords.xCoord & 15, (int) localCoords.yCoord, (int) localCoords.zCoord & 15, p_72915_5_);
	
	                        for (int i1 = 0; i1 < world.worldAccesses.size(); ++i1)
	                        {
	                            ((IWorldAccess)world.worldAccesses.get(i1)).markBlockForRenderUpdate(p_72915_2_, p_72915_3_, p_72915_4_);
	                        }
	                    }
	                }
	            }
	        }
    	}
    }*/
}
