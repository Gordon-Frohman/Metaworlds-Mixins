package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.entity;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.IMixinEntityLivingBase;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends MixinEntity implements IMixinEntityLivingBase {

    @Shadow(remap = true)
    protected int recentlyHit;

    @Shadow(remap = true)
    protected int entityAge;

    @Shadow(remap = true)
    public int attackTime;

    @Shadow(remap = true)
    public float prevSwingProgress;

    @Shadow(remap = true)
    public float swingProgress;

    @Shadow(remap = true)
    public float prevLimbSwingAmount;

    @Shadow(remap = true)
    public float limbSwingAmount;

    @Shadow(remap = true)
    protected float lastDamage;

    @Shadow(remap = true)
    public float prevHealth;

    @Shadow(remap = true)
    public int hurtTime;

    @Shadow(remap = true)
    public int maxHurtTime;

    @Shadow(remap = true)
    public float attackedAtYaw;

    @Shadow(remap = true)
    protected EntityPlayer attackingPlayer;

    @Shadow(remap = true)
    public int maxHurtResistantTime;

    @Shadow(remap = true)
    public float jumpMovementFactor;

    @Shadow(remap = true)
    public float limbSwing;

    // TODO

    @Shadow(remap = true)
    public float getAIMoveSpeed() {
        return 0;
    }

    @Shadow(remap = true)
    protected boolean isAIEnabled() {
        return false;
    }

    @Shadow(remap = true)
    public void onDeath(DamageSource p_70645_1_) {}

    @Shadow(remap = true)
    protected float getSoundVolume() {
        return 0;
    }

    @Shadow(remap = true)
    protected float getSoundPitch() {
        return 0;
    }

    @Shadow(remap = true)
    protected String getHurtSound() {
        return "";
    }

    @Shadow(remap = true)
    protected String getDeathSound() {
        return "";
    }

    @Shadow(remap = true)
    public void knockBack(Entity p_70653_1_, float p_70653_2_, double p_70653_3_, double p_70653_5_) {}

    @Shadow(remap = true)
    protected void setBeenAttacked() {}

    @Shadow(remap = true)
    public void setRevengeTarget(EntityLivingBase p_70604_1_) {}

    @Shadow(remap = true)
    protected void damageEntity(DamageSource p_70665_1_, float p_70665_2_) {}

    @Shadow(remap = true)
    public ItemStack getEquipmentInSlot(int p_71124_1_) {
        return null;
    }

    @Shadow(remap = true)
    public boolean isPotionActive(Potion p_70644_1_) {
        return false;
    }

    @Shadow(remap = true)
    public final float getHealth() {
        return 0;
    }

    @Shadow(remap = true)
    public boolean isOnLadder() {
        return false;
    }

    /**
     * @author Sergius Onesimus
     * @reason We need to be able to modify local variables i, j, k and block, which is impossible without overwriting
     */
    @Overwrite
    protected void updateFallState(double distanceFallenThisTick, boolean isOnGround) {
        if (!this.isInWater()) {
            this.handleWaterMovement();
        }

        if (isOnGround && this.fallDistance > 0.0F) {
            int i = MathHelper.floor_double(this.posX);
            int j = MathHelper.floor_double(this.posY - 0.20000000298023224D - (double) this.yOffset);
            int k = MathHelper.floor_double(this.posZ);
            Block block = this.worldObj.getBlock(i, j, k);
            World targetWorld = this.worldObj;
            if (block.getMaterial() == Material.air) {
                Vec3 globalCoords = Vec3
                    .createVectorHelper(this.posX, this.posY - 0.20000000298023224D - (double) this.yOffset, this.posZ);
                for (World world : ((IMixinWorld) this.worldObj).getSubWorlds()) {
                    AxisAlignedBB worldBB = ((SubWorld) world).getMaximumCloseWorldBBRotated()
                        .expand(0, 1, 0);
                    if (worldBB.intersectsWith(this.boundingBox)) {
                        Vec3 localCoords = ((IMixinWorld) world).transformToLocal(globalCoords);
                        int localI = MathHelper.floor_double(localCoords.xCoord);
                        int localJ = MathHelper.floor_double(localCoords.yCoord);
                        int localK = MathHelper.floor_double(localCoords.zCoord);
                        block = world.getBlock(localI, localJ, localK);
                        if (block.getMaterial() != Material.air) {
                            targetWorld = world;
                            i = localI;
                            j = localJ;
                            k = localK;
                            break;
                        } else {
                            int l = world.getBlock(localI, localJ - 1, localK)
                                .getRenderType();
                            if (l == 11 || l == 32 || l == 21) {
                                targetWorld = world;
                                i = localI;
                                j = localJ;
                                k = localK;
                                block = world.getBlock(localI, localJ - 1, localK);
                                break;
                            }
                        }
                    }
                }
            }
            if (block.getMaterial() == Material.air) {
                int l = targetWorld.getBlock(i, j - 1, k)
                    .getRenderType();
                if (l == 11 || l == 32 || l == 21) {
                    block = targetWorld.getBlock(i, j - 1, k);
                }
            } else if (!this.worldObj.isRemote && this.fallDistance > 3.0F) {
                targetWorld.playAuxSFX(2006, i, j, k, MathHelper.ceiling_float_int(this.fallDistance - 3.0F));
            }

            block.onFallenUpon(targetWorld, i, j, k, (Entity) (Object) this, this.fallDistance);
        }

        super.updateFallState(distanceFallenThisTick, isOnGround);
    }

    // attackEntityFrom

    private Vec3 entityPos;
    private Vec3 thisPos;

    @Inject(
        method = "attackEntityFrom",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/entity/Entity;posX:D",
            opcode = Opcodes.GETFIELD,
            shift = Shift.BEFORE,
            ordinal = 0))
    private void injectAttackEntityFrom(DamageSource source, float amount, CallbackInfoReturnable<Boolean> ci,
        @Local(name = "entity") Entity entity) {
        entityPos = ((IMixinWorld) entity.worldObj).transformToGlobal(entity);
        thisPos = ((IMixinWorld) this.worldObj).transformToGlobal((EntityLivingBase) (Object) this);
    }

    @WrapOperation(
        method = "attackEntityFrom",
        at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;posX:D", opcode = Opcodes.GETFIELD))
    private double wrapPosX(Entity instance, Operation<Double> original) {
        return entityPos.xCoord;
    }

    @WrapOperation(
        method = "attackEntityFrom",
        at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLivingBase;posX:D", opcode = Opcodes.GETFIELD))
    private double wrapPosX(EntityLivingBase instance, Operation<Double> original) {
        return thisPos.xCoord;
    }

    @WrapOperation(
        method = "attackEntityFrom",
        at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;posZ:D", opcode = Opcodes.GETFIELD))
    private double wrapPosZ(Entity instance, Operation<Double> original) {
        return entityPos.zCoord;
    }

    @WrapOperation(
        method = "attackEntityFrom",
        at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLivingBase;posZ:D", opcode = Opcodes.GETFIELD))
    private double wrapPosZ(EntityLivingBase instance, Operation<Double> original) {
        return thisPos.zCoord;
    }

    // moveEntityWithHeading

    /**
     * Moves the entity based on the specified heading. Args: strafe, forward
     * 
     * @author Sergius Onesimus
     * @reason Temporary solution for player spawn on subworlds
     */
    @Overwrite
    public void moveEntityWithHeading(float strafe, float forward) {
        double d0;

        if (this.isInWater() && (!(((EntityLivingBase) (Object) this) instanceof EntityPlayer)
            || !((EntityPlayer) (Object) this).capabilities.isFlying)) {
            d0 = this.posY;
            this.moveFlying(strafe, forward, this.isAIEnabled() ? 0.04F : 0.02F);
            this.moveEntity(this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.800000011920929D;
            this.motionY *= 0.800000011920929D;
            this.motionZ *= 0.800000011920929D;
            this.motionY -= 0.02D;

            if (this.isCollidedHorizontally && this.isOffsetPositionInLiquid(
                this.motionX,
                this.motionY + 0.6000000238418579D - this.posY + d0,
                this.motionZ)) {
                this.motionY = 0.30000001192092896D;
            }
        } else if (this.handleLavaMovement() && (!(((EntityLivingBase) (Object) this) instanceof EntityPlayer)
            || !((EntityPlayer) (Object) this).capabilities.isFlying)) {
                d0 = this.posY;
                this.moveFlying(strafe, forward, 0.02F);
                this.moveEntity(this.motionX, this.motionY, this.motionZ);
                this.motionX *= 0.5D;
                this.motionY *= 0.5D;
                this.motionZ *= 0.5D;
                this.motionY -= 0.02D;

                if (this.isCollidedHorizontally && this.isOffsetPositionInLiquid(
                    this.motionX,
                    this.motionY + 0.6000000238418579D - this.posY + d0,
                    this.motionZ)) {
                    this.motionY = 0.30000001192092896D;
                }
            } else {
                float f2 = 0.91F;

                if (this.onGround) {
                    f2 = this.worldObj.getBlock(
                        MathHelper.floor_double(this.posX),
                        MathHelper.floor_double(this.boundingBox.minY) - 1,
                        MathHelper.floor_double(this.posZ)).slipperiness * 0.91F;
                }

                float f3 = 0.16277136F / (f2 * f2 * f2);
                float f4;

                if (this.onGround) {
                    f4 = this.getAIMoveSpeed() * f3;
                } else {
                    f4 = this.jumpMovementFactor;
                }

                this.moveFlying(strafe, forward, f4);
                f2 = 0.91F;

                if (this.onGround) {
                    f2 = this.worldObj.getBlock(
                        MathHelper.floor_double(this.posX),
                        MathHelper.floor_double(this.boundingBox.minY) - 1,
                        MathHelper.floor_double(this.posZ)).slipperiness * 0.91F;
                }

                if (this.isOnLadder()) {
                    float f5 = 0.15F;

                    if (this.motionX < (double) (-f5)) {
                        this.motionX = (double) (-f5);
                    }

                    if (this.motionX > (double) f5) {
                        this.motionX = (double) f5;
                    }

                    if (this.motionZ < (double) (-f5)) {
                        this.motionZ = (double) (-f5);
                    }

                    if (this.motionZ > (double) f5) {
                        this.motionZ = (double) f5;
                    }

                    this.fallDistance = 0.0F;

                    if (this.motionY < -0.15D) {
                        this.motionY = -0.15D;
                    }

                    boolean flag = this.isSneaking() && ((EntityLivingBase) (Object) this) instanceof EntityPlayer;

                    if (flag && this.motionY < 0.0D) {
                        this.motionY = 0.0D;
                    }
                }

                this.moveEntity(this.motionX, this.motionY, this.motionZ);

                if (this.isCollidedHorizontally && this.isOnLadder()) {
                    this.motionY = 0.2D;
                }

                if (this.worldObj.isRemote) {
                    boolean decreaseMotion = true;
                    if ((!this.worldObj.blockExists((int) this.posX, 0, (int) this.posZ)
                        || !this.worldObj.getChunkFromBlockCoords((int) this.posX, (int) this.posZ).isChunkLoaded)) {
                        if (this.posY > 0.0D) {
                            this.motionY = -0.1D;
                        } else {
                            this.motionY = 0.0D;
                        }
                        decreaseMotion = false;
                    }
                    World worldBelowFeet = getWorldBelowFeet();
                    if (worldBelowFeet instanceof SubWorld subworld) {
                        Vec3 localPos = subworld.transformToLocal((Entity) (Object) this);
                        int localX = MathHelper.floor_double(localPos.xCoord);
                        int localZ = MathHelper.floor_double(localPos.zCoord);
                        if (!subworld.canUpdate() || !worldBelowFeet.blockExists(localX, 0, localZ)
                            || !worldBelowFeet.getChunkFromBlockCoords(localX, localZ).isChunkLoaded) {
                            this.motionX = 0.0D;
                            this.motionY = 0.0D;
                            this.motionZ = 0.0D;
                            subworld.registerEntityToDrag((Entity) (Object) this);
                            this.setWorldBelowFeet(worldBelowFeet);
                            decreaseMotion = false;
                        }
                    }
                    if (decreaseMotion) this.motionY -= 0.08D;
                } else {
                    this.motionY -= 0.08D;
                }

                this.motionY *= 0.9800000190734863D;
                this.motionX *= (double) f2;
                this.motionZ *= (double) f2;
            }

        this.prevLimbSwingAmount = this.limbSwingAmount;
        d0 = this.posX - this.prevPosX;
        double d1 = this.posZ - this.prevPosZ;
        float f6 = MathHelper.sqrt_double(d0 * d0 + d1 * d1) * 4.0F;

        if (f6 > 1.0F) {
            f6 = 1.0F;
        }

        this.limbSwingAmount += (f6 - this.limbSwingAmount) * 0.4F;
        this.limbSwing += this.limbSwingAmount;
    }

}
