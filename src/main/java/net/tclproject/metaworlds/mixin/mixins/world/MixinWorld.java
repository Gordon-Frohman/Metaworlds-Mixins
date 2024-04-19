package net.tclproject.metaworlds.mixin.mixins.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.tclproject.metaworlds.api.IMixinEntity;
import net.tclproject.metaworlds.api.IMixinWorld;
import net.tclproject.metaworlds.api.PlayerManagerSuperClass;
import net.tclproject.metaworlds.api.SubWorld;
import net.tclproject.metaworlds.mixin.interfaces.util.IMixinAxisAlignedBB;
import net.tclproject.metaworlds.mixin.interfaces.util.IMixinMovingObjectPosition;
import net.tclproject.metaworlds.patcher.EntityPlayerMPSubWorldProxy;
import net.tclproject.metaworlds.patcher.UnmodifiableSingleObjPlusCollection;
import org.jblas.DoubleMatrix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(World.class)
public abstract class MixinWorld implements IMixinWorld {

    private ArrayList collidingBBCacheIntermediate = new ArrayList();

    private final boolean isSubWorld = this instanceof SubWorld;
    public Map<Integer, World> childSubWorlds = new TreeMap();
    private UnmodifiableSingleObjPlusCollection<World> allWorlds = new UnmodifiableSingleObjPlusCollection(
        (World) (Object) this,
        this.childSubWorlds.values());

    @Shadow(remap = true)
    public static double MAX_ENTITY_RADIUS;

    @Shadow(remap = true)
    public boolean isRemote;

    @Shadow(remap = true)
    public ArrayList collidingBoundingBoxes;

    @Shadow(remap = true)
    public boolean restoringBlockSnapshots;

    @Shadow(remap = true)
    public List playerEntities;

    @Shadow(remap = true)
    public List loadedEntityList;
    
    //TODO

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

    public abstract World CreateSubWorld(int var1);

    public Collection<World> getWorlds() {
        return this.allWorlds;
    }

    public Collection<World> getSubWorlds() {
        return this.childSubWorlds.values();
    }

    public Map<Integer, World> getSubWorldsMap() {
        return this.childSubWorlds;
    }

    public int getWorldsCount() {
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
        return ((IMixinWorld)this).getCollidingBoundingBoxesLocal(par1Entity, par2AxisAlignedBB);
    }

    public void doTickPartial(double interpolationFactor) {}

    public boolean isChunkWatchable(int chunkX, int chunkZ) {
        return true;
    }

    public Chunk createNewChunk(int xPos, int zPos) {
        return new Chunk((World) (Object) this, xPos, zPos);
    }

    @Overwrite
    public MovingObjectPosition func_147447_a(Vec3 par1Vec3, Vec3 par2Vec3, boolean par3, boolean par4, boolean par5) {
        MovingObjectPosition bestResult = null;
        Vec3 vecSource = ((IMixinWorld) this).transformToGlobal(par1Vec3);
        Vec3 vecDest = ((IMixinWorld) this).transformToGlobal(par2Vec3);
        Iterator i$ = ((IMixinWorld) ((IMixinWorld) this).getParentWorld()).getWorlds()
            .iterator();

        while (i$.hasNext()) {
            World curWorld = (World) i$.next();
            MovingObjectPosition curResult = ((IMixinWorld) curWorld).rayTraceBlocks_do_do_single(
                ((IMixinWorld) curWorld).transformToLocal(vecSource),
                ((IMixinWorld) curWorld).transformToLocal(vecDest),
                par3,
                par4,
                par5);
            if (curResult != null) {
                // This field is actually inserted on runtime
                ((IMixinMovingObjectPosition)curResult).setWorld(curWorld);
                curResult.hitVec = ((IMixinWorld) curWorld).transformLocalToOther((World)(Object)this, curResult.hitVec);
            }

            if (bestResult == null || bestResult.typeOfHit == MovingObjectPosition.MovingObjectType.MISS
                || curResult != null && curResult.typeOfHit != MovingObjectPosition.MovingObjectType.MISS
                    && bestResult.hitVec.squareDistanceTo(par1Vec3) > curResult.hitVec.squareDistanceTo(par1Vec3)) {
                bestResult = curResult;
            }
        }

        return bestResult;
    }

