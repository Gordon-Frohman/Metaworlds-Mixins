package su.sergiusonesimus.metaworlds.client.multiplayer;

import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Facing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;

import org.jblas.DoubleMatrix;

import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.api.SubWorldTypeManager;
import su.sergiusonesimus.metaworlds.client.renderer.RenderGlobalSubWorld;
import su.sergiusonesimus.metaworlds.compat.packet.SubWorldUpdatePacket;
import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.metaworlds.util.SubWorldTransformationHandler;
import su.sergiusonesimus.metaworlds.world.chunk.DirectionalChunk;
import su.sergiusonesimus.metaworlds.world.chunk.DirectionalChunkProvider;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.client.renderer.IMixinRenderGlobal;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.IMixinEntity;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.util.IMixinAxisAlignedBB;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

public class SubWorldClient extends WorldClient implements SubWorld {

    private WorldClient m_parentWorld;
    private int subWorldID;
    private ArrayList collidingBBCache = new ArrayList();
    private SubWorldTransformationHandler transformationHandler = new SubWorldTransformationHandler(this);
    private DirectionalChunkProvider directionalChunkProvider = new DirectionalChunkProvider(this);
    private Vec3 lightVector = Vec3.createVectorHelper(0, 1, 0);
    public int localTickCounter;
    public int lastServerTickReceived;
    public float serverTickDiff;
    private double lastTickX;
    private double lastTickY;
    private double lastTickZ;
    private double lastTickRotationYaw;
    private double lastTickRotationPitch;
    private double lastTickRotationRoll;
    private double lastTickScaling = 1.0D;
    private double nextTickX;
    private double nextTickY;
    private double nextTickZ;
    private double nextTickRotationYaw;
    private double nextTickRotationPitch;
    private double nextTickRotationRoll;
    private double nextTickScaling = 1.0D;
    private double prevRendererUpdateX;
    private double prevRendererUpdateY;
    private double prevRendererUpdateZ;
    private double prevRendererRotation;
    private double prevRendererScaling;
    private Map<Entity, Vec3> entitiesToDrag = new TreeMap();
    private Map<Entity, Vec3> entitiesToNotDrag = new TreeMap();
    private ChunkCoordinates minCoordinates = new ChunkCoordinates();
    private ChunkCoordinates maxCoordinates = new ChunkCoordinates();
    private double maxRadius = 0.0D;
    private String subWorldType = SubWorldTypeManager.SUBWORLD_TYPE_DEFAULT;
    private RenderGlobalSubWorld renderGlobalSubWorld;
    private SubWorldUpdatePacket updatePacketToHandle;
    private List entitiesWithinAABBExcludingEntityResult = new ArrayList();

    public boolean canUpdate = false;

