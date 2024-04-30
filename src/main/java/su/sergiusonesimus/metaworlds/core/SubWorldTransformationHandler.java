package su.sergiusonesimus.metaworlds.core;

import java.nio.DoubleBuffer;

import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import su.sergiusonesimus.metaworlds.api.IMixinWorld;

import org.jblas.DoubleMatrix;
import org.lwjgl.BufferUtils;

public class SubWorldTransformationHandler {

    World holderWorld;
    private double translationX;
    private double translationY;
    private double translationZ;
    private double motionX;
    private double motionY;
    private double motionZ;
    private double centerX;
    private double centerY;
    private double centerZ;
    private double rotationYaw;
    private double rotationPitch;
    private double rotationRoll;
    private double cosRotationYaw = 1.0D;
    private double sinRotationYaw;
    private double cosRotationPitch = 1.0D;
    private double sinRotationPitch;
    private double cosRotationRoll = 1.0D;
    private double sinRotationRoll;
    private double rotationYawFrequency;
    private double rotationPitchFrequency;
    private double rotationRollFrequency;
    private double scaling = 1.0D;
    private double scaleChangeRate;
    DoubleMatrix matrixTranslation = DoubleMatrix.eye(4);
    DoubleMatrix matrixTranslationInverse = DoubleMatrix.eye(4);
    DoubleMatrix matrixCenterTranslation = DoubleMatrix.eye(4);
    DoubleMatrix matrixCenterTranslationInverse = DoubleMatrix.eye(4);
    boolean needRotationRecalc = false;
    DoubleMatrix matrixRotationYaw = DoubleMatrix.eye(4);
    DoubleMatrix matrixRotationYawInverse = DoubleMatrix.eye(4);
    DoubleMatrix matrixRotationPitch = DoubleMatrix.eye(4);
    DoubleMatrix matrixRotationPitchInverse = DoubleMatrix.eye(4);
    DoubleMatrix matrixRotationRoll = DoubleMatrix.eye(4);
    DoubleMatrix matrixRotationRollInverse = DoubleMatrix.eye(4);
    DoubleMatrix matrixRotation = DoubleMatrix.eye(4);
    DoubleMatrix matrixRotationInverse = DoubleMatrix.eye(4);
    DoubleMatrix matrixScaling = DoubleMatrix.eye(4);
    DoubleMatrix matrixScalingInverse = DoubleMatrix.eye(4);
    boolean needTransformationRecalc = false;
    DoubleMatrix matrixTransformToLocal = DoubleMatrix.eye(4);
    DoubleMatrix matrixTransformToGlobal = DoubleMatrix.eye(4);

    public SubWorldTransformationHandler(World thisHolderWorld) {
        this.holderWorld = thisHolderWorld;
    }

    public double getTranslationX() {
        return this.translationX;
    }

    public double getTranslationY() {
        return this.translationY;
    }

    public double getTranslationZ() {
        return this.translationZ;
    }

    public double getCenterX() {
        return this.centerX;
    }

    public double getCenterY() {
        return this.centerY;
    }

    public double getCenterZ() {
        return this.centerZ;
    }

    public double getRotationYaw() {
        return this.rotationYaw;
    }

    public double getRotationPitch() {
        return this.rotationPitch;
    }

    public double getRotationRoll() {
        return this.rotationRoll;
    }

    public double getCosRotationYaw() {
        return this.cosRotationYaw;
    }

    public double getSinRotationYaw() {
        return this.sinRotationYaw;
    }

    public double getCosRotationPitch() {
        return this.cosRotationPitch;
    }

    public double getSinRotationPitch() {
        return this.sinRotationPitch;
    }

    public double getCosRotationRoll() {
        return this.cosRotationRoll;
    }

    public double getSinRotationRoll() {
        return this.sinRotationRoll;
    }

    public double getScaling() {
        return this.scaling;
    }

    public void setTranslation(Vec3 newTranslation) {
        this.setTranslation(newTranslation.xCoord, newTranslation.yCoord, newTranslation.zCoord);
    }

    public void setTranslation(double newX, double newY, double newZ) {
        this.translationX = newX;
        this.translationY = newY;
        this.translationZ = newZ;
        this.matrixTranslation.data[12] = newX;
        this.matrixTranslation.data[13] = newY;
        this.matrixTranslation.data[14] = newZ;
        this.matrixTranslationInverse.data[12] = -newX;
        this.matrixTranslationInverse.data[13] = -newY;
        this.matrixTranslationInverse.data[14] = -newZ;
        this.needTransformationRecalc = true;
    }

