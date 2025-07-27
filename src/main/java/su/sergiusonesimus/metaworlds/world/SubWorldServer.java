package su.sergiusonesimus.metaworlds.world;

import java.io.File;
import java.lang.reflect.Method;
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
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.SaveHandler;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jblas.DoubleMatrix;

import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.api.SubWorldTypeManager;
import su.sergiusonesimus.metaworlds.compat.packet.SubWorldDestroyPacket;
import su.sergiusonesimus.metaworlds.compat.packet.SubWorldUpdatePacket;
import su.sergiusonesimus.metaworlds.entity.player.EntityPlayerMPSubWorldProxy;
import su.sergiusonesimus.metaworlds.network.MetaMagicNetwork;
import su.sergiusonesimus.metaworlds.server.MinecraftServerSubWorldProxy;
import su.sergiusonesimus.metaworlds.util.SubWorldTransformationHandler;
import su.sergiusonesimus.metaworlds.world.chunk.ChunkSubWorld;
import su.sergiusonesimus.metaworlds.world.chunk.storage.AnvilChunkLoaderSubWorld;
import su.sergiusonesimus.metaworlds.world.gen.ChunkProviderServerSubWorld;
import su.sergiusonesimus.metaworlds.world.gen.ChunkProviderServerSubWorldBlank;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.IMixinEntity;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.server.IMixinMinecraftServer;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.server.management.IMixinPlayerManager;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.util.IMixinAxisAlignedBB;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorldIntermediate;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.storage.IMixinWorldInfo;

// This says that there is an error, but it reality there isn't because we change WorldServer's superclass at runtime
public class SubWorldServer extends WorldServer implements SubWorld {

    private static final Logger logger = LogManager.getLogger();
    public static int global_newSubWorldID = 1;
    private WorldServer m_parentWorld;
    private int subWorldID;
    private ArrayList collidingBBCache = new ArrayList();
    private SubWorldTransformationHandler transformationHandler = new SubWorldTransformationHandler(this);
    public Map<Entity, Vec3> entitiesToDrag = new TreeMap();
    private Map<Entity, Vec3> entitiesToNotDrag = new TreeMap();
    private ChunkCoordinates minCoordinates = new ChunkCoordinates();
    private ChunkCoordinates maxCoordinates = new ChunkCoordinates();
    private boolean boundariesChanged = true;
    private boolean centerChanged = true;
    private boolean isEmpty = true;
    private String subWorldType = SubWorldTypeManager.SUBWORLD_TYPE_DEFAULT;
    private List entitiesWithinAABBExcludingEntityResult = new ArrayList();

    public SubWorldServer(WorldServer parentWorld, int newSubWorldID, MinecraftServer par1MinecraftServer,
        ISaveHandler par2ISaveHandler, String par3Str, int par4, WorldSettings par5WorldSettings,
        Profiler par6Profiler) {
        super(
            new MinecraftServerSubWorldProxy(par1MinecraftServer),
            par2ISaveHandler,
            par3Str,
            par4,
            par5WorldSettings,
            par6Profiler);
        this.m_parentWorld = parentWorld;
        this.subWorldID = newSubWorldID;
        this.setRotationYaw(45.0D);
        this.setTranslation(0.0D, 0.0D, 0.0D);
        this.setBoundaries(
            Integer.MAX_VALUE,
            Integer.MAX_VALUE,
            Integer.MAX_VALUE,
            Integer.MIN_VALUE,
            Integer.MIN_VALUE,
            Integer.MIN_VALUE);
        ((MinecraftServerSubWorldProxy) this.func_73046_m()).setWorld(this);
    }

    public World CreateSubWorld() {
        return ((IMixinWorld) this.m_parentWorld).createSubWorld();
    }

    public World CreateSubWorld(int newSubWorldID) {
        return ((IMixinWorld) this.m_parentWorld).createSubWorld(newSubWorldID);
    }

