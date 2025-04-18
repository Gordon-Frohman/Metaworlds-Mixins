package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.client.multiplayer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.RenderList;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.client.entity.EntityClientPlayerMPSubWorldProxy;
import su.sergiusonesimus.metaworlds.client.multiplayer.SubWorldClientFactory;
import su.sergiusonesimus.metaworlds.entity.player.EntityPlayerMPSubWorldProxy;
import su.sergiusonesimus.metaworlds.world.SubWorldFactory;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.client.renderer.IMixinRenderGlobal;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.entity.IMixinEntity;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.server.management.IMixinPlayerManager;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorldIntermediate;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.util.IMixinAxisAlignedBB;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.util.IMixinMovingObjectPosition;
import su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.world.MixinWorld;

@Mixin(value = WorldClient.class)
public abstract class MixinWorldClient extends MixinWorld implements IMixinWorldIntermediate {

    private ArrayList collidingBBCacheIntermediate = new ArrayList();

    public int worldDimension;
    private static SubWorldFactory subWorldFactory = null;

    @Shadow(remap = true)
    private Minecraft mc;

    @Shadow(remap = true)
    private IntHashMap entityHashSet;

    @Shadow(remap = true)
    private Set entityList;

    @Shadow(remap = true)
    public NetHandlerPlayClient sendQueue;

    @Shadow(remap = true)
    private Set entitySpawnQueue;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void WorldClient(NetHandlerPlayClient p_i45063_1_, WorldSettings p_i45063_2_, int p_i45063_3_,
        EnumDifficulty p_i45063_4_, Profiler p_i45063_5_, CallbackInfo ci) {
        this.worldDimension = p_i45063_3_;
        if (subWorldFactory == null) subWorldFactory = new SubWorldClientFactory();
    }

    public World createSubWorld() {
        return this.createSubWorld(((IMixinWorld) this).getWorldsCount());
    }

    public World createSubWorld(int newSubWorldID) {
        if (this.subWorldFactory == null) return null;
        World newSubWorld = this.subWorldFactory.CreateSubWorld(((World) (Object) this), newSubWorldID);
        if (((IMixinWorld) this).getSubWorldsMap()
            .get(((IMixinWorld) newSubWorld).getSubWorldID()) == null) {
            ((IMixinWorld) this).getSubWorldsMap()
                .put(((IMixinWorld) newSubWorld).getSubWorldID(), newSubWorld);

            // The constructor assigns the proxy to the real player
            EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
            EntityClientPlayerMPSubWorldProxy proxyPlayer = new EntityClientPlayerMPSubWorldProxy(player, newSubWorld);
            proxyPlayer.getMinecraft().renderViewEntity = proxyPlayer;

            newSubWorld.addWorldAccess(proxyPlayer.getMinecraft().renderGlobal);

            ((IMixinRenderGlobal) this.mc.renderGlobal)
                .loadRenderersForNewSubWorld(((IMixinWorld) newSubWorld).getSubWorldID());
            this.mc.renderGlobal.allRenderLists = new RenderList[4 * ((IMixinWorld) this).getWorldsCount()];
            for (int i = 0; i < 4 * ((IMixinWorld) this).getWorldsCount(); ++i) {
                this.mc.renderGlobal.allRenderLists[i] = new RenderList();
            }
        }
        return newSubWorld;
    }

    public Minecraft getMinecraft() {
        return this.mc;
    }

    public void setMinecraft(Minecraft newMinecraft) {
        this.mc = newMinecraft;
    }

    public void setSubworldFactory(SubWorldFactory subWorldFactory) {
        this.subWorldFactory = subWorldFactory;
    }

    public SubWorldFactory getSubworldFactory() {
        return subWorldFactory;
    }

