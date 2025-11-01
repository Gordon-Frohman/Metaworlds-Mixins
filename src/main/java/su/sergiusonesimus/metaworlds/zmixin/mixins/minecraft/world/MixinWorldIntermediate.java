package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.world;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import org.spongepowered.asm.mixin.Mixin;

import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.entity.player.EntityPlayerMPSubWorldProxy;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.IMixinEntity;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.util.IMixinAxisAlignedBB;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.util.IMixinMovingObjectPosition;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorldIntermediate;

@Mixin(value = { WorldClient.class, WorldServer.class })
public abstract class MixinWorldIntermediate extends MixinWorld implements IMixinWorldIntermediate {

    private List<AxisAlignedBB> collidingBBCacheIntermediate = new ArrayList<AxisAlignedBB>();

    public MovingObjectPosition func_147447_a(Vec3 par1Vec3, Vec3 par2Vec3, boolean par3, boolean par4, boolean par5) {
        MovingObjectPosition bestResult = null;
        Vec3 vecSource = ((IMixinWorld) this).transformToGlobal(par1Vec3);
        Vec3 vecDest = ((IMixinWorld) this).transformToGlobal(par2Vec3);
        Iterator<World> i$ = ((IMixinWorld) ((IMixinWorld) this).getParentWorld()).getWorlds()
            .iterator();

        while (i$.hasNext()) {
            World curWorld = i$.next();
            MovingObjectPosition curResult = ((IMixinWorldIntermediate) curWorld).rayTraceBlocks_do_do_single(
                ((IMixinWorld) curWorld).transformToLocal(vecSource),
                ((IMixinWorld) curWorld).transformToLocal(vecDest),
                par3,
                par4,
                par5);
            if (curResult != null) {
                // This field is actually inserted on runtime
                ((IMixinMovingObjectPosition) curResult).setWorld(curWorld);
                curResult.hitVec = ((IMixinWorld) curWorld)
                    .transformLocalToOther((World) (Object) this, curResult.hitVec);
            }

            if (bestResult == null || bestResult.typeOfHit == MovingObjectPosition.MovingObjectType.MISS
                || curResult != null && curResult.typeOfHit != MovingObjectPosition.MovingObjectType.MISS
                    && bestResult.hitVec.squareDistanceTo(par1Vec3) > curResult.hitVec.squareDistanceTo(par1Vec3)) {
                bestResult = curResult;
            }
        }

        return bestResult;
    }

    public MovingObjectPosition rayTraceBlocks_do_do_single(Vec3 par1Vec3, Vec3 par2Vec3, boolean par3, boolean par4,
        boolean par5) {
        return super.func_147447_a(par1Vec3, par2Vec3, par3, par4, par5);
    }

    public List<AxisAlignedBB> getCollidingBoundingBoxes(Entity entity, AxisAlignedBB aabb) {
        this.collidingBBCacheIntermediate.clear();
        this.collidingBBCacheIntermediate = this.getCollidingBoundingBoxesLocal(entity, aabb);
        Iterator<World> i$ = ((IMixinWorld) this).getSubWorlds()
            .iterator();

        while (i$.hasNext()) {
            World curSubWorld = i$.next();
            if (!((SubWorld) curSubWorld).getMaximumCloseWorldBBRotated()
                .intersectsWith(aabb)) continue;
            double worldRotationY = ((IMixinWorld) curSubWorld).getRotationYaw() % 360;
            if (worldRotationY != 0) {
                Vec3 rotationPoint;
                AxisAlignedBB localBB;
                Vec3 moveVec;
                if (aabb.maxX - aabb.minX == entity.boundingBox.maxX - entity.boundingBox.minX
                    && aabb.maxZ - aabb.minZ == entity.boundingBox.maxZ - entity.boundingBox.minZ) {
                    // BB was moved, not expanded
                    rotationPoint = Vec3.createVectorHelper(
                        (aabb.maxX + aabb.minX) / 2,
                        (aabb.maxY + aabb.minY) / 2,
                        (aabb.maxZ + aabb.minZ) / 2);
                    localBB = aabb;
                    moveVec = Vec3.createVectorHelper(0, 0, 0);
                } else {
                    // BB was expanded, so we'll have to regenerate it
                    double dxPos = aabb.maxX - entity.posX;
                    double dxNeg = entity.posX - aabb.minX;
                    double dzPos = aabb.maxZ - entity.posZ;
                    double dzNeg = entity.posZ - aabb.minZ;
                    moveVec = Vec3.createVectorHelper(dxPos - dxNeg, 0, dzPos - dzNeg);
                    double xHalf = dxPos < dxNeg ? dxPos : dxNeg;
                    double zHalf = dzPos < dzNeg ? dzPos : dzNeg;
                    localBB = AxisAlignedBB.getBoundingBox(
                        entity.posX - xHalf,
                        aabb.minY,
                        entity.posZ - zHalf,
                        entity.posX + xHalf,
                        aabb.maxY,
                        entity.posZ + zHalf);
                    rotationPoint = Vec3.createVectorHelper(entity.posX, (aabb.maxY + aabb.minY) / 2, entity.posZ);
                }

                localBB = ((IMixinAxisAlignedBB) localBB)
                    .rotateYaw(-worldRotationY, rotationPoint.xCoord, rotationPoint.zCoord);
                this.collidingBBCacheIntermediate.addAll(
                    ((SubWorld) curSubWorld).getCollidingBoundingBoxesGlobalWithMovement(entity, localBB, moveVec));
            } else {
                this.collidingBBCacheIntermediate
                    .addAll(((SubWorld) curSubWorld).getCollidingBoundingBoxesGlobal(entity, aabb));
            }
        }

        return this.collidingBBCacheIntermediate;
    }

