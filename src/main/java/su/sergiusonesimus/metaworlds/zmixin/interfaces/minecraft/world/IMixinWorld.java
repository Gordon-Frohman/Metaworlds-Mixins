package su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import org.jblas.DoubleMatrix;

import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.api.SubWorldTypeManager;
import su.sergiusonesimus.metaworlds.command.ISubWorldSelector;

public interface IMixinWorld {

    /**
     * Creates a new SubWorld. <br>
     * If this world is a subworld this will function will redirect to parent.CreateSubWorld()
     */
    public abstract World createSubWorld();

    public abstract World createSubWorld(int newSubWorldID);

    /** Returns collection containing this world and all of its subworlds */
    public Collection<World> getWorlds();

    /** Returns collection only containing the subworlds, not this world itself */
    public Collection<World> getSubWorlds();

    public int getWorldsCount(); // Including this one. Equal to getSubWorlds().size() + 1 (= getWorlds().size())

    /** Returns first ID which is not occupied by any subworld */
    public int getUnoccupiedSubworldID();

    /**
     * The parent worlds always have subWorldID 0. SubWorlds start from ID 1 counting up
     * The ID is the same as the number suffix of the save folders
     */
    public default int getSubWorldID() {
        return 0;
    }

    public default String getSubWorldType() {
        return SubWorldTypeManager.SUBWORLD_TYPE_DEFAULT;
    }

    public World getParentWorld();

    public World getSubWorld(int targetSubWorldID);

    public boolean isSubWorld();

    /** Returns this world's position relative to its parent World */
    public double getTranslationX();

    /** Returns this world's position relative to its parent World */
    public double getTranslationY();

    /** Returns this world's position relative to its parent World */
    public double getTranslationZ();

    /** Returns this world's rotation relative to its parent World */
    public double getRotationYaw();

    /** Returns this world's rotation relative to its parent World */
    public double getRotationPitch();

    /** Returns this world's rotation relative to its parent World */
    public double getRotationRoll();

    public double getScaling();

    /** Returns the position in local coordinates around which the world is rotating and scaling */
    public double getCenterX();

    /** Returns the position in local coordinates around which the world is rotating and scaling */
    public double getCenterY();

    /** Returns the position in local coordinates around which the world is rotating and scaling */
    public double getCenterZ();

    public Vec3 transformToGlobal(Entity localEntity);

    public Vec3 transformToGlobal(Vec3 localVec);

    public Vec3 transformToGlobal(double localX, double localY, double localZ);

    public DoubleMatrix transformToGlobal(DoubleMatrix localVectors);

    public DoubleMatrix transformToGlobal(DoubleMatrix localVectors, DoubleMatrix result);// In-Place operation for
                                                                                          // smaller memory footprint

    public Vec3 transformToLocal(Entity globalEntity);

    public Vec3 transformToLocal(Vec3 globalVec);

    public Vec3 transformToLocal(double globalX, double globalY, double globalZ);

    public DoubleMatrix transformToLocal(DoubleMatrix globalVectors);

    public DoubleMatrix transformToLocal(DoubleMatrix globalVectors, DoubleMatrix result);// In-Place operation for
                                                                                          // smaller memory footprint

    /** Transform from this world's coordinates to another world's coordinates */
    public Vec3 transformLocalToOther(World targetWorld, Entity localEntity);

    /** Transform from this world's coordinates to another world's coordinates */
    public Vec3 transformLocalToOther(World targetWorld, Vec3 localVec);

    /** Transform from this world's coordinates to another world's coordinates */
    public Vec3 transformLocalToOther(World targetWorld, double localX, double localY, double localZ);

    /** Transform from this world's coordinates to another world's coordinates */
    public DoubleMatrix transformLocalToOther(World targetWorld, DoubleMatrix localVectors);

    /** Transform from this world's coordinates to another world's coordinates */
    public DoubleMatrix transformLocalToOther(World targetWorld, DoubleMatrix localVectors, DoubleMatrix result);// In-Place
                                                                                                                 // operation
                                                                                                                 // for
                                                                                                                 // smaller
                                                                                                                 // memory
                                                                                                                 // footprint

    /** Transform from another world's coordinates to this world's coordinates */
    public Vec3 transformOtherToLocal(World sourceWorld, Entity otherEntity);

    /** Transform from another world's coordinates to this world's coordinates */
    public Vec3 transformOtherToLocal(World sourceWorld, Vec3 otherVec);

    /** Transform from another world's coordinates to this world's coordinates */
    public Vec3 transformOtherToLocal(World sourceWorld, double otherX, double otherY, double otherZ);

    /** Transform from another world's coordinates to this world's coordinates */
    public DoubleMatrix transformOtherToLocal(World sourceWorld, DoubleMatrix otherVectors);

    /** Transform from another world's coordinates to this world's coordinates */
    public DoubleMatrix transformOtherToLocal(World sourceWorld, DoubleMatrix otherVectors, DoubleMatrix result);// In-Place
                                                                                                                 // operation
                                                                                                                 // for
                                                                                                                 // smaller
                                                                                                                 // memory
                                                                                                                 // footprint

    public Vec3 rotateToGlobal(Vec3 localVec);

    public Vec3 rotateToGlobal(double localX, double localY, double localZ);

    public DoubleMatrix rotateToGlobal(DoubleMatrix localVectors);

    public Vec3 rotateToLocal(Vec3 globalVec);

    public Vec3 rotateToLocal(double globalX, double globalY, double globalZ);

    public DoubleMatrix rotateToLocal(DoubleMatrix globalVectors);

    // Retrieve list of bounding boxes from this world (does not include those from its child subworlds) colliding with
    // par2AxisAlignedBB
    // Expects par2AxisAlignedBB in global coordinates
    // Returns list of bounding boxes in global coordinates
    public List<AxisAlignedBB> getCollidingBoundingBoxesGlobal(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB);

    // Expects par2AxisAlignedBB in local coordinates
    // In comparison:
    // getEntitiesWithinAABBExcludingEntity returns entities of this world and its parent's world
    // getEntitiesWithinAABBExcludingEntityLocal returns entities from this world only
    public List<Entity> getEntitiesWithinAABBExcludingEntityLocal(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB);

    public List<Entity> getEntitiesWithinAABBExcludingEntityLocal(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB,
        IEntitySelector par3IEntitySelector);

    public Chunk createNewChunk(int xPos, int zPos);

    public boolean isChunkWatchable(int chunkX, int chunkZ);

    public Map<Integer, World> getSubWorldsMap();

    public void doTickPartial(double interpolationFactor);

    /**
     * Will get all subworlds intersecting the specified AABB excluding the one passed into it. Args: subworldToExclude,
     * aabb
     */
    public List<World> getSubworldsWithinAABBExcludingSubworld(SubWorld subworld, AxisAlignedBB bb);

    public List<World> getSubworldsWithinAABBExcludingSubworld(SubWorld subworld, AxisAlignedBB bb,
        ISubWorldSelector selector);

    /**
     * Returns all subworlds of the specified class type which intersect with the AABB. Args: entityClass, aabb
     */
    public <T> List<T> getSubworldsWithinAABB(Class<T> subworldClass, AxisAlignedBB bb);

    public <T> List<T> getSubworldsWithinAABB(Class<T> subworldClass, AxisAlignedBB bb, ISubWorldSelector selector);
}
