package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.world;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.entity.player.EntityPlayerMPSubWorldProxy;
import su.sergiusonesimus.metaworlds.world.SubWorldFactory;
import su.sergiusonesimus.metaworlds.world.SubWorldInfoHolder;
import su.sergiusonesimus.metaworlds.world.SubWorldServerFactory;
import su.sergiusonesimus.metaworlds.world.WorldManagerSubWorld;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.entity.IMixinEntity;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.server.IMixinMinecraftServer;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.server.management.IMixinPlayerManager;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorldIntermediate;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.storage.IMixinWorldInfo;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.util.IMixinAxisAlignedBB;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.util.IMixinMovingObjectPosition;

@Mixin(WorldServer.class)
public abstract class MixinWorldServer extends MixinWorld implements IMixinWorldIntermediate {

    private ArrayList collidingBBCacheIntermediate = new ArrayList();

    private int worldDimension;

    private static SubWorldFactory subWorldFactory = null;

    // TODO

    @Inject(method = "<init>", at = @At("TAIL"))
    public void WorldServer(MinecraftServer p_i45284_1_, ISaveHandler p_i45284_2_, String p_i45284_3_, int p_i45284_4_,
        WorldSettings p_i45284_5_, Profiler p_i45284_6_, CallbackInfo ci) {
        this.worldDimension = p_i45284_4_;
        if (subWorldFactory == null) subWorldFactory = new SubWorldServerFactory();
    }

    @Redirect(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/common/DimensionManager;setWorld(ILnet/minecraft/world/WorldServer;)V"))
    public void setWorld(int par1int, WorldServer par2worldServer) {
        if (!((IMixinWorld) par2worldServer).isSubWorld()) DimensionManager.setWorld(par1int, par2worldServer);
    }

    @Redirect(
        method = "tick()V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/WorldInfo;setWorldTime(J)V"))
    public void setWorldTimeRedirected(WorldInfo worldInfo, long par1long) {
        this.setWorldTime(par1long);
    }

    @Redirect(
        method = "tick()V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/WorldInfo;incrementTotalWorldTime(J)V"))
    public void incrementTotalWorldTime(WorldInfo worldInfo, long par1long) {
        this.func_82738_a(this.getTotalWorldTime() + 1L);
    }

    @Redirect(
        method = "tick()V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/GameRules;getGameRuleBooleanValue(Ljava/lang/String;)Z"))
    public boolean getGameRuleBooleanValue(GameRules gameRules, String string) {
        if (string == "doMobSpawning") return !this.isSubWorld() && this.getGameRules()
            .getGameRuleBooleanValue("doMobSpawning");
        else return gameRules.getGameRuleBooleanValue(string);
    }

    @Inject(method = "saveAllChunks(ZLnet/minecraft/util/IProgressUpdate;)V", at = @At("TAIL"))
    public void saveAllChunks(boolean par1, IProgressUpdate par2IProgressUpdate, CallbackInfo ci) {
        if (!this.isSubWorld()) {
            for (World curSubWorld : this.getSubWorlds()) try {
                ((WorldServer) curSubWorld).saveAllChunks(par1, par2IProgressUpdate);
            } catch (MinecraftException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public World createSubWorld() {
        return this.createSubWorld(((IMixinWorld) this).getUnoccupiedSubworldID());
    }

    public World createSubWorld(int newSubWorldID) {
        if (this.subWorldFactory == null) return null;

        World newSubWorld = this.subWorldFactory.CreateSubWorld((World) (Object) this, newSubWorldID);
        if (((IMixinMinecraftServer) MinecraftServer.mcServer).getExistingSubWorlds()
            .put(((IMixinWorld) newSubWorld).getSubWorldID(), newSubWorld) != null) {
            throw new IllegalArgumentException("SubWorld with this ID already exists!");
        } else this.childSubWorlds.put(((IMixinWorld) newSubWorld).getSubWorldID(), newSubWorld);

        newSubWorld.worldInfo = this.worldInfo;
        ((WorldServer) newSubWorld).difficultySetting = EnumDifficulty.EASY;// Fixes AI crashes
        // newSubWorld.playerEntities = this.playerEntities;//Instead we create proxies for every player in every
        // subworld now

        SubWorldInfoHolder curSubWorldInfo = ((IMixinWorldInfo) DimensionManager.getWorld(0)
            .getWorldInfo()).getSubWorldInfo(((IMixinWorld) newSubWorld).getSubWorldID());
        if (curSubWorldInfo != null) curSubWorldInfo.applyToSubWorld((SubWorld) newSubWorld);

        try {
            Class[] cArg = new Class[1];
            cArg[0] = World.class;
            Method loadWorldMethod = ForgeChunkManager.class.getDeclaredMethod("loadWorld", cArg);
            loadWorldMethod.setAccessible(true);
            try {
                loadWorldMethod.invoke(null, newSubWorld);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (NoSuchMethodException e) {
            System.out.println(e.toString());
        }

        // ForgeChunkManager.loadWorld(newSubWorld);

        newSubWorld.addWorldAccess(
            new WorldManagerSubWorld(((WorldServer) newSubWorld).func_73046_m(), (WorldServer) newSubWorld));

        for (Object curPlayer : this.playerEntities) {
            EntityPlayerMPSubWorldProxy proxyPlayer = (EntityPlayerMPSubWorldProxy) ((IMixinEntity) curPlayer)
                .getPlayerProxyMap()
                .get(((IMixinWorld) newSubWorld).getSubWorldID());

            if (proxyPlayer == null) {
                proxyPlayer = new EntityPlayerMPSubWorldProxy((EntityPlayerMP) curPlayer, newSubWorld);
                // TODO: newManager.setGameType(this.getGameType()); make this synchronized over all proxies and the
                // real player
            }

            proxyPlayer.theItemInWorldManager.setWorld((WorldServer) newSubWorld);

            newSubWorld.spawnEntityInWorld(proxyPlayer);
        }

        for (Object curPlayer : ((WorldServer) (Object) this).getPlayerManager().players) {
            EntityPlayerMPSubWorldProxy proxyPlayer = (EntityPlayerMPSubWorldProxy) ((IMixinEntity) curPlayer)
                .getPlayerProxyMap()
                .get(((IMixinWorld) newSubWorld).getSubWorldID());

            if (proxyPlayer == null) {
                proxyPlayer = new EntityPlayerMPSubWorldProxy((EntityPlayerMP) curPlayer, newSubWorld);
                // TODO: newManager.setGameType(this.getGameType()); make this synchronized over all proxies and the
                // real player
            }

            proxyPlayer.theItemInWorldManager.setWorld((WorldServer) newSubWorld);// make sure the right one is assigned
                                                                                  // if the player is not in
                                                                                  // playerEntities somehow

            ((WorldServer) newSubWorld).getPlayerManager()
                .addPlayer(proxyPlayer);
        }

        MinecraftForge.EVENT_BUS.post(new WorldEvent.Load(newSubWorld));

        return newSubWorld;
    }

    public int getDimension() {
        return this.worldDimension;
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
                    .transformLocalToOther((WorldServer) (Object) this, curResult.hitVec);
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
                if (aabb.maxX - aabb.minX == entity.boundingBox.maxX - entity.boundingBox.minX) {
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
            par1Entity = ((IMixinEntity) par1Entity).getProxyPlayer(((WorldServer) (Object) this));
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

    public boolean spawnEntityInWorld(Entity par1Entity) {
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

    public void removeEntity(Entity par1Entity) {
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
        if (!((WorldServer) (Object) this).isRemote && !((IMixinWorld) this).isSubWorld()
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

}
