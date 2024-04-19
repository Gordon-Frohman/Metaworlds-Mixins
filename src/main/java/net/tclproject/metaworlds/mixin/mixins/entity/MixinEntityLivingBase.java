package net.tclproject.metaworlds.mixin.mixins.entity;

import java.util.Iterator;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.tclproject.metaworlds.api.IMixinEntity;
import net.tclproject.metaworlds.api.IMixinEntityLivingBase;
import net.tclproject.metaworlds.api.IMixinWorld;
import net.tclproject.metaworlds.patcher.EntityPlayerProxy;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase implements IMixinEntityLivingBase {

    private boolean isPlayerProxy = this instanceof EntityPlayerProxy;
    private static boolean isTransformingClient = false;
    private static boolean isTransformingServer = false;

    @Shadow(remap = true)
    public World worldObj;

    @Shadow(remap = true)
    public double posX;

    @Shadow(remap = true)
    public double posY;

    @Shadow(remap = true)
    public double posZ;

    @Shadow(remap = true)
    public boolean onGround;

    // TODO

    @Shadow(remap = true)
    public abstract void setPosition(double x, double y, double z);

    @Shadow(remap = true)
    protected abstract boolean isPlayer();

    @Inject(method = "setPosition", at = @At("TAIL"))
    public void setPosition(double par1, double par3, double par5, CallbackInfo ci) {
        if (this.tryLockTransformations()) {
            EntityPlayer curProxyPlayer;
            if (this.isPlayerProxy) {
                EntityPlayerProxy i$ = (EntityPlayerProxy) this;
                Vec3 curProxy = ((IMixinEntity) this).getGlobalPos();
                curProxyPlayer = i$.getRealPlayer();
                if (curProxyPlayer == null) {
                    this.releaseTransformationLock();
                    return;
                }

                ((IMixinEntityLivingBase) curProxyPlayer)
                    .setPositionLocal(curProxy.xCoord, curProxy.yCoord, curProxy.zCoord);
                Iterator transformedToLocalPos = ((IMixinEntity) curProxyPlayer).getPlayerProxyMap()
                    .values()
                    .iterator();

                while (transformedToLocalPos.hasNext()) {
                    EntityPlayerProxy curProxy1 = (EntityPlayerProxy) transformedToLocalPos.next();
                    if (curProxy1 != this) {
                        EntityPlayer curProxyPlayer1 = (EntityPlayer) curProxy1;
                        Vec3 transformedToLocalPos1 = ((IMixinWorld) curProxyPlayer1.worldObj)
                            .transformToLocal(curProxy);
                        ((IMixinEntityLivingBase) curProxyPlayer1).setPositionLocal(
                            transformedToLocalPos1.xCoord,
                            transformedToLocalPos1.yCoord,
                            transformedToLocalPos1.zCoord);
                    }
                }
            } else if (this.isPlayer()) {
                Iterator i$1 = ((IMixinEntity) (Object) this).getPlayerProxyMap()
                    .values()
                    .iterator();

                while (i$1.hasNext()) {
                    EntityPlayerProxy curProxy2 = (EntityPlayerProxy) i$1.next();
                    curProxyPlayer = (EntityPlayer) curProxy2;
                    Vec3 transformedToLocalPos2 = ((IMixinWorld) curProxyPlayer.worldObj)
                        .transformToLocal(par1, par3, par5);
                    ((IMixinEntityLivingBase) curProxyPlayer).setPositionLocal(
                        transformedToLocalPos2.xCoord,
                        transformedToLocalPos2.yCoord,
                        transformedToLocalPos2.zCoord);
                }
            }

            this.releaseTransformationLock();
        }
    }

    public void setPositionLocal(double par1, double par3, double par5) {
        ((Entity) (Object) this).setPosition(par1, par3, par5);
    }

    public void setRotation(float par1, float par2) {
        ((Entity) (Object) this).setRotation(par1, par2);
        if (this.tryLockTransformations()) {
            if (this.isPlayerProxy) {
                EntityPlayerProxy i$ = (EntityPlayerProxy) this;
                EntityPlayer curProxy = i$.getRealPlayer();
                if (curProxy == null) {
                    this.releaseTransformationLock();
                    return;
                }

                curProxy.setRotation(transformYawToGlobal(par1, (Entity) (Object) this), par2);
                Iterator curProxyPlayer = ((IMixinEntity) curProxy).getPlayerProxyMap()
                    .values()
                    .iterator();

                while (curProxyPlayer.hasNext()) {
                    EntityPlayerProxy curProxy1 = (EntityPlayerProxy) curProxyPlayer.next();
                    if (curProxy1 != this) {
                        EntityPlayer curProxyPlayer1 = (EntityPlayer) curProxy1;
                        curProxyPlayer1.setRotation(transformYawToLocal(curProxy.rotationYaw, curProxyPlayer1), par2);
                    }
                }
            } else if (this.isPlayer()) {
                Iterator i$1 = ((IMixinEntity) (Object) this).getPlayerProxyMap()
                    .values()
                    .iterator();

                while (i$1.hasNext()) {
                    EntityPlayerProxy curProxy2 = (EntityPlayerProxy) i$1.next();
                    EntityPlayer curProxyPlayer2 = (EntityPlayer) curProxy2;
                    curProxyPlayer2.setRotation(transformYawToLocal(par1, curProxyPlayer2), par2);
                }
            }

            this.releaseTransformationLock();
        }
    }

    public void setRotationLocal(float par1, float par2) {
        ((Entity) (Object) this).setRotation(par1, par2);
    }

    public void moveEntity(double par1, double par3, double par5) {
        ((Entity) (Object) this).moveEntity(par1, par3, par5);
        if (this.isPlayerProxy || this.isPlayer()) {
            this.setPosition(this.posX, this.posY, this.posZ);
            EntityPlayerProxy curProxyPlayer;
            if (!this.isPlayerProxy) {
                for (Iterator i$ = ((IMixinEntity) (Object) this).getPlayerProxyMap()
                    .values()
                    .iterator(); i$.hasNext(); ((EntityPlayer) curProxyPlayer).onGround = this.onGround) {
                    curProxyPlayer = (EntityPlayerProxy) i$.next();
                }
            }
        }
    }

    public void setPositionAndRotation(double par1, double par3, double par5, float par7, float par8) {
        ((Entity) (Object) this).setPositionAndRotation(par1, par3, par5, par7, par8);
        if (this.tryLockTransformations()) {
            EntityPlayer curProxyPlayer;
            if (this.isPlayerProxy) {
                EntityPlayerProxy i$ = (EntityPlayerProxy) this;
                Vec3 curProxy = ((IMixinEntity) this).getGlobalPos();
                curProxyPlayer = i$.getRealPlayer();
                if (curProxyPlayer == null) {
                    this.releaseTransformationLock();
                    return;
                }

                ((IMixinEntityLivingBase) curProxyPlayer).setPositionAndRotationLocal(
                    curProxy.xCoord,
                    curProxy.yCoord,
                    curProxy.zCoord,
                    transformYawToGlobal(par7, (EntityLivingBase) (Object) this),
                    par8);
                Iterator transformedToLocalPos = ((IMixinEntity) curProxyPlayer).getPlayerProxyMap()
                    .values()
                    .iterator();

                while (transformedToLocalPos.hasNext()) {
                    EntityPlayerProxy curProxy1 = (EntityPlayerProxy) transformedToLocalPos.next();
                    if (curProxy1 != this) {
                        EntityPlayer curProxyPlayer1 = (EntityPlayer) curProxy1;
                        Vec3 transformedToLocalPos1 = ((IMixinWorld) curProxyPlayer1.worldObj)
                            .transformToLocal(curProxy);
                        ((IMixinEntityLivingBase) curProxyPlayer).setPositionAndRotationLocal(
                            transformedToLocalPos1.xCoord,
                            transformedToLocalPos1.yCoord,
                            transformedToLocalPos1.zCoord,
                            transformYawToLocal(curProxyPlayer.rotationYaw, curProxyPlayer1),
                            par8);
                    }
                }
            } else if (this.isPlayer()) {
                Iterator i$1 = ((IMixinEntity) (Object) this).getPlayerProxyMap()
                    .values()
                    .iterator();

                while (i$1.hasNext()) {
                    EntityPlayerProxy curProxy2 = (EntityPlayerProxy) i$1.next();
                    curProxyPlayer = (EntityPlayer) curProxy2;
                    Vec3 transformedToLocalPos2 = ((IMixinWorld) curProxyPlayer.worldObj)
                        .transformToLocal(par1, par3, par5);
                    ((IMixinEntityLivingBase) curProxyPlayer).setPositionAndRotationLocal(
                        transformedToLocalPos2.xCoord,
                        transformedToLocalPos2.yCoord,
                        transformedToLocalPos2.zCoord,
                        transformYawToLocal(par7, curProxyPlayer),
                        par8);
                }
            }

            this.releaseTransformationLock();
        }
    }

    public void setPositionAndRotationLocal(double par1, double par3, double par5, float par7, float par8) {
        ((Entity) (Object) this).setPositionAndRotation(par1, par3, par5, par7, par8);
    }

    @SideOnly(Side.CLIENT)
    public void setAngles(float par1, float par2) {
        ((Entity) (Object) this).setAngles(par1, par2);
        if (this.tryLockTransformations()) {
            if (this.isPlayerProxy) {
                EntityPlayerProxy i$ = (EntityPlayerProxy) this;
                EntityPlayer curProxy = i$.getRealPlayer();
                if (curProxy == null) {
                    this.releaseTransformationLock();
                    return;
                }

                curProxy.setAngles(par1, par2);
                Iterator curProxyPlayer = ((IMixinEntity) curProxy).getPlayerProxyMap()
                    .values()
                    .iterator();

                while (curProxyPlayer.hasNext()) {
                    EntityPlayerProxy curProxy1 = (EntityPlayerProxy) curProxyPlayer.next();
                    if (curProxy1 != this) {
                        EntityPlayer curProxyPlayer1 = (EntityPlayer) curProxy1;
                        curProxyPlayer1.setAngles(par1, par2);
                    }
                }
            } else if (this.isPlayer()) {
                Iterator i$1 = ((IMixinEntity) (Object) this).getPlayerProxyMap()
                    .values()
                    .iterator();

                while (i$1.hasNext()) {
                    EntityPlayerProxy curProxy2 = (EntityPlayerProxy) i$1.next();
                    EntityPlayer curProxyPlayer2 = (EntityPlayer) curProxy2;
                    curProxyPlayer2.setAngles(par1, par2);
                }
            }

            this.releaseTransformationLock();
        }
    }

    public void setAnglesLocal(float par1, float par2) {
        ((Entity) (Object) this).setAngles(par1, par2);
    }

    public void setLocationAndAngles(double par1, double par3, double par5, float par7, float par8) {
        ((Entity) (Object) this).setLocationAndAngles(par1, par3, par5, par7, par8);
        if (this.tryLockTransformations()) {
            EntityPlayer curProxyPlayer;
            if (this.isPlayerProxy) {
                EntityPlayerProxy i$ = (EntityPlayerProxy) this;
                Vec3 curProxy = ((IMixinEntity) this).getGlobalPos();
                curProxyPlayer = i$.getRealPlayer();
                if (curProxyPlayer == null) {
                    this.releaseTransformationLock();
                    return;
                }

                ((IMixinEntityLivingBase) curProxyPlayer).setLocationAndAnglesLocal(
                    curProxy.xCoord,
                    curProxy.yCoord,
                    curProxy.zCoord,
                    transformYawToGlobal(par7, (EntityLivingBase) (Object) this),
                    par8);
                Iterator transformedToLocalPos = ((IMixinEntity) curProxyPlayer).getPlayerProxyMap()
                    .values()
                    .iterator();

                while (transformedToLocalPos.hasNext()) {
                    EntityPlayerProxy curProxy1 = (EntityPlayerProxy) transformedToLocalPos.next();
                    if (curProxy1 != this) {
                        EntityPlayer curProxyPlayer1 = (EntityPlayer) curProxy1;
                        Vec3 transformedToLocalPos1 = ((IMixinWorld) curProxyPlayer1.worldObj)
                            .transformToLocal(curProxy);
                        ((IMixinEntityLivingBase) curProxyPlayer1).setLocationAndAnglesLocal(
                            transformedToLocalPos1.xCoord,
                            transformedToLocalPos1.yCoord,
                            transformedToLocalPos1.zCoord,
                            transformYawToLocal(curProxyPlayer.rotationYaw, curProxyPlayer1),
                            par8);
                    }
                }
            } else if (this.isPlayer()) {
                Iterator i$1 = ((IMixinEntity) (Object) this).getPlayerProxyMap()
                    .values()
                    .iterator();

                while (i$1.hasNext()) {
                    EntityPlayerProxy curProxy2 = (EntityPlayerProxy) i$1.next();
                    curProxyPlayer = (EntityPlayer) curProxy2;
                    Vec3 transformedToLocalPos2 = ((IMixinWorld) curProxyPlayer.worldObj)
                        .transformToLocal(par1, par3, par5);
                    ((IMixinEntityLivingBase) curProxyPlayer).setLocationAndAnglesLocal(
                        transformedToLocalPos2.xCoord,
                        transformedToLocalPos2.yCoord,
                        transformedToLocalPos2.zCoord,
                        transformYawToLocal(par7, curProxyPlayer),
                        par8);
                }
            }

            this.releaseTransformationLock();
        }
    }

    public void setLocationAndAnglesLocal(double par1, double par3, double par5, float par7, float par8) {
        ((Entity) (Object) this).setLocationAndAngles(par1, par3, par5, par7, par8);
    }

    private boolean tryLockTransformations() {
        if ((!this.worldObj.isRemote || !isTransformingClient) && (this.worldObj.isRemote || !isTransformingServer)) {
            if (this.worldObj.isRemote) {
                isTransformingClient = true;
            } else {
                isTransformingServer = true;
            }

            return true;
        } else {
            return false;
        }
    }

    private void releaseTransformationLock() {
        if (this.worldObj.isRemote) {
            isTransformingClient = false;
        } else {
            isTransformingServer = false;
        }
    }

    private static float transformYawToGlobal(float parYaw, Entity transformingEntity) {
        return (float) ((double) parYaw - ((IMixinWorld) transformingEntity.worldObj).getRotationYaw());
    }

    private static float transformYawToLocal(float parYaw, Entity transformingEntity) {
        return (float) ((double) parYaw + ((IMixinWorld) transformingEntity.worldObj).getRotationYaw());
    }
}
