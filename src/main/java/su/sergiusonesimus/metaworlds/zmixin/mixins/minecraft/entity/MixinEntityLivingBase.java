package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.entity;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
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
import net.minecraftforge.common.ForgeHooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.entity.IMixinEntityLivingBase;
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
    public abstract ItemStack getEquipmentInSlot(int p_71124_1_);

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
                        block = world.getBlock(
                            MathHelper.floor_double(localCoords.xCoord),
                            MathHelper.floor_double(localCoords.yCoord),
                            MathHelper.floor_double(localCoords.zCoord));
                        if (block.getMaterial() != Material.air) {
                            targetWorld = world;
                            i = MathHelper.floor_double(localCoords.xCoord);
                            j = MathHelper.floor_double(localCoords.yCoord);
                            k = MathHelper.floor_double(localCoords.zCoord);
                            break;
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

    /**
     * Called when the entity is attacked.
     */
    @Overwrite
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (ForgeHooks.onLivingAttack((EntityLivingBase) (Object) this, source, amount)) return false;
        if (this.isEntityInvulnerable()) {
            return false;
        } else if (this.worldObj.isRemote) {
            return false;
        } else {
            this.entityAge = 0;

            if (this.getHealth() <= 0.0F) {
                return false;
            } else if (source.isFireDamage() && this.isPotionActive(Potion.fireResistance)) {
                return false;
            } else {
                if ((source == DamageSource.anvil || source == DamageSource.fallingBlock)
                    && this.getEquipmentInSlot(4) != null) {
                    this.getEquipmentInSlot(4)
                        .damageItem(
                            (int) (amount * 4.0F + this.rand.nextFloat() * amount * 2.0F),
                            (EntityLivingBase) (Object) this);
                    amount *= 0.75F;
                }

                this.limbSwingAmount = 1.5F;
                boolean flag = true;

                if ((float) this.hurtResistantTime > (float) this.maxHurtResistantTime / 2.0F) {
                    if (amount <= this.lastDamage) {
                        return false;
                    }

                    this.damageEntity(source, amount - this.lastDamage);
                    this.lastDamage = amount;
                    flag = false;
                } else {
                    this.lastDamage = amount;
                    this.prevHealth = this.getHealth();
                    this.hurtResistantTime = this.maxHurtResistantTime;
                    this.damageEntity(source, amount);
                    this.hurtTime = this.maxHurtTime = 10;
                }

                this.attackedAtYaw = 0.0F;
                Entity entity = source.getEntity();

                if (entity != null) {
                    if (entity instanceof EntityLivingBase) {
                        this.setRevengeTarget((EntityLivingBase) entity);
                    }

                    if (entity instanceof EntityPlayer) {
                        this.recentlyHit = 100;
                        this.attackingPlayer = (EntityPlayer) entity;
                    } else if (entity instanceof net.minecraft.entity.passive.EntityTameable) {
                        net.minecraft.entity.passive.EntityTameable entitywolf = (net.minecraft.entity.passive.EntityTameable) entity;

                        if (entitywolf.isTamed()) {
                            this.recentlyHit = 100;
                            this.attackingPlayer = null;
                        }
                    }
                }

                if (flag) {
                    this.worldObj.setEntityState((EntityLivingBase) (Object) this, (byte) 2);

                    if (source != DamageSource.drown) {
                        this.setBeenAttacked();
                    }

                    if (entity != null) {
                        Vec3 entityPos = ((IMixinWorld) entity.worldObj).transformToGlobal(entity);
                        Vec3 thisPos = ((IMixinWorld) this.worldObj)
                            .transformToGlobal((EntityLivingBase) (Object) this);

                        double dirX = entityPos.xCoord - thisPos.xCoord;
                        double dirZ;

                        for (dirZ = entityPos.zCoord - thisPos.zCoord; dirX * dirX + dirZ * dirZ
                            < 1.0E-4D; dirZ = (Math.random() - Math.random()) * 0.01D) {
                            dirX = (Math.random() - Math.random()) * 0.01D;
                        }

                        this.attackedAtYaw = (float) (Math.atan2(dirZ, dirX) * 180.0D / Math.PI) - this.rotationYaw;
                        this.knockBack(entity, amount, dirX, dirZ);
                    } else {
                        this.attackedAtYaw = (float) ((int) (Math.random() * 2.0D) * 180);
                    }
                }

                String s;

                if (this.getHealth() <= 0.0F) {
                    s = this.getDeathSound();

                    if (flag && s != null) {
                        this.playSound(s, this.getSoundVolume(), this.getSoundPitch());
                    }

                    this.onDeath(source);
                } else {
                    s = this.getHurtSound();

                    if (flag && s != null) {
                        this.playSound(s, this.getSoundVolume(), this.getSoundPitch());
                    }
                }

                return true;
            }
        }
    }

    /**
     * Moves the entity based on the specified heading.  Args: strafe, forward
     */
    @Overwrite
    public void moveEntityWithHeading(float strafe, float forward)
    {
        double d0;

        if (this.isInWater() && (!(((EntityLivingBase)(Object)this) instanceof EntityPlayer) || !((EntityPlayer)(Object)this).capabilities.isFlying))
        {
            d0 = this.posY;
            this.moveFlying(strafe, forward, this.isAIEnabled() ? 0.04F : 0.02F);
            this.moveEntity(this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.800000011920929D;
            this.motionY *= 0.800000011920929D;
            this.motionZ *= 0.800000011920929D;
            this.motionY -= 0.02D;

            if (this.isCollidedHorizontally && this.isOffsetPositionInLiquid(this.motionX, this.motionY + 0.6000000238418579D - this.posY + d0, this.motionZ))
            {
                this.motionY = 0.30000001192092896D;
            }
        }
        else if (this.handleLavaMovement() && (!(((EntityLivingBase)(Object)this) instanceof EntityPlayer) || !((EntityPlayer)(Object)this).capabilities.isFlying))
        {
            d0 = this.posY;
            this.moveFlying(strafe, forward, 0.02F);
            this.moveEntity(this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.5D;
            this.motionY *= 0.5D;
            this.motionZ *= 0.5D;
            this.motionY -= 0.02D;

            if (this.isCollidedHorizontally && this.isOffsetPositionInLiquid(this.motionX, this.motionY + 0.6000000238418579D - this.posY + d0, this.motionZ))
            {
                this.motionY = 0.30000001192092896D;
            }
        }
        else
        {
            float f2 = 0.91F;

            if (this.onGround)
            {
                f2 = this.worldObj.getBlock(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.boundingBox.minY) - 1, MathHelper.floor_double(this.posZ)).slipperiness * 0.91F;
            }

            float f3 = 0.16277136F / (f2 * f2 * f2);
            float f4;

            if (this.onGround)
            {
                f4 = this.getAIMoveSpeed() * f3;
            }
            else
            {
                f4 = this.jumpMovementFactor;
            }

            this.moveFlying(strafe, forward, f4);
            f2 = 0.91F;

            if (this.onGround)
            {
                f2 = this.worldObj.getBlock(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.boundingBox.minY) - 1, MathHelper.floor_double(this.posZ)).slipperiness * 0.91F;
            }

            if (this.isOnLadder())
            {
                if((Entity)(Object)this instanceof EntityPlayer)
                	Minecraft.logger.info("Player is on ladder");
                float f5 = 0.15F;

                if (this.motionX < (double)(-f5))
                {
                    this.motionX = (double)(-f5);
                }

                if (this.motionX > (double)f5)
                {
                    this.motionX = (double)f5;
                }

                if (this.motionZ < (double)(-f5))
                {
                    this.motionZ = (double)(-f5);
                }

                if (this.motionZ > (double)f5)
                {
                    this.motionZ = (double)f5;
                }

                this.fallDistance = 0.0F;

                if (this.motionY < -0.15D)
                {
                    this.motionY = -0.15D;
                }

                boolean flag = this.isSneaking() && ((EntityLivingBase)(Object)this) instanceof EntityPlayer;

                if (flag && this.motionY < 0.0D)
                {
                    this.motionY = 0.0D;
                }
            } else {
                if((Entity)(Object)this instanceof EntityPlayer)
                	Minecraft.logger.info("Player is not on ladder");
            }

            this.moveEntity(this.motionX, this.motionY, this.motionZ);

            if (this.isCollidedHorizontally && this.isOnLadder())
            {
                this.motionY = 0.2D;
            }

            if (this.worldObj.isRemote && (!this.worldObj.blockExists((int)this.posX, 0, (int)this.posZ) || !this.worldObj.getChunkFromBlockCoords((int)this.posX, (int)this.posZ).isChunkLoaded))
            {
                if (this.posY > 0.0D)
                {
                    this.motionY = -0.1D;
                }
                else
                {
                    this.motionY = 0.0D;
                }
            }
            else
            {
                this.motionY -= 0.08D;
            }

            this.motionY *= 0.9800000190734863D;
            this.motionX *= (double)f2;
            this.motionZ *= (double)f2;
        }

        this.prevLimbSwingAmount = this.limbSwingAmount;
        d0 = this.posX - this.prevPosX;
        double d1 = this.posZ - this.prevPosZ;
        float f6 = MathHelper.sqrt_double(d0 * d0 + d1 * d1) * 4.0F;

        if (f6 > 1.0F)
        {
            f6 = 1.0F;
        }

        this.limbSwingAmount += (f6 - this.limbSwingAmount) * 0.4F;
        this.limbSwing += this.limbSwingAmount;
    }

}