    public void removeSubWorld() {
        Iterator minecraftexception = this.playerEntities.iterator();

        while (minecraftexception.hasNext()) {
            Object unloadWorldMethod = minecraftexception.next();
            EntityPlayerMPSubWorldProxy e = (EntityPlayerMPSubWorldProxy) unloadWorldMethod;
            ((IMixinEntity) e.getRealPlayer()).getPlayerProxyMap()
                .remove(Integer.valueOf(this.getSubWorldID()));
        }

        ((IMixinWorld) this.m_parentWorld).getSubWorlds()
            .remove(this);
        ((IMixinMinecraftServer) MinecraftServer.mcServer).getExistingSubWorlds()
            .remove(this.subWorldID);

        try {
            Class[] minecraftexception1 = new Class[] { World.class };
            Method unloadWorldMethod1 = ForgeChunkManager.class.getDeclaredMethod("unloadWorld", minecraftexception1);
            unloadWorldMethod1.setAccessible(true);

            try {
                unloadWorldMethod1.invoke((Object) null, new Object[] { this });
            } catch (Exception var5) {
                var5.printStackTrace();
            }
        } catch (NoSuchMethodException var6) {
            System.out.println(var6.toString());
        }

        ((IMixinWorldInfo) DimensionManager.getWorld(0)
            .getWorldInfo()).updateSubWorldInfo(this);

        try {
            this.saveAllChunks(true, (IProgressUpdate) null);
        } catch (MinecraftException var4) {
            logger.warn(var4.getMessage());
        }

        MetaMagicNetwork.dispatcher.sendToDimension(
            new SubWorldDestroyPacket(1, new Integer[] { Integer.valueOf(this.getSubWorldID()) }),
            this.provider.dimensionId);
    }

    public boolean deleteSubWorldData() {
        ((IMixinWorldInfo) DimensionManager.getWorld(0)
            .getWorldInfo()).removeSubWorldInfo(this);
        String folder = MinecraftServer.mcServer.worldServers[0].getSaveHandler()
            .getWorldDirectoryName();
        folder += "/SUBWORLD" + this.subWorldID;
        ISaveFormat isaveformat = MinecraftServer.mcServer.anvilConverterForAnvilFile;
        isaveformat.flushCache();
        return isaveformat.deleteWorldDirectory(folder);
    }

