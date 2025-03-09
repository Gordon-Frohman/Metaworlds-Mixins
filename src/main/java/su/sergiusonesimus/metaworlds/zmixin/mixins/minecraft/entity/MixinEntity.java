package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ReportedException;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.client.multiplayer.SubWorldClient;
import su.sergiusonesimus.metaworlds.entity.player.EntityPlayerProxy;
import su.sergiusonesimus.metaworlds.util.OrientedBB;
import su.sergiusonesimus.metaworlds.world.SubWorldServer;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.entity.IMixinEntity;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorldIntermediate;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.util.IMixinAxisAlignedBB;

@Mixin(Entity.class)
public abstract class MixinEntity implements Comparable, IMixinEntity {

    @Shadow(remap = true)
    public boolean noClip;

    @Shadow(remap = true)
    public AxisAlignedBB boundingBox;

    @Shadow(remap = true)
    public double posX;

    @Shadow(remap = true)
    public double posY;

    @Shadow(remap = true)
    public double posZ;

    @Shadow(remap = true)
    protected float yOffset;

    @Shadow(remap = true)
    public float ySize;

    @Shadow(remap = true)
    protected World worldObj;

    @Shadow(remap = true)
    private boolean isInWeb;

    @Shadow(remap = true)
    private double motionX;

    @Shadow(remap = true)
    private double motionY;

    @Shadow(remap = true)
    private double motionZ;

    @Shadow(remap = true)
    private boolean onGround;

    @Shadow(remap = true)
    public boolean field_70135_K;

    @Shadow(remap = true)
    public float stepHeight;

    @Shadow(remap = true)
    public boolean isCollidedHorizontally;

    @Shadow(remap = true)
    public boolean isCollidedVertically;

    @Shadow(remap = true)
    public boolean isCollided;

    @Shadow(remap = true)
    public Entity ridingEntity;

    @Shadow(remap = true)
    public float prevDistanceWalkedModified;

    @Shadow(remap = true)
    public float distanceWalkedModified;

    @Shadow(remap = true)
    public float distanceWalkedOnStepModified;

    @Shadow(remap = true)
    public float fallDistance;

    @Shadow(remap = true)
    private int nextStepDistance;

    @Shadow(remap = true)
    protected Random rand;

    @Shadow(remap = true)
    public int fireResistance;

    @Shadow(remap = true)
    private int fire;

    @Shadow(remap = true)
    protected boolean inWater;

    @Shadow(remap = true)
    private boolean firstUpdate;

    @Shadow(remap = true)
    public float width;

    @Shadow(remap = true)
    public double prevPosX;

    @Shadow(remap = true)
    public double prevPosY;

    @Shadow(remap = true)
    public double prevPosZ;

    @Shadow(remap = true)
    public float rotationYaw;

    @Shadow(remap = true)
    public float rotationPitch;

    @Shadow(remap = true)
    public float prevRotationYaw;

    @Shadow(remap = true)
    public float prevRotationPitch;

    @Shadow(remap = true)
    protected int portalCounter;

    @Shadow(remap = true)
    public int timeUntilPortal;

    @Shadow(remap = true)
    protected boolean inPortal;

    @Shadow(remap = true)
    protected boolean isImmuneToFire;

    @Shadow(remap = true)
    public int hurtResistantTime;

    // TODO

    @Shadow(remap = true)
    public boolean isEntityInvulnerable() {
        return false;
    }

    @Shadow(remap = true)
    protected void kill() {}

    @Shadow(remap = true)
    protected void setOnFireFromLava() {}

    @Shadow(remap = true)
    public boolean handleLavaMovement() {
        return false;
    }

    @Shadow(remap = true)
    public boolean attackEntityFrom(DamageSource source, float amount) {
        return false;
    }

    @Shadow(remap = true)
    public boolean isSprinting() {
        return false;
    }

    @Shadow(remap = true)
    public void travelToDimension(int dimensionId) {}

    @Shadow(remap = true)
    public int getPortalCooldown() {
        return 300;
    }

    @Shadow(remap = true)
    public int getMaxInPortalTime() {
        return 0;
    }

    @Shadow(remap = true)
    protected void setFlag(int flag, boolean set) {}

    @Shadow(remap = true)
    public boolean shouldRenderInPass(int pass) {
        return false;
    }

    @Shadow(remap = true)
    protected void setPosition(double par1, double par3, double par5) {}

    @Shadow(remap = true)
    public void setRotation(float par1, float par2) {}

    @Shadow(remap = true)
    public void setPositionAndRotation(double par1, double par3, double par5, float par7, float par8) {}

    @Shadow(remap = true)
    public void setAngles(float par1, float par2) {}