    public void setCenter(Vec3 newCenter) {
        this.setCenter(newCenter.xCoord, newCenter.yCoord, newCenter.zCoord);
    }

    public void setCenter(double newX, double newY, double newZ) {
        Vec3 oldGlobalPos = this.transformToGlobal(0.0D, 0.0D, 0.0D);
        this.centerX = newX;
        this.centerY = newY;
        this.centerZ = newZ;
        this.matrixCenterTranslation.data[12] = -newX;
        this.matrixCenterTranslation.data[13] = -newY;
        this.matrixCenterTranslation.data[14] = -newZ;
        this.matrixCenterTranslationInverse.data[12] = newX;
        this.matrixCenterTranslationInverse.data[13] = newY;
        this.matrixCenterTranslationInverse.data[14] = newZ;
        this.needTransformationRecalc = true;
        Vec3 newGlobalPos = this.transformToGlobal(0.0D, 0.0D, 0.0D);
        this.setTranslation(
            this.getTranslationX() + oldGlobalPos.xCoord - newGlobalPos.xCoord,
            this.getTranslationY() + oldGlobalPos.yCoord - newGlobalPos.yCoord,
            this.getTranslationZ() + oldGlobalPos.zCoord - newGlobalPos.zCoord);
    }

    public void setRotationYaw(double newYaw) {
        this.rotationYaw = newYaw;
        this.cosRotationYaw = Math.cos(newYaw * Math.PI / 180.0D);
        this.sinRotationYaw = Math.sin(newYaw * Math.PI / 180.0D);
        this.matrixRotationYaw.data[0] = this.cosRotationYaw;
        this.matrixRotationYaw.data[8] = this.sinRotationYaw;
        this.matrixRotationYaw.data[2] = -this.sinRotationYaw;
        this.matrixRotationYaw.data[10] = this.cosRotationYaw;
        this.matrixRotationYawInverse.data[0] = this.cosRotationYaw;
        this.matrixRotationYawInverse.data[8] = -this.sinRotationYaw;
        this.matrixRotationYawInverse.data[2] = this.sinRotationYaw;
        this.matrixRotationYawInverse.data[10] = this.cosRotationYaw;
        this.needRotationRecalc = true;
        this.needTransformationRecalc = true;
    }

    public void setRotationPitch(double newPitch) {
        this.rotationPitch = newPitch;
        this.cosRotationPitch = Math.cos(newPitch * Math.PI / 180.0D);
        this.sinRotationPitch = Math.sin(newPitch * Math.PI / 180.0D);
        this.matrixRotationPitch.data[0] = this.cosRotationPitch;
        this.matrixRotationPitch.data[1] = this.sinRotationPitch;
        this.matrixRotationPitch.data[4] = -this.sinRotationPitch;
        this.matrixRotationPitch.data[5] = this.cosRotationPitch;
        this.matrixRotationPitchInverse.data[0] = this.cosRotationPitch;
        this.matrixRotationPitchInverse.data[1] = -this.sinRotationPitch;
        this.matrixRotationPitchInverse.data[4] = this.sinRotationPitch;
        this.matrixRotationPitchInverse.data[5] = this.cosRotationPitch;
        this.needRotationRecalc = true;
        this.needTransformationRecalc = true;
    }

    public void setRotationRoll(double newRoll) {
        this.rotationRoll = newRoll;
        this.cosRotationRoll = Math.cos(newRoll * Math.PI / 180.0D);
        this.sinRotationRoll = Math.sin(newRoll * Math.PI / 180.0D);
        this.matrixRotationRoll.data[5] = this.cosRotationRoll;
        this.matrixRotationRoll.data[6] = this.sinRotationRoll;
        this.matrixRotationRoll.data[9] = -this.sinRotationRoll;
        this.matrixRotationRoll.data[10] = this.cosRotationRoll;
        this.matrixRotationRollInverse.data[5] = this.cosRotationRoll;
        this.matrixRotationRollInverse.data[6] = -this.sinRotationRoll;
        this.matrixRotationRollInverse.data[9] = this.sinRotationRoll;
        this.matrixRotationRollInverse.data[10] = this.cosRotationRoll;
        this.needRotationRecalc = true;
        this.needTransformationRecalc = true;
    }

