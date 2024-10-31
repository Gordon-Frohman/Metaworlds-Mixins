package su.sergiusonesimus.metaworlds.mixin.mixins.minecraft.entity;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import su.sergiusonesimus.metaworlds.api.IMixinEntityLivingBase;
import su.sergiusonesimus.metaworlds.api.IMixinWorld;
import su.sergiusonesimus.metaworlds.api.SubWorld;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends MixinEntity implements IMixinEntityLivingBase {

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
                    AxisAlignedBB worldBB = ((SubWorld) world).getMaximumCloseWorldBBRotated();
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

}