    @Shadow(remap = true)
    public void setLocationAndAngles(double par1, double par3, double par5, float par7, float par8) {}

    @Shadow(remap = true)
    public abstract double getDistanceSq(double p_70092_1_, double p_70092_3_, double p_70092_5_);

    @Shadow(remap = true)
    protected abstract String getSplashSound();

    @Shadow(remap = true)
    public abstract void setFire(int p_70015_1_);

    @Shadow(remap = true)
    protected abstract void dealFireDamage(int p_70081_1_);

    @Shadow(remap = true)
    public abstract boolean isWet();

    @Shadow(remap = true)
    public abstract void addEntityCrashInfo(CrashReportCategory p_85029_1_);

    @Shadow(remap = true)
    public abstract void playSound(String p_85030_1_, float p_85030_2_, float p_85030_3_);

    @Shadow(remap = true)
    protected abstract String getSwimSound();

    @Shadow(remap = true)
    protected abstract boolean isSneaking();

    @Shadow(remap = true)
    protected abstract boolean canTriggerWalking();

    @Shadow(remap = true)
    protected void updateFallState(double p_70064_1_, boolean p_70064_3_) {}

    @Shadow(remap = true)
    public abstract boolean isInWater();

    public World worldBelowFeet;
    protected byte tractionLoss;
    protected boolean losingTraction = false;
    public int serverPosXOnSubWorld;
    public int serverPosYOnSubWorld;
    public int serverPosZOnSubWorld;
    public HashMap<Integer, EntityPlayerProxy> playerProxyMap = ((Entity) (Object) this) instanceof EntityPlayer
        ? new HashMap()
        : null;

    public int getServerPosXOnSubWorld() {
        return serverPosXOnSubWorld;
    }

    public int getServerPosYOnSubWorld() {
        return serverPosYOnSubWorld;
    }

    public int getServerPosZOnSubWorld() {
        return serverPosZOnSubWorld;
    }

    public void setServerPosXOnSubWorld(int serverPosXOnSubWorld) {
        this.serverPosXOnSubWorld = serverPosXOnSubWorld;
    }

    public void setServerPosYOnSubWorld(int serverPosYOnSubWorld) {
        this.serverPosYOnSubWorld = serverPosYOnSubWorld;
    }

    public void setServerPosZOnSubWorld(int serverPosZOnSubWorld) {
        this.serverPosZOnSubWorld = serverPosZOnSubWorld;
    }

    public double getTractionFactor() {
        return 1.0D - (double) this.tractionLoss * (double) this.tractionLoss / 400.0D;
    }

    public byte getTractionLossTicks() {
        return this.tractionLoss;
    }

    public HashMap getPlayerProxyMap() {
        return playerProxyMap;
    }

    public void setTractionTickCount(byte newTickCount) {
        this.tractionLoss = newTickCount;
    }

    public boolean isLosingTraction() {
        return this.losingTraction;
    }

    public void slowlyRemoveWorldBelowFeet() {
        this.losingTraction = true;
    }

    public void setWorldBelowFeet(World newWorldBelowFeet) {
        this.losingTraction = false;
        this.tractionLoss = 0;
        if (newWorldBelowFeet != this.worldBelowFeet) {
            if (this.worldBelowFeet != null && ((IMixinWorld) this.worldBelowFeet).isSubWorld()) {
                ((SubWorld) this.worldBelowFeet).unregisterEntityToDrag((Entity) (Object) this);
            }

            this.worldBelowFeet = newWorldBelowFeet;
            if (this.worldBelowFeet != null && ((IMixinWorld) this.worldBelowFeet).isSubWorld()) {
                ((SubWorld) this.worldBelowFeet).registerEntityToDrag((Entity) (Object) this);
            }

            if (this.worldBelowFeet != ((Entity) (Object) this).worldObj
                && ((IMixinWorld) ((Entity) (Object) this).worldObj).isSubWorld()) {
                ((SubWorld) ((Entity) (Object) this).worldObj).registerDetachedEntity((Entity) (Object) this);
            } else if (this.worldBelowFeet == ((Entity) (Object) this).worldObj
                && ((IMixinWorld) ((Entity) (Object) this).worldObj).isSubWorld()) {
                    ((SubWorld) ((Entity) (Object) this).worldObj).unregisterDetachedEntity((Entity) (Object) this);
                }
        }
    }

    public World getWorldBelowFeet() {
        return this.worldBelowFeet == null ? ((Entity) (Object) this).worldObj : this.worldBelowFeet;
    }

    public int compareTo(Object par1Obj) {
        return ((Object) this) instanceof Entity && par1Obj instanceof Entity
            ? ((Entity) par1Obj).getEntityId() - ((Entity) (Object) this).getEntityId()
            : 0;
    }