    public void setScaling(double newScaling) {
        if (Math.abs(newScaling) < 1.0E-4D) {
            newScaling = 1.0E-4D;
        }

        this.scaling = newScaling;
        this.matrixScaling.data[0] = this.scaling;
        this.matrixScaling.data[5] = this.scaling;
        this.matrixScaling.data[10] = this.scaling;
        this.matrixScalingInverse.data[0] = 1.0D / this.scaling;
        this.matrixScalingInverse.data[5] = 1.0D / this.scaling;
        this.matrixScalingInverse.data[10] = 1.0D / this.scaling;
        this.needTransformationRecalc = true;
    }

    public void recalcRotationMatrix() {
        if (this.needRotationRecalc) {
            this.matrixRotation = this.matrixRotationYaw.mmul(this.matrixRotationPitch.mmul(this.matrixRotationRoll));
            this.matrixRotationInverse = this.matrixRotationRollInverse
                .mmul(this.matrixRotationPitchInverse.mmul(this.matrixRotationYawInverse));
            this.needRotationRecalc = false;
        }
    }

    public void recalcTransformationMatrices() {
        this.recalcRotationMatrix();
        if (this.needTransformationRecalc) {
            this.matrixTransformToGlobal = this.matrixTranslation.mmul(
                this.matrixCenterTranslationInverse
                    .mmul(this.matrixScaling.mmul(this.matrixRotation.mmul(this.matrixCenterTranslation))));
            this.matrixTransformToLocal = this.matrixCenterTranslationInverse.mmul(
                this.matrixRotationInverse.mmul(
                    this.matrixScalingInverse.mmul(this.matrixCenterTranslation.mmul(this.matrixTranslationInverse))));
            this.needTransformationRecalc = false;
        }
    }

    public void setMotion(double par1MotionX, double par2MotionY, double par3MotionZ) {
        this.motionX = par1MotionX;
        this.motionY = par2MotionY;
        this.motionZ = par3MotionZ;
    }

    public void setRotationYawSpeed(double par1Speed) {
        this.rotationYawFrequency = par1Speed;
    }

    public void setRotationPitchSpeed(double par1Speed) {
        this.rotationPitchFrequency = par1Speed;
    }

    public void setRotationRollSpeed(double par1Speed) {
        this.rotationRollFrequency = par1Speed;
    }

    public void setScaleChangeRate(double par1Rate) {
        this.scaleChangeRate = par1Rate;
    }

    public double getMotionX() {
        return this.motionX;
    }

    public double getMotionY() {
        return this.motionY;
    }

    public double getMotionZ() {
        return this.motionZ;
    }

    public double getRotationYawSpeed() {
        return this.rotationYawFrequency;
    }

    public double getRotationPitchSpeed() {
        return this.rotationPitchFrequency;
    }

    public double getRotationRollSpeed() {
        return this.rotationRollFrequency;
    }

    public double getScaleChangeRate() {
        return this.scaleChangeRate;
    }

    public boolean getIsInMotion() {
        return this.motionX != 0.0D || this.motionY != 0.0D
            || this.motionZ != 0.0D
            || this.rotationYawFrequency != 0.0D
            || this.rotationPitchFrequency != 0.0D
            || this.rotationRollFrequency != 0.0D
            || this.scaleChangeRate != 0.0D;
    }

    public DoubleMatrix getRotationMatrix() {
        this.recalcRotationMatrix();
        return this.matrixRotation;
    }

    public DoubleMatrix getRotationInverseMatrix() {
        this.recalcRotationMatrix();
        return this.matrixRotationInverse;
    }

    public DoubleMatrix getTransformToLocalMatrix() {
        this.recalcTransformationMatrices();
        return this.matrixTransformToLocal;
    }

    public DoubleMatrix getTransformToGlobalMatrix() {
        this.recalcTransformationMatrices();
        return this.matrixTransformToGlobal;
    }

    public DoubleBuffer getTransformToLocalMatrixDirectBuffer() {
        this.recalcTransformationMatrices();
        return (DoubleBuffer) BufferUtils.createDoubleBuffer(16)
            .put(this.matrixTransformToLocal.data)
            .rewind();
    }

    public DoubleBuffer getTransformToGlobalMatrixDirectBuffer() {
        this.recalcTransformationMatrices();
        return (DoubleBuffer) BufferUtils.createDoubleBuffer(16)
            .put(this.matrixTransformToGlobal.data)
            .rewind();
    }

    public Vec3 transformToLocal(Vec3 globalVec) {
        return this.transformToLocal(globalVec.xCoord, globalVec.yCoord, globalVec.zCoord);
    }