    /**
     * Runs a single tick for the world
     */
    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        for (World curSubWorld : ((IMixinWorld) this).getSubWorlds()) {
            curSubWorld.tick();
        }
    }

    @WrapOperation(
        method = "getEntityByID",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/util/IntHashMap;lookup(I)Ljava/lang/Object;"))
    private Object wrapEntityHashSetLookup(IntHashMap instance, int value, Operation<Object> original) {
        Entity result = (Entity) original.call(instance, value);
        if (result == null && !((IMixinWorld) this).isSubWorld())
            for (World curWorld : ((IMixinWorld) this).getSubWorlds()) {
                result = curWorld.getEntityByID(value);
                if (result != null) return result;
            }
        return result;
    }

    @Inject(method = "removeEntityFromWorld", at = { @At(value = "RETURN") })
    public void injectRemoveEntityFromWorld(int entityId, CallbackInfoReturnable<Entity> ci, @Local Entity entity) {
        if (entity == null && !((IMixinWorld) this).isSubWorld()) {
            for (World curSubWorld : ((IMixinWorld) this).getSubWorlds())
                ((WorldClient) curSubWorld).removeEntityFromWorld(entityId);
        }
    }

    @Inject(method = "doVoidFogParticles", at = @At("TAIL"))
    public void doVoidFogParticles(int p_73029_1_, int p_73029_2_, int p_73029_3_, CallbackInfo ci) {
        for (World curSubWorld : ((IMixinWorld) this).getSubWorlds()) {
            Vec3 transformedPos = ((IMixinWorld) curSubWorld).transformToLocal(p_73029_1_, p_73029_2_, p_73029_3_);
            ((WorldClient) curSubWorld).doVoidFogParticles(
                MathHelper.floor_double(transformedPos.xCoord),
                MathHelper.floor_double(transformedPos.yCoord),
                MathHelper.floor_double(transformedPos.zCoord));
        }
    }

    public NetHandlerPlayClient getSendQueue() {
        return this.sendQueue;
    }

    public MovingObjectPosition func_147447_a(Vec3 par1Vec3, Vec3 par2Vec3, boolean par3, boolean par4, boolean par5) {
        MovingObjectPosition bestResult = null;
        Vec3 vecSource = ((IMixinWorld) this).transformToGlobal(par1Vec3);
        Vec3 vecDest = ((IMixinWorld) this).transformToGlobal(par2Vec3);
        Iterator i$ = ((IMixinWorld) ((IMixinWorld) this).getParentWorld()).getWorlds()
            .iterator();

        while (i$.hasNext()) {
            World curWorld = (World) i$.next();
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
                    .transformLocalToOther((WorldClient) (Object) this, curResult.hitVec);
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

    public List getCollidingBoundingBoxes(Entity entity, AxisAlignedBB aabb) {
        this.collidingBBCacheIntermediate.clear();
        this.collidingBBCacheIntermediate = (ArrayList) this.getCollidingBoundingBoxesLocal(entity, aabb);
        Iterator i$ = ((IMixinWorld) this).getSubWorlds()
            .iterator();

        while (i$.hasNext()) {
            World curSubWorld = (World) i$.next();
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

    public List getCollidingBoundingBoxesLocal(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB) {
        return super.getCollidingBoundingBoxes(par1Entity, par2AxisAlignedBB);
    }

    public boolean isMaterialInBB(AxisAlignedBB par1AxisAlignedBB, Material par2Material) {
        if (this.isMaterialInBBLocal(par1AxisAlignedBB, par2Material)) {
            return true;
        } else {
            if (!((IMixinWorld) this).isSubWorld()) {
                Iterator i$ = ((IMixinWorld) this).getSubWorlds()
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
                Iterator i$ = ((IMixinWorld) this).getSubWorlds()
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

    public List getEntitiesWithinAABBExcludingEntity(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB,
        IEntitySelector par3IEntitySelector) {
        ArrayList arraylist = new ArrayList();
        arraylist
            .addAll(this.getEntitiesWithinAABBExcludingEntityLocal(par1Entity, par2AxisAlignedBB, par3IEntitySelector));
        Iterator i$ = ((IMixinWorld) this).getSubWorlds()
            .iterator();

        while (i$.hasNext()) {
            World curSubWorld = (World) i$.next();
            arraylist.addAll(
                ((IMixinWorld) curSubWorld).getEntitiesWithinAABBExcludingEntityLocal(
                    par1Entity,
                    ((IMixinAxisAlignedBB) par2AxisAlignedBB).getTransformedToLocalBoundingBox(curSubWorld),
                    par3IEntitySelector));
        }

        return arraylist;
    }

    public List getEntitiesWithinAABBExcludingEntityLocal(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB) {
        return this.getEntitiesWithinAABBExcludingEntityLocal(par1Entity, par2AxisAlignedBB, (IEntitySelector) null);
    }

    public List getEntitiesWithinAABBExcludingEntityLocal(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB,
        IEntitySelector par3IEntitySelector) {
        if (par1Entity instanceof EntityPlayer) {
            par1Entity = ((IMixinEntity) par1Entity).getProxyPlayer(((WorldClient) (Object) this));
        }

        return super.getEntitiesWithinAABBExcludingEntity((Entity) par1Entity, par2AxisAlignedBB, par3IEntitySelector);
    }

    public List selectEntitiesWithinAABB(Class par1Class, AxisAlignedBB par2AxisAlignedBB,
        IEntitySelector par3IEntitySelector) {
        ArrayList arraylist = new ArrayList();
        arraylist.addAll(this.selectEntitiesWithinAABBLocal(par1Class, par2AxisAlignedBB, par3IEntitySelector));
        Iterator i$ = ((IMixinWorld) this).getSubWorlds()
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

    public List selectEntitiesWithinAABBLocal(Class par1Class, AxisAlignedBB par2AxisAlignedBB,
        IEntitySelector par3IEntitySelector) {
        return super.selectEntitiesWithinAABB(par1Class, par2AxisAlignedBB, par3IEntitySelector);
    }

    @Redirect(
        method = "spawnEntityInWorld",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;spawnEntityInWorld(Lnet/minecraft/entity/Entity;)Z"))
    public boolean spawnEntityInWorld(World world, Entity entity) {
        return spawnEntityInWorldIntermediate(entity);
    }

    public boolean spawnEntityInWorldIntermediate(Entity par1Entity) {
        boolean result = super.spawnEntityInWorld(par1Entity);
        World curSubWorld;
        Object proxyPlayer;
        if (!((World) (Object) this).isRemote && !((IMixinWorld) this).isSubWorld()
            && par1Entity instanceof EntityPlayer) {
            for (Iterator i$ = ((IMixinWorld) this).getSubWorlds()
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

    @Redirect(
        method = "removeEntity",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;removeEntity(Lnet/minecraft/entity/Entity;)V"))
    public void removeEntity(World world, Entity entity) {
        removeEntityIntermediate(entity);
    }

    public void removeEntityIntermediate(Entity par1Entity) {
        super.removeEntity(par1Entity);
        if (!((World) (Object) this).isRemote && !((IMixinWorld) this).isSubWorld()
            && par1Entity instanceof EntityPlayer) {
            Iterator i$ = ((IMixinWorld) this).getSubWorlds()
                .iterator();

            while (i$.hasNext()) {
                World curSubWorld = (World) i$.next();
                EntityPlayer proxyPlayer = ((IMixinEntity) par1Entity).getProxyPlayer(curSubWorld);
                if (proxyPlayer != null) {
                    curSubWorld.removeEntity(proxyPlayer);
                    if (!((IMixinPlayerManager) ((WorldServer) curSubWorld).getPlayerManager()).getPlayers()
                        .contains(proxyPlayer)) {
                        ((IMixinEntity) par1Entity).getPlayerProxyMap()
                            .remove(Integer.valueOf(((IMixinWorld) curSubWorld).getSubWorldID()));
                    }
                }
            }
        }
    }

    public void removePlayerEntityDangerously(Entity par1Entity) {
        super.removePlayerEntityDangerously(par1Entity);
        if (!((WorldClient) (Object) this).isRemote && !((IMixinWorld) this).isSubWorld()
            && par1Entity instanceof EntityPlayer) {
            Iterator i$ = ((IMixinWorld) this).getSubWorlds()
                .iterator();

            while (i$.hasNext()) {
                World curSubWorld = (World) i$.next();
                EntityPlayer proxyPlayer = ((IMixinEntity) par1Entity).getProxyPlayer(curSubWorld);
                curSubWorld.removeEntity(proxyPlayer);
                if (!((IMixinPlayerManager) ((WorldServer) curSubWorld).getPlayerManager()).getPlayers()
                    .contains(proxyPlayer)) {
                    ((IMixinEntity) par1Entity).getPlayerProxyMap()
                        .remove(Integer.valueOf(((IMixinWorld) curSubWorld).getSubWorldID()));
                }
            }
        }
    }

    @ModifyArgs(
        method = "playSound",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/audio/PositionedSoundRecord;<init>(Lnet/minecraft/util/ResourceLocation;FFFFF)V"))
    private void modifyPositionedSoundRecord(Args args) {
        Vec3 globalPosition = this.transformToGlobal((float) args.get(3), (float) args.get(4), (float) args.get(5));
        args.set(3, (float) globalPosition.xCoord);
        args.set(4, (float) globalPosition.yCoord);
        args.set(5, (float) globalPosition.zCoord);
    }

}