    public Vec3 getGlobalPos() {
        return ((IMixinWorld) ((Entity) (Object) this).worldObj).transformToGlobal((Entity) (Object) this);
    }

    public Vec3 getLocalPos(World referenceWorld) {
        Entity eThis = ((Entity) (Object) this);
        return referenceWorld == null && eThis.worldObj == null
            ? Vec3.createVectorHelper(eThis.posX, eThis.posY, eThis.posZ)
            : (referenceWorld != eThis.worldObj && referenceWorld != null
                ? ((IMixinWorld) referenceWorld).transformToLocal(this.getGlobalPos())
                : Vec3.createVectorHelper(eThis.posX, eThis.posY, eThis.posZ));
    }

    public double getGlobalRotationYaw() {
        return (double) ((Entity) (Object) this).rotationYaw
            - ((IMixinWorld) ((Entity) (Object) this).worldObj).getRotationYaw();
    }

    public double getDistanceSq(double par1, double par3, double par5, World targetWorld) {
        Vec3 thisVecGlobal = this.getGlobalPos();
        Vec3 targetVecGlobal = ((IMixinWorld) targetWorld).transformToGlobal(par1, par3, par5);
        return targetVecGlobal.squareDistanceTo(thisVecGlobal);
    }

    public boolean getIsJumping() {
        return ((Entity) (Object) this) instanceof EntityLivingBase ? (((EntityLivingBase) (Object) this)).isJumping
            : false;
    }

    public EntityPlayer getProxyPlayer(World subWorld) {
        return this.getProxyPlayer(((IMixinWorld) subWorld).getSubWorldID());
    }

    public EntityPlayer getProxyPlayer(int subWorldID) {
        return subWorldID == 0 ? ((EntityPlayer) (Object) this)
            : (EntityPlayer) this.playerProxyMap.get(Integer.valueOf(subWorldID));
    }

    /**
     * Will get destroyed next tick.
     */
    @Inject(method = "setDead", at = @At("TAIL"))
    public void setDead(CallbackInfo ci) {
        this.setWorldBelowFeet(null);
    }