    protected IChunkProvider createChunkProvider() {
        Object var1;
        if (this.saveHandler instanceof SaveHandler) {
            File file1 = ((SaveHandler) this.saveHandler).getWorldDirectory();
            if (DimensionManager.getWorld(this.provider.dimensionId) != null) {
                file1 = new File(file1, "SUBWORLD" + global_newSubWorldID);
                file1.mkdirs();
            }

            var1 = new AnvilChunkLoaderSubWorld(file1);
        } else {
            var1 = this.saveHandler.getChunkLoader(this.provider);
        }

        this.theChunkProviderServer = new ChunkProviderServerSubWorld(
            this,
            (IChunkLoader) var1,
            new ChunkProviderServerSubWorldBlank(this, (IChunkLoader) var1, (IChunkProvider) null));
        return this.theChunkProviderServer;
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

    public List selectEntitiesWithinAABB(Class par1Class, AxisAlignedBB par2AxisAlignedBB,
        IEntitySelector par3IEntitySelector) {
        ArrayList arraylist = new ArrayList();
        arraylist.addAll(
            ((IMixinWorldIntermediate) this)
                .selectEntitiesWithinAABBLocal(par1Class, par2AxisAlignedBB, par3IEntitySelector));
        AxisAlignedBB globalBB = ((IMixinAxisAlignedBB) par2AxisAlignedBB).getTransformedToGlobalBoundingBox(this);
        arraylist.addAll(
            ((IMixinWorldIntermediate) this.m_parentWorld)
                .selectEntitiesWithinAABBLocal(par1Class, globalBB, par3IEntitySelector));
        Iterator i$ = ((IMixinWorld) this.m_parentWorld).getSubWorlds()
            .iterator();

        while (i$.hasNext()) {
            World curSubWorld = (World) i$.next();
            if (curSubWorld != this) {
                arraylist.addAll(
                    ((IMixinWorldIntermediate) curSubWorld).selectEntitiesWithinAABBLocal(
                        par1Class,
                        ((IMixinAxisAlignedBB) globalBB).getTransformedToLocalBoundingBox(curSubWorld),
                        par3IEntitySelector));
            }
        }

        return arraylist;
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
        this.setCenter(newCenter.xCoord, newCenter.yCoord, newCenter.zCoord);
    }

    public void setCenter(double newX, double newY, double newZ) {
        if (!this.centerChanged) {
            this.centerChanged = this.getCenterX() != newX || this.getCenterY() != newY || this.getCenterZ() != newZ;
        }

        this.transformationHandler.setCenter(newX, newY, newZ);
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
        if (!this.boundariesChanged) {
            this.boundariesChanged = this.minCoordinates.posX != minX || this.minCoordinates.posY != minY
                || this.minCoordinates.posZ != minZ
                || this.maxCoordinates.posX != maxX
                || this.maxCoordinates.posY != maxY
                || this.maxCoordinates.posZ != maxZ;
        }

        boolean willBeEmpty = maxX <= minX || maxY <= minY || maxZ <= minZ;
        if (this.boundariesChanged) {
            if (willBeEmpty) {
                minX = 0;
                maxX = 0;
                minY = 0;
                maxY = 0;
                minZ = 0;
                maxZ = 0;
            }

            List chunksToRemoveFromWatch;
            if (!willBeEmpty) {
                if (this.isEmpty) {
                    chunksToRemoveFromWatch = this.makeChunkList(minX, minZ, maxX, maxZ);
                } else {
                    chunksToRemoveFromWatch = this.makeChunkListAreaAWithoutB(
                        minX,
                        minZ,
                        maxX,
                        maxZ,
                        this.getMinX(),
                        this.getMinZ(),
                        this.getMaxX(),
                        this.getMaxZ());
                }

                ((IMixinPlayerManager) this.getPlayerManager()).addWatchableChunks(chunksToRemoveFromWatch);
            }

            if (!this.isEmpty) {
                if (willBeEmpty) {
                    chunksToRemoveFromWatch = this
                        .makeChunkList(this.getMinX(), this.getMinZ(), this.getMaxX(), this.getMaxZ());
                } else {
                    chunksToRemoveFromWatch = this.makeChunkListAreaAWithoutB(
                        this.getMinX(),
                        this.getMinZ(),
                        this.getMaxX(),
                        this.getMaxZ(),
                        minX,
                        minZ,
                        maxX,
                        maxZ);
                }

                ((IMixinPlayerManager) this.getPlayerManager()).removeWatchableChunks(chunksToRemoveFromWatch);
            }

            this.minCoordinates.posX = minX;
            this.minCoordinates.posY = minY;
            this.minCoordinates.posZ = minZ;
            this.maxCoordinates.posX = maxX;
            this.maxCoordinates.posY = maxY;
            this.maxCoordinates.posZ = maxZ;
        }

        this.isEmpty = willBeEmpty;
    }

    private List<ChunkCoordIntPair> makeChunkList(int minX, int minZ, int maxX, int maxZ) {
        ArrayList chunksList = new ArrayList();

        for (int curChunkX = (minX >> 4) - 1; curChunkX <= (maxX - 1 >> 4) + 1; ++curChunkX) {
            for (int curChunkZ = (minZ >> 4) - 1; curChunkZ <= (maxZ - 1 >> 4) + 1; ++curChunkZ) {
                chunksList.add(new ChunkCoordIntPair(curChunkX, curChunkZ));
            }
        }

        return chunksList;
    }

    private List<ChunkCoordIntPair> makeChunkListAreaAWithoutB(int minXA, int minZA, int maxXA, int maxZA, int minXB,
        int minZB, int maxXB, int maxZB) {
        ArrayList chunksAWithoutB = new ArrayList();

        int startX;
        int endX;
        for (startX = (minXA >> 4) - 1; startX < (minXB >> 4) - 1; ++startX) {
            for (endX = (minZA >> 4) - 1; endX <= (maxZA - 1 >> 4) + 1; ++endX) {
                chunksAWithoutB.add(new ChunkCoordIntPair(startX, endX));
            }
        }

        for (startX = (maxXB - 1 >> 4) + 2; startX <= (maxXA - 1 >> 4) + 1; ++startX) {
            for (endX = (minZA >> 4) - 1; endX <= (maxZA - 1 >> 4) + 1; ++endX) {
                chunksAWithoutB.add(new ChunkCoordIntPair(startX, endX));
            }
        }

        startX = (Math.max(minXA, minXB) >> 4) - 1;
        endX = (Math.min(maxXA - 1, maxXB - 1) >> 4) + 1;

        int curChunkX;
        int curChunkZ;
        for (curChunkX = startX; curChunkX <= endX; ++curChunkX) {
            for (curChunkZ = (minZA >> 4) - 1; curChunkZ < (minZB >> 4) - 1; ++curChunkZ) {
                chunksAWithoutB.add(new ChunkCoordIntPair(curChunkX, curChunkZ));
            }
        }

        for (curChunkX = startX; curChunkX <= endX; ++curChunkX) {
            for (curChunkZ = (maxZB - 1 >> 4) + 2; curChunkZ <= (maxZA - 1 >> 4) + 1; ++curChunkZ) {
                chunksAWithoutB.add(new ChunkCoordIntPair(curChunkX, curChunkZ));
            }
        }

        return chunksAWithoutB;
    }

    public boolean isEmpty() {
        return this.isEmpty;
    }

    public boolean isChunkWatchable(int chunkX, int chunkZ) {
        return !this.isEmpty() && chunkX >= (this.getMinX() >> 4) - 1
            && chunkX <= (this.getMaxX() - 1 >> 4) + 1
            && chunkZ >= (this.getMinZ() >> 4) - 1
            && chunkZ <= (this.getMaxZ() - 1 >> 4) + 1;
    }

    public Block getBlock(int par1, int par2, int par3) {
        if (this.minCoordinates != null && this.maxCoordinates != null) {
            int chunkX = par1 >> 4;
            int chunkZ = par3 >> 4;
            if (chunkX < this.getMinX() >> 4 || chunkX > this.getMaxX() - 1 >> 4
                || chunkZ < this.getMinZ() >> 4
                || chunkZ > this.getMaxZ() - 1 >> 4) {
                return Blocks.air;
            }
        }

        return super.getBlock(par1, par2, par3);
    }

    public void tick() {
        super.tick();
        int updateFlags = 0;
        if (MinecraftServer.getServer()
            .getTickCounter() % 20 == 0) {
            updateFlags |= 1;
            if (this.transformationHandler.getIsInMotion()) {
                updateFlags |= 2;
            }

            updateFlags |= 4;
            updateFlags |= 8;
            updateFlags |= 16;
        } else {
            if (this.transformationHandler.getIsInMotion()) {
                updateFlags |= 3;
            } else if (MinecraftServer.getServer()
                .getTickCounter() % 5 == 0) {
                    updateFlags |= 1;
                }

            if (this.centerChanged) {
                updateFlags |= 4;
                this.centerChanged = false;
            }

            if (this.boundariesChanged) {
                updateFlags |= 8;
                this.boundariesChanged = false;
            }
        }

        MetaMagicNetwork.dispatcher
            .sendToDimension(new SubWorldUpdatePacket(this, updateFlags), this.provider.dimensionId);
        if (this.transformationHandler.getIsInMotion()) {
            Iterator i$ = this.entitiesToDrag.entrySet()
                .iterator();

            Entry curEntry;
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

            this.setTranslation(
                this.getTranslationX() + this.getMotionX(),
                this.getTranslationY() + this.getMotionY(),
                this.getTranslationZ() + this.getMotionZ());
            this.setRotationYaw(this.getRotationYaw() + this.getRotationYawSpeed());
            this.setRotationPitch(this.getRotationPitch() + this.getRotationPitchSpeed());
            this.setRotationRoll(this.getRotationRoll() + this.getRotationRollSpeed());
            this.setScaling(this.getScaling() + this.getScaleChangeRate());
            i$ = this.entitiesToDrag.entrySet()
                .iterator();

            Vec3 newPosition;
            while (i$.hasNext()) {
                curEntry = (Entry) i$.next();
                newPosition = this.transformToGlobal((Vec3) curEntry.getValue());
                if (curEntry.getKey() instanceof EntityLivingBase) {
                    // Making sure player's body is rotating with the world
                    EntityLivingBase curEntity = (EntityLivingBase) curEntry.getKey();
                    curEntity.prevRenderYawOffset = curEntity.prevRenderYawOffset
                        - (float) this.getRotationYawSpeed() / 2;
                    curEntity.renderYawOffset = curEntity.renderYawOffset - (float) this.getRotationYawSpeed() / 2;
                }
                if (curEntry.getKey() instanceof EntityPlayer) {
                    Entity curEntity = (Entity) curEntry.getKey();
                    double subWorldWeight = ((IMixinEntity) curEntity).getTractionFactor();
                    double globalWeight = 1.0D - subWorldWeight;
                    curEntity.setPosition(
                        curEntity.posX * globalWeight + newPosition.xCoord * subWorldWeight,
                        curEntity.posY * globalWeight + newPosition.yCoord * subWorldWeight,
                        curEntity.posZ * globalWeight + newPosition.zCoord * subWorldWeight);
                } else {
                    ((Entity) curEntry.getKey()).setPositionAndRotation(
                        newPosition.xCoord,
                        newPosition.yCoord,
                        newPosition.zCoord,
                        ((Entity) curEntry.getKey()).rotationYaw - (float) this.getRotationYawSpeed(),
                        ((Entity) curEntry.getKey()).rotationPitch);
                }
            }

            i$ = this.entitiesToNotDrag.entrySet()
                .iterator();

            while (i$.hasNext()) {
                curEntry = (Entry) i$.next();
                newPosition = this.transformToLocal((Vec3) curEntry.getValue());
                if (curEntry.getKey() instanceof EntityPlayer) {
                    ((Entity) curEntry.getKey())
                        .setPosition(newPosition.xCoord, newPosition.yCoord, newPosition.zCoord);
                } else {
                    ((Entity) curEntry.getKey()).setPositionAndRotation(
                        newPosition.xCoord,
                        newPosition.yCoord,
                        newPosition.zCoord,
                        ((Entity) curEntry.getKey()).rotationYaw + (float) this.getRotationYawSpeed(),
                        ((Entity) curEntry.getKey()).rotationPitch);
                }
            }
        }
    }

    public void func_82738_a(long par1) {}

    public void setWorldTime(long par1) {}

    public boolean spawnEntityInWorld(Entity par1Entity) {
        return par1Entity.worldObj != null && par1Entity.worldObj != this
            ? par1Entity.worldObj.spawnEntityInWorld(par1Entity)
            : super.spawnEntityInWorld(par1Entity);
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

    public boolean intersectsWithCuboid(int x, int y, int z) {
        return false;
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

    public double getRotationActualYaw() {
        return this.getRotationYaw() % 360;
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
        return this.isMaterialInBB(
            ((IMixinAxisAlignedBB) par1AxisAlignedBB).getTransformedToLocalBoundingBox(this),
            par2Material);
    }

    public boolean setBlock(int x, int y, int z, Block block, int meta, int flags) {
        boolean result = super.setBlock(x, y, z, block, meta, flags);
        if (result) {
            if (block != Blocks.air) {
                if (this.isEmpty) {
                    this.setBoundaries(x, y, z, x + 1, y + 1, z + 1);
                } else {
                    this.setBoundaries(
                        Math.min(this.minCoordinates.posX, x),
                        Math.min(this.minCoordinates.posY, y),
                        Math.min(this.minCoordinates.posZ, z),
                        Math.max(this.maxCoordinates.posX, x + 1),
                        Math.max(this.maxCoordinates.posY, y + 1),
                        Math.max(this.maxCoordinates.posZ, z + 1));
                }
            } else if (!this.isEmpty) {
                int minX = this.getMinX();
                int minY = this.getMinY();
                int minZ = this.getMinZ();
                int maxX = this.getMaxX();
                int maxY = this.getMaxY();
                int maxZ = this.getMaxZ();
                boolean nowEmpty = this.isEmpty();
                int foundBlockCoord;
                ChunkSubWorld curChunk;
                int curChunkX;
                int curChunkZ;
                int inChunkX;
                int inChunkZ;
                int curX;
                int curY;
                int curZ;
                if (y == maxY - 1) {
                    foundBlockCoord = minY - 1;
                    curY = y;

                    for (curChunkX = minX >> 4; curChunkX <= maxX - 1 >> 4; ++curChunkX) {
                        for (curChunkZ = minZ >> 4; curChunkZ <= maxZ - 1 >> 4; ++curChunkZ) {
                            curChunk = (ChunkSubWorld) this.getChunkFromChunkCoords(curChunkX, curChunkZ);
                            if (!curChunk.getAreLevelsEmpty(minY, curY)) {
                                for (inChunkX = 0; inChunkX < 16; ++inChunkX) {
                                    curX = (curChunkX << 4) + inChunkX;
                                    if (curX >= minX) {
                                        if (curX >= maxX) {
                                            break;
                                        }

                                        for (inChunkZ = 0; inChunkZ < 16; ++inChunkZ) {
                                            curZ = (curChunkZ << 4) + inChunkZ;
                                            if (curZ >= minZ) {
                                                if (curZ >= maxZ) {
                                                    break;
                                                }

                                                int heightValue = curChunk.getCollisionLimitYPos(inChunkX, inChunkZ)
                                                    - 1;
                                                foundBlockCoord = Math.max(foundBlockCoord, heightValue);
                                                if (foundBlockCoord >= maxY - 1) {
                                                    break;
                                                }
                                            }
                                        }

                                        if (foundBlockCoord >= maxY - 1) {
                                            break;
                                        }
                                    }
                                }

                                if (foundBlockCoord >= maxY - 1) {
                                    break;
                                }
                            }
                        }

                        if (foundBlockCoord >= maxY - 1) {
                            break;
                        }
                    }

                    if (foundBlockCoord == minY - 1) {
                        nowEmpty = true;
                    }

                    if (foundBlockCoord + 1 != maxY) {
                        this.boundariesChanged = true;
                    }

                    maxY = foundBlockCoord + 1;
                } else if (y == minY) {
                    foundBlockCoord = maxY;

                    for (curY = y; curY < maxY && foundBlockCoord == maxY; ++curY) {
                        for (curChunkX = minX >> 4; curChunkX <= maxX - 1 >> 4; ++curChunkX) {
                            for (curChunkZ = minZ >> 4; curChunkZ <= maxZ - 1 >> 4; ++curChunkZ) {
                                curChunk = (ChunkSubWorld) this.getChunkFromChunkCoords(curChunkX, curChunkZ);
                                if (!curChunk.getAreLevelsEmpty(curY, curY)) {
                                    ExtendedBlockStorage var27 = curChunk.getBlockStorageArray()[curY >> 4];

                                    for (inChunkX = 0; inChunkX < 16; ++inChunkX) {
                                        curX = (curChunkX << 4) + inChunkX;
                                        if (curX >= minX) {
                                            if (curX >= maxX) {
                                                break;
                                            }

                                            for (inChunkZ = 0; inChunkZ < 16; ++inChunkZ) {
                                                curZ = (curChunkZ << 4) + inChunkZ;
                                                if (curZ >= minZ) {
                                                    if (curZ >= maxZ) {
                                                        break;
                                                    }

                                                    if (var27.getBlockByExtId(inChunkX, curY & 15, inChunkZ)
                                                        != Blocks.air) {
                                                        foundBlockCoord = curY;
                                                        break;
                                                    }
                                                }
                                            }

                                            if (foundBlockCoord != maxY) {
                                                break;
                                            }
                                        }
                                    }

                                    if (foundBlockCoord != maxY) {
                                        break;
                                    }
                                }
                            }

                            if (foundBlockCoord != maxY) {
                                break;
                            }
                        }

                        if (foundBlockCoord != maxY) {
                            break;
                        }
                    }

                    if (foundBlockCoord == maxY) {
                        nowEmpty = true;
                    }

                    if (foundBlockCoord != minZ) {
                        this.boundariesChanged = true;
                    }

                    minY = foundBlockCoord;
                }

                if (!nowEmpty && x == minX) {
                    foundBlockCoord = maxX;

                    for (curX = x; curX < maxX && foundBlockCoord == maxX; ++curX) {
                        for (curChunkZ = minZ >> 4; curChunkZ <= maxZ - 1 >> 4
                            && foundBlockCoord == maxX; ++curChunkZ) {
                            curChunk = (ChunkSubWorld) this.getChunkFromChunkCoords(curX >> 4, curChunkZ);
                            inChunkX = curX & 15;

                            for (inChunkZ = 0; inChunkZ < 16; ++inChunkZ) {
                                curZ = (curChunkZ << 4) + inChunkZ;
                                if (curZ >= minZ) {
                                    if (curZ > maxZ - 1) {
                                        break;
                                    }

                                    if (curChunk.getCollisionLimitYPos(inChunkX, inChunkZ) > 0) {
                                        foundBlockCoord = curX;
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    if (foundBlockCoord == maxX) {
                        nowEmpty = true;
                    }

                    if (foundBlockCoord != minX) {
                        this.boundariesChanged = true;
                    }

                    minX = foundBlockCoord;
                } else if (!nowEmpty && x == maxX - 1) {
                    foundBlockCoord = minX - 1;

                    for (curX = x; curX >= minX && foundBlockCoord == minX - 1; --curX) {
                        for (curChunkZ = minZ >> 4; curChunkZ <= maxZ - 1 >> 4
                            && foundBlockCoord == minX - 1; ++curChunkZ) {
                            curChunk = (ChunkSubWorld) this.getChunkFromChunkCoords(curX >> 4, curChunkZ);
                            curChunkX = curX & 15;

                            for (inChunkZ = 0; inChunkZ < 16; ++inChunkZ) {
                                curZ = (curChunkZ << 4) + inChunkZ;
                                if (curZ >= minZ) {
                                    if (curZ > maxZ - 1) {
                                        break;
                                    }

                                    if (curChunk.getCollisionLimitYPos(curChunkX, inChunkZ) > 0) {
                                        foundBlockCoord = curX;
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    if (foundBlockCoord == minX - 1) {
                        nowEmpty = true;
                    }

                    if (foundBlockCoord + 1 != maxX) {
                        this.boundariesChanged = true;
                    }

                    maxX = foundBlockCoord + 1;
                }

                if (!nowEmpty && z == minZ) {
                    foundBlockCoord = maxZ;

                    for (curZ = z; curZ < maxZ && foundBlockCoord == maxZ; ++curZ) {
                        for (curChunkX = minX >> 4; curChunkX <= maxX - 1 >> 4
                            && foundBlockCoord == maxZ; ++curChunkX) {
                            curChunk = (ChunkSubWorld) this.getChunkFromChunkCoords(curChunkX, curZ >> 4);
                            inChunkZ = curZ & 15;

                            for (inChunkX = 0; inChunkX < 16; ++inChunkX) {
                                curX = (curChunkX << 4) + inChunkX;
                                if (curX >= minX) {
                                    if (curX > maxX - 1) {
                                        break;
                                    }

                                    if (curChunk.getCollisionLimitYPos(inChunkX, inChunkZ) > 0) {
                                        foundBlockCoord = curZ;
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    if (foundBlockCoord == maxZ) {
                        nowEmpty = true;
                    }

                    if (foundBlockCoord != minZ) {
                        this.boundariesChanged = true;
                    }

                    minZ = foundBlockCoord;
                } else if (!nowEmpty && z == maxZ - 1) {
                    foundBlockCoord = minZ - 1;

                    for (curZ = z; curZ >= minZ && foundBlockCoord == minZ - 1; --curZ) {
                        for (curChunkX = minX >> 4; curChunkX <= maxX - 1 >> 4
                            && foundBlockCoord == minZ - 1; ++curChunkX) {
                            curChunk = (ChunkSubWorld) this.getChunkFromChunkCoords(curChunkX, curZ >> 4);
                            inChunkZ = curZ & 15;

                            for (inChunkX = 0; inChunkX < 16; ++inChunkX) {
                                curX = (curChunkX << 4) + inChunkX;
                                if (curX >= minX) {
                                    if (curX > maxX - 1) {
                                        break;
                                    }

                                    if (curChunk.getCollisionLimitYPos(inChunkX, inChunkZ) > 0) {
                                        foundBlockCoord = curZ;
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    if (foundBlockCoord == minZ - 1) {
                        nowEmpty = true;
                    }

                    if (foundBlockCoord + 1 != maxZ) {
                        this.boundariesChanged = true;
                    }

                    maxZ = foundBlockCoord + 1;
                }

                this.setBoundaries(minX, minY, minZ, maxX, maxY, maxZ);
            }
        }

        return result;
    }

    public void updateWeatherBody() {}

    public long getTotalWorldTime() {
        return this.m_parentWorld.getTotalWorldTime();
    }

    public Chunk createNewChunk(int xPos, int zPos) {
        return new ChunkSubWorld(this, xPos, zPos);
    }

    public boolean canBlockFreezeBody(int par1, int par2, int par3, boolean par4) {
        return false;
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
}
