package net.tclproject.metaworlds.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.minecraft.block.material.Material;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import org.jblas.DoubleMatrix;

public interface IMixinWorld {

    // Creates a new SubWorld
    // If this world is a subworld this will function will redirect to parent.CreateSubWorld()
    public abstract World CreateSubWorld();

    public abstract World CreateSubWorld(int newSubWorldID);

    // Returns collection containing this world and all of its subworlds
    public Collection<World> getWorlds();

    // Returns collection only containing the subworlds, not this world itself
    public Collection<World> getSubWorlds();

    public int getWorldsCount(); // Including this one. Equal to getSubWorlds().size() + 1 (= getWorlds().size())

    // The parent worlds always have subWorldID 0. SubWorlds start from ID 1 counting up
    // The ID is the same as the number suffix of the save folders
    public int getSubWorldID();

    public World getParentWorld();

    public World getSubWorld(int targetSubWorldID);

    public boolean isSubWorld();

    // Returns this world's position and rotation relative to its parent World
    public double getTranslationX();

    public double getTranslationY();

    public double getTranslationZ();

    public double getRotationYaw();

    public double getRotationPitch();

    public double getRotationRoll();

    public double getScaling();

    // Returns the position in local coordinates around which the world is rotating and scaling
    public double getCenterX();

    public double getCenterY();

    public double getCenterZ();

    // Transform coordinates from global to local
    // Returns the coordinates inside this (Sub)World's coordinate system which correspond to the given global
    // coordinates
    // public double transformToLocalX(double globalPosX, double globalPosZ);
    // public double transformToLocalY(double globalPosY);
    // public double transformToLocalZ(double globalPosX, double globalPosZ);

    // Transform coordinates from local to global
    // Returns the global coordinates which correspond to the given coordinates inside this (Sub)World's coordinate
    // system
    // public double transformToGlobalX(double subWorldPosX, double subWorldPosZ);
    // public double transformToGlobalY(double subWorldPosY);
    // public double transformToGlobalZ(double subWorldPosX, double subWorldPosZ);

    // Rotate a vector's components from its local direction to its global direction
    // This does not change the length of the vector, only its direction
    // public double rotateToGlobalX(double localX, double localZ);
    // public double rotateToGlobalY(double localY);
    // public double rotateToGlobalZ(double localX, double localZ);

    // Inverse to rotateToGlobal
    // public double rotateToLocalX(double globalX, double globalZ);
    // public double rotateToLocalY(double globalY);
    // public double rotateToLocalZ(double globalX, double globalZ);

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

    // Transform from this world's coordinates to another world's coordinates
    public Vec3 transformLocalToOther(World targetWorld, Entity localEntity);

    public Vec3 transformLocalToOther(World targetWorld, Vec3 localVec);

    public Vec3 transformLocalToOther(World targetWorld, double localX, double localY, double localZ);

    public DoubleMatrix transformLocalToOther(World targetWorld, DoubleMatrix localVectors);

    public DoubleMatrix transformLocalToOther(World targetWorld, DoubleMatrix localVectors, DoubleMatrix result);// In-Place
                                                                                                                 // operation
                                                                                                                 // for
                                                                                                                 // smaller
                                                                                                                 // memory
                                                                                                                 // footprint

    // Transform from another world's coordinates to this world's coordinates
    public Vec3 transformOtherToLocal(World sourceWorld, Entity otherEntity);

    public Vec3 transformOtherToLocal(World sourceWorld, Vec3 otherVec);

    public Vec3 transformOtherToLocal(World sourceWorld, double otherX, double otherY, double otherZ);

    public DoubleMatrix transformOtherToLocal(World sourceWorld, DoubleMatrix otherVectors);

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
    public List getCollidingBoundingBoxesGlobal(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB);

    // Expects par2AxisAlignedBB in local coordinates
    // In comparison:
    // getEntitiesWithinAABBExcludingEntity returns entities of this world and its parent's world
    // getEntitiesWithinAABBExcludingEntityLocal returns entities from this world only
    public List getEntitiesWithinAABBExcludingEntityLocal(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB);

    public List getEntitiesWithinAABBExcludingEntityLocal(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB,
        IEntitySelector par3IEntitySelector);

    public Chunk createNewChunk(int xPos, int zPos);

    public boolean isChunkWatchable(int chunkX, int chunkZ);

    public Map<Integer, World> getSubWorldsMap();
    
    public void doTickPartial(double interpolationFactor);
}