    /**
     * Tries to moves the entity by the passed in displacement. Args: x, y, z
     */
    @Overwrite
    public void moveEntity(double x, double y, double z) {
        if (this.noClip) {
            this.boundingBox.offset(x, y, z);
            this.posX = (this.boundingBox.minX + this.boundingBox.maxX) / 2.0D;
            this.posY = this.boundingBox.minY + (double) this.yOffset - (double) this.ySize;
            this.posZ = (this.boundingBox.minZ + this.boundingBox.maxZ) / 2.0D;
        } else {
            this.worldObj.theProfiler.startSection("move");
            this.ySize *= 0.4F;
            double entityX = this.posX;
            double entityY = this.posY;
            double entityZ = this.posZ;

            if (this.isInWeb) {
                this.isInWeb = false;
                x *= 0.25D;
                y *= 0.05000000074505806D;
                z *= 0.25D;
                this.motionX = 0.0D;
                this.motionY = 0.0D;
                this.motionZ = 0.0D;
            }

            double xStored = x;
            double yStored = y;
            double zStored = z;
            AxisAlignedBB axisalignedbb = this.boundingBox.copy();
            boolean flag = this.onGround && this.isSneaking() && ((Entity) (Object) this) instanceof EntityPlayer;

            if (flag) {
                double d9;

                for (d9 = 0.05D; x != 0.0D && this.worldObj
                    .getCollidingBoundingBoxes(
                        (Entity) (Object) this,
                        this.boundingBox.getOffsetBoundingBox(x, -1.0D, 0.0D))
                    .isEmpty(); xStored = x) {
                    if (x < d9 && x >= -d9) {
                        x = 0.0D;
                    } else if (x > 0.0D) {
                        x -= d9;
                    } else {
                        x += d9;
                    }
                }

                for (; z != 0.0D && this.worldObj
                    .getCollidingBoundingBoxes(
                        (Entity) (Object) this,
                        this.boundingBox.getOffsetBoundingBox(0.0D, -1.0D, z))
                    .isEmpty(); zStored = z) {
                    if (z < d9 && z >= -d9) {
                        z = 0.0D;
                    } else if (z > 0.0D) {
                        z -= d9;
                    } else {
                        z += d9;
                    }
                }

                while (x != 0.0D && z != 0.0D
                    && this.worldObj
                        .getCollidingBoundingBoxes(
                            (Entity) (Object) this,
                            this.boundingBox.getOffsetBoundingBox(x, -1.0D, z))
                        .isEmpty()) {
                    if (x < d9 && x >= -d9) {
                        x = 0.0D;
                    } else if (x > 0.0D) {
                        x -= d9;
                    } else {
                        x += d9;
                    }

                    if (z < d9 && z >= -d9) {
                        z = 0.0D;
                    } else if (z > 0.0D) {
                        z -= d9;
                    } else {
                        z += d9;
                    }

                    xStored = x;
                    zStored = z;
                }
            }

            List list = this.worldObj
                .getCollidingBoundingBoxes((Entity) (Object) this, this.boundingBox.addCoord(x, y, z));

            World newWorldBelowFeet = this.worldObj;
            double yOffset;
            AxisAlignedBB curAABB;

            for (int i = 0; i < list.size(); ++i) {
                curAABB = (AxisAlignedBB) list.get(i);

                yOffset = (curAABB).calculateYOffset(this.boundingBox.addCoord(x, 0, z), y);
                if (yOffset != y) {
                    y = yOffset;
                    if (curAABB instanceof OrientedBB) newWorldBelowFeet = ((OrientedBB) curAABB).lastTransformedBy;
                    else newWorldBelowFeet = this.worldObj;
                }
            }

            this.boundingBox.offset(0.0D, y, 0.0D);

            if (!this.field_70135_K && yStored != y) {
                z = 0.0D;
                y = 0.0D;
                x = 0.0D;
            }

            boolean flag1 = this.onGround || yStored != y && yStored < 0.0D;
            int j;

            for (j = 0; j < list.size(); ++j) {
                x = ((AxisAlignedBB) list.get(j)).calculateXOffset(this.boundingBox, x);
            }

            this.boundingBox.offset(x, 0.0D, 0.0D);

            if (!this.field_70135_K && xStored != x) {
                z = 0.0D;
                y = 0.0D;
                x = 0.0D;
            }

            for (j = 0; j < list.size(); ++j) {
                z = ((AxisAlignedBB) list.get(j)).calculateZOffset(this.boundingBox, z);
            }

            this.boundingBox.offset(0.0D, 0.0D, z);

            if (!this.field_70135_K && zStored != z) {
                z = 0.0D;
                y = 0.0D;
                x = 0.0D;
            }

            double yStoredLocal;
            double zStoredLocal;
            int k;
            double xStoredLocal;

            if (this.stepHeight > 0.0F && flag1 && (flag || this.ySize < 0.05F) && (xStored != x || zStored != z)) {
                xStoredLocal = x;
                yStoredLocal = y;
                zStoredLocal = z;
                x = xStored;
                y = (double) this.stepHeight;
                z = zStored;
                AxisAlignedBB axisalignedbb1 = this.boundingBox.copy();
                this.boundingBox.setBB(axisalignedbb);
                list = this.worldObj
                    .getCollidingBoundingBoxes((Entity) (Object) this, this.boundingBox.addCoord(xStored, y, zStored));

                for (k = 0; k < list.size(); ++k) {
                    y = ((AxisAlignedBB) list.get(k)).calculateYOffset(this.boundingBox, y);
                }

                this.boundingBox.offset(0.0D, y, 0.0D);

                if (!this.field_70135_K && yStored != y) {
                    z = 0.0D;
                    y = 0.0D;
                    x = 0.0D;
                }

                for (k = 0; k < list.size(); ++k) {
                    x = ((AxisAlignedBB) list.get(k)).calculateXOffset(this.boundingBox, x);
                }

                this.boundingBox.offset(x, 0.0D, 0.0D);

                if (!this.field_70135_K && xStored != x) {
                    z = 0.0D;
                    y = 0.0D;
                    x = 0.0D;
                }

                for (k = 0; k < list.size(); ++k) {
                    z = ((AxisAlignedBB) list.get(k)).calculateZOffset(this.boundingBox, z);
                }

                this.boundingBox.offset(0.0D, 0.0D, z);

                if (!this.field_70135_K && zStored != z) {
                    z = 0.0D;
                    y = 0.0D;
                    x = 0.0D;
                }

                if (!this.field_70135_K && yStored != y) {
                    z = 0.0D;
                    y = 0.0D;
                    x = 0.0D;
                } else {
                    y = (double) (-this.stepHeight);

                    for (k = 0; k < list.size(); ++k) {
                        y = ((AxisAlignedBB) list.get(k)).calculateYOffset(this.boundingBox, y);
                    }

                    this.boundingBox.offset(0.0D, y, 0.0D);
                }

                if (xStoredLocal * xStoredLocal + zStoredLocal * zStoredLocal >= x * x + z * z && xStoredLocal * x > 0
                    && zStoredLocal * z > 0) {
                    x = xStoredLocal;
                    y = yStoredLocal;
                    z = zStoredLocal;
                    this.boundingBox.setBB(axisalignedbb1);
                }
            }

            this.worldObj.theProfiler.endSection();
            this.worldObj.theProfiler.startSection("rest");
            this.posX = (this.boundingBox.minX + this.boundingBox.maxX) / 2.0D;
            this.posY = this.boundingBox.minY + (double) this.yOffset - (double) this.ySize;
            this.posZ = (this.boundingBox.minZ + this.boundingBox.maxZ) / 2.0D;
            this.isCollidedHorizontally = xStored != x || zStored != z;
            this.isCollidedVertically = yStored != y;
            this.onGround = yStored != y && yStored < 0.0D;

            if (this.onGround) this.setWorldBelowFeet(newWorldBelowFeet);
            else if (yStored != 0.0d) this.slowlyRemoveWorldBelowFeet();

            this.isCollided = this.isCollidedHorizontally || this.isCollidedVertically;
            this.updateFallState(y, this.onGround);

            if (xStored != x) {
                this.motionX = 0.0D;
            }

            if (yStored != y) {
                this.motionY = 0.0D;
            }

            if (zStored != z) {
                this.motionZ = 0.0D;
            }

            xStoredLocal = this.posX - entityX;
            yStoredLocal = this.posY - entityY;
            zStoredLocal = this.posZ - entityZ;

            if (this.canTriggerWalking() && !flag && this.ridingEntity == null) {
                double xCoord = this.posX;
                double yCoord = this.posY - 0.20000000298023224D - (double) this.yOffset;
                double zCoord = this.posZ;
                World targetWorld = this.worldBelowFeet == null ? this.worldObj : this.worldBelowFeet;
                if (targetWorld != this.worldObj) {
                    Vec3 localCoords = ((IMixinWorld) targetWorld)
                        .transformToLocal(Vec3.createVectorHelper(xCoord, yCoord, zCoord));
                    xCoord = localCoords.xCoord;
                    yCoord = localCoords.yCoord;
                    zCoord = localCoords.zCoord;
                }
                int j1 = MathHelper.floor_double(xCoord);
                k = MathHelper.floor_double(yCoord);
                int l = MathHelper.floor_double(zCoord);
                Block block = targetWorld.getBlock(j1, k, l);
                int i1 = targetWorld.getBlock(j1, k - 1, l)
                    .getRenderType();

                if (i1 == 11 || i1 == 32 || i1 == 21) {
                    block = targetWorld.getBlock(j1, k - 1, l);
                }

                if (block != Blocks.ladder) {
                    yStoredLocal = 0.0D;
                }

                this.distanceWalkedModified = (float) ((double) this.distanceWalkedModified
                    + (double) MathHelper.sqrt_double(xStoredLocal * xStoredLocal + zStoredLocal * zStoredLocal)
                        * 0.6D);
                this.distanceWalkedOnStepModified = (float) ((double) this.distanceWalkedOnStepModified
                    + (double) MathHelper.sqrt_double(
                        xStoredLocal * xStoredLocal + yStoredLocal * yStoredLocal + zStoredLocal * zStoredLocal)
                        * 0.6D);

                if (this.distanceWalkedOnStepModified > (float) this.nextStepDistance
                    && block.getMaterial() != Material.air) {
                    this.nextStepDistance = (int) this.distanceWalkedOnStepModified + 1;

                    if (this.isInWater()) {
                        float f = MathHelper.sqrt_double(
                            this.motionX * this.motionX * 0.20000000298023224D + this.motionY * this.motionY
                                + this.motionZ * this.motionZ * 0.20000000298023224D)
                            * 0.35F;

                        if (f > 1.0F) {
                            f = 1.0F;
                        }

                        this.playSound(
                            this.getSwimSound(),
                            f,
                            1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F);
                    }

                    this.func_145780_a(j1, k, l, block);
                    block.onEntityWalking(targetWorld, j1, k, l, (Entity) (Object) this);
                }
            }

            try {
                this.func_145775_I();
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Checking entity block collision");
                CrashReportCategory crashreportcategory = crashreport
                    .makeCategory("Entity being checked for collision");
                this.addEntityCrashInfo(crashreportcategory);
                throw new ReportedException(crashreport);
            }

            boolean flag2 = this.isWet();

            if (this.worldObj.func_147470_e(this.boundingBox.contract(0.001D, 0.001D, 0.001D))) {
                this.dealFireDamage(1);

                if (!flag2) {
                    ++this.fire;

                    if (this.fire == 0) {
                        this.setFire(8);
                    }
                }
            } else if (this.fire <= 0) {
                this.fire = -this.fireResistance;
            }

            if (flag2 && this.fire > 0) {
                this.playSound("random.fizz", 0.7F, 1.6F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F);
                this.fire = -this.fireResistance;
            }

            this.worldObj.theProfiler.endSection();
        }
    }