    // Old func_147447_a function
	public MovingObjectPosition rayTraceBlocks_do_do_single(Vec3 transformToLocal, Vec3 transformToLocal2, boolean par3,
			boolean par4, boolean par5) {
        if (!Double.isNaN(transformToLocal.xCoord) && !Double.isNaN(transformToLocal.yCoord) && !Double.isNaN(transformToLocal.zCoord))
        {
            if (!Double.isNaN(transformToLocal2.xCoord) && !Double.isNaN(transformToLocal2.yCoord) && !Double.isNaN(transformToLocal2.zCoord))
            {
                int i = MathHelper.floor_double(transformToLocal2.xCoord);
                int j = MathHelper.floor_double(transformToLocal2.yCoord);
                int k = MathHelper.floor_double(transformToLocal2.zCoord);
                int l = MathHelper.floor_double(transformToLocal.xCoord);
                int i1 = MathHelper.floor_double(transformToLocal.yCoord);
                int j1 = MathHelper.floor_double(transformToLocal.zCoord);
                Block block = this.getBlock(l, i1, j1);
                int k1 = this.getBlockMetadata(l, i1, j1);

                if ((!par4 || block.getCollisionBoundingBoxFromPool(((World)(Object)this), l, i1, j1) != null) && block.canCollideCheck(k1, par3))
                {
                    MovingObjectPosition movingobjectposition = block.collisionRayTrace(((World)(Object)this), l, i1, j1, transformToLocal, transformToLocal2);

                    if (movingobjectposition != null)
                    {
                        return movingobjectposition;
                    }
                }

                MovingObjectPosition movingobjectposition2 = null;
                k1 = 200;

                while (k1-- >= 0)
                {
                    if (Double.isNaN(transformToLocal.xCoord) || Double.isNaN(transformToLocal.yCoord) || Double.isNaN(transformToLocal.zCoord))
                    {
                        return null;
                    }

                    if (l == i && i1 == j && j1 == k)
                    {
                        return par5 ? movingobjectposition2 : null;
                    }

                    boolean flag6 = true;
                    boolean flag3 = true;
                    boolean flag4 = true;
                    double d0 = 999.0D;
                    double d1 = 999.0D;
                    double d2 = 999.0D;

                    if (i > l)
                    {
                        d0 = (double)l + 1.0D;
                    }
                    else if (i < l)
                    {
                        d0 = (double)l + 0.0D;
                    }
                    else
                    {
                        flag6 = false;
                    }

                    if (j > i1)
                    {
                        d1 = (double)i1 + 1.0D;
                    }
                    else if (j < i1)
                    {
                        d1 = (double)i1 + 0.0D;
                    }
                    else
                    {
                        flag3 = false;
                    }

                    if (k > j1)
                    {
                        d2 = (double)j1 + 1.0D;
                    }
                    else if (k < j1)
                    {
                        d2 = (double)j1 + 0.0D;
                    }
                    else
                    {
                        flag4 = false;
                    }

                    double d3 = 999.0D;
                    double d4 = 999.0D;
                    double d5 = 999.0D;
                    double d6 = transformToLocal2.xCoord - transformToLocal.xCoord;
                    double d7 = transformToLocal2.yCoord - transformToLocal.yCoord;
                    double d8 = transformToLocal2.zCoord - transformToLocal.zCoord;

                    if (flag6)
                    {
                        d3 = (d0 - transformToLocal.xCoord) / d6;
                    }

                    if (flag3)
                    {
                        d4 = (d1 - transformToLocal.yCoord) / d7;
                    }

                    if (flag4)
                    {
                        d5 = (d2 - transformToLocal.zCoord) / d8;
                    }

                    boolean flag5 = false;
                    byte b0;

                    if (d3 < d4 && d3 < d5)
                    {
                        if (i > l)
                        {
                            b0 = 4;
                        }
                        else
                        {
                            b0 = 5;
                        }

                        transformToLocal.xCoord = d0;
                        transformToLocal.yCoord += d7 * d3;
                        transformToLocal.zCoord += d8 * d3;
                    }
                    else if (d4 < d5)
                    {
                        if (j > i1)
                        {
                            b0 = 0;
                        }
                        else
                        {
                            b0 = 1;
                        }

                        transformToLocal.xCoord += d6 * d4;
                        transformToLocal.yCoord = d1;
                        transformToLocal.zCoord += d8 * d4;
                    }
                    else
                    {
                        if (k > j1)
                        {
                            b0 = 2;
                        }
                        else
                        {
                            b0 = 3;
                        }

                        transformToLocal.xCoord += d6 * d5;
                        transformToLocal.yCoord += d7 * d5;
                        transformToLocal.zCoord = d2;
                    }

                    Vec3 vec32 = Vec3.createVectorHelper(transformToLocal.xCoord, transformToLocal.yCoord, transformToLocal.zCoord);
                    l = (int)(vec32.xCoord = (double)MathHelper.floor_double(transformToLocal.xCoord));

                    if (b0 == 5)
                    {
                        --l;
                        ++vec32.xCoord;
                    }

                    i1 = (int)(vec32.yCoord = (double)MathHelper.floor_double(transformToLocal.yCoord));

                    if (b0 == 1)
                    {
                        --i1;
                        ++vec32.yCoord;
                    }

                    j1 = (int)(vec32.zCoord = (double)MathHelper.floor_double(transformToLocal.zCoord));

                    if (b0 == 3)
                    {
                        --j1;
                        ++vec32.zCoord;
                    }

                    Block block1 = this.getBlock(l, i1, j1);
                    int l1 = this.getBlockMetadata(l, i1, j1);

                    if (!par4 || block1.getCollisionBoundingBoxFromPool(((World)(Object)this), l, i1, j1) != null)
                    {
                        if (block1.canCollideCheck(l1, par3))
                        {
                            MovingObjectPosition movingobjectposition1 = block1.collisionRayTrace(((World)(Object)this), l, i1, j1, transformToLocal, transformToLocal2);

                            if (movingobjectposition1 != null)
                            {
                                return movingobjectposition1;
                            }
                        }
                        else
                        {
                            movingobjectposition2 = new MovingObjectPosition(l, i1, j1, b0, transformToLocal, false);
                        }
                    }
                }

                return par5 ? movingobjectposition2 : null;
            }
            else
            {
                return null;
            }
        }
        else
        {
            return null;
        }
	}