    public Vec3 transformToLocal(double globalX, double globalY, double globalZ) {
        DoubleMatrix tempVector = new DoubleMatrix(new double[] { globalX, globalY, globalZ, 1.0D });
        this.transformToLocal(tempVector, tempVector);
        return Vec3.createVectorHelper(tempVector.data[0], tempVector.data[1], tempVector.data[2]);
    }

    public DoubleMatrix transformToLocal(DoubleMatrix globalVectors) {
        return this.getTransformToLocalMatrix()
            .mmul(globalVectors);
    }

    public DoubleMatrix transformToLocal(DoubleMatrix globalVectors, DoubleMatrix result) {
        return this.getTransformToLocalMatrix()
            .mmuli(globalVectors, result);
    }

    public Vec3 transformToGlobal(Vec3 localVec) {
        return this.transformToGlobal(localVec.xCoord, localVec.yCoord, localVec.zCoord);
    }

    public Vec3 transformToGlobal(double localX, double localY, double localZ) {
        DoubleMatrix tempVector = new DoubleMatrix(new double[] { localX, localY, localZ, 1.0D });
        this.transformToGlobal(tempVector, tempVector);
        return Vec3.createVectorHelper(tempVector.data[0], tempVector.data[1], tempVector.data[2]);
    }

    public DoubleMatrix transformToGlobal(DoubleMatrix localVectors) {
        return this.getTransformToGlobalMatrix()
            .mmul(localVectors);
    }

    public DoubleMatrix transformToGlobal(DoubleMatrix localVectors, DoubleMatrix result) {
        return this.getTransformToGlobalMatrix()
            .mmuli(localVectors, result);
    }

    public Vec3 transformLocalToOther(World targetWorld, Vec3 localVec) {
        return this.transformLocalToOther(targetWorld, localVec.xCoord, localVec.yCoord, localVec.zCoord);
    }

    public Vec3 transformLocalToOther(World targetWorld, double localX, double localY, double localZ) {
        if (targetWorld == null) {
            targetWorld = ((IMixinWorld) this.holderWorld).getParentWorld();
        }

        if (!((IMixinWorld) targetWorld).isSubWorld()) {
            return this.transformToGlobal(localX, localY, localZ);
        } else if (targetWorld == this.holderWorld) {
            return Vec3.createVectorHelper(localX, localY, localZ);
        } else {
            DoubleMatrix tempVector = new DoubleMatrix(new double[] { localX, localY, localZ, 1.0D });
            this.transformLocalToOther(targetWorld, tempVector, tempVector);
            return Vec3.createVectorHelper(tempVector.data[0], tempVector.data[1], tempVector.data[2]);
        }
    }

    public DoubleMatrix transformLocalToOther(World targetWorld, DoubleMatrix localVectors) {
        if (targetWorld == null) {
            targetWorld = ((IMixinWorld) this.holderWorld).getParentWorld();
        }

        if (targetWorld == this.holderWorld) {
            return localVectors.dup();
        } else {
            DoubleMatrix tempVectors = this.transformToGlobal(localVectors);
            return !((IMixinWorld) targetWorld).isSubWorld() ? tempVectors
                : ((IMixinWorld) targetWorld).transformToLocal(tempVectors, tempVectors);
        }
    }

    public DoubleMatrix transformLocalToOther(World targetWorld, DoubleMatrix localVectors, DoubleMatrix result) {
        if (targetWorld == null) {
            targetWorld = ((IMixinWorld) this.holderWorld).getParentWorld();
        }

        if (targetWorld == this.holderWorld) {
            return result.copy(localVectors);
        } else {
            localVectors = this.transformToGlobal(localVectors, result);
            return !((IMixinWorld) targetWorld).isSubWorld() ? localVectors
                : ((IMixinWorld) targetWorld).transformToLocal(localVectors, result);
        }
    }

    public Vec3 transformOtherToLocal(World sourceWorld, Vec3 otherVec) {
        return this.transformOtherToLocal(sourceWorld, otherVec.xCoord, otherVec.yCoord, otherVec.zCoord);
    }

    public Vec3 transformOtherToLocal(World sourceWorld, double otherX, double otherY, double otherZ) {
        if (sourceWorld == null) {
            sourceWorld = ((IMixinWorld) this.holderWorld).getParentWorld();
        }

        if (!((IMixinWorld) sourceWorld).isSubWorld()) {
            return this.transformToLocal(otherX, otherY, otherZ);
        } else if (sourceWorld == this.holderWorld) {
            return Vec3.createVectorHelper(otherX, otherY, otherZ);
        } else {
            DoubleMatrix tempVector = new DoubleMatrix(new double[] { otherX, otherY, otherZ, 1.0D });
            this.transformOtherToLocal(sourceWorld, tempVector, tempVector);
            return Vec3.createVectorHelper(tempVector.data[0], tempVector.data[1], tempVector.data[2]);
        }
    }