    @Overwrite
    protected void func_145780_a(int x, int y, int z, Block blockIn) {
        Block.SoundType soundtype = blockIn.stepSound;
        World targetWorld = this.worldBelowFeet == null ? this.worldObj : this.worldBelowFeet;

        if (targetWorld.getBlock(x, y + 1, z) == Blocks.snow_layer) {
            soundtype = Blocks.snow_layer.stepSound;
            this.playSound(soundtype.getStepResourcePath(), soundtype.getVolume() * 0.15F, soundtype.getPitch());
        } else if (!blockIn.getMaterial()
            .isLiquid()) {
                this.playSound(soundtype.getStepResourcePath(), soundtype.getVolume() * 0.15F, soundtype.getPitch());
            }
    }

    @Overwrite
    protected void func_145775_I() {

        AxisAlignedBB globalBB = ((IMixinAxisAlignedBB) this.boundingBox)
            .getTransformedToGlobalBoundingBox(this.worldObj);

        for (World curSubWorld : ((IMixinWorld) ((IMixinWorld) this.worldObj).getParentWorld()).getWorlds()) {
            AxisAlignedBB localBB = ((IMixinAxisAlignedBB) globalBB).getTransformedToLocalBoundingBox(curSubWorld);

            if (((IMixinWorld) curSubWorld).isSubWorld()) {
                SubWorld curSubWorldObj = (SubWorld) curSubWorld;
                localBB.minX = Math.max(localBB.minX, curSubWorldObj.getMinX());
                localBB.maxX = Math.min(localBB.maxX, curSubWorldObj.getMaxX());
                localBB.minY = Math.max(localBB.minY, curSubWorldObj.getMinY());
                localBB.maxY = Math.min(localBB.maxY, curSubWorldObj.getMaxY());
                localBB.minZ = Math.max(localBB.minZ, curSubWorldObj.getMinZ());
                localBB.maxZ = Math.min(localBB.maxZ, curSubWorldObj.getMaxZ());
            }
            int i = MathHelper.floor_double(localBB.minX + 0.001D);
            int j = MathHelper.floor_double(localBB.minY + 0.001D);
            int k = MathHelper.floor_double(localBB.minZ + 0.001D);
            int l = MathHelper.floor_double(localBB.maxX - 0.001D);
            int i1 = MathHelper.floor_double(localBB.maxY - 0.001D);
            int j1 = MathHelper.floor_double(localBB.maxZ - 0.001D);

            if (curSubWorld.checkChunksExist(i, j, k, l, i1, j1)) {
                for (int k1 = i; k1 <= l; ++k1) {
                    for (int l1 = j; l1 <= i1; ++l1) {
                        for (int i2 = k; i2 <= j1; ++i2) {
                            Block block = curSubWorld.getBlock(k1, l1, i2);
                            try {
                                block.onEntityCollidedWithBlock(curSubWorld, k1, l1, i2, (Entity) (Object) this);
                            } catch (Throwable throwable) {
                                CrashReport crashreport = CrashReport
                                    .makeCrashReport(throwable, "Colliding entity with block");
                                CrashReportCategory crashreportcategory = crashreport
                                    .makeCategory("Block being collided with");
                                CrashReportCategory.func_147153_a(
                                    crashreportcategory,
                                    k1,
                                    l1,
                                    i2,
                                    block,
                                    curSubWorld.getBlockMetadata(k1, l1, i2));
                                throw new ReportedException(crashreport);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns if this entity is in water and will end up adding the waters velocity to the entity
     */
    @Overwrite
    public boolean handleWaterMovement() {
        boolean waterFound = false;
        for (World curSubWorld : ((IMixinWorld) this.worldObj).getWorlds()) {
            if (curSubWorld.handleMaterialAcceleration(
                this.boundingBox.expand(0.0D, -0.4000000059604645D, 0.0D)
                    .contract(0.001D, 0.001D, 0.001D),
                Material.water,
                (Entity) (Object) this)) {
                if (!this.inWater && !this.firstUpdate) {
                    float f = MathHelper.sqrt_double(
                        this.motionX * this.motionX * 0.20000000298023224D + this.motionY * this.motionY
                            + this.motionZ * this.motionZ * 0.20000000298023224D)
                        * 0.2F;
                    if (f > 1.0F) {
                        f = 1.0F;
                    }

                    this.playSound(
                        this.getSplashSound(),
                        f,
                        1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F);
                    float f1 = (float) MathHelper.floor_double(this.boundingBox.minY);

                    int i;
                    float f2;
                    float f3;
                    for (i = 0; (float) i < 1.0F + this.width * 20.0F; ++i) {
                        f2 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width;
                        f3 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width;
                        this.worldObj.spawnParticle(
                            "bubble",
                            this.posX + (double) f2,
                            (double) (f1 + 1.0F),
                            this.posZ + (double) f3,
                            this.motionX,
                            this.motionY - (double) (this.rand.nextFloat() * 0.2F),
                            this.motionZ);
                    }
                    for (i = 0; (float) i < 1.0F + this.width * 20.0F; ++i) {
                        f2 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width;
                        f3 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width;
                        this.worldObj.spawnParticle(
                            "splash",
                            this.posX + (double) f2,
                            (double) (f1 + 1.0F),
                            this.posZ + (double) f3,
                            this.motionX,
                            this.motionY,
                            this.motionZ);
                    }
                }

                this.fallDistance = 0.0F;
                this.inWater = true;
                this.fire = 0;
                waterFound = true;
                break;
            }
        }

        if (!waterFound) {
            this.inWater = false;
        }

        return this.inWater;
    }

    /**
     * Gets the distance to the position. Args: x, y, z
     */
    @Overwrite
    public double getDistance(double p_70011_1_, double p_70011_3_, double p_70011_5_) {
        return (double) MathHelper.sqrt_double(getDistanceSq(p_70011_1_, p_70011_3_, p_70011_5_));
        /*
         * double d3 = this.posX - p_70011_1_;
         * double d4 = this.posY - p_70011_3_;
         * double d5 = this.posZ - p_70011_5_;
         * return (double)MathHelper.sqrt_double(d3 * d3 + d4 * d4 + d5 * d5);
         */
    }

    @Inject(method = "setSneaking(Z)V", at = @At("TAIL"))
    public void setSneaking(boolean sneaking, CallbackInfo ci) {
        if ((Entity) (Object) this instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) (Object) this;
            for (World subworld : ((IMixinWorld) this.worldObj).getSubWorlds()) {
                EntityPlayer proxy = (EntityPlayer) ((IMixinEntity) player).getProxyPlayer(subworld);
                proxy.setFlag(1, sneaking);
            }
        }
    }

    /**
     * Gets called every tick from main Entity class
     */
    @Overwrite
    public void onEntityUpdate() {
        this.worldObj.theProfiler.startSection("entityBaseTick");

        if (this.ridingEntity != null && this.ridingEntity.isDead) {
            this.ridingEntity = null;
        }

        this.prevDistanceWalkedModified = this.distanceWalkedModified;
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.prevRotationPitch = this.rotationPitch;
        this.prevRotationYaw = this.rotationYaw;
        int i;

        if (!this.worldObj.isRemote && this.worldObj instanceof WorldServer) {
            this.worldObj.theProfiler.startSection("portal");
            MinecraftServer minecraftserver = ((WorldServer) this.worldObj).func_73046_m();
            i = this.getMaxInPortalTime();

            if (this.inPortal) {
                if (minecraftserver.getAllowNether()) {
                    if (this.ridingEntity == null && this.portalCounter++ >= i) {
                        this.portalCounter = i;
                        this.timeUntilPortal = this.getPortalCooldown();
                        byte b0;

                        if (this.worldObj.provider.dimensionId == -1) {
                            b0 = 0;
                        } else {
                            b0 = -1;
                        }

                        this.travelToDimension(b0);
                    }

                    this.inPortal = false;
                }
            } else {
                if (this.portalCounter > 0) {
                    this.portalCounter -= 4;
                }

                if (this.portalCounter < 0) {
                    this.portalCounter = 0;
                }
            }

            if (this.timeUntilPortal > 0) {
                --this.timeUntilPortal;
            }

            this.worldObj.theProfiler.endSection();
        }

        if (this.isSprinting() && !this.isInWater()) {
            int j = MathHelper.floor_double(this.posX);
            i = MathHelper.floor_double(this.posY - 0.20000000298023224D - (double) this.yOffset);
            int k = MathHelper.floor_double(this.posZ);
            Block block = this.worldObj.getBlock(j, i, k);

            if (block.getMaterial() != Material.air) {
                this.worldObj.spawnParticle(
                    "blockcrack_" + Block.getIdFromBlock(block) + "_" + this.worldObj.getBlockMetadata(j, i, k),
                    this.posX + ((double) this.rand.nextFloat() - 0.5D) * (double) this.width,
                    this.boundingBox.minY + 0.1D,
                    this.posZ + ((double) this.rand.nextFloat() - 0.5D) * (double) this.width,
                    -this.motionX * 4.0D,
                    1.5D,
                    -this.motionZ * 4.0D);
            }
        }

        this.handleWaterMovement();

        if (this.worldObj.isRemote) {
            this.fire = 0;
        } else if (this.fire > 0) {
            if (this.isImmuneToFire) {
                this.fire -= 4;

                if (this.fire < 0) {
                    this.fire = 0;
                }
            } else {
                if (this.fire % 20 == 0) {
                    this.attackEntityFrom(DamageSource.onFire, 1.0F);
                }

                --this.fire;
            }
        }

        if (this.handleLavaMovement()) {
            this.setOnFireFromLava();
            this.fallDistance *= 0.5F;
        }

        this.handleSubWorldsInteraction();

        if (this.posY < -64.0D) {
            this.kill();
        }

        if (!this.worldObj.isRemote) {
            this.setFlag(0, this.fire > 0);
        }

        this.firstUpdate = false;
        this.worldObj.theProfiler.endSection();
    }

    public void handleSubWorldsInteraction() {
        if (this.posX == this.prevPosX && this.posY == this.prevPosY && this.posZ == this.prevPosZ) {
            for (World world : ((IMixinWorld) this.worldObj).getSubWorlds()) {
                if ((world instanceof SubWorldServer && !((Entity) (Object) this instanceof EntityPlayer))
                    || (world instanceof SubWorldClient && ((Entity) (Object) this instanceof EntityPlayer))) {
                    SubWorld subworld = (SubWorld) world;
                    if (!subworld.getEntitiesToDrag()
                        .containsKey(this)
                        && (subworld.getRotationYawSpeed() != 0 || subworld.getRotationPitchSpeed() != 0
                            || subworld.getRotationRollSpeed() != 0
                            || subworld.getMotionX() != 0
                            || subworld.getMotionY() != 0
                            || subworld.getMotionZ() != 0)) {
                        AxisAlignedBB worldBB = subworld.getMaximumCloseWorldBBRotated();
                        if (this.boundingBox.intersectsWith(worldBB)) {
                            double expansion = 0.1;
                            AxisAlignedBB localEntityBB = ((IMixinAxisAlignedBB) this.boundingBox)
                                .getTransformedToLocalBoundingBox(world);
                            localEntityBB = AxisAlignedBB
                                .getBoundingBox(
                                    localEntityBB.minX,
                                    localEntityBB.minY,
                                    localEntityBB.minZ,
                                    localEntityBB.maxX,
                                    localEntityBB.maxY,
                                    localEntityBB.maxZ)
                                .expand(expansion, expansion, expansion);
                            List<AxisAlignedBB> collidingBBs = subworld
                                .getCollidingBoundingBoxesLocal((Entity) (Object) this, localEntityBB);

                            if (!collidingBBs.isEmpty()) {
                                for (Object aabb : ((IMixinWorldIntermediate) ((SubWorld) world).getParentWorld())
                                    .getCollidingBoundingBoxesLocal((Entity) (Object) this, this.boundingBox))
                                    collidingBBs
                                        .add(((IMixinAxisAlignedBB) aabb).getTransformedToLocalBoundingBox(world));
                                double modifierX = 0;
                                double modifierZ = 0;
                                int modXcount = 0;
                                int modZcount = 0;
                                for (AxisAlignedBB cBB : collidingBBs) {
                                    double dXpos = localEntityBB.minX - cBB.maxX;
                                    double dXneg = cBB.minX - localEntityBB.maxX;
                                    double dZpos = localEntityBB.minZ - cBB.maxZ;
                                    double dZneg = cBB.minZ - localEntityBB.maxZ;
                                    if (dXpos < 0 && dXneg < 0 && dZpos < 0 && dZneg < 0) {
                                        double modX = -dXpos < -dXneg ? -dXpos : dXneg;
                                        double modZ = -dZpos < -dZneg ? -dZpos : dZneg;
                                        if (Math.abs(modX) < Math.abs(modZ)) {
                                            modifierX += modX;
                                            modXcount++;
                                        } else {
                                            modifierZ += modZ;
                                            modZcount++;
                                        }
                                    }
                                }
                                if (modXcount > 0) modifierX /= modXcount;
                                if (modZcount > 0) modifierZ /= modZcount;
                                Vec3 localPos = subworld.transformToLocal((Entity) (Object) this);
                                double multiplier = 1;
                                Vec3 newlocalPos = localPos
                                    .addVector(modifierX * multiplier, 0, modifierZ * multiplier);
                                Vec3 entityPos = subworld.transformToGlobal(newlocalPos);
                                if ((modXcount + modZcount) > 0) {
                                    Vec3 moveVec = this.getGlobalPos()
                                        .subtract(entityPos);
                                    ((Entity) (Object) this).addVelocity(moveVec.xCoord, 0, moveVec.zCoord);
                                    this.setRotation(
                                        this.rotationYaw - (float) subworld.getRotationYawSpeed(),
                                        this.rotationPitch);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
