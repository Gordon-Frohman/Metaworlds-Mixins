package net.tclproject.metaworlds.patcher;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.tclproject.metaworlds.api.SubWorld;

public class SubWorldInfoHolder {

    public int subWorldId;
    public int subWorldType = 0;
    public double translationX = 0.0D;
    public double translationY = 0.0D;
    public double translationZ = 0.0D;
    public double centerX = 0.0D;
    public double centerY = 0.0D;
    public double centerZ = 0.0D;
    public double rotationYaw = 0.0D;
    public double rotationPitch = 0.0D;
    public double rotationRoll = 0.0D;
    public double scaling = 1.0D;
    public double motionX = 0.0D;
    public double motionY = 0.0D;
    public double motionZ = 0.0D;
    public double rotationYawSpeed = 0.0D;
    public double rotationPitchSpeed = 0.0D;
    public double rotationRollSpeed = 0.0D;
    public double scaleChangeRate = 0.0D;
    public ChunkCoordinates minCoordinates = new ChunkCoordinates();
    public ChunkCoordinates maxCoordinates = new ChunkCoordinates();

    public SubWorldInfoHolder() {}

    public SubWorldInfoHolder(SubWorld sourceWorld) {
        this.subWorldId = sourceWorld.getSubWorldID();
        this.subWorldType = sourceWorld.getSubWorldType();
        this.translationX = sourceWorld.getTranslationX();
        this.translationY = sourceWorld.getTranslationY();
        this.translationZ = sourceWorld.getTranslationZ();
        this.centerX = sourceWorld.getCenterX();
        this.centerY = sourceWorld.getCenterY();
        this.centerZ = sourceWorld.getCenterZ();
        this.rotationYaw = sourceWorld.getRotationYaw();
        this.rotationPitch = sourceWorld.getRotationPitch();
        this.rotationRoll = sourceWorld.getRotationRoll();
        this.scaling = sourceWorld.getScaling();
        this.motionX = sourceWorld.getMotionX();
        this.motionY = sourceWorld.getMotionY();
        this.motionZ = sourceWorld.getMotionZ();
        this.rotationYawSpeed = sourceWorld.getRotationYawSpeed();
        this.rotationPitchSpeed = sourceWorld.getRotationPitchSpeed();
        this.rotationRollSpeed = sourceWorld.getRotationRollSpeed();
        this.scaleChangeRate = sourceWorld.getScaleChangeRate();
        this.minCoordinates.posX = sourceWorld.getMinX();
        this.minCoordinates.posY = sourceWorld.getMinY();
        this.minCoordinates.posZ = sourceWorld.getMinZ();
        this.maxCoordinates.posX = sourceWorld.getMaxX();
        this.maxCoordinates.posY = sourceWorld.getMaxY();
        this.maxCoordinates.posZ = sourceWorld.getMaxZ();
    }

    public SubWorldInfoHolder(SubWorldInfoHolder sourceWorld) {
        this.subWorldId = sourceWorld.subWorldId;
        this.subWorldType = sourceWorld.subWorldType;
        this.translationX = sourceWorld.translationX;
        this.translationY = sourceWorld.translationY;
        this.translationZ = sourceWorld.translationZ;
        this.centerX = sourceWorld.centerX;
        this.centerY = sourceWorld.centerY;
        this.centerZ = sourceWorld.centerZ;
        this.rotationYaw = sourceWorld.rotationYaw;
        this.rotationPitch = sourceWorld.rotationPitch;
        this.rotationRoll = sourceWorld.rotationRoll;
        this.scaling = sourceWorld.scaling;
        this.motionX = sourceWorld.motionX;
        this.motionY = sourceWorld.motionY;
        this.motionZ = sourceWorld.motionZ;
        this.rotationYawSpeed = sourceWorld.rotationYawSpeed;
        this.rotationPitchSpeed = sourceWorld.rotationPitchSpeed;
        this.rotationRollSpeed = sourceWorld.rotationRollSpeed;
        this.scaleChangeRate = sourceWorld.scaleChangeRate;
        this.minCoordinates.posX = sourceWorld.minCoordinates.posX;
        this.minCoordinates.posY = sourceWorld.minCoordinates.posY;
        this.minCoordinates.posZ = sourceWorld.minCoordinates.posZ;
        this.maxCoordinates.posX = sourceWorld.maxCoordinates.posX;
        this.maxCoordinates.posY = sourceWorld.maxCoordinates.posY;
        this.maxCoordinates.posZ = sourceWorld.maxCoordinates.posZ;
    }

