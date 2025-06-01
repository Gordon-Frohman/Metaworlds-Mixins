package su.sergiusonesimus.metaworlds.api;

import java.nio.DoubleBuffer;
import java.util.List;
import java.util.Map;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.jblas.DoubleMatrix;

public interface SubWorld {

    World getParentWorld();

    int getSubWorldID();

    void removeSubWorld();

    int getSubWorldType();

    void setSubWorldType(int var1);

    double getTranslationX();

    double getTranslationY();

    double getTranslationZ();

    double getRotationYaw();

    /** gets the ACTUAL bloody rotation not the nonsense getRotationYaw returns */
    double getRotationActualYaw();

    double getRotationPitch();

    double getRotationRoll();

    double getCosRotationYaw();

    double getSinRotationYaw();

    double getCosRotationPitch();

    double getSinRotationPitch();

    double getCosRotationRoll();

    double getSinRotationRoll();

    double getScaling();

    double getCenterX();

    double getCenterY();

    double getCenterZ();

    void setTranslation(double var1, double var3, double var5);

    void setTranslation(Vec3 var1);

    void setRotationYaw(double var1);

    void setRotationPitch(double var1);

    void setRotationRoll(double var1);

    void setScaling(double var1);

    void setCenter(double var1, double var3, double var5);

    void setCenter(Vec3 var1);

    void setMotion(double var1, double var3, double var5);

    void setRotationYawSpeed(double var1);

    void setRotationPitchSpeed(double var1);

    void setRotationRollSpeed(double var1);

    void setScaleChangeRate(double var1);

    double getMotionX();

    double getMotionY();

    double getMotionZ();

    double getRotationYawSpeed();

    double getRotationPitchSpeed();

    double getRotationRollSpeed();

    double getScaleChangeRate();

    int getMinX();

    int getMinY();

    int getMinZ();

    ChunkCoordinates getMinCoordinates();

    int getMaxX();

    int getMaxY();

    int getMaxZ();

    ChunkCoordinates getMaxCoordinates();

    void setBoundaries(int var1, int var2, int var3, int var4, int var5, int var6);

    DoubleBuffer getTransformToLocalMatrixDirectBuffer();

    DoubleBuffer getTransformToGlobalMatrixDirectBuffer();

    Vec3 transformToLocal(Entity var1);

    Vec3 transformToLocal(Vec3 var1);

    Vec3 transformToLocal(double var1, double var3, double var5);

    DoubleMatrix transformToLocal(DoubleMatrix var1);

    DoubleMatrix transformToLocal(DoubleMatrix var1, DoubleMatrix var2);

    Vec3 transformToGlobal(Entity var1);

    Vec3 transformToGlobal(Vec3 var1);

    Vec3 transformToGlobal(double var1, double var3, double var5);

    DoubleMatrix transformToGlobal(DoubleMatrix var1);

    DoubleMatrix transformToGlobal(DoubleMatrix var1, DoubleMatrix var2);

    Vec3 transformLocalToOther(World var1, Entity var2);

    Vec3 transformLocalToOther(World var1, Vec3 var2);

    Vec3 transformLocalToOther(World var1, double var2, double var4, double var6);

    DoubleMatrix transformLocalToOther(World var1, DoubleMatrix var2);

    DoubleMatrix transformLocalToOther(World var1, DoubleMatrix var2, DoubleMatrix var3);

    Vec3 transformOtherToLocal(World var1, Entity var2);

    Vec3 transformOtherToLocal(World var1, Vec3 var2);

    Vec3 transformOtherToLocal(World var1, double var2, double var4, double var6);

    DoubleMatrix transformOtherToLocal(World var1, DoubleMatrix var2);

    DoubleMatrix transformOtherToLocal(World var1, DoubleMatrix var2, DoubleMatrix var3);

    Vec3 rotateToGlobal(Vec3 var1);

    Vec3 rotateToGlobal(double var1, double var3, double var5);

    DoubleMatrix rotateToGlobal(DoubleMatrix var1);

    Vec3 rotateToLocal(Vec3 var1);

    Vec3 rotateToLocal(double var1, double var3, double var5);

    DoubleMatrix rotateToLocal(DoubleMatrix var1);

    Vec3 rotateYawToGlobal(Vec3 var1);

    Vec3 rotateYawToGlobal(double var1, double var3, double var5);

    DoubleMatrix rotateYawToGlobal(DoubleMatrix var1);

    Vec3 rotateYawToLocal(Vec3 var1);

    Vec3 rotateYawToLocal(double var1, double var3, double var5);

    DoubleMatrix rotateYawToLocal(DoubleMatrix var1);

    List getCollidingBoundingBoxesLocal(Entity var1, AxisAlignedBB var2);

    List getCollidingBoundingBoxesGlobal(Entity var1, AxisAlignedBB var2);

    List getCollidingBoundingBoxesLocalWithMovement(Entity entity, AxisAlignedBB aabb, Vec3 movement);

    List getCollidingBoundingBoxesGlobalWithMovement(Entity entity, AxisAlignedBB aabb, Vec3 movement);

    boolean isAABBInMaterialGlobal(AxisAlignedBB var1, Material var2);

    boolean isMaterialInBBGlobal(AxisAlignedBB var1, Material var2);

    Map<Entity, Vec3> getEntitiesToDrag();

    void registerEntityToDrag(Entity var1);

    void unregisterEntityToDrag(Entity var1);

    void registerDetachedEntity(Entity var1);

    void unregisterDetachedEntity(Entity var1);

    /** Gets the closest bounding box there is, not accounting for rotation. */
    AxisAlignedBB getMaximumCloseWorldBB();

    /**
     * Gets the closest bounding box there is, accounting for rotation.
     * Note: As far as I tested, the mechanism provided for this by the original Metaworlds author is not accurate, so
     * do not use.
     */
    AxisAlignedBB getMaximumCloseWorldBBRotated();

    /**
     * Same as getMaximumCloseWorldBB but expands the bounding box to be the same in x and z instead so that we would
     * account for rotation.
     * Do not pass both params as true. You can path both as false though to be able to expand both.
     */
    AxisAlignedBB getMaximumStretchedWorldBB(boolean shouldOnlyExpandX, boolean shouldOnlyExpandZ);

    /** How larger X of ship is, compared to Z (range x / range z) */
    double getRatioXToZ();

    /**
     * If it has collision with the main world. A VERY BAD, UNRELIABLE MECHANISM IS USED. If anyone can improve it,
     * please, do <3 - please
     */
    boolean hasCollision();

    default boolean isEmpty() {
        return false;
    }
}
