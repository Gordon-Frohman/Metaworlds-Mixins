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
     * Creates a new SubWorld with the first unoccupied id <br>
     * If this world is a subworld this will function will redirect to parent.createSubWorld()
     * 
     * @return new subworld
     */
    public default World createSubWorld() {
        return this.createSubWorld(this.getUnoccupiedSubworldID());
    }

    /**
     * Creates a new SubWorld with the first unoccupied id and given parameters <br>
     * If this world is a subworld this will function will redirect to parent.createSubWorld()
     * 
     * @return new subworld
     */
    public default World createSubWorld(double centerX, double centerY, double centerZ, double translationX,
        double translationY, double translationZ, double rotationPitch, double rotationYaw, double rotationRoll,
        double scaling) {
        return this.createSubWorld(
            this.getUnoccupiedSubworldID(),
            centerX,
            centerY,
            centerZ,
            translationX,
            translationY,
            translationZ,
            rotationPitch,
            rotationYaw,
            rotationRoll,
            scaling);
    }

    /**
     * Creates a new SubWorld with the given id <br>
     * If this world is a subworld this will function will redirect to parent.createSubWorld()
     * 
     * @return new subworld
     */
    public abstract World createSubWorld(int newSubWorldID);

    /**
     * Creates a new SubWorld with the given id and parameters <br>
     * If this world is a subworld this will function will redirect to parent.createSubWorld()
     * 
     * @return new subworld
     */
    public abstract World createSubWorld(int newSubWorldID, double centerX, double centerY, double centerZ,
        double translationX, double translationY, double translationZ, double rotationPitch, double rotationYaw,
        double rotationRoll, double scaling);

    /**
     * @return collection containing this world and all of its subworlds
     */
    public Collection<World> getWorlds();

    /**
     * @return collection only containing the subworlds, not this world itself
     */
    public Collection<World> getSubWorlds();

    /**
     * @return amount of worlds including this one. Equal to getSubWorlds().size() + 1 (= getWorlds().size())
     */
    public int getWorldsCount();

    /**
     * @return first ID which is not occupied by any subworld
     */
    public int getUnoccupiedSubworldID();

    /**
     * The parent worlds always have subWorldID 0. SubWorlds start from ID 1 counting up <br>
     * The ID is the same as the number suffix of the save folders
     * 
     * @return ID of the current world
     */
    public default int getSubWorldID() {
        return 0;
    }

    public default String getSubWorldType() {
        return SubWorldTypeManager.SUBWORLD_TYPE_DEFAULT;
    }

    /**
     * If this world is not subworld will return itself
     * 
     * @return the parent world of this one
     */
    public World getParentWorld();

    /**
     * @param targetSubWorldID
     * @return subworld with the given ID <br>
     *         null if there's none
     */
    public World getSubWorld(int targetSubWorldID);

    /**
     * @return true if this world is instance of SubWorld
     */
    public boolean isSubWorld();

    /**
     * @return This world's X position relative to its parent World
     */
    public double getTranslationX();

    /**
     * @return This world's Y position relative to its parent World
     */
    public double getTranslationY();

    /**
     * @return This world's Z position relative to its parent World
     */
    public double getTranslationZ();

    /**
     * @return This world's rotation around Y axis relative to its parent World
     */
    public double getRotationYaw();

    /**
     * @return This world's rotation around X axis relative to its parent World
     */
    public double getRotationPitch();

    /**
     * @return This world's rotation around Z axis relative to its parent World
     */
    public double getRotationRoll();

    /**
     * @return This world's scaling relative to its parent World
     */
    public double getScaling();

    /**
     * @return The local X coordinate of a point around which the world is rotating and scaling
     */
    public double getCenterX();

    /**
     * @return The local Y coordinate of a point around which the world is rotating and scaling
     */
    public double getCenterY();

    /**
     * @return The local Z coordinate of a point around which the world is rotating and scaling
     */
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

    /**
     * Transform from this world's coordinates to another world's coordinates
     * 
     * @param targetWorld
     * @param localEntity
     * @return
     */
    public Vec3 transformLocalToOther(World targetWorld, Entity localEntity);

    /**
     * Transform from this world's coordinates to another world's coordinates
     * 
     * @param targetWorld
     * @param localVec
     * @return
     */
    public Vec3 transformLocalToOther(World targetWorld, Vec3 localVec);

    /**
     * Transform from this world's coordinates to another world's coordinates
     * 
     * @param targetWorld
     * @param localX
     * @param localY
     * @param localZ
     * @return
     */
    public Vec3 transformLocalToOther(World targetWorld, double localX, double localY, double localZ);

    /**
     * Transform from this world's coordinates to another world's coordinates
     * 
     * @param targetWorld
     * @param localVectors
     * @return
     */
    public DoubleMatrix transformLocalToOther(World targetWorld, DoubleMatrix localVectors);

    /**
     * Transform from this world's coordinates to another world's coordinates
     * 
     * @param targetWorld
     * @param localVectors
     * @param result
     * @return
     */
    public DoubleMatrix transformLocalToOther(World targetWorld, DoubleMatrix localVectors, DoubleMatrix result);// In-Place
                                                                                                                 // operation
                                                                                                                 // for
                                                                                                                 // smaller
                                                                                                                 // memory
                                                                                                                 // footprint

    /**
     * Transform from another world's coordinates to this world's coordinates
     * 
     * @param sourceWorld
     * @param otherEntity
     * @return
     */
    public Vec3 transformOtherToLocal(World sourceWorld, Entity otherEntity);

    /**
     * Transform from another world's coordinates to this world's coordinates
     * 
     * @param sourceWorld
     * @param otherVec
     * @return
     */
    public Vec3 transformOtherToLocal(World sourceWorld, Vec3 otherVec);

    /**
     * Transform from another world's coordinates to this world's coordinates
     * 
     * @param sourceWorld
     * @param otherX
     * @param otherY
     * @param otherZ
     * @return
     */
    public Vec3 transformOtherToLocal(World sourceWorld, double otherX, double otherY, double otherZ);

    /**
     * Transform from another world's coordinates to this world's coordinates
     * 
     * @param sourceWorld
     * @param otherVectors
     * @return
     */
    public DoubleMatrix transformOtherToLocal(World sourceWorld, DoubleMatrix otherVectors);

    /**
     * Transform from another world's coordinates to this world's coordinates
     * 
     * @param sourceWorld
     * @param otherVectors
     * @param result
     * @return
     */
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
     * Will get all subworlds intersecting the specified AABB excluding the one passed into it.
     * 
     * @param subworld
     * @param bb
     * @return
     */
    public List<World> getSubworldsWithinAABBExcludingSubworld(SubWorld subworld, AxisAlignedBB bb);

    /**
     * Will get all subworlds intersecting the specified AABB excluding the one passed into it.
     * 
     * @param subworld
     * @param bb
     * @param selector
     * @return
     */
    public List<World> getSubworldsWithinAABBExcludingSubworld(SubWorld subworld, AxisAlignedBB bb,
        ISubWorldSelector selector);

    /**
     * Returns all subworlds of the specified class type which intersect with the AABB.
     * 
     * @param <T>
     * @param subworldClass
     * @param bb
     * @return
     */
    public <T> List<T> getSubworldsWithinAABB(Class<T> subworldClass, AxisAlignedBB bb);

    /**
     * Returns all subworlds of the specified class type which intersect with the AABB.
     * 
     * @param <T>
     * @param subworldClass
     * @param bb
     * @param selector
     * @return
     */
    public <T> List<T> getSubworldsWithinAABB(Class<T> subworldClass, AxisAlignedBB bb, ISubWorldSelector selector);
}