	@Overwrite
    public List getCollidingBoundingBoxes(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB) {
        this.collidingBBCacheIntermediate.clear();
        this.collidingBBCacheIntermediate = (ArrayList) this
            .getCollidingBoundingBoxesLocal(par1Entity, par2AxisAlignedBB);
        Iterator i$ = ((IMixinWorld) this).getSubWorlds()
            .iterator();

        while (i$.hasNext()) {
            World curSubWorld = (World) i$.next();
            this.collidingBBCacheIntermediate
                .addAll(((SubWorld) curSubWorld).getCollidingBoundingBoxesGlobal(par1Entity, par2AxisAlignedBB));
        }

        return this.collidingBBCacheIntermediate;
    }

	// Old getCollidingBoundingBoxes function
    public List getCollidingBoundingBoxesLocal(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB) {
    	this.collidingBoundingBoxes.clear();
        int i = MathHelper.floor_double(par2AxisAlignedBB.minX);
        int j = MathHelper.floor_double(par2AxisAlignedBB.maxX + 1.0D);
        int k = MathHelper.floor_double(par2AxisAlignedBB.minY);
        int l = MathHelper.floor_double(par2AxisAlignedBB.maxY + 1.0D);
        int i1 = MathHelper.floor_double(par2AxisAlignedBB.minZ);
        int j1 = MathHelper.floor_double(par2AxisAlignedBB.maxZ + 1.0D);

        for (int k1 = i; k1 < j; ++k1)
        {
            for (int l1 = i1; l1 < j1; ++l1)
            {
                if (this.blockExists(k1, 64, l1))
                {
                    for (int i2 = k - 1; i2 < l; ++i2)
                    {
                        Block block;

                        if (k1 >= -30000000 && k1 < 30000000 && l1 >= -30000000 && l1 < 30000000)
                        {
                            block = this.getBlock(k1, i2, l1);
                        }
                        else
                        {
                            block = Blocks.stone;
                        }

                        block.addCollisionBoxesToList((World)(Object)this, k1, i2, l1, par2AxisAlignedBB, this.collidingBoundingBoxes, par1Entity);
                    }
                }
            }
        }

        double d0 = 0.25D;
        List list = this.getEntitiesWithinAABBExcludingEntity(par1Entity, par2AxisAlignedBB.expand(d0, d0, d0));

        for (int j2 = 0; j2 < list.size(); ++j2)
        {
            AxisAlignedBB axisalignedbb1 = ((Entity)list.get(j2)).getBoundingBox();

            if (axisalignedbb1 != null && axisalignedbb1.intersectsWith(par2AxisAlignedBB))
            {
            	this.collidingBoundingBoxes.add(axisalignedbb1);
            }

            axisalignedbb1 = par1Entity.getCollisionBox((Entity)list.get(j2));

            if (axisalignedbb1 != null && axisalignedbb1.intersectsWith(par2AxisAlignedBB))
            {
            	this.collidingBoundingBoxes.add(axisalignedbb1);
            }
        }

        return this.collidingBoundingBoxes;
    }