    public SubWorldInfoHolder(NBTTagCompound sourceNBT) {
        this.subWorldId = sourceNBT.getInteger("subWorldId");
        if (sourceNBT.hasKey("subWorldType", 3)) {
            this.subWorldType = sourceNBT.getInteger("subWorldType");
        }

        this.translationX = sourceNBT.getDouble("positionX");
        this.translationY = sourceNBT.getDouble("positionY");
        this.translationZ = sourceNBT.getDouble("positionZ");
        this.centerX = sourceNBT.getDouble("centerX");
        this.centerY = sourceNBT.getDouble("centerY");
        this.centerZ = sourceNBT.getDouble("centerZ");
        this.rotationYaw = sourceNBT.getDouble("rotationYaw");
        this.rotationPitch = sourceNBT.getDouble("rotationPitch");
        this.rotationRoll = sourceNBT.getDouble("rotationRoll");
        this.scaling = sourceNBT.getDouble("scaling");
        if (sourceNBT.hasKey("motionX", 6)) {
            this.motionX = sourceNBT.getDouble("motionX");
        }

        if (sourceNBT.hasKey("motionY", 6)) {
            this.motionY = sourceNBT.getDouble("motionY");
        }

        if (sourceNBT.hasKey("motionZ", 6)) {
            this.motionZ = sourceNBT.getDouble("motionZ");
        }

        if (sourceNBT.hasKey("rotationYawSpeed", 6)) {
            this.rotationYawSpeed = sourceNBT.getDouble("rotationYawSpeed");
        }

        if (sourceNBT.hasKey("rotationPitchSpeed", 6)) {
            this.rotationPitchSpeed = sourceNBT.getDouble("rotationPitchSpeed");
        }

        if (sourceNBT.hasKey("rotationRollSpeed", 6)) {
            this.rotationRollSpeed = sourceNBT.getDouble("rotationRollSpeed");
        }

        if (sourceNBT.hasKey("scaleChangeRate", 6)) {
            this.scaleChangeRate = sourceNBT.getDouble("scaleChangeRate");
        }

        this.minCoordinates.posX = sourceNBT.getInteger("minX");
        this.minCoordinates.posY = sourceNBT.getInteger("minY");
        this.minCoordinates.posZ = sourceNBT.getInteger("minZ");
        this.maxCoordinates.posX = sourceNBT.getInteger("maxX");
        this.maxCoordinates.posY = sourceNBT.getInteger("maxY");
        this.maxCoordinates.posZ = sourceNBT.getInteger("maxZ");
    }

    public SubWorldInfoHolder(int parSubWorldId) {
        this.subWorldId = parSubWorldId;
    }

    public void writeToNBT(NBTTagCompound targetNBT) {
        targetNBT.setInteger("subWorldId", this.subWorldId);
        targetNBT.setInteger("subWorldType", this.subWorldType);
        targetNBT.setDouble("positionX", this.translationX);
        targetNBT.setDouble("positionY", this.translationY);
        targetNBT.setDouble("positionZ", this.translationZ);
        targetNBT.setDouble("centerX", this.centerX);
        targetNBT.setDouble("centerY", this.centerY);
        targetNBT.setDouble("centerZ", this.centerZ);
        targetNBT.setDouble("rotationYaw", this.rotationYaw);
        targetNBT.setDouble("rotationPitch", this.rotationPitch);
        targetNBT.setDouble("rotationRoll", this.rotationRoll);
        targetNBT.setDouble("scaling", this.scaling);
        targetNBT.setDouble("motionX", this.motionX);
        targetNBT.setDouble("motionY", this.motionY);
        targetNBT.setDouble("motionZ", this.motionZ);
        targetNBT.setDouble("rotationYawSpeed", this.rotationYawSpeed);
        targetNBT.setDouble("rotationPitchSpeed", this.rotationPitchSpeed);
        targetNBT.setDouble("rotationRollSpeed", this.rotationRollSpeed);
        targetNBT.setDouble("scaleChangeRate", this.scaleChangeRate);
        targetNBT.setInteger("minX", this.minCoordinates.posX);
        targetNBT.setInteger("minY", this.minCoordinates.posY);
        targetNBT.setInteger("minZ", this.minCoordinates.posZ);
        targetNBT.setInteger("maxX", this.maxCoordinates.posX);
        targetNBT.setInteger("maxY", this.maxCoordinates.posY);
        targetNBT.setInteger("maxZ", this.maxCoordinates.posZ);
    }

    public void applyToSubWorld(SubWorld targetWorld) {
        targetWorld.setSubWorldType(this.subWorldType);
        targetWorld.setCenter(this.centerX, this.centerY, this.centerZ);
        targetWorld.setTranslation(this.translationX, this.translationY, this.translationZ);
        targetWorld.setRotationYaw(this.rotationYaw);
        targetWorld.setRotationPitch(this.rotationPitch);
        targetWorld.setRotationRoll(this.rotationRoll);
        targetWorld.setScaling(this.scaling);
        targetWorld.setMotion(this.motionX, this.motionY, this.motionZ);
        targetWorld.setRotationYawSpeed(this.rotationYawSpeed);
        targetWorld.setRotationPitchSpeed(this.rotationPitchSpeed);
        targetWorld.setRotationRollSpeed(this.rotationRollSpeed);
        targetWorld.setScaleChangeRate(this.scaleChangeRate);
        targetWorld.setBoundaries(
            this.minCoordinates.posX,
            this.minCoordinates.posY,
            this.minCoordinates.posZ,
            this.maxCoordinates.posX,
            this.maxCoordinates.posY,
            this.maxCoordinates.posZ);
    }
}