    public DoubleMatrix transformOtherToLocal(World sourceWorld, DoubleMatrix otherVectors) {
        if (sourceWorld == null) {
            sourceWorld = ((IMixinWorld) this.holderWorld).getParentWorld();
        }

        if (sourceWorld == this.holderWorld) {
            return otherVectors.dup();
        } else {
            DoubleMatrix tempVectors = otherVectors;
            if (((IMixinWorld) sourceWorld).isSubWorld()) {
                tempVectors = ((IMixinWorld) sourceWorld).transformToGlobal(otherVectors);
            }

            return this.transformToLocal(tempVectors, tempVectors);
        }
    }

    public DoubleMatrix transformOtherToLocal(World sourceWorld, DoubleMatrix otherVectors, DoubleMatrix result) {
        if (sourceWorld == null) {
            sourceWorld = ((IMixinWorld) this.holderWorld).getParentWorld();
        }

        if (sourceWorld == this.holderWorld) {
            return result.copy(otherVectors);
        } else {
            if (((IMixinWorld) sourceWorld).isSubWorld()) {
                otherVectors = ((IMixinWorld) sourceWorld).transformToGlobal(otherVectors, result);
            }

            return this.transformToLocal(otherVectors, result);
        }
    }

    public Vec3 rotateToGlobal(Vec3 localVec) {
        return this.rotateToGlobal(localVec.xCoord, localVec.yCoord, localVec.zCoord);
    }

    public Vec3 rotateToGlobal(double localX, double localY, double localZ) {
        DoubleMatrix tempVector = new DoubleMatrix(new double[] { localX, localY, localZ, 1.0D });
        tempVector = this.rotateToGlobal(tempVector);
        return Vec3.createVectorHelper(tempVector.data[0], tempVector.data[1], tempVector.data[2]);
    }

    public DoubleMatrix rotateToGlobal(DoubleMatrix localVectors) {
        return this.getRotationMatrix()
            .mmul(localVectors);
    }

    public Vec3 rotateToLocal(Vec3 globalVec) {
        return this.rotateToLocal(globalVec.xCoord, globalVec.yCoord, globalVec.zCoord);
    }

    public Vec3 rotateToLocal(double globalX, double globalY, double globalZ) {
        DoubleMatrix tempVector = new DoubleMatrix(new double[] { globalX, globalY, globalZ, 1.0D });
        tempVector = this.rotateToLocal(tempVector);
        return Vec3.createVectorHelper(tempVector.data[0], tempVector.data[1], tempVector.data[2]);
    }

    public DoubleMatrix rotateToLocal(DoubleMatrix globalVectors) {
        return this.getRotationInverseMatrix()
            .mmul(globalVectors);
    }

    public Vec3 rotateYawToGlobal(Vec3 localVec) {
        return this.rotateYawToGlobal(localVec.xCoord, localVec.yCoord, localVec.zCoord);
    }

    public Vec3 rotateYawToGlobal(double localX, double localY, double localZ) {
        DoubleMatrix tempVector = new DoubleMatrix(new double[] { localX, localY, localZ, 1.0D });
        tempVector = this.rotateYawToGlobal(tempVector);
        return Vec3.createVectorHelper(tempVector.data[0], tempVector.data[1], tempVector.data[2]);
    }

    public DoubleMatrix rotateYawToGlobal(DoubleMatrix localVectors) {
        return this.matrixRotationYaw.mmul(localVectors);
    }

    public Vec3 rotateYawToLocal(Vec3 globalVec) {
        return this.rotateYawToLocal(globalVec.xCoord, globalVec.yCoord, globalVec.zCoord);
    }

    public Vec3 rotateYawToLocal(double globalX, double globalY, double globalZ) {
        DoubleMatrix tempVector = new DoubleMatrix(new double[] { globalX, globalY, globalZ, 1.0D });
        tempVector = this.rotateYawToLocal(tempVector);
        return Vec3.createVectorHelper(tempVector.data[0], tempVector.data[1], tempVector.data[2]);
    }

    public DoubleMatrix rotateYawToLocal(DoubleMatrix globalVectors) {
        return this.matrixRotationYawInverse.mmul(globalVectors);
    }
}