    @Overwrite
    public boolean isMaterialInBB(AxisAlignedBB par1AxisAlignedBB, Material par2Material) {
        if (this.isMaterialInBBLocal(par1AxisAlignedBB, par2Material)) {
            return true;
        } else {
            if (!((IMixinWorld) this).isSubWorld()) {
                Iterator i$ = ((IMixinWorld) this).getSubWorlds()
                    .iterator();

                while (i$.hasNext()) {
                    World curSubWorld = (World) i$.next();
                    if (((SubWorld) curSubWorld).isMaterialInBBGlobal(par1AxisAlignedBB, par2Material)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    // Old isMaterialInBB function
    public boolean isMaterialInBBLocal(AxisAlignedBB par1AxisAlignedBB, Material par2Material) {
        int i = MathHelper.floor_double(par1AxisAlignedBB.minX);
        int j = MathHelper.floor_double(par1AxisAlignedBB.maxX + 1.0D);
        int k = MathHelper.floor_double(par1AxisAlignedBB.minY);
        int l = MathHelper.floor_double(par1AxisAlignedBB.maxY + 1.0D);
        int i1 = MathHelper.floor_double(par1AxisAlignedBB.minZ);
        int j1 = MathHelper.floor_double(par1AxisAlignedBB.maxZ + 1.0D);

        for (int k1 = i; k1 < j; ++k1)
        {
            for (int l1 = k; l1 < l; ++l1)
            {
                for (int i2 = i1; i2 < j1; ++i2)
                {
                    if (this.getBlock(k1, l1, i2).getMaterial() == par2Material)
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Overwrite
    public boolean isAABBInMaterial(AxisAlignedBB par1AxisAlignedBB, Material par2Material) {
        if (((IMixinWorld) this).isAABBInMaterialLocal(par1AxisAlignedBB, par2Material)) {
            return true;
        } else {
            if (!((IMixinWorld) this).isSubWorld()) {
                Iterator i$ = ((IMixinWorld) this).getSubWorlds()
                    .iterator();

                while (i$.hasNext()) {
                    World curSubWorld = (World) i$.next();
                    if (((SubWorld) curSubWorld).isAABBInMaterialGlobal(par1AxisAlignedBB, par2Material)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }
    
    // Old isAABBInMaterial function
    public boolean isAABBInMaterialLocal(AxisAlignedBB par1AxisAlignedBB, Material par2Material) {
        int i = MathHelper.floor_double(par1AxisAlignedBB.minX);
        int j = MathHelper.floor_double(par1AxisAlignedBB.maxX + 1.0D);
        int k = MathHelper.floor_double(par1AxisAlignedBB.minY);
        int l = MathHelper.floor_double(par1AxisAlignedBB.maxY + 1.0D);
        int i1 = MathHelper.floor_double(par1AxisAlignedBB.minZ);
        int j1 = MathHelper.floor_double(par1AxisAlignedBB.maxZ + 1.0D);

        for (int k1 = i; k1 < j; ++k1)
        {
            for (int l1 = k; l1 < l; ++l1)
            {
                for (int i2 = i1; i2 < j1; ++i2)
                {
                    Block block = this.getBlock(k1, l1, i2);

                    if (block.getMaterial() == par2Material)
                    {
                        int j2 = this.getBlockMetadata(k1, l1, i2);
                        double d0 = (double)(l1 + 1);

                        if (j2 < 8)
                        {
                            d0 = (double)(l1 + 1) - (double)j2 / 8.0D;
                        }

                        if (d0 >= par1AxisAlignedBB.minY)
                        {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    @Overwrite
    public List getEntitiesWithinAABBExcludingEntity(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB,
        IEntitySelector par3IEntitySelector) {
        ArrayList arraylist = new ArrayList();
        arraylist
            .addAll(this.getEntitiesWithinAABBExcludingEntityLocal(par1Entity, par2AxisAlignedBB, par3IEntitySelector));
        Iterator i$ = ((IMixinWorld) this).getSubWorlds()
            .iterator();

        while (i$.hasNext()) {
            World curSubWorld = (World) i$.next();
            arraylist.addAll(
                ((IMixinWorld) curSubWorld).getEntitiesWithinAABBExcludingEntityLocal(
                    par1Entity,
                    ((IMixinAxisAlignedBB) par2AxisAlignedBB).getTransformedToLocalBoundingBox(curSubWorld),
                    par3IEntitySelector));
        }

        return arraylist;
    }

    // Old getEntitiesWithinAABBExcludingEntity function
    public List getEntitiesWithinAABBExcludingEntityLocal(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB) {
    	return ((IMixinWorld)this).getEntitiesWithinAABBExcludingEntityLocal(par1Entity, par2AxisAlignedBB, (IEntitySelector)null);
    }

    // Old getEntitiesWithinAABBExcludingEntity function
    public List getEntitiesWithinAABBExcludingEntityLocal(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB,
        IEntitySelector par3IEntitySelector) {
        if (par1Entity instanceof EntityPlayer) {
            par1Entity = ((IMixinEntity) par1Entity).getProxyPlayer((World)(Object)this);
        }
        ArrayList arraylist = new ArrayList();
        int i = MathHelper.floor_double((par2AxisAlignedBB.minX - MAX_ENTITY_RADIUS) / 16.0D);
        int j = MathHelper.floor_double((par2AxisAlignedBB.maxX + MAX_ENTITY_RADIUS) / 16.0D);
        int k = MathHelper.floor_double((par2AxisAlignedBB.minZ - MAX_ENTITY_RADIUS) / 16.0D);
        int l = MathHelper.floor_double((par2AxisAlignedBB.maxZ + MAX_ENTITY_RADIUS) / 16.0D);

        for (int i1 = i; i1 <= j; ++i1)
        {
            for (int j1 = k; j1 <= l; ++j1)
            {
                if (this.chunkExists(i1, j1))
                {
                	this.getChunkFromChunkCoords(i1, j1).getEntitiesWithinAABBForEntity(par1Entity, par2AxisAlignedBB, arraylist, par3IEntitySelector);
                }
            }
        }

        return arraylist;
    }

    @Overwrite
    public List selectEntitiesWithinAABB(Class par1Class, AxisAlignedBB par2AxisAlignedBB,
        IEntitySelector par3IEntitySelector) {
        ArrayList arraylist = new ArrayList();
        arraylist.addAll(this.selectEntitiesWithinAABBLocal(par1Class, par2AxisAlignedBB, par3IEntitySelector));
        Iterator i$ = ((IMixinWorld) this).getSubWorlds()
            .iterator();

        while (i$.hasNext()) {
            World curSubWorld = (World) i$.next();
            arraylist.addAll(
                ((IMixinWorld) curSubWorld).selectEntitiesWithinAABBLocal(
                    par1Class,
                    ((IMixinAxisAlignedBB) par2AxisAlignedBB).getTransformedToLocalBoundingBox(curSubWorld),
                    par3IEntitySelector));
        }

        return arraylist;
    }

    // Old selectEntitiesWithinAABB function
    public List selectEntitiesWithinAABBLocal(Class par1Class, AxisAlignedBB par2AxisAlignedBB,
        IEntitySelector par3IEntitySelector) {
        int i = MathHelper.floor_double((par2AxisAlignedBB.minX - MAX_ENTITY_RADIUS) / 16.0D);
        int j = MathHelper.floor_double((par2AxisAlignedBB.maxX + MAX_ENTITY_RADIUS) / 16.0D);
        int k = MathHelper.floor_double((par2AxisAlignedBB.minZ - MAX_ENTITY_RADIUS) / 16.0D);
        int l = MathHelper.floor_double((par2AxisAlignedBB.maxZ + MAX_ENTITY_RADIUS) / 16.0D);
        ArrayList arraylist = new ArrayList();

        for (int i1 = i; i1 <= j; ++i1)
        {
            for (int j1 = k; j1 <= l; ++j1)
            {
                if (this.chunkExists(i1, j1))
                {
                	this.getChunkFromChunkCoords(i1, j1).getEntitiesOfTypeWithinAAAB(par1Class, par2AxisAlignedBB, arraylist, par3IEntitySelector);
                }
            }
        }

        return arraylist;
    }

    @Overwrite
    public boolean spawnEntityInWorld(Entity par1Entity) {
        boolean result = ((IMixinWorld)this).spawnEntityInWorldLocal(par1Entity);
        World curSubWorld;
        Object proxyPlayer;
        if (!this.isRemote && !((IMixinWorld) this).isSubWorld() && par1Entity instanceof EntityPlayer) {
            for (Iterator i$ = ((IMixinWorld) this).getSubWorlds()
                .iterator(); i$.hasNext(); curSubWorld.spawnEntityInWorld((Entity) proxyPlayer)) {
                curSubWorld = (World) i$.next();
                proxyPlayer = ((IMixinEntity) par1Entity).getProxyPlayer(curSubWorld);
                if (proxyPlayer == null) {
                    proxyPlayer = new EntityPlayerMPSubWorldProxy((EntityPlayerMP) par1Entity, curSubWorld);
                    ((EntityPlayerMP) proxyPlayer).theItemInWorldManager.setWorld((WorldServer) curSubWorld);
                }
            }
        }

        return result;
    }

    // Old spawnEntityInWorld function
    public boolean spawnEntityInWorldLocal(Entity par1Entity) {
        // do not drop any items while restoring blocksnapshots. Prevents dupes
        if (!this.isRemote && (par1Entity == null || (par1Entity instanceof net.minecraft.entity.item.EntityItem && this.restoringBlockSnapshots))) return false;

        int i = MathHelper.floor_double(par1Entity.posX / 16.0D);
        int j = MathHelper.floor_double(par1Entity.posZ / 16.0D);
        boolean flag = par1Entity.forceSpawn;

        if (par1Entity instanceof EntityPlayer)
        {
            flag = true;
        }

        if (!flag && !this.chunkExists(i, j))
        {
            return false;
        }
        else
        {
            if (par1Entity instanceof EntityPlayer)
            {
                EntityPlayer entityplayer = (EntityPlayer)par1Entity;
                this.playerEntities.add(entityplayer);
                this.updateAllPlayersSleepingFlag();
            }
            if (MinecraftForge.EVENT_BUS.post(new EntityJoinWorldEvent(par1Entity, ((World)(Object)this))) && !flag) return false;

            this.getChunkFromChunkCoords(i, j).addEntity(par1Entity);
            this.loadedEntityList.add(par1Entity);
            this.onEntityAdded(par1Entity);
            return true;
        }
    }

    @Overwrite
    public void removeEntity(Entity par1Entity) {
        ((IMixinWorld)this).removeEntityLocal(par1Entity);
        if (!this.isRemote && !((IMixinWorld) this).isSubWorld() && par1Entity instanceof EntityPlayer) {
            Iterator i$ = ((IMixinWorld) this).getSubWorlds()
                .iterator();

            while (i$.hasNext()) {
                World curSubWorld = (World) i$.next();
                EntityPlayer proxyPlayer = ((IMixinEntity) par1Entity).getProxyPlayer(curSubWorld);
                if (proxyPlayer != null) {
                    curSubWorld.removeEntity(proxyPlayer);
                    if (!((PlayerManagerSuperClass) ((WorldServer) curSubWorld).getPlayerManager()).getPlayers()
                        .contains(proxyPlayer)) {
                        ((IMixinEntity) par1Entity).getPlayerProxyMap()
                            .remove(Integer.valueOf(((IMixinWorld) curSubWorld).getSubWorldID()));
                    }
                }
            }
        }
    }

    // Old removeEntity function
    public void removeEntityLocal(Entity par1Entity) {
        if (par1Entity.riddenByEntity != null)
        {
            par1Entity.riddenByEntity.mountEntity((Entity)null);
        }

        if (par1Entity.ridingEntity != null)
        {
            par1Entity.mountEntity((Entity)null);
        }

        par1Entity.setDead();

        if (par1Entity instanceof EntityPlayer)
        {
            this.playerEntities.remove(par1Entity);
            this.updateAllPlayersSleepingFlag();
            this.onEntityRemoved(par1Entity);
        }
    }

    @Overwrite
    public void removePlayerEntityDangerously(Entity par1Entity) {
        ((IMixinWorld)this).removePlayerEntityDangerouslyLocal(par1Entity);
        if (!this.isRemote && !((IMixinWorld) this).isSubWorld() && par1Entity instanceof EntityPlayer) {
            Iterator i$ = ((IMixinWorld) this).getSubWorlds()
                .iterator();

            while (i$.hasNext()) {
                World curSubWorld = (World) i$.next();
                EntityPlayer proxyPlayer = ((IMixinEntity) par1Entity).getProxyPlayer(curSubWorld);
                curSubWorld.removeEntity(proxyPlayer);
                if (!((PlayerManagerSuperClass) ((WorldServer) curSubWorld).getPlayerManager()).getPlayers()
                    .contains(proxyPlayer)) {
                    ((IMixinEntity) par1Entity).getPlayerProxyMap()
                        .remove(Integer.valueOf(((IMixinWorld) curSubWorld).getSubWorldID()));
                }
            }
        }
    }

    // Old removePlayerEntityDangerously function
    public void removePlayerEntityDangerouslyLocal(Entity par1Entity) {
        par1Entity.setDead();

        if (par1Entity instanceof EntityPlayer)
        {
            this.playerEntities.remove(par1Entity);
            this.updateAllPlayersSleepingFlag();
        }

        int i = par1Entity.chunkCoordX;
        int j = par1Entity.chunkCoordZ;

        if (par1Entity.addedToChunk && this.chunkExists(i, j))
        {
            this.getChunkFromChunkCoords(i, j).removeEntity(par1Entity);
        }

        this.loadedEntityList.remove(par1Entity);
        this.onEntityRemoved(par1Entity);
    }
}