    public SubWorldClient(WorldClient parentWorld, int newSubWorldID, NetHandlerPlayClient par1NetClientHandler,
        WorldSettings par2WorldSettings, int par3, EnumDifficulty par4, Profiler par5Profiler) {
        super(par1NetClientHandler, par2WorldSettings, par3, par4, par5Profiler);
        this.m_parentWorld = parentWorld;
        this.isRemote = true;
        this.subWorldID = newSubWorldID;
        this.setRotationYaw(0.0D);
        this.setTranslation(0.0D, 0.0D, 0.0D);
        this.lastTickX = 0.0D;
        this.lastTickY = 0.0D;
        this.lastTickZ = 0.0D;
        this.lastTickRotationYaw = 0.0D;
        this.lastTickScaling = 1.0D;
        this.localTickCounter = 0;
        this.lastServerTickReceived = -1;
        this.serverTickDiff = -1.0F;
        this.minCoordinates.set(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        this.maxCoordinates.set(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    public World CreateSubWorld() {
        return ((IMixinWorld) this.m_parentWorld).createSubWorld();
    }

    public World CreateSubWorld(int newSubWorldID) {
        return ((IMixinWorld) this.m_parentWorld).createSubWorld(newSubWorldID);
    }

    public void removeSubWorld() {
        ((IMixinWorld) this.m_parentWorld).getSubWorlds()
            .remove(this);
        ((IMixinRenderGlobal) Minecraft.getMinecraft().renderGlobal).unloadRenderersForSubWorld(this.getSubWorldID());
        if (this.renderGlobalSubWorld != null) {
            this.renderGlobalSubWorld.onWorldRemove();
        }
    }

    public SubWorldUpdatePacket getUpdatePacketToHandle() {
        return this.updatePacketToHandle;
    }

    public void setUpdatePacketToHandle(SubWorldUpdatePacket newPacket) {
        this.updatePacketToHandle = newPacket;
    }

    public void addWorldAccess(IWorldAccess par1IWorldAccess) {
        super.addWorldAccess(par1IWorldAccess);
        if (par1IWorldAccess instanceof RenderGlobalSubWorld) {
            this.renderGlobalSubWorld = (RenderGlobalSubWorld) par1IWorldAccess;
        }
    }

    public RenderGlobal getRenderGlobal() {
        return this.renderGlobalSubWorld;
    }

    public boolean rendererUpdateRequired() {
        double dX = this.getTranslationX() - this.prevRendererUpdateX;
        double dY = this.getTranslationY() - this.prevRendererUpdateY;
        double dZ = this.getTranslationZ() - this.prevRendererUpdateZ;
        double dSsq = dX * dX + dY * dY + dZ * dZ;
        double dScale = this.maxRadius * Math.abs(this.getScaling() - this.prevRendererScaling);
        return dSsq > 256.0D || Math.sqrt(dSsq) + dScale
            + Math.min(Math.abs(this.getRotationYaw() - this.prevRendererRotation), 180.0D) * Math.PI
                / 180.0D
                * this.maxRadius
            > 16.0D;
    }

    public void markRendererUpdateDone() {
        this.prevRendererUpdateX = this.getTranslationX();
        this.prevRendererUpdateY = this.getTranslationY();
        this.prevRendererUpdateZ = this.getTranslationZ();
        this.prevRendererRotation = this.getRotationYaw();
        this.prevRendererScaling = this.getScaling();
    }

    public World getParentWorld() {
        return this.m_parentWorld;
    }

    public int getSubWorldID() {
        return this.subWorldID;
    }

    public String getSubWorldType() {
        return this.subWorldType;
    }

    public void setSubWorldType(String newType) {
        this.subWorldType = newType;
    }

    public List getEntitiesWithinAABBExcludingEntity(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB,
        IEntitySelector par3IEntitySelector) {
        this.entitiesWithinAABBExcludingEntityResult.clear();
        this.entitiesWithinAABBExcludingEntityResult.addAll(
            ((IMixinWorld) this)
                .getEntitiesWithinAABBExcludingEntityLocal(par1Entity, par2AxisAlignedBB, par3IEntitySelector));
        AxisAlignedBB globalBB = ((IMixinAxisAlignedBB) par2AxisAlignedBB).getTransformedToGlobalBoundingBox(this);
        this.entitiesWithinAABBExcludingEntityResult.addAll(
            ((IMixinWorld) this.m_parentWorld)
                .getEntitiesWithinAABBExcludingEntityLocal(par1Entity, globalBB, par3IEntitySelector));
        Iterator i$ = ((IMixinWorld) this.m_parentWorld).getSubWorlds()
            .iterator();

        while (i$.hasNext()) {
            World curSubWorld = (World) i$.next();
            if (curSubWorld != this) {
                this.entitiesWithinAABBExcludingEntityResult.addAll(
                    ((IMixinWorld) curSubWorld).getEntitiesWithinAABBExcludingEntityLocal(
                        par1Entity,
                        ((IMixinAxisAlignedBB) globalBB).getTransformedToLocalBoundingBox(curSubWorld),
                        par3IEntitySelector));
            }
        }

        return this.entitiesWithinAABBExcludingEntityResult;
    }

    public boolean checkNoEntityCollision(AxisAlignedBB par1AxisAlignedBB, Entity par2Entity) {
        return super.checkNoEntityCollision(par1AxisAlignedBB, par2Entity) ? this.m_parentWorld.checkNoEntityCollision(
            ((IMixinAxisAlignedBB) par1AxisAlignedBB).getTransformedToGlobalBoundingBox(this),
            par2Entity) : false;
    }

    public double getTranslationX() {
        return this.transformationHandler.getTranslationX();
    }

    public double getTranslationY() {
        return this.transformationHandler.getTranslationY();
    }

    public double getTranslationZ() {
        return this.transformationHandler.getTranslationZ();
    }

    public double getCenterX() {
        return this.transformationHandler.getCenterX();
    }

    public double getCenterY() {
        return this.transformationHandler.getCenterY();
    }

    public double getCenterZ() {
        return this.transformationHandler.getCenterZ();
    }

    public double getRotationYaw() {
        return this.transformationHandler.getRotationYaw();
    }

    public double getRotationPitch() {
        return this.transformationHandler.getRotationPitch();
    }

    public double getRotationRoll() {
        return this.transformationHandler.getRotationRoll();
    }

    public double getCosRotationYaw() {
        return this.transformationHandler.getCosRotationYaw();
    }

    public double getSinRotationYaw() {
        return this.transformationHandler.getSinRotationYaw();
    }

    public double getCosRotationPitch() {
        return this.transformationHandler.getCosRotationPitch();
    }

    public double getSinRotationPitch() {
        return this.transformationHandler.getSinRotationPitch();
    }

    public double getCosRotationRoll() {
        return this.transformationHandler.getCosRotationRoll();
    }

    public double getSinRotationRoll() {
        return this.transformationHandler.getSinRotationRoll();
    }

    public double getScaling() {
        return this.transformationHandler.getScaling();
    }

    public void setTranslation(Vec3 newTranslation) {
        this.transformationHandler.setTranslation(newTranslation);
    }

    public void setTranslation(double newX, double newY, double newZ) {
        this.transformationHandler.setTranslation(newX, newY, newZ);
    }

    public void setCenter(Vec3 newCenter) {
        this.transformationHandler.setCenter(newCenter);
    }

    public void setCenter(double newX, double newY, double newZ) {
        this.transformationHandler.setCenter(newX, newY, newZ);
    }

    public void setCenterOnCreate(double newX, double newY, double newZ) {
        this.transformationHandler.setCenterOnCreate(newX, newY, newZ);
    }

    public void setRotationYaw(double newYaw) {
        this.transformationHandler.setRotationYaw(newYaw);
    }

    public void setRotationPitch(double newPitch) {
        this.transformationHandler.setRotationPitch(newPitch);
    }

    public void setRotationRoll(double newRoll) {
        this.transformationHandler.setRotationRoll(newRoll);
    }

    public void setScaling(double newScaling) {
        this.transformationHandler.setScaling(newScaling);
    }

    public double getMotionX() {
        return this.transformationHandler.getMotionX();
    }

    public double getMotionY() {
        return this.transformationHandler.getMotionY();
    }

    public double getMotionZ() {
        return this.transformationHandler.getMotionZ();
    }

    public double getRotationYawSpeed() {
        return this.transformationHandler.getRotationYawSpeed();
    }

    public double getRotationPitchSpeed() {
        return this.transformationHandler.getRotationPitchSpeed();
    }

    public double getRotationRollSpeed() {
        return this.transformationHandler.getRotationRollSpeed();
    }

    public double getScaleChangeRate() {
        return this.transformationHandler.getScaleChangeRate();
    }

    public void setMotion(double par1MotionX, double par2MotionY, double par3MotionZ) {
        this.transformationHandler.setMotion(par1MotionX, par2MotionY, par3MotionZ);
    }

    public void setRotationYawSpeed(double par1Speed) {
        this.transformationHandler.setRotationYawSpeed(par1Speed);
    }

    public void setRotationPitchSpeed(double par1Speed) {
        this.transformationHandler.setRotationPitchSpeed(par1Speed);
    }

    public void setRotationRollSpeed(double par1Speed) {
        this.transformationHandler.setRotationRollSpeed(par1Speed);
    }

    public void setScaleChangeRate(double par1Rate) {
        this.transformationHandler.setScaleChangeRate(par1Rate);
    }

    public DoubleBuffer getTransformToLocalMatrixDirectBuffer() {
        return this.transformationHandler.getTransformToLocalMatrixDirectBuffer();
    }

    public DoubleBuffer getTransformToGlobalMatrixDirectBuffer() {
        return this.transformationHandler.getTransformToGlobalMatrixDirectBuffer();
    }

    public Vec3 transformToLocal(Vec3 globalVec) {
        return this.transformationHandler.transformToLocal(globalVec);
    }

    public Vec3 transformToLocal(double globalX, double globalY, double globalZ) {
        return this.transformationHandler.transformToLocal(globalX, globalY, globalZ);
    }

    public DoubleMatrix transformToLocal(DoubleMatrix globalVectors) {
        return this.transformationHandler.transformToLocal(globalVectors);
    }

    public DoubleMatrix transformToLocal(DoubleMatrix globalVectors, DoubleMatrix result) {
        return this.transformationHandler.transformToLocal(globalVectors, result);
    }

    public Vec3 transformToGlobal(Vec3 localVec) {
        return this.transformationHandler.transformToGlobal(localVec);
    }

    public Vec3 transformToGlobal(double localX, double localY, double localZ) {
        return this.transformationHandler.transformToGlobal(localX, localY, localZ);
    }

    public DoubleMatrix transformToGlobal(DoubleMatrix localVectors) {
        return this.transformationHandler.transformToGlobal(localVectors);
    }

    public DoubleMatrix transformToGlobal(DoubleMatrix localVectors, DoubleMatrix result) {
        return this.transformationHandler.transformToGlobal(localVectors, result);
    }

    public Vec3 transformLocalToOther(World targetWorld, Vec3 localVec) {
        return this.transformationHandler.transformLocalToOther(targetWorld, localVec);
    }

    public Vec3 transformLocalToOther(World targetWorld, double localX, double localY, double localZ) {
        return this.transformationHandler.transformLocalToOther(targetWorld, localX, localY, localZ);
    }

    public DoubleMatrix transformLocalToOther(World targetWorld, DoubleMatrix localVectors) {
        return this.transformationHandler.transformLocalToOther(targetWorld, localVectors);
    }

    public DoubleMatrix transformLocalToOther(World targetWorld, DoubleMatrix localVectors, DoubleMatrix result) {
        return this.transformationHandler.transformLocalToOther(targetWorld, localVectors, result);
    }

    public Vec3 transformOtherToLocal(World sourceWorld, Vec3 otherVec) {
        return this.transformationHandler.transformOtherToLocal(sourceWorld, otherVec);
    }

    public Vec3 transformOtherToLocal(World sourceWorld, double otherX, double otherY, double otherZ) {
        return this.transformationHandler.transformOtherToLocal(sourceWorld, otherX, otherY, otherZ);
    }

    public DoubleMatrix transformOtherToLocal(World sourceWorld, DoubleMatrix otherVectors) {
        return this.transformationHandler.transformOtherToLocal(sourceWorld, otherVectors);
    }

    public DoubleMatrix transformOtherToLocal(World sourceWorld, DoubleMatrix otherVectors, DoubleMatrix result) {
        return this.transformationHandler.transformOtherToLocal(sourceWorld, otherVectors, result);
    }

    public Vec3 rotateToGlobal(Vec3 localVec) {
        return this.transformationHandler.rotateToGlobal(localVec);
    }

    public Vec3 rotateToGlobal(double localX, double localY, double localZ) {
        return this.transformationHandler.rotateToGlobal(localX, localY, localZ);
    }

    public DoubleMatrix rotateToGlobal(DoubleMatrix localVectors) {
        return this.transformationHandler.rotateToGlobal(localVectors);
    }

    public Vec3 rotateToLocal(Vec3 globalVec) {
        return this.transformationHandler.rotateToLocal(globalVec);
    }

    public Vec3 rotateToLocal(double globalX, double globalY, double globalZ) {
        return this.transformationHandler.rotateToLocal(globalX, globalY, globalZ);
    }

    public DoubleMatrix rotateToLocal(DoubleMatrix globalVectors) {
        return this.transformationHandler.rotateToLocal(globalVectors);
    }

    public Vec3 rotateYawToGlobal(Vec3 localVec) {
        return this.transformationHandler.rotateYawToGlobal(localVec);
    }

    public Vec3 rotateYawToGlobal(double localX, double localY, double localZ) {
        return this.transformationHandler.rotateYawToGlobal(localX, localY, localZ);
    }

    public DoubleMatrix rotateYawToGlobal(DoubleMatrix localVectors) {
        return this.transformationHandler.rotateYawToGlobal(localVectors);
    }

    public Vec3 rotateYawToLocal(Vec3 globalVec) {
        return this.transformationHandler.rotateYawToLocal(globalVec);
    }

    public Vec3 rotateYawToLocal(double globalX, double globalY, double globalZ) {
        return this.transformationHandler.rotateYawToLocal(globalX, globalY, globalZ);
    }

    public DoubleMatrix rotateYawToLocal(DoubleMatrix globalVectors) {
        return this.transformationHandler.rotateYawToLocal(globalVectors);
    }

    public int getMinX() {
        return this.minCoordinates.posX;
    }

    public int getMinY() {
        return this.minCoordinates.posY;
    }

    public int getMinZ() {
        return this.minCoordinates.posZ;
    }

    public ChunkCoordinates getMinCoordinates() {
        return this.minCoordinates;
    }

    public int getMaxX() {
        return this.maxCoordinates.posX;
    }

    public int getMaxY() {
        return this.maxCoordinates.posY;
    }

    public int getMaxZ() {
        return this.maxCoordinates.posZ;
    }

    public ChunkCoordinates getMaxCoordinates() {
        return this.maxCoordinates;
    }

    public void setBoundaries(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        if (this.getMinX() != minX || this.getMinY() != minY
            || this.getMinZ() != minZ
            || this.getMaxX() != maxX
            || this.getMaxY() != maxY
            || this.getMaxZ() != maxZ) {
            this.minCoordinates.posX = minX;
            this.minCoordinates.posY = minY;
            this.minCoordinates.posZ = minZ;
            this.maxCoordinates.posX = maxX;
            this.maxCoordinates.posY = maxY;
            this.maxCoordinates.posZ = maxZ;
            double minXSq = (double) (minX * minX);
            double minYSq = (double) (minY * minY);
            double minZSq = (double) (minZ * minZ);
            double maxXSq = (double) (maxX * maxX);
            double maxYSq = (double) (maxY * maxY);
            double maxZSq = (double) (maxZ * maxZ);
            double l1 = minXSq + minZSq + Math.max(minYSq, maxYSq);
            double l2 = minXSq + maxZSq + Math.max(minYSq, maxYSq);
            double l3 = maxXSq + minZSq + Math.max(minYSq, maxYSq);
            double l4 = maxXSq + maxZSq + Math.max(minYSq, maxYSq);
            double lmaxSq = Math.max(Math.max(l1, l2), Math.max(l3, l4));
            if (lmaxSq > this.maxRadius * this.maxRadius) {
                this.maxRadius = Math.sqrt(lmaxSq);
            }

            EntityLivingBase playerEntity = Minecraft.getMinecraft().renderViewEntity;
            ((IMixinRenderGlobal) Minecraft.getMinecraft().renderGlobal).markRenderersForNewPositionSingle(
                playerEntity.posX,
                playerEntity.posY,
                playerEntity.posZ,
                this.getSubWorldID());
        }
    }

    public void playAuxSFXAtEntity(EntityPlayer par1EntityPlayer, int par2, int par3, int par4, int par5, int par6) {
        super.playAuxSFXAtEntity(par1EntityPlayer, par2, par3, par4, par5, par6);
    }

    public void func_82738_a(long par1) {}

    public void setWorldTime(long par1) {}

    public boolean spawnEntityInWorld(Entity par1Entity) {
        return super.spawnEntityInWorld(par1Entity);
    }

    public Map<Entity, Vec3> getEntitiesToDrag() {
        return this.entitiesToDrag;
    }

    public void registerEntityToDrag(Entity targetEntity) {
        if (targetEntity instanceof Entity && targetEntity.worldObj != this) {
            this.entitiesToDrag.put(targetEntity, (Vec3) null);
        }
    }

    public void unregisterEntityToDrag(Entity targetEntity) {
        if (targetEntity instanceof Entity && targetEntity.worldObj != this) {
            this.entitiesToDrag.remove(targetEntity);
        }
    }

    public void registerDetachedEntity(Entity targetEntity) {
        if (targetEntity instanceof Entity && targetEntity.worldObj == this) {
            this.entitiesToNotDrag.put(targetEntity, (Vec3) null);
        }
    }

    public void unregisterDetachedEntity(Entity targetEntity) {
        if (targetEntity instanceof Entity && targetEntity.worldObj == this) {
            this.entitiesToNotDrag.remove(targetEntity);
        }
    }

    public void tick() {
        ++this.localTickCounter;
        super.tick();
        if (this.getUpdatePacketToHandle() != null) {
            this.getUpdatePacketToHandle()
                .executeOnTick();
            this.setUpdatePacketToHandle((SubWorldUpdatePacket) null);
        }

        this.tickPosition(1);
    }

    public void tickPosition(int count) {
        if (count != 0) {
            if (this.transformationHandler.getIsInMotion()) {
                double prevRotationYaw = this.getRotationYaw();
                boolean skipDragging = this.lastTickRotationPitch == 0 && this.lastTickRotationYaw == 0
                    && this.lastTickRotationRoll == 0
                    && this.lastTickX == 0
                    && this.lastTickY == 0
                    && this.lastTickZ == 0;
                Iterator i$;
                Entry curEntry;

                if (canUpdate && !skipDragging) {
                    i$ = this.entitiesToDrag.entrySet()
                        .iterator();

                    while (i$.hasNext()) {
                        curEntry = (Entry) i$.next();
                        curEntry.setValue(this.transformToLocal((Entity) curEntry.getKey()));
                    }

                    i$ = this.entitiesToNotDrag.entrySet()
                        .iterator();

                    while (i$.hasNext()) {
                        curEntry = (Entry) i$.next();
                        curEntry.setValue(this.transformToGlobal((Entity) curEntry.getKey()));
                    }
                }

                this.setTranslation(
                    this.getTranslationX() + this.getMotionX() * (double) count,
                    this.getTranslationY() + this.getMotionY() * (double) count,
                    this.getTranslationZ() + this.getMotionZ() * (double) count);
                this.setRotationYaw(this.getRotationYaw() + this.getRotationYawSpeed() * (double) count);
                this.setRotationPitch(this.getRotationPitch() + this.getRotationPitchSpeed() * (double) count);
                this.setRotationRoll(this.getRotationRoll() + this.getRotationRollSpeed() * (double) count);
                this.setScaling(this.getScaling() + this.getScaleChangeRate() * (double) count);

                if (this.getRotationPitchSpeed() != 0 || this.getRotationRollSpeed() != 0) {
                    this.lightVector = this.rotateToLocal(0, 1, 0);
                }

                float newEntityPrevRotationYawDiff1;
                if (canUpdate && !skipDragging) {
                    for (i$ = this.entitiesToDrag.entrySet()
                        .iterator(); i$
                            .hasNext(); ((Entity) curEntry.getKey()).prevRotationYaw = newEntityPrevRotationYawDiff1
                                + ((Entity) curEntry.getKey()).rotationYaw) {
                        curEntry = (Entry) i$.next();
                        Entity newPosition = (Entity) curEntry.getKey();
                        double newEntityPrevRotationYawDiff = ((IMixinEntity) newPosition).getTractionFactor();
                        double globalWeight = 1.0D - newEntityPrevRotationYawDiff;
                        Vec3 newPosition1 = this.transformToGlobal((Vec3) curEntry.getValue());
                        ((Entity) curEntry.getKey()).setPosition(
                            newPosition.posX * globalWeight + newPosition1.xCoord * newEntityPrevRotationYawDiff,
                            newPosition.posY * globalWeight + newPosition1.yCoord * newEntityPrevRotationYawDiff,
                            newPosition.posZ * globalWeight + newPosition1.zCoord * newEntityPrevRotationYawDiff);
                        newEntityPrevRotationYawDiff1 = ((Entity) curEntry.getKey()).prevRotationYaw
                            - (((Entity) curEntry.getKey()).rotationYaw
                                - (float) (this.getRotationYaw() - prevRotationYaw));
                        ((Entity) curEntry.getKey()).setRotation(
                            ((Entity) curEntry.getKey()).rotationYaw
                                - (float) (this.getRotationYaw() - prevRotationYaw),
                            ((Entity) curEntry.getKey()).rotationPitch);
                        if (curEntry.getKey() instanceof EntityLivingBase) {
                            // Making sure player's body is rotating with the world
                            EntityLivingBase curEntity = (EntityLivingBase) curEntry.getKey();
                            curEntity.prevRenderYawOffset = curEntity.prevRenderYawOffset
                                - (float) this.getRotationYawSpeed() / 2;
                            curEntity.renderYawOffset = curEntity.renderYawOffset
                                - (float) this.getRotationYawSpeed() / 2;
                        }
                    }

                    float newEntityPrevRotationYawDiff2;
                    for (i$ = this.entitiesToNotDrag.entrySet()
                        .iterator(); i$
                            .hasNext(); ((Entity) curEntry.getKey()).prevRotationYaw = newEntityPrevRotationYawDiff2
                                + ((Entity) curEntry.getKey()).rotationYaw) {
                        curEntry = (Entry) i$.next();
                        Vec3 newPosition2 = this.transformToLocal((Vec3) curEntry.getValue());
                        ((Entity) curEntry.getKey())
                            .setPosition(newPosition2.xCoord, newPosition2.yCoord, newPosition2.zCoord);
                        newEntityPrevRotationYawDiff2 = ((Entity) curEntry.getKey()).prevRotationYaw
                            - (((Entity) curEntry.getKey()).rotationYaw
                                + (float) (this.getRotationYaw() - prevRotationYaw));
                        ((Entity) curEntry.getKey()).setRotation(
                            ((Entity) curEntry.getKey()).rotationYaw
                                + (float) (this.getRotationYaw() - prevRotationYaw),
                            ((Entity) curEntry.getKey()).rotationPitch);
                    }
                }

                this.nextTickX = this.getTranslationX();
                this.nextTickY = this.getTranslationY();
                this.nextTickZ = this.getTranslationZ();
                this.nextTickRotationYaw = this.getRotationYaw();
                this.nextTickRotationPitch = this.getRotationPitch();
                this.nextTickRotationRoll = this.getRotationRoll();
                this.nextTickScaling = this.getScaling();
            }
        }
    }

    public void doTickPartial(double interpolationFactor) {
        if (interpolationFactor != 1.0D) {
            boolean skipDragging = this.lastTickRotationPitch == 0 && this.lastTickRotationYaw == 0
                && this.lastTickRotationRoll == 0
                && this.lastTickX == 0
                && this.lastTickY == 0
                && this.lastTickZ == 0;
            Iterator i$;

            Entry curEntry;
            Entity newPosition;
            double curEntity;
            double globalWeight;
            double newPosition1;
            Vec3 curEntityPos;

            if (canUpdate && !skipDragging) {
                i$ = this.entitiesToDrag.entrySet()
                    .iterator();
                while (i$.hasNext()) {
                    curEntry = (Entry) i$.next();
                    newPosition = (Entity) curEntry.getKey();
                    curEntity = newPosition.prevPosX + (newPosition.posX - newPosition.prevPosX) * interpolationFactor;
                    globalWeight = newPosition.prevPosY
                        + (newPosition.posY - newPosition.prevPosY) * interpolationFactor;
                    newPosition1 = newPosition.prevPosZ
                        + (newPosition.posZ - newPosition.prevPosZ) * interpolationFactor;
                    curEntityPos = this.transformToLocal(curEntity, globalWeight, newPosition1);
                    curEntry.setValue(curEntityPos);
                }

                i$ = this.entitiesToNotDrag.entrySet()
                    .iterator();

                while (i$.hasNext()) {
                    curEntry = (Entry) i$.next();
                    newPosition = (Entity) curEntry.getKey();
                    curEntity = newPosition.prevPosX + (newPosition.posX - newPosition.prevPosX) * interpolationFactor;
                    globalWeight = newPosition.prevPosY
                        + (newPosition.posY - newPosition.prevPosY) * interpolationFactor;
                    newPosition1 = newPosition.prevPosZ
                        + (newPosition.posZ - newPosition.prevPosZ) * interpolationFactor;
                    curEntityPos = this.transformToGlobal(curEntity, globalWeight, newPosition1);
                    curEntry.setValue(curEntityPos);
                }

                this.setTranslation(
                    this.lastTickX + (this.nextTickX - this.lastTickX) * interpolationFactor,
                    this.lastTickY + (this.nextTickY - this.lastTickY) * interpolationFactor,
                    this.lastTickZ + (this.nextTickZ - this.lastTickZ) * interpolationFactor);
                this.setRotationYaw(
                    this.lastTickRotationYaw
                        + (this.nextTickRotationYaw - this.lastTickRotationYaw) * interpolationFactor);
                this.setRotationPitch(
                    this.lastTickRotationPitch
                        + (this.nextTickRotationPitch - this.lastTickRotationPitch) * interpolationFactor);
                this.setRotationRoll(
                    this.lastTickRotationRoll
                        + (this.nextTickRotationRoll - this.lastTickRotationRoll) * interpolationFactor);
                this.setScaling(
                    this.lastTickScaling + (this.nextTickScaling - this.lastTickScaling) * interpolationFactor);

                if (this.lastTickRotationPitch != this.nextTickRotationPitch
                    || this.lastTickRotationRoll != this.nextTickRotationRoll) {
                    this.lightVector = this.rotateToLocal(0, 1, 0);
                }

                Vec3 newPosition2;
                for (i$ = this.entitiesToDrag.entrySet()
                    .iterator(); i$
                        .hasNext(); newPosition.prevPosZ = newPosition2.zCoord
                            + (newPosition2.zCoord - newPosition.posZ) * interpolationFactor
                                / (1.0D - interpolationFactor)) {
                    curEntry = (Entry) i$.next();
                    newPosition = (Entity) curEntry.getKey();
                    if (newPosition instanceof EntityPlayer) {
                        int x = 0;
                    }
                    curEntity = ((IMixinEntity) newPosition).getTractionFactor();
                    globalWeight = 1.0D - curEntity;
                    newPosition2 = this.transformToGlobal((Vec3) curEntry.getValue());
                    double curEntityX = newPosition.prevPosX
                        + (newPosition.posX - newPosition.prevPosX) * interpolationFactor;
                    double curEntityY = newPosition.prevPosY
                        + (newPosition.posY - newPosition.prevPosY) * interpolationFactor;
                    double curEntityZ = newPosition.prevPosZ
                        + (newPosition.posZ - newPosition.prevPosZ) * interpolationFactor;
                    newPosition2.xCoord = curEntityX * globalWeight + newPosition2.xCoord * curEntity;
                    newPosition2.yCoord = curEntityY * globalWeight + newPosition2.yCoord * curEntity;
                    newPosition2.zCoord = curEntityZ * globalWeight + newPosition2.zCoord * curEntity;
                    newPosition.prevPosX = newPosition2.xCoord
                        + (newPosition2.xCoord - newPosition.posX) * interpolationFactor / (1.0D - interpolationFactor);
                    newPosition.prevPosY = newPosition2.yCoord
                        + (newPosition2.yCoord - newPosition.posY) * interpolationFactor / (1.0D - interpolationFactor);
                }

                Vec3 newPosition3;
                Entity curEntity1;
                for (i$ = this.entitiesToNotDrag.entrySet()
                    .iterator(); i$
                        .hasNext(); curEntity1.prevPosZ = newPosition3.zCoord
                            + (newPosition3.zCoord - curEntity1.posZ) * interpolationFactor
                                / (1.0D - interpolationFactor)) {
                    curEntry = (Entry) i$.next();
                    newPosition3 = this.transformToLocal((Vec3) curEntry.getValue());
                    curEntity1 = (Entity) curEntry.getKey();
                    curEntity1.prevPosX = newPosition3.xCoord
                        + (newPosition3.xCoord - curEntity1.posX) * interpolationFactor / (1.0D - interpolationFactor);
                    curEntity1.prevPosY = newPosition3.yCoord
                        + (newPosition3.yCoord - curEntity1.posY) * interpolationFactor / (1.0D - interpolationFactor);
                }
            } else {
                this.setTranslation(this.nextTickX, this.nextTickY, this.nextTickZ);
                this.setRotationYaw(this.nextTickRotationYaw);
                this.setRotationPitch(this.nextTickRotationPitch);
                this.setRotationRoll(this.nextTickRotationRoll);
                this.setScaling(this.nextTickScaling);
            }
        }
    }

    public void UpdatePositionAndRotation(double newX, double newY, double newZ, double newRotationYaw,
        double newRotationPitch, double newRotationRoll, double newScaling) {
        Iterator rotationDiff = this.entitiesToDrag.entrySet()
            .iterator();

        boolean skipDragging = this.lastTickRotationPitch == 0 && this.lastTickRotationYaw == 0
            && this.lastTickRotationRoll == 0
            && this.lastTickX == 0
            && this.lastTickY == 0
            && this.lastTickZ == 0;
        if (canUpdate && !skipDragging) {
            Entry curEntry;
            while (rotationDiff.hasNext()) {
                curEntry = (Entry) rotationDiff.next();
                curEntry.setValue(this.transformToLocal((Entity) curEntry.getKey()));
            }

            rotationDiff = this.entitiesToNotDrag.entrySet()
                .iterator();

            while (rotationDiff.hasNext()) {
                curEntry = (Entry) rotationDiff.next();
                curEntry.setValue(this.transformToGlobal((Entity) curEntry.getKey()));
            }
        }

        this.setTranslation(newX, newY, newZ);
        double rotationDiff1 = newRotationYaw - this.getRotationYaw();
        this.setRotationYaw(newRotationYaw);
        this.setRotationPitch(newRotationPitch);
        this.setRotationRoll(newRotationRoll);
        this.setScaling(newScaling);

        if (canUpdate && !skipDragging) {
            Iterator i$;
            Entry curEntry1;
            float newEntityPrevRotationYawDiff1;
            for (i$ = this.entitiesToDrag.entrySet()
                .iterator(); i$
                    .hasNext(); ((Entity) curEntry1.getKey()).prevRotationYaw = newEntityPrevRotationYawDiff1
                        + ((Entity) curEntry1.getKey()).rotationYaw) {
                curEntry1 = (Entry) i$.next();
                Entity newPosition = (Entity) curEntry1.getKey();
                if (newPosition instanceof EntityPlayer) {
                    int x = 0;
                }
                double newEntityPrevRotationYawDiff = ((IMixinEntity) newPosition).getTractionFactor();
                double globalWeight = 1.0D - newEntityPrevRotationYawDiff;
                Vec3 newPosition1 = this.transformToGlobal((Vec3) curEntry1.getValue());
                ((Entity) curEntry1.getKey()).setPosition(
                    newPosition.posX * globalWeight + newPosition1.xCoord * newEntityPrevRotationYawDiff,
                    newPosition.posY * globalWeight + newPosition1.yCoord * newEntityPrevRotationYawDiff,
                    newPosition.posZ * globalWeight + newPosition1.zCoord * newEntityPrevRotationYawDiff);
                newEntityPrevRotationYawDiff1 = ((Entity) curEntry1.getKey()).prevRotationYaw
                    - (((Entity) curEntry1.getKey()).rotationYaw - (float) rotationDiff1);
                ((Entity) curEntry1.getKey()).setRotation(
                    ((Entity) curEntry1.getKey()).rotationYaw - (float) rotationDiff1,
                    ((Entity) curEntry1.getKey()).rotationPitch);
            }

            float newEntityPrevRotationYawDiff2;
            for (i$ = this.entitiesToNotDrag.entrySet()
                .iterator(); i$
                    .hasNext(); ((Entity) curEntry1.getKey()).prevRotationYaw = newEntityPrevRotationYawDiff2
                        + ((Entity) curEntry1.getKey()).rotationYaw) {
                curEntry1 = (Entry) i$.next();
                Vec3 newPosition2 = this.transformToLocal((Vec3) curEntry1.getValue());
                ((Entity) curEntry1.getKey())
                    .setPosition(newPosition2.xCoord, newPosition2.yCoord, newPosition2.zCoord);
                newEntityPrevRotationYawDiff2 = ((Entity) curEntry1.getKey()).prevRotationYaw
                    - (((Entity) curEntry1.getKey()).rotationYaw + (float) rotationDiff1);
                ((Entity) curEntry1.getKey()).setRotation(
                    ((Entity) curEntry1.getKey()).rotationYaw + (float) rotationDiff1,
                    ((Entity) curEntry1.getKey()).rotationPitch);
            }
        }

        this.nextTickX = this.getTranslationX();
        this.nextTickY = this.getTranslationY();
        this.nextTickZ = this.getTranslationZ();
        this.nextTickRotationYaw = this.getRotationYaw();
        this.nextTickRotationPitch = this.getRotationPitch();
        this.nextTickRotationRoll = this.getRotationRoll();
        this.nextTickScaling = this.getScaling();
    }

    public void onPreTick() {
        this.setTranslation(this.nextTickX, this.nextTickY, this.nextTickZ);
        this.setRotationYaw(this.nextTickRotationYaw);
        this.setRotationPitch(this.nextTickRotationPitch);
        this.setRotationRoll(this.nextTickRotationRoll);
        this.setScaling(this.nextTickScaling);
        this.lastTickX = this.getTranslationX();
        this.lastTickY = this.getTranslationY();
        this.lastTickZ = this.getTranslationZ();
        this.lastTickRotationYaw = this.getRotationYaw();
        this.lastTickRotationPitch = this.getRotationPitch();
        this.lastTickRotationRoll = this.getRotationRoll();
        this.lastTickScaling = this.getScaling();
    }

    public List getCollidingBoundingBoxes(Entity entity, AxisAlignedBB aabb) {
        ArrayList result = (ArrayList) this.getCollidingBoundingBoxesLocal(entity, aabb);
        Iterator i$ = ((IMixinWorld) this).getSubWorlds()
            .iterator();

        while (i$.hasNext()) {
            World curSubWorld = (World) i$.next();
            if (!((SubWorld) curSubWorld).getMaximumCloseWorldBBRotated()
                .intersectsWith(aabb)) continue;
            double worldRotationY = ((IMixinWorld) curSubWorld).getRotationYaw() % 360;
            if (worldRotationY != 0) {
                double dxPos = aabb.maxX - entity.posX;
                double dxNeg = entity.posX - aabb.minX;
                double dzPos = aabb.maxZ - entity.posZ;
                double dzNeg = entity.posZ - aabb.minZ;
                Vec3 moveVec = Vec3.createVectorHelper(dxPos - dxNeg, 0, dzPos - dzNeg);
                double xHalf = dxPos < dxNeg ? dxPos : dxNeg;
                double zHalf = dzPos < dzNeg ? dzPos : dzNeg;
                AxisAlignedBB localBB = ((IMixinAxisAlignedBB) AxisAlignedBB.getBoundingBox(
                    entity.posX - xHalf,
                    aabb.minY,
                    entity.posZ - zHalf,
                    entity.posX + xHalf,
                    aabb.maxY,
                    entity.posZ + zHalf)).rotateYaw(-worldRotationY, entity.posX, entity.posZ);
                result.addAll(
                    ((SubWorld) curSubWorld).getCollidingBoundingBoxesGlobalWithMovement(entity, localBB, moveVec));
            } else {
                result.addAll(((SubWorld) curSubWorld).getCollidingBoundingBoxesGlobal(entity, aabb));
            }
        }

        return result;
    }

    public List getCollidingBoundingBoxesLocal(Entity entity, AxisAlignedBB aabb) {
        this.collidingBBCache.clear();
        int i = MathHelper.floor_double(Math.max(aabb.minX, (double) this.getMinX()));
        int j = MathHelper.floor_double(Math.min(aabb.maxX + 1.0D, (double) this.getMaxX()));
        int k = MathHelper.floor_double(Math.max(aabb.minY, (double) this.getMinY()));
        int l = MathHelper.floor_double(Math.min(aabb.maxY + 1.0D, (double) this.getMaxY()));
        int i1 = MathHelper.floor_double(Math.max(aabb.minZ, (double) this.getMinZ()));
        int j1 = MathHelper.floor_double(Math.min(aabb.maxZ + 1.0D, (double) this.getMaxZ()));

        for (int d0 = i; d0 < j; ++d0) {
            for (int l1 = i1; l1 < j1; ++l1) {
                if (this.blockExists(d0, 64, l1)) {
                    for (int list = k - 1; list < l; ++list) {
                        Block j2 = this.getBlock(d0, list, l1);
                        if (j2 != Blocks.air) {
                            j2.addCollisionBoxesToList(this, d0, list, l1, aabb, this.collidingBBCache, entity);
                        }
                    }
                }
            }
        }

        double var14 = 0.25D;
        List var15 = ((IMixinWorld) this)
            .getEntitiesWithinAABBExcludingEntityLocal(entity, aabb.expand(var14, var14, var14));

        for (int var16 = 0; var16 < var15.size(); ++var16) {
            AxisAlignedBB axisalignedbb1 = ((Entity) var15.get(var16)).getBoundingBox();
            if (axisalignedbb1 != null && axisalignedbb1.intersectsWith(aabb)) {
                this.collidingBBCache.add(axisalignedbb1);
            }

            axisalignedbb1 = entity.getCollisionBox((Entity) var15.get(var16));
            if (axisalignedbb1 != null && axisalignedbb1.intersectsWith(aabb)) {
                this.collidingBBCache.add(axisalignedbb1);
            }
        }

        return this.collidingBBCache;
    }

    public List getCollidingBoundingBoxesGlobal(Entity entity, AxisAlignedBB aabb) {
        List result = this.getCollidingBoundingBoxesLocal(
            entity,
            ((IMixinAxisAlignedBB) aabb).getTransformedToLocalBoundingBox(this));
        ListIterator iter = result.listIterator();

        while (iter.hasNext()) {
            AxisAlignedBB replacementBB = ((IMixinAxisAlignedBB) iter.next()).getTransformedToGlobalBoundingBox(this);
            iter.set(replacementBB);
        }

        return result;
    }

    public List getCollidingBoundingBoxesLocalWithMovement(Entity entity, AxisAlignedBB aabb, Vec3 movement) {
        AxisAlignedBB localBB = ((IMixinAxisAlignedBB) aabb).getTransformedToLocalBoundingBox(this);
        localBB = AxisAlignedBB
            .getBoundingBox(localBB.minX, localBB.minY, localBB.minZ, localBB.maxX, localBB.maxY, localBB.maxZ);
        if (movement != null && movement.lengthVector() != 0) {
            Vec3 start = Vec3.createVectorHelper(aabb.minX, aabb.minY, aabb.minZ);
            Vec3 finish = start.addVector(movement.xCoord, movement.yCoord, movement.zCoord);
            start = this.transformToLocal(start);
            finish = this.transformToLocal(finish);
            Vec3 localMovement = start.subtract(finish);
            localBB = localBB.addCoord(localMovement.xCoord, localMovement.yCoord, localMovement.zCoord);
        }
        return this.getCollidingBoundingBoxesLocal(entity, localBB);
    }

    public List getCollidingBoundingBoxesGlobalWithMovement(Entity entity, AxisAlignedBB aabb, Vec3 movement) {
        List result = this.getCollidingBoundingBoxesLocalWithMovement(entity, aabb, movement);
        ListIterator iter = result.listIterator();

        while (iter.hasNext()) {
            AxisAlignedBB replacementBB = ((IMixinAxisAlignedBB) iter.next()).getTransformedToGlobalBoundingBox(this);
            iter.set(replacementBB);
        }

        return result;
    }

    public boolean isAnyLiquid(AxisAlignedBB par1AxisAlignedBB) {
        return super.isAnyLiquid(((IMixinAxisAlignedBB) par1AxisAlignedBB).getTransformedToLocalBoundingBox(this));
    }

    public boolean handleMaterialAcceleration(AxisAlignedBB par1AxisAlignedBB, Material par2Material,
        Entity par3Entity) {
        return this.handleMaterialAccelerationLocal(
            ((IMixinAxisAlignedBB) par1AxisAlignedBB).getTransformedToLocalBoundingBox(this),
            par2Material,
            par3Entity);
    }

    public boolean handleMaterialAccelerationLocal(AxisAlignedBB par1AxisAlignedBB, Material par2Material,
        Entity par3Entity) {
        int i = MathHelper.floor_double(Math.max(par1AxisAlignedBB.minX, (double) this.getMinX()));
        int j = MathHelper.floor_double(Math.min(par1AxisAlignedBB.maxX + 1.0D, (double) this.getMaxX()));
        int k = MathHelper.floor_double(Math.max(par1AxisAlignedBB.minY, (double) this.getMinY()));
        int l = MathHelper.floor_double(Math.min(par1AxisAlignedBB.maxY + 1.0D, (double) this.getMaxY()));
        int i1 = MathHelper.floor_double(Math.max(par1AxisAlignedBB.minZ, (double) this.getMinZ()));
        int j1 = MathHelper.floor_double(Math.min(par1AxisAlignedBB.maxZ + 1.0D, (double) this.getMaxZ()));
        if (!this.checkChunksExist(i, k, i1, j, l, j1)) {
            return false;
        } else {
            boolean flag = false;
            Vec3 vec3 = Vec3.createVectorHelper(0.0D, 0.0D, 0.0D);

            for (int d1 = i; d1 < j; ++d1) {
                for (int l1 = k; l1 < l; ++l1) {
                    for (int i2 = i1; i2 < j1; ++i2) {
                        Block block = this.getBlock(d1, l1, i2);
                        if (block != Blocks.air && block.getMaterial() == par2Material) {
                            double d0 = (double) ((float) (l1 + 1)
                                - BlockLiquid.getLiquidHeightPercent(this.getBlockMetadata(d1, l1, i2)));
                            if ((double) l >= d0) {
                                flag = true;
                                block.velocityToAddToEntity(this, d1, l1, i2, par3Entity, vec3);
                            }
                        }
                    }
                }
            }

            if (vec3.lengthVector() > 0.0D && par3Entity.isPushedByWater()) {
                vec3 = vec3.normalize();
                double var18 = 0.014D;
                vec3 = this.rotateToGlobal(vec3.xCoord * var18, vec3.yCoord * var18, vec3.zCoord * var18);
                par3Entity.motionX += vec3.xCoord;
                par3Entity.motionY += vec3.yCoord;
                par3Entity.motionZ += vec3.zCoord;
            }

            return flag;
        }
    }

    public boolean isAABBInMaterialGlobal(AxisAlignedBB par1AxisAlignedBB, Material par2Material) {
        return super.isAABBInMaterial(
            ((IMixinAxisAlignedBB) par1AxisAlignedBB).getTransformedToLocalBoundingBox(this),
            par2Material);
    }

    public boolean isMaterialInBBLocal(AxisAlignedBB par1AxisAlignedBB, Material par2Material) {
        int i = MathHelper.floor_double(Math.max(par1AxisAlignedBB.minX, (double) this.getMinX()));
        int j = MathHelper.floor_double(Math.min(par1AxisAlignedBB.maxX + 1.0D, (double) this.getMaxX()));
        int k = MathHelper.floor_double(Math.max(par1AxisAlignedBB.minY, (double) this.getMinY()));
        int l = MathHelper.floor_double(Math.min(par1AxisAlignedBB.maxY + 1.0D, (double) this.getMaxY()));
        int i1 = MathHelper.floor_double(Math.max(par1AxisAlignedBB.minZ, (double) this.getMinZ()));
        int j1 = MathHelper.floor_double(Math.min(par1AxisAlignedBB.maxZ + 1.0D, (double) this.getMaxZ()));

        for (int k1 = i; k1 < j; ++k1) {
            for (int l1 = k; l1 < l; ++l1) {
                for (int i2 = i1; i2 < j1; ++i2) {
                    if (this.getBlock(k1, l1, i2)
                        .getMaterial() == par2Material) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean isMaterialInBBGlobal(AxisAlignedBB par1AxisAlignedBB, Material par2Material) {
        return super.isMaterialInBB(
            ((IMixinAxisAlignedBB) par1AxisAlignedBB).getTransformedToLocalBoundingBox(this),
            par2Material);
    }

    public void updateWeatherBody() {}

    public long getTotalWorldTime() {
        return this.m_parentWorld == null ? Minecraft.getMinecraft().theWorld.getTotalWorldTime()
            : this.m_parentWorld.getTotalWorldTime();
    }

    public AxisAlignedBB getMaximumCloseWorldBB() {
        return AxisAlignedBB.getBoundingBox(
            this.getMinX() + this.getTranslationX(),
            this.getMinY() + this.getTranslationY(),
            this.getMinZ() + this.getTranslationZ(),
            this.getMaxX() + this.getTranslationX(),
            this.getMaxY() + this.getTranslationY(),
            this.getMaxZ() + this.getTranslationZ());
    }

    public AxisAlignedBB getMaximumCloseWorldBBRotated() {
        int minX = this.getMinX();
        int minY = this.getMinY();
        int minZ = this.getMinZ();
        int maxX = this.getMaxX();
        int maxY = this.getMaxY();
        int maxZ = this.getMaxZ();

        int[] xCoords = { minX, minX, minX, minX, maxX, maxX, maxX, maxX };
        int[] yCoords = { minY, minY, maxY, maxY, minY, minY, maxY, maxY };
        int[] zCoords = { minZ, maxZ, minZ, maxZ, minZ, maxZ, minZ, maxZ };

        Vec3 globalCoord = this.transformToGlobal(Vec3.createVectorHelper(xCoords[0], yCoords[0], zCoords[0]));

        double newMinX = globalCoord.xCoord;
        double newMaxX = globalCoord.xCoord;
        double newMinY = globalCoord.yCoord;
        double newMaxY = globalCoord.yCoord;
        double newMinZ = globalCoord.zCoord;
        double newMaxZ = globalCoord.zCoord;

        for (int i = 1; i < 8; i++) {
            globalCoord = this.transformToGlobal(Vec3.createVectorHelper(xCoords[i], yCoords[i], zCoords[i]));
            if (globalCoord.xCoord < newMinX) newMinX = globalCoord.xCoord;
            if (globalCoord.xCoord > newMaxX) newMaxX = globalCoord.xCoord;
            if (globalCoord.yCoord < newMinY) newMinY = globalCoord.yCoord;
            if (globalCoord.yCoord > newMaxY) newMaxY = globalCoord.yCoord;
            if (globalCoord.zCoord < newMinZ) newMinZ = globalCoord.zCoord;
            if (globalCoord.zCoord > newMaxZ) newMaxZ = globalCoord.zCoord;
        }

        return AxisAlignedBB.getBoundingBox(newMinX, newMinY, newMinZ, newMaxX, newMaxY, newMaxZ);
    }

    private AxisAlignedBB actuallyRotateBB(AxisAlignedBB original, double degrees) {
        double[] rotMin = rotatePoint(original.minX, original.minZ, degrees);
        double[] rotMax = rotatePoint(original.maxX, original.maxZ, degrees);
        return AxisAlignedBB.getBoundingBox(rotMin[0], original.minY, rotMin[1], rotMax[0], original.maxY, rotMax[1]);
    }

    private double[] rotatePoint(double pointX, double pointZ, double degrees) {
        double transformedX = pointX * Math.cos(degrees) - pointZ * Math.sin(degrees);
        double transformedZ = pointX * Math.sin(degrees) + pointZ * Math.cos(degrees);
        return new double[] { transformedX, transformedZ };
    }

    private double radiansYawRotation(double rot) {
        return rot * Math.PI / 180;
    }

    public AxisAlignedBB getMaximumStretchedWorldBB(boolean shouldOnlyExpandX, boolean shouldOnlyExpandZ) {
        AxisAlignedBB closeWorldBB = getMaximumCloseWorldBB();

        double rangeX = closeWorldBB.maxX - closeWorldBB.minX;
        double rangeZ = closeWorldBB.maxZ - closeWorldBB.minZ;

        double minX;
        double minZ;
        double maxX;
        double maxZ;

        if (shouldOnlyExpandX) {
            minX = rangeX > rangeZ ? closeWorldBB.minX : closeWorldBB.minX - ((rangeZ - rangeX) / 2);
            minZ = closeWorldBB.minZ;
            maxX = rangeX > rangeZ ? closeWorldBB.maxX : closeWorldBB.maxX + ((rangeZ - rangeX) / 2);
            maxZ = closeWorldBB.maxZ;
        } else if (shouldOnlyExpandZ) {
            minX = closeWorldBB.minX;
            minZ = rangeZ > rangeX ? closeWorldBB.minZ : closeWorldBB.minZ - ((rangeX - rangeZ) / 2);
            maxX = closeWorldBB.maxX;
            maxZ = rangeZ > rangeX ? closeWorldBB.maxZ : closeWorldBB.maxZ + ((rangeX - rangeZ) / 2);
        } else {
            minX = rangeX > rangeZ ? closeWorldBB.minX : closeWorldBB.minX - ((rangeZ - rangeX) / 2);
            minZ = rangeZ > rangeX ? closeWorldBB.minZ : closeWorldBB.minZ - ((rangeX - rangeZ) / 2);
            maxX = rangeX > rangeZ ? closeWorldBB.maxX : closeWorldBB.maxX + ((rangeZ - rangeX) / 2);
            maxZ = rangeZ > rangeX ? closeWorldBB.maxZ : closeWorldBB.maxZ + ((rangeX - rangeZ) / 2);
        }
        double minY = closeWorldBB.minY;
        double maxY = closeWorldBB.maxY;

        return AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public double getRatioXToZ() {
        AxisAlignedBB closeWorldBB = getMaximumCloseWorldBB();
        double rangeX = closeWorldBB.maxX - closeWorldBB.minX;
        double rangeZ = closeWorldBB.maxZ - closeWorldBB.minZ;
        return rangeX / rangeZ;
    }

    public boolean hasCollision() {
        AxisAlignedBB maxBB = getMaximumStretchedWorldBB(false, false);
        for (int x = (int) Math.floor(maxBB.minX); x <= Math.ceil(maxBB.maxX); x++) {
            for (int y = (int) Math.floor(maxBB.minY); y <= Math.ceil(maxBB.maxY); y++) {
                for (int z = (int) Math.floor(maxBB.minZ); z <= Math.ceil(maxBB.maxZ); z++) {
                    if (!this.getParentWorld()
                        .isAirBlock(x, y, z)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public double getRotationActualYaw() {
        return this.getRotationYaw() % 360;
    }

    public EntityPlayer getClosestPlayer(double par1, double par3, double par5, double par7) {
        EntityPlayer closestPlayer = super.getClosestPlayer(par1, par3, par5, par7);
        EntityPlayer localProxyPlayer = ((IMixinEntity) Minecraft.getMinecraft().thePlayer).getProxyPlayer(this);
        if (closestPlayer == null) {
            if (par7 < 0.0D || localProxyPlayer.getDistanceSq(par1, par3, par5) < par7 * par7) {
                closestPlayer = localProxyPlayer;
            }
        } else if (localProxyPlayer.getDistanceSq(par1, par3, par5) < closestPlayer.getDistanceSq(par1, par3, par5)) {
            closestPlayer = localProxyPlayer;
        }

        return closestPlayer;
    }

    @Override
    public Vec3 transformToLocal(Entity var1) {
        return transformToLocal(var1.posX, var1.posY, var1.posZ);
    }

    @Override
    public Vec3 transformToGlobal(Entity var1) {
        return transformToGlobal(var1.posX, var1.posY, var1.posZ);
    }

    @Override
    public Vec3 transformLocalToOther(World var1, Entity var2) {
        return transformLocalToOther(var1, var2.posX, var2.posY, var2.posZ);
    }

    @Override
    public Vec3 transformOtherToLocal(World var1, Entity var2) {
        return transformOtherToLocal(var1, var2.posX, var2.posY, var2.posZ);
    }

    @Override
    public BiomeGenBase getBiomeGenForCoordsBody(final int x, final int z) {
        ChunkCoordinates globalCoords = this.transformBlockToGlobal(x, (int) this.getCenterY(), z);
        return this.getParentWorld()
            .getBiomeGenForCoordsBody(globalCoords.posX, globalCoords.posZ);
    }

    @Override
    public DirectionalChunk getDirectionalChunk(Direction dir, int x, int y, int z) {
        DirectionalChunk chunk = directionalChunkProvider.provideChunk(dir, x, y, z);
        if (chunk.getSegmentsCount() > 0) {
            ((ChunkProviderClientSubWorld) this.clientChunkProvider).directionalChunkListing.add(chunk);
        } else {
            ((ChunkProviderClientSubWorld) this.clientChunkProvider).directionalChunkListing.remove(chunk);
        }
        return chunk;
    }

    /**
     * Creates the chunk provider for this world. Called in the constructor. Retrieves provider from worldProvider?
     */
    protected IChunkProvider createChunkProvider() {
        this.clientChunkProvider = new ChunkProviderClientSubWorld(this);
        return this.clientChunkProvider;
    }

    @Override
    public Vec3 getLightVector() {
        return this.lightVector;
    }

    @Override
    public boolean updateLightByType(EnumSkyBlock lightType, int x, int y, int z) {
        if (lightType == EnumSkyBlock.Sky) return ((IMixinWorld) this).updateSkyLight(x, y, z);
        else return super.updateLightByType(lightType, x, y, z);
    }

    @Override
    public int computeLightValue(int x, int y, int z, EnumSkyBlock lightType) {
        if (lightType == EnumSkyBlock.Sky && this.canBlockSeeTheSky(x, y, z)) {
            return 15;
        } else {
            Block block = this.getBlock(x, y, z);
            int blockLight = block.getLightValue(this, x, y, z);
            int l = lightType == EnumSkyBlock.Sky ? 0 : blockLight;
            int i1 = block.getLightOpacity(this, x, y, z);

            if (i1 >= 15 && blockLight > 0) {
                i1 = 1;
            }

            if (i1 < 1) {
                i1 = 1;
            }

            if (i1 >= 15) {
                return 0;
            } else if (l >= 14) {
                return l;
            } else {
                for (int j1 = 0; j1 < 6; ++j1) {
                    int k1 = x + Facing.offsetsXForSide[j1];
                    int l1 = y + Facing.offsetsYForSide[j1];
                    int i2 = z + Facing.offsetsZForSide[j1];
                    int j2 = (lightType == EnumSkyBlock.Sky
                        ? ((IMixinWorld) this).getSavedSkyLightValue(Direction.UP, k1, l1, i2)
                        : this.getSavedLightValue(lightType, k1, l1, i2)) - i1;

                    if (j2 > l) {
                        l = j2;
                    }

                    if (l >= 14) {
                        return l;
                    }
                }

                return l;
            }
        }
    }
}
