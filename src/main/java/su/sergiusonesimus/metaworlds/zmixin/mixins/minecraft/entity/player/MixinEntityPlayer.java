package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.entity.player;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import su.sergiusonesimus.metaworlds.entity.player.EntityPlayerProxy;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.IMixinEntity;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.IMixinEntityLivingBase;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.player.IMixinEntityPlayer;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;
import su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.entity.MixinEntityLivingBase;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer extends MixinEntityLivingBase implements IMixinEntityPlayer {

    private boolean isPlayerProxy = this instanceof EntityPlayerProxy;
    private static boolean isTransformingClient = false;
    private static boolean isTransformingServer = false;

    private final boolean isProxyPlayer = this instanceof EntityPlayerProxy;

    /** holds the ID of the spawn world of the player */
    private int spawnWorldID = 0;

    /** holds the IDs of the spawn worlds of the player for different dimensions */
    private HashMap<Integer, Integer> spawnSubworldMap = new HashMap<Integer, Integer>();

    @Shadow(remap = true)
    public ChunkCoordinates playerLocation;

    @Shadow(remap = true)
    protected boolean sleeping;

    // TODO

    @Shadow(remap = true)
    protected abstract boolean isPlayer();

    // isInBed

    @WrapOperation(
        method = "isInBed",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/Block;isBed(Lnet/minecraft/world/IBlockAccess;IIILnet/minecraft/entity/EntityLivingBase;)Z"))
    private boolean wrapIsBed(Block instance, IBlockAccess world, int x, int y, int z, EntityLivingBase player,
        Operation<Boolean> original) {
        if (original.call(instance, world, x, y, z, player)) {
            return true;
        } else {
            if (!((IMixinWorld) this.worldObj).isSubWorld()) {
                Iterator<EntityPlayerProxy> i$ = ((IMixinEntity) (Object) this).getPlayerProxyMap()
                    .values()
                    .iterator();

                while (i$.hasNext()) {
                    EntityPlayerProxy curProxy = (EntityPlayerProxy) i$.next();
                    if (((EntityPlayer) curProxy).isPlayerSleeping() && ((EntityPlayer) curProxy).isInBed()) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public void setSleeping(boolean newState) {
        this.sleeping = newState;
    }

    @Inject(method = "wakeUpPlayer", at = @At("TAIL"))
    public void wakeUpPlayer(boolean par1, boolean par2, boolean par3, CallbackInfo ci) {
        if (!this.isProxyPlayer) {
            Iterator<EntityPlayerProxy> i$ = ((IMixinEntity) this).getPlayerProxyMap()
                .values()
                .iterator();

            while (i$.hasNext()) {
                EntityPlayerProxy curPlayerProxy = i$.next();
                ((EntityPlayer) curPlayerProxy).wakeUpPlayer(par1, par2, par3);
            }
        }
    }

    public boolean isOnLadder() {
        if (this.isProxyPlayer) {
            return ((EntityPlayerProxy) this).getRealPlayer()
                .isOnLadder();
        } else if (super.isOnLadder()) {
            return true;
        } else {
            Iterator<EntityPlayerProxy> i$ = ((IMixinEntity) (Object) this).getPlayerProxyMap()
                .values()
                .iterator();

            EntityPlayerProxy curPlayerProxy;
            do {
                if (!i$.hasNext()) {
                    return false;
                }

                curPlayerProxy = (EntityPlayerProxy) i$.next();
            } while (!((IMixinEntityPlayer) curPlayerProxy).isOnLadderLocal());
            this.setWorldBelowFeet(((EntityPlayer) curPlayerProxy).worldObj);
            return true;
        }
    }

    public boolean isOnLadderLocal() {
        return super.isOnLadder();
    }

    public boolean shouldRenderInPass(int pass) {
        return ((IMixinWorld) ((EntityPlayer) (Object) this).worldObj).isSubWorld() ? false
            : super.shouldRenderInPass(pass);
    }

    // EntityIntermediateClass

    public void setPosition(double x, double y, double z) {
        this.setPositionLocal(x, y, z);
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
                Iterator<EntityPlayerProxy> transformedToLocalPos = ((IMixinEntity) curProxyPlayer).getPlayerProxyMap()
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
                Iterator<EntityPlayerProxy> i$1 = ((IMixinEntity) (Object) this).getPlayerProxyMap()
                    .values()
                    .iterator();

                while (i$1.hasNext()) {
                    EntityPlayerProxy curProxy2 = (EntityPlayerProxy) i$1.next();
                    curProxyPlayer = (EntityPlayer) curProxy2;
                    Vec3 transformedToLocalPos2 = ((IMixinWorld) curProxyPlayer.worldObj).transformToLocal(x, y, z);
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
        super.setPosition(par1, par3, par5);
    }

    public void setRotation(float par1, float par2) {
        this.setRotationLocal(par1, par2);
        if (this.tryLockTransformations()) {
            if (this.isPlayerProxy) {
                EntityPlayerProxy i$ = (EntityPlayerProxy) this;
                EntityPlayer curProxy = i$.getRealPlayer();
                if (curProxy == null) {
                    this.releaseTransformationLock();
                    return;
                }

                curProxy.setRotation(transformYawToGlobal(par1, (Entity) (Object) this), par2);
                Iterator<EntityPlayerProxy> curProxyPlayer = ((IMixinEntity) curProxy).getPlayerProxyMap()
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
                Iterator<EntityPlayerProxy> i$1 = ((IMixinEntity) (Object) this).getPlayerProxyMap()
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
        super.setRotation(par1, par2);
    }

    public void moveEntity(double par1, double par3, double par5) {
        super.moveEntity(par1, par3, par5);
        if (this.isPlayerProxy || this.isPlayer()) {
            ((EntityLivingBase) (Object) this).setPosition(
                ((EntityLivingBase) (Object) this).posX,
                ((EntityLivingBase) (Object) this).posY,
                ((EntityLivingBase) (Object) this).posZ);
            EntityPlayerProxy curProxyPlayer;
            if (!this.isPlayerProxy) {
                for (Iterator<EntityPlayerProxy> i$ = ((IMixinEntity) (Object) this).getPlayerProxyMap()
                    .values()
                    .iterator(); i$
                        .hasNext(); ((EntityPlayer) curProxyPlayer).onGround = ((EntityLivingBase) (Object) this).onGround) {
                    curProxyPlayer = (EntityPlayerProxy) i$.next();
                }
            }
        }
    }

    public void setPositionAndRotation(double par1, double par3, double par5, float par7, float par8) {
        this.setPositionAndRotationLocal(par1, par3, par5, par7, par8);
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
                Iterator<EntityPlayerProxy> transformedToLocalPos = ((IMixinEntity) curProxyPlayer).getPlayerProxyMap()
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
                Iterator<EntityPlayerProxy> i$1 = ((IMixinEntity) (Object) this).getPlayerProxyMap()
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
        super.setPositionAndRotation(par1, par3, par5, par7, par8);
    }

    @SideOnly(Side.CLIENT)
    public void setAngles(float par1, float par2) {
        this.setAnglesLocal(par1, par2);
        if (this.tryLockTransformations()) {
            if (this.isPlayerProxy) {
                EntityPlayerProxy i$ = (EntityPlayerProxy) this;
                EntityPlayer curProxy = i$.getRealPlayer();
                if (curProxy == null) {
                    this.releaseTransformationLock();
                    return;
                }

                curProxy.setAngles(par1, par2);
                Iterator<EntityPlayerProxy> curProxyPlayer = ((IMixinEntity) curProxy).getPlayerProxyMap()
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
                Iterator<EntityPlayerProxy> i$1 = ((IMixinEntity) (Object) this).getPlayerProxyMap()
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
        super.setAngles(par1, par2);
    }

    public void setLocationAndAngles(double par1, double par3, double par5, float par7, float par8) {
        this.setLocationAndAnglesLocal(par1, par3, par5, par7, par8);
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
                Iterator<EntityPlayerProxy> transformedToLocalPos = ((IMixinEntity) curProxyPlayer).getPlayerProxyMap()
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
                Iterator<EntityPlayerProxy> i$1 = ((IMixinEntity) (Object) this).getPlayerProxyMap()
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
        super.setLocationAndAngles(par1, par3, par5, par7, par8);
    }

    private boolean tryLockTransformations() {
        if ((!((EntityLivingBase) (Object) this).worldObj.isRemote || !isTransformingClient)
            && (((EntityLivingBase) (Object) this).worldObj.isRemote || !isTransformingServer)) {
            if (((EntityLivingBase) (Object) this).worldObj.isRemote) {
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
        if (((EntityLivingBase) (Object) this).worldObj.isRemote) {
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

    public HashMap<Integer, Integer> getSpawnSubworldMap() {
        return this.spawnSubworldMap;
    }

    public int getSpawnWorldID() {
        return getSpawnWorldID(0);
    }

    public int getSpawnWorldID(int dimension) {
        return dimension == 0 ? this.spawnWorldID : this.spawnSubworldMap.get(dimension);
    }

    public void setSpawnWorldID(int id) {
        setSpawnWorldID(this.dimension, id);
    }

    public void setSpawnWorldID(int dimension, int id) {
        if (dimension == 0) this.spawnWorldID = id;
        else this.spawnSubworldMap.put(dimension, id);
    }

    @Inject(
        method = "readEntityFromNBT",
        at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/EntityPlayer;spawnForced:Z"),
        locals = LocalCapture.CAPTURE_FAILHARD)
    public void readEntityFromNBT(NBTTagCompound tagCompound, CallbackInfo ci) {
        spawnWorldID = tagCompound.hasKey("SpawnWorldID") ? tagCompound.getInteger("SpawnWorldID") : 0;
    }

    @Inject(
        method = "readEntityFromNBT",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/entity/player/EntityPlayer;spawnForcedMap:Ljava/util/HashMap;"),
        locals = LocalCapture.CAPTURE_FAILHARD)
    public void readEntityFromNBT(NBTTagCompound tagCompound, CallbackInfo ci,
        @Local(name = "spawndata") NBTTagCompound spawndata, @Local(name = "spawndim") int spawndim) {
        int worldID = 0;
        if (spawndata.hasKey("SpawnWorldID")) {
            worldID = spawndata.getInteger("SpawnWorldID");
        }
        this.spawnSubworldMap.put(spawndim, worldID);
    }

    @Inject(method = "writeEntityToNBT", at = @At(value = "TAIL"))
    public void writeEntityToNBT(NBTTagCompound tagCompound, CallbackInfo ci) {
        tagCompound.setInteger("SpawnWorldID", spawnWorldID);
    }

    @Inject(
        method = "writeEntityToNBT",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NBTTagList;appendTag(Lnet/minecraft/nbt/NBTBase;)V"),
        locals = LocalCapture.CAPTURE_FAILHARD)
    public void writeEntityToNBT(NBTTagCompound tagCompound, CallbackInfo ci,
        @Local(name = "spawndata") NBTTagCompound spawndata,
        @Local(name = "entry") Entry<Integer, ChunkCoordinates> entry) {
        spawndata.setInteger("SpawnWorldID", this.spawnSubworldMap.get(entry.getKey()));
    }

    @Inject(
        method = "clonePlayer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/EntityPlayer;getEntityData()Lnet/minecraft/nbt/NBTTagCompound;",
            ordinal = 0,
            shift = Shift.BEFORE))
    public void clonePlayer(EntityPlayer p_71049_1_, boolean p_71049_2_, CallbackInfo ci) {
        this.spawnSubworldMap = ((IMixinEntityPlayer) p_71049_1_).getSpawnSubworldMap();
    }

}
