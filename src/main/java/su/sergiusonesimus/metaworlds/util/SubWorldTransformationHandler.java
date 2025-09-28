package su.sergiusonesimus.metaworlds.util;

import java.nio.DoubleBuffer;

import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.lwjgl.BufferUtils;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

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
    INDArray matrixTranslation = Nd4j.eye(4);
    INDArray matrixTranslationInverse = Nd4j.eye(4);
    INDArray matrixCenterTranslation = Nd4j.eye(4);
    INDArray matrixCenterTranslationInverse = Nd4j.eye(4);
    boolean needRotationRecalc = false;
    INDArray matrixRotationYaw = Nd4j.eye(4);
    INDArray matrixRotationYawInverse = Nd4j.eye(4);
    INDArray matrixRotationPitch = Nd4j.eye(4);
    INDArray matrixRotationPitchInverse = Nd4j.eye(4);
    INDArray matrixRotationRoll = Nd4j.eye(4);
    INDArray matrixRotationRollInverse = Nd4j.eye(4);
    INDArray matrixRotation = Nd4j.eye(4);
    INDArray matrixRotationInverse = Nd4j.eye(4);
    INDArray matrixScaling = Nd4j.eye(4);
    INDArray matrixScalingInverse = Nd4j.eye(4);
    boolean needTransformationRecalc = false;
    INDArray matrixTransformToLocal = Nd4j.eye(4);
    INDArray matrixTransformToGlobal = Nd4j.eye(4);

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
        if (this.translationX != newX || this.translationY != newY || this.translationZ != newZ) {
            this.translationX = newX;
            this.translationY = newY;
            this.translationZ = newZ;
            this.matrixTranslation.putScalar(0, 3, newX);
            this.matrixTranslation.putScalar(1, 3, newY);
            this.matrixTranslation.putScalar(2, 3, newZ);
            this.matrixTranslationInverse.putScalar(0, 3, -newX);
            this.matrixTranslationInverse.putScalar(1, 3, -newY);
            this.matrixTranslationInverse.putScalar(2, 3, -newZ);
            this.needTransformationRecalc = true;
        }
    }

    public void setCenter(Vec3 newCenter) {
        this.setCenter(newCenter.xCoord, newCenter.yCoord, newCenter.zCoord);
    }

    public void setCenter(double newX, double newY, double newZ) {
        if (this.centerX != newX || this.centerY != newY || this.centerZ != newZ) {
            Vec3 oldGlobalPos = this.transformToGlobal(0.0D, 0.0D, 0.0D);
            this.centerX = newX;
            this.centerY = newY;
            this.centerZ = newZ;
            this.matrixCenterTranslation.putScalar(0, 3, -newX);
            this.matrixCenterTranslation.putScalar(1, 3, -newY);
            this.matrixCenterTranslation.putScalar(2, 3, -newZ);
            this.matrixCenterTranslationInverse.putScalar(0, 3, newX);
            this.matrixCenterTranslationInverse.putScalar(1, 3, newY);
            this.matrixCenterTranslationInverse.putScalar(2, 3, newZ);
            this.needTransformationRecalc = true;
            Vec3 newGlobalPos = this.transformToGlobal(0.0D, 0.0D, 0.0D);
            this.setTranslation(
                this.getTranslationX() + oldGlobalPos.xCoord - newGlobalPos.xCoord,
                this.getTranslationY() + oldGlobalPos.yCoord - newGlobalPos.yCoord,
                this.getTranslationZ() + oldGlobalPos.zCoord - newGlobalPos.zCoord);
        }
    }

    public void setCenterOnCreate(double newX, double newY, double newZ) {
        if (this.centerX != newX || this.centerY != newY || this.centerZ != newZ) {
            this.centerX = newX;
            this.centerY = newY;
            this.centerZ = newZ;
            this.matrixCenterTranslation.putScalar(0, 3, -newX);
            this.matrixCenterTranslation.putScalar(1, 3, -newY);
            this.matrixCenterTranslation.putScalar(2, 3, -newZ);
            this.matrixCenterTranslationInverse.putScalar(0, 3, newX);
            this.matrixCenterTranslationInverse.putScalar(1, 3, newY);
            this.matrixCenterTranslationInverse.putScalar(2, 3, newZ);
            this.needTransformationRecalc = true;
        }
    }

    public void setRotationYaw(double newYaw) {
        if (this.rotationYaw != newYaw) {
            this.rotationYaw = newYaw;
            this.cosRotationYaw = Math.cos(newYaw * Math.PI / 180.0D);
            this.sinRotationYaw = Math.sin(newYaw * Math.PI / 180.0D);
            this.matrixRotationYaw.putScalar(0, 0, this.cosRotationYaw);
            this.matrixRotationYaw.putScalar(2, 2, this.cosRotationYaw);
            this.matrixRotationYaw.putScalar(0, 2, -this.sinRotationYaw);
            this.matrixRotationYaw.putScalar(2, 0, this.sinRotationYaw);
            this.matrixRotationYawInverse.putScalar(0, 0, this.cosRotationYaw);
            this.matrixRotationYawInverse.putScalar(2, 2, this.cosRotationYaw);
            this.matrixRotationYawInverse.putScalar(0, 2, this.sinRotationYaw);
            this.matrixRotationYawInverse.putScalar(2, 0, -this.sinRotationYaw);
            this.needRotationRecalc = true;
            this.needTransformationRecalc = true;
        }
    }

    public void setRotationPitch(double newPitch) {
        if (this.rotationPitch != newPitch) {
            this.rotationPitch = newPitch;
            this.cosRotationPitch = Math.cos(newPitch * Math.PI / 180.0D);
            this.sinRotationPitch = Math.sin(newPitch * Math.PI / 180.0D);
            this.matrixRotationPitch.putScalar(0, 0, this.cosRotationPitch);
            this.matrixRotationPitch.putScalar(1, 1, this.cosRotationPitch);
            this.matrixRotationPitch.putScalar(0, 1, this.sinRotationPitch);
            this.matrixRotationPitch.putScalar(1, 0, -this.sinRotationPitch);
            this.matrixRotationPitchInverse.putScalar(0, 0, this.cosRotationPitch);
            this.matrixRotationPitchInverse.putScalar(1, 1, this.cosRotationPitch);
            this.matrixRotationPitchInverse.putScalar(0, 1, -this.sinRotationPitch);
            this.matrixRotationPitchInverse.putScalar(1, 0, this.sinRotationPitch);
            this.needRotationRecalc = true;
            this.needTransformationRecalc = true;
        }
    }

    public void setRotationRoll(double newRoll) {
        if (this.rotationRoll != newRoll) {
            this.rotationRoll = newRoll;
            this.cosRotationRoll = Math.cos(newRoll * Math.PI / 180.0D);
            this.sinRotationRoll = Math.sin(newRoll * Math.PI / 180.0D);
            this.matrixRotationRoll.putScalar(1, 1, this.cosRotationRoll);
            this.matrixRotationRoll.putScalar(2, 2, this.cosRotationRoll);
            this.matrixRotationRoll.putScalar(1, 2, this.sinRotationRoll);
            this.matrixRotationRoll.putScalar(2, 1, -this.sinRotationRoll);
            this.matrixRotationRollInverse.putScalar(1, 1, this.cosRotationRoll);
            this.matrixRotationRollInverse.putScalar(2, 2, this.cosRotationRoll);
            this.matrixRotationRollInverse.putScalar(1, 2, -this.sinRotationRoll);
            this.matrixRotationRollInverse.putScalar(2, 1, this.sinRotationRoll);
            this.needRotationRecalc = true;
            this.needTransformationRecalc = true;
        }
    }

    public void setScaling(double newScaling) {
        if (Math.abs(newScaling) < 1.0E-4D) {
            newScaling = 1.0E-4D;
        }

        if (this.scaling != newScaling) {
            this.scaling = newScaling;
            this.matrixScaling.putScalar(0, 0, this.scaling);
            this.matrixScaling.putScalar(1, 1, this.scaling);
            this.matrixScaling.putScalar(2, 2, this.scaling);
            this.matrixScalingInverse.putScalar(0, 0, 1.0D / this.scaling);
            this.matrixScalingInverse.putScalar(1, 1, 1.0D / this.scaling);
            this.matrixScalingInverse.putScalar(2, 2, 1.0D / this.scaling);
            this.needTransformationRecalc = true;
        }
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

    public INDArray getRotationMatrix() {
        this.recalcRotationMatrix();
        return this.matrixRotation;
    }

    public INDArray getRotationInverseMatrix() {
        this.recalcRotationMatrix();
        return this.matrixRotationInverse;
    }

    public INDArray getTransformToLocalMatrix() {
        this.recalcTransformationMatrices();
        return this.matrixTransformToLocal;
    }

    public INDArray getTransformToGlobalMatrix() {
        this.recalcTransformationMatrices();
        return this.matrixTransformToGlobal;
    }

    public DoubleBuffer getTransformToLocalMatrixDirectBuffer() {
        this.recalcTransformationMatrices();
        return (DoubleBuffer) BufferUtils.createDoubleBuffer(16)
            .put(
                this.matrixTransformToLocal.data()
                    .asNioDouble())
            .rewind();
    }

    public DoubleBuffer getTransformToGlobalMatrixDirectBuffer() {
        this.recalcTransformationMatrices();
        return (DoubleBuffer) BufferUtils.createDoubleBuffer(16)
            .put(
                this.matrixTransformToGlobal.data()
                    .asNioDouble())
            .rewind();
    }

    public Vec3 transformToLocal(Vec3 globalVec) {
        return this.transformToLocal(globalVec.xCoord, globalVec.yCoord, globalVec.zCoord);
    }

    public Vec3 transformToLocal(double globalX, double globalY, double globalZ) {
        INDArray tempVector = Nd4j.create(new double[] { globalX, globalY, globalZ, 1.0D });
        this.transformToLocal(tempVector, tempVector);
        return Vec3.createVectorHelper(tempVector.getDouble(0), tempVector.getDouble(1), tempVector.getDouble(2));
    }

    public INDArray transformToLocal(INDArray globalVectors) {
        return this.getTransformToLocalMatrix()
            .mmul(globalVectors);
    }

    public INDArray transformToLocal(INDArray globalVectors, INDArray result) {
        return this.getTransformToLocalMatrix()
            .mmuli(globalVectors, result);
    }

    public Vec3 transformToGlobal(Vec3 localVec) {
        return this.transformToGlobal(localVec.xCoord, localVec.yCoord, localVec.zCoord);
    }

    public Vec3 transformToGlobal(double localX, double localY, double localZ) {
        INDArray tempVector = Nd4j.create(new double[] { localX, localY, localZ, 1.0D });
        this.transformToGlobal(tempVector, tempVector);
        return Vec3.createVectorHelper(tempVector.getDouble(0), tempVector.getDouble(1), tempVector.getDouble(2));
    }

    public INDArray transformToGlobal(INDArray localVectors) {
        return this.getTransformToGlobalMatrix()
            .mmul(localVectors);
    }

    public INDArray transformToGlobal(INDArray localVectors, INDArray result) {
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
            INDArray tempVector = Nd4j.create(new double[] { localX, localY, localZ, 1.0D });
            this.transformLocalToOther(targetWorld, tempVector, tempVector);
            return Vec3.createVectorHelper(tempVector.getDouble(0), tempVector.getDouble(1), tempVector.getDouble(2));
        }
    }

    public INDArray transformLocalToOther(World targetWorld, INDArray localVectors) {
        if (targetWorld == null) {
            targetWorld = ((IMixinWorld) this.holderWorld).getParentWorld();
        }

        if (targetWorld == this.holderWorld) {
            return localVectors.dup();
        } else {
            INDArray tempVectors = this.transformToGlobal(localVectors);
            return !((IMixinWorld) targetWorld).isSubWorld() ? tempVectors
                : ((IMixinWorld) targetWorld).transformToLocal(tempVectors, tempVectors);
        }
    }

    public INDArray transformLocalToOther(World targetWorld, INDArray localVectors, INDArray result) {
        if (targetWorld == null) {
            targetWorld = ((IMixinWorld) this.holderWorld).getParentWorld();
        }

        if (targetWorld == this.holderWorld) {
            return result.assign(localVectors);
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
            INDArray tempVector = Nd4j.create(new double[] { otherX, otherY, otherZ, 1.0D });
            this.transformOtherToLocal(sourceWorld, tempVector, tempVector);
            return Vec3.createVectorHelper(tempVector.getDouble(0), tempVector.getDouble(1), tempVector.getDouble(2));
        }
    }

    public INDArray transformOtherToLocal(World sourceWorld, INDArray otherVectors) {
        if (sourceWorld == null) {
            sourceWorld = ((IMixinWorld) this.holderWorld).getParentWorld();
        }

        if (sourceWorld == this.holderWorld) {
            return otherVectors.dup();
        } else {
            INDArray tempVectors = otherVectors;
            if (((IMixinWorld) sourceWorld).isSubWorld()) {
                tempVectors = ((IMixinWorld) sourceWorld).transformToGlobal(otherVectors);
            }

            return this.transformToLocal(tempVectors, tempVectors);
        }
    }

    public INDArray transformOtherToLocal(World sourceWorld, INDArray otherVectors, INDArray result) {
        if (sourceWorld == null) {
            sourceWorld = ((IMixinWorld) this.holderWorld).getParentWorld();
        }

        if (sourceWorld == this.holderWorld) {
            return result.assign(otherVectors);
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
        INDArray tempVector = Nd4j.create(new double[] { localX, localY, localZ, 1.0D });
        tempVector = this.rotateToGlobal(tempVector);
        return Vec3.createVectorHelper(tempVector.getDouble(0), tempVector.getDouble(1), tempVector.getDouble(2));
    }

    public INDArray rotateToGlobal(INDArray localVectors) {
        return this.getRotationMatrix()
            .mmul(localVectors);
    }

    public Vec3 rotateToLocal(Vec3 globalVec) {
        return this.rotateToLocal(globalVec.xCoord, globalVec.yCoord, globalVec.zCoord);
    }

    public Vec3 rotateToLocal(double globalX, double globalY, double globalZ) {
        INDArray tempVector = Nd4j.create(new double[] { globalX, globalY, globalZ, 1.0D });
        tempVector = this.rotateToLocal(tempVector);
        return Vec3.createVectorHelper(tempVector.getDouble(0), tempVector.getDouble(1), tempVector.getDouble(2));
    }

    public INDArray rotateToLocal(INDArray globalVectors) {
        return this.getRotationInverseMatrix()
            .mmul(globalVectors);
    }

    public Vec3 rotateYawToGlobal(Vec3 localVec) {
        return this.rotateYawToGlobal(localVec.xCoord, localVec.yCoord, localVec.zCoord);
    }

    public Vec3 rotateYawToGlobal(double localX, double localY, double localZ) {
        INDArray tempVector = Nd4j.create(new double[] { localX, localY, localZ, 1.0D });
        tempVector = this.rotateYawToGlobal(tempVector);
        return Vec3.createVectorHelper(tempVector.getDouble(0), tempVector.getDouble(1), tempVector.getDouble(2));
    }

    public INDArray rotateYawToGlobal(INDArray localVectors) {
        return this.matrixRotationYaw.mmul(localVectors);
    }

    public Vec3 rotateYawToLocal(Vec3 globalVec) {
        return this.rotateYawToLocal(globalVec.xCoord, globalVec.yCoord, globalVec.zCoord);
    }

    public Vec3 rotateYawToLocal(double globalX, double globalY, double globalZ) {
        INDArray tempVector = Nd4j.create(new double[] { globalX, globalY, globalZ, 1.0D });
        tempVector = this.rotateYawToLocal(tempVector);
        return Vec3.createVectorHelper(tempVector.getDouble(0), tempVector.getDouble(1), tempVector.getDouble(2));
    }

    public INDArray rotateYawToLocal(INDArray globalVectors) {
        return this.matrixRotationYawInverse.mmul(globalVectors);
    }
}