    public List<AxisAlignedBB> getCollidingBoundingBoxesLocal(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB) {
        return super.getCollidingBoundingBoxes(par1Entity, par2AxisAlignedBB);
    }

    public boolean isMaterialInBB(AxisAlignedBB par1AxisAlignedBB, Material par2Material) {
        if (this.isMaterialInBBLocal(par1AxisAlignedBB, par2Material)) {
            return true;
        } else {
            if (!((IMixinWorld) this).isSubWorld()) {
                Iterator<World> i$ = ((IMixinWorld) this).getSubWorlds()
                    .iterator();

                while (i$.hasNext()) {
                    World curSubWorld = (World) i$.next();
                    if (((SubWorld) curSubWorld).isMaterialInBBGlobal(par1AxisAlignedBB, par2Material)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public boolean isMaterialInBBLocal(AxisAlignedBB par1AxisAlignedBB, Material par2Material) {
        return super.isMaterialInBB(par1AxisAlignedBB, par2Material);
    }

    public boolean isAABBInMaterial(AxisAlignedBB par1AxisAlignedBB, Material par2Material) {
        if (super.isAABBInMaterial(par1AxisAlignedBB, par2Material)) {
            return true;
        } else {
            if (!((IMixinWorld) this).isSubWorld()) {
                Iterator<World> i$ = ((IMixinWorld) this).getSubWorlds()
                    .iterator();

                while (i$.hasNext()) {
                    World curSubWorld = (World) i$.next();
                    if (((SubWorld) curSubWorld).isAABBInMaterialGlobal(par1AxisAlignedBB, par2Material)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public List<Entity> getEntitiesWithinAABBExcludingEntity(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB,
        IEntitySelector par3IEntitySelector) {
        ArrayList<Entity> arraylist = new ArrayList<Entity>();
        arraylist
            .addAll(this.getEntitiesWithinAABBExcludingEntityLocal(par1Entity, par2AxisAlignedBB, par3IEntitySelector));
        Iterator<World> i$ = ((IMixinWorld) this).getSubWorlds()
            .iterator();

        while (i$.hasNext()) {
            World curSubWorld = i$.next();
            arraylist.addAll(
                ((IMixinWorld) curSubWorld).getEntitiesWithinAABBExcludingEntityLocal(
                    par1Entity,
                    ((IMixinAxisAlignedBB) par2AxisAlignedBB).getTransformedToLocalBoundingBox(curSubWorld),
                    par3IEntitySelector));
        }

        return arraylist;
    }

    public List<Entity> getEntitiesWithinAABBExcludingEntityLocal(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB) {
        return this.getEntitiesWithinAABBExcludingEntityLocal(par1Entity, par2AxisAlignedBB, (IEntitySelector) null);
    }

    public List<Entity> getEntitiesWithinAABBExcludingEntityLocal(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB,
        IEntitySelector par3IEntitySelector) {
        if (par1Entity instanceof EntityPlayer) {
            par1Entity = ((IMixinEntity) par1Entity).getProxyPlayer(((World) (Object) this));
        }

        return super.getEntitiesWithinAABBExcludingEntity((Entity) par1Entity, par2AxisAlignedBB, par3IEntitySelector);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<Entity> selectEntitiesWithinAABB(Class par1Class, AxisAlignedBB par2AxisAlignedBB,
        IEntitySelector par3IEntitySelector) {
        ArrayList<Entity> arraylist = new ArrayList<Entity>();
        arraylist.addAll(this.selectEntitiesWithinAABBLocal(par1Class, par2AxisAlignedBB, par3IEntitySelector));
        Iterator<World> i$ = ((IMixinWorld) this).getSubWorlds()
            .iterator();

        while (i$.hasNext()) {
            World curSubWorld = (World) i$.next();
            arraylist.addAll(
                ((IMixinWorldIntermediate) curSubWorld).selectEntitiesWithinAABBLocal(
                    par1Class,
                    ((IMixinAxisAlignedBB) par2AxisAlignedBB).getTransformedToLocalBoundingBox(curSubWorld),
                    par3IEntitySelector));
        }

        return arraylist;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<Entity> selectEntitiesWithinAABBLocal(Class par1Class, AxisAlignedBB par2AxisAlignedBB,
        IEntitySelector par3IEntitySelector) {
        return super.selectEntitiesWithinAABB(par1Class, par2AxisAlignedBB, par3IEntitySelector);
    }

    public boolean spawnEntityInWorldIntermediate(Entity par1Entity) {
        boolean result = super.spawnEntityInWorld(par1Entity);
        World curSubWorld;
        Object proxyPlayer;
        if (!this.isRemote && !((IMixinWorld) this).isSubWorld() && par1Entity instanceof EntityPlayer) {
            for (Iterator<World> i$ = ((IMixinWorld) this).getSubWorlds()
                .iterator(); i$.hasNext(); curSubWorld.spawnEntityInWorld((Entity) proxyPlayer)) {
                curSubWorld = (World) i$.next();
                proxyPlayer = ((IMixinEntity) par1Entity).getProxyPlayer(curSubWorld);
                if (proxyPlayer == null) {
                    proxyPlayer = new EntityPlayerMPSubWorldProxy((EntityPlayerMP) par1Entity, curSubWorld);
                    ((EntityPlayerMP) proxyPlayer).theItemInWorldManager.setWorld((WorldServer) curSubWorld);
                }
            }
        }

        return result;
    }

    public void removeEntityIntermediate(Entity par1Entity) {
        super.removeEntity(par1Entity);
        if (!((World) (Object) this).isRemote && !((IMixinWorld) this).isSubWorld()
            && par1Entity instanceof EntityPlayer) {
            Iterator<World> i$ = ((IMixinWorld) this).getSubWorlds()
                .iterator();

            while (i$.hasNext()) {
                World curSubWorld = (World) i$.next();
                EntityPlayer proxyPlayer = ((IMixinEntity) par1Entity).getProxyPlayer(curSubWorld);
                if (proxyPlayer != null) {
                    curSubWorld.removeEntity(proxyPlayer);
                    if (!((WorldServer) curSubWorld).getPlayerManager().players.contains(proxyPlayer)) {
                        ((IMixinEntity) par1Entity).getPlayerProxyMap()
                            .remove(Integer.valueOf(((IMixinWorld) curSubWorld).getSubWorldID()));
                    }
                }
            }
        }
    }

    public void removePlayerEntityDangerously(Entity par1Entity) {
        super.removePlayerEntityDangerously(par1Entity);
        if (!((WorldServer) (Object) this).isRemote && !((IMixinWorld) this).isSubWorld()
            && par1Entity instanceof EntityPlayer) {
            Iterator<World> i$ = ((IMixinWorld) this).getSubWorlds()
                .iterator();

            while (i$.hasNext()) {
                World curSubWorld = (World) i$.next();
                EntityPlayer proxyPlayer = ((IMixinEntity) par1Entity).getProxyPlayer(curSubWorld);
                curSubWorld.removeEntity(proxyPlayer);
                if (!((WorldServer) curSubWorld).getPlayerManager().players.contains(proxyPlayer)) {
                    ((IMixinEntity) par1Entity).getPlayerProxyMap()
                        .remove(Integer.valueOf(((IMixinWorld) curSubWorld).getSubWorldID()));
                }
            }
        }
    }

}
