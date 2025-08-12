package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Facing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.GameRules;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.IWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.util.ForgeDirection;

import org.jblas.DoubleMatrix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import su.sergiusonesimus.metaworlds.MetaworldsMod;
import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.metaworlds.util.Direction.Axis;
import su.sergiusonesimus.metaworlds.util.UnmodifiableSingleObjPlusCollection;
import su.sergiusonesimus.metaworlds.world.chunk.ChunkSubWorld;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.server.IMixinMinecraftServer;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

@Mixin(World.class)
public abstract class MixinWorld implements IMixinWorld {

    private final boolean isSubWorld = this instanceof SubWorld;
    public Map<Integer, World> childSubWorlds;
    private UnmodifiableSingleObjPlusCollection<World> allWorlds;

    @Shadow(remap = true)
    public static double MAX_ENTITY_RADIUS;

    @Shadow(remap = true)
    public ArrayList collidingBoundingBoxes;

    @Shadow(remap = true)
    public boolean restoringBlockSnapshots;

    @Shadow(remap = true)
    public List playerEntities;

    @Shadow(remap = true)
    public List loadedEntityList;

    @Shadow(remap = true)
    public WorldInfo worldInfo;

    @Shadow(remap = true)
    protected List worldAccesses;

    @Shadow(remap = true)
    public WorldProvider provider;

    @Shadow(remap = true)
    public Profiler theProfiler;

    @Shadow(remap = true)
    int[] lightUpdateBlockList;

    // TODO

    @Shadow(remap = true)
    public int computeLightValue(int x, int y, int z, EnumSkyBlock p_98179_4_) {
        return 0;
    }

    @Shadow(remap = true)
    public void setLightValue(EnumSkyBlock lightType, int x, int y, int z, int value) {}

    @Shadow(remap = true)
    public int getSavedLightValue(EnumSkyBlock p_72972_1_, int p_72972_2_, int p_72972_3_, int p_72972_4_) {
        return 0;
    }

    @Shadow(remap = true)
    public boolean doChunksNearChunkExist(int p_72873_1_, int p_72873_2_, int p_72873_3_, int p_72873_4_) {
        return false;
    }

    @Shadow(remap = true)
    public void markBlockRangeForRenderUpdate(int p_147458_1_, int p_147458_2_, int p_147458_3_, int p_147458_4_,
        int p_147458_5_, int p_147458_6_) {}

    @Shadow(remap = true)
    public void markBlocksDirtyVertical(int p_72975_1_, int p_72975_2_, int p_72975_3_, int p_72975_4_) {}

    @Shadow(remap = true)
    public int getSkyBlockTypeBrightness(EnumSkyBlock p_72925_1_, int p_72925_2_, int p_72925_3_, int p_72925_4_) {
        return 0;
    }

    @Shadow(remap = true)
    public Chunk getChunkFromBlockCoords(int p_72938_1_, int p_72938_2_) {
        return null;
    }

    @Shadow(remap = true)
    public boolean isAABBInMaterial(AxisAlignedBB p_72830_1_, Material p_72830_2_) {
        return false;
    }

    @Shadow(remap = true)
    public GameRules getGameRules() {
        return null;
    }

    @SideOnly(Side.CLIENT)
    @Shadow(remap = true)
    public void func_82738_a(long p_82738_1_) {}

    @Shadow(remap = true)
    public void setWorldTime(long time) {}

    @Shadow(remap = true)
    public long getTotalWorldTime() {
        return -1;
    }

    @Shadow(remap = true)
    public MovingObjectPosition func_147447_a(Vec3 par1Vec3, Vec3 par2Vec3, boolean par3, boolean par4, boolean par5) {
        return null;
    }

    @Shadow(remap = true)
    public void removePlayerEntityDangerously(Entity par1Entity) {}

    @Shadow(remap = true)
    public void removeEntity(Entity p_72900_1_) {}

    @Shadow(remap = true)
    public boolean spawnEntityInWorld(Entity p_72838_1_) {
        return false;
    }

    @Shadow(remap = true)
    public List selectEntitiesWithinAABB(Class par1Class, AxisAlignedBB par2AxisAlignedBB,
        IEntitySelector par3IEntitySelector) {
        return null;
    }

    @Shadow(remap = true)
    public List getEntitiesWithinAABBExcludingEntity(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB,
        IEntitySelector par3IEntitySelector) {
        return null;
    }

    @Shadow(remap = true)
    public boolean isMaterialInBB(AxisAlignedBB par1AxisAlignedBB, Material par2Material) {
        return false;
    }

    @Shadow(remap = true)
    public List getCollidingBoundingBoxes(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB) {
        return null;
    }

    @Shadow(remap = true)
    public void onEntityRemoved(Entity p_72847_1_) {}

    @Shadow(remap = true)
    public void onEntityAdded(Entity p_72923_1_) {}

    @Shadow(remap = true)
    public void updateAllPlayersSleepingFlag() {}

    @Shadow(remap = true)
    public Chunk getChunkFromChunkCoords(int p_72964_1_, int p_72964_2_) {
        return null;
    }

    @Shadow(remap = true)
    public List getEntitiesWithinAABBExcludingEntity(Entity p_72839_1_, AxisAlignedBB p_72839_2_) {
        return null;
    }

    @Shadow(remap = true)
    public boolean blockExists(int p_72899_1_, int p_72899_2_, int p_72899_3_) {
        return false;
    }

    @Shadow(remap = true)
    public int getBlockMetadata(int p_72805_1_, int p_72805_2_, int p_72805_3_) {
        return -1;
    }

    @Shadow(remap = true)
    public Block getBlock(final int p_150810_1_, final int p_150810_2_, final int p_150810_3_) {
        return null;
    }

    @Shadow(remap = true)
    public boolean chunkExists(int p_72916_1_, int p_72916_2_) {
        return false;
    }

    public Collection<World> getWorlds() {
        if (this.allWorlds == null)
            this.allWorlds = new UnmodifiableSingleObjPlusCollection<World>((World) (Object) this, this.getSubWorlds());
        return this.allWorlds;
    }

    public Collection<World> getSubWorlds() {
        if (this.childSubWorlds == null) childSubWorlds = new ConcurrentHashMap<Integer, World>();
        return this.childSubWorlds.values();
    }

    public Map<Integer, World> getSubWorldsMap() {
        if (this.childSubWorlds == null) childSubWorlds = new ConcurrentHashMap<Integer, World>();
        return this.childSubWorlds;
    }

    public int getWorldsCount() {
        if (this.childSubWorlds == null) childSubWorlds = new ConcurrentHashMap<Integer, World>();
        return this.childSubWorlds.size() + 1;
    }

    public int getUnoccupiedSubworldID() {
        Map<Integer, World> subworlds = ((IMixinMinecraftServer) MinecraftServer.mcServer).getExistingSubWorlds();
        int maxID = subworlds.size() + 1;
        for (int i = 1; i < maxID; i++) if (!subworlds.containsKey(i)) return i;
        return maxID;
    }

    public int getSubWorldID() {
        return 0;
    }

    public World getParentWorld() {
        return (World) (Object) this;
    }

    public World getSubWorld(int targetSubWorldID) {
        return targetSubWorldID < 0 ? null
            : (targetSubWorldID == 0 ? (World) (Object) this
                : (World) this.childSubWorlds.get(Integer.valueOf(targetSubWorldID)));
    }

    public boolean isSubWorld() {
        return this.isSubWorld;
    }

    public double getTranslationX() {
        return 0.0D;
    }

    public double getTranslationY() {
        return 0.0D;
    }

    public double getTranslationZ() {
        return 0.0D;
    }

    public double getRotationYaw() {
        return 0.0D;
    }

    public double getRotationPitch() {
        return 0.0D;
    }

    public double getRotationRoll() {
        return 0.0D;
    }

    public double getScaling() {
        return 1.0D;
    }

    public double getCenterX() {
        return 0.0D;
    }

    public double getCenterY() {
        return 0.0D;
    }

    public double getCenterZ() {
        return 0.0D;
    }

    public Vec3 transformToGlobal(Entity localEntity) {
        return this.transformToGlobal(localEntity.posX, localEntity.posY, localEntity.posZ);
    }

    public Vec3 transformToGlobal(Vec3 localVec) {
        return this.transformToGlobal(localVec.xCoord, localVec.yCoord, localVec.zCoord);
    }

    public Vec3 transformToGlobal(double localX, double localY, double localZ) {
        return Vec3.createVectorHelper(localX, localY, localZ);
    }

    public DoubleMatrix transformToGlobal(DoubleMatrix localVectors) {
        return localVectors.dup();
    }

    public DoubleMatrix transformToGlobal(DoubleMatrix localVectors, DoubleMatrix result) {
        result.copy(localVectors);
        return result;
    }

    public Vec3 transformToLocal(Entity globalEntity) {
        return this.transformToLocal(globalEntity.posX, globalEntity.posY, globalEntity.posZ);
    }

    public Vec3 transformToLocal(Vec3 globalVec) {
        return this.transformToLocal(globalVec.xCoord, globalVec.yCoord, globalVec.zCoord);
    }

    public Vec3 transformToLocal(double globalX, double globalY, double globalZ) {
        return Vec3.createVectorHelper(globalX, globalY, globalZ);
    }

    public DoubleMatrix transformToLocal(DoubleMatrix globalVectors) {
        return globalVectors.dup();
    }

    public DoubleMatrix transformToLocal(DoubleMatrix globalVectors, DoubleMatrix result) {
        result.copy(globalVectors);
        return result;
    }

    public Vec3 transformLocalToOther(World targetWorld, Entity localEntity) {
        return this.transformLocalToOther(targetWorld, localEntity.posX, localEntity.posY, localEntity.posZ);
    }

    public Vec3 transformLocalToOther(World targetWorld, Vec3 localVec) {
        return targetWorld == null ? this.transformToLocal(localVec)
            : ((IMixinWorld) targetWorld).transformToLocal(localVec);
    }

    public Vec3 transformLocalToOther(World targetWorld, double localX, double localY, double localZ) {
        return targetWorld == null ? Vec3.createVectorHelper(localX, localY, localZ)
            : ((IMixinWorld) targetWorld).transformToLocal(localX, localY, localZ);
    }

    public DoubleMatrix transformLocalToOther(World targetWorld, DoubleMatrix localVectors) {
        return targetWorld == null ? localVectors.dup() : ((IMixinWorld) targetWorld).transformToLocal(localVectors);
    }

    public DoubleMatrix transformLocalToOther(World targetWorld, DoubleMatrix localVectors, DoubleMatrix result) {
        return targetWorld == null ? result.copy(localVectors)
            : ((IMixinWorld) targetWorld).transformToLocal(localVectors, result);
    }

    public Vec3 transformOtherToLocal(World sourceWorld, Entity otherEntity) {
        return this.transformOtherToLocal(sourceWorld, otherEntity.posX, otherEntity.posY, otherEntity.posZ);
    }

    public Vec3 transformOtherToLocal(World sourceWorld, Vec3 otherVec) {
        return sourceWorld == null ? this.transformToGlobal(otherVec)
            : ((IMixinWorld) sourceWorld).transformToGlobal(otherVec);
    }

    public Vec3 transformOtherToLocal(World sourceWorld, double otherX, double otherY, double otherZ) {
        return sourceWorld == null ? Vec3.createVectorHelper(otherX, otherY, otherZ)
            : ((IMixinWorld) sourceWorld).transformToGlobal(otherX, otherY, otherZ);
    }

    public DoubleMatrix transformOtherToLocal(World sourceWorld, DoubleMatrix otherVectors) {
        return sourceWorld == null ? otherVectors.dup() : ((IMixinWorld) sourceWorld).transformToGlobal(otherVectors);
    }

    public DoubleMatrix transformOtherToLocal(World sourceWorld, DoubleMatrix otherVectors, DoubleMatrix result) {
        return sourceWorld == null ? result.copy(otherVectors)
            : ((IMixinWorld) sourceWorld).transformToGlobal(otherVectors, result);
    }

    public Vec3 rotateToGlobal(Vec3 localVec) {
        return localVec;
    }

    public Vec3 rotateToGlobal(double localX, double localY, double localZ) {
        return Vec3.createVectorHelper(localX, localY, localZ);
    }

    public DoubleMatrix rotateToGlobal(DoubleMatrix localVectors) {
        return localVectors;
    }

    public Vec3 rotateToLocal(Vec3 globalVec) {
        return globalVec;
    }

    public Vec3 rotateToLocal(double globalX, double globalY, double globalZ) {
        return Vec3.createVectorHelper(globalX, globalY, globalZ);
    }

    public DoubleMatrix rotateToLocal(DoubleMatrix globalVectors) {
        return globalVectors;
    }

    public List getCollidingBoundingBoxesGlobal(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB) {
        return this.getCollidingBoundingBoxes(par1Entity, par2AxisAlignedBB);
    }

    public List getEntitiesWithinAABBExcludingEntityLocal(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB) {
        return this.getEntitiesWithinAABBExcludingEntityLocal(par1Entity, par2AxisAlignedBB, (IEntitySelector) null);
    }

    public List getEntitiesWithinAABBExcludingEntityLocal(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB,
        IEntitySelector par3IEntitySelector) {
        return null;
    }

    public void doTickPartial(double interpolationFactor) {}

    public boolean isChunkWatchable(int chunkX, int chunkZ) {
        return true;
    }

    public Chunk createNewChunk(int xPos, int zPos) {
        return new Chunk((World) (Object) this, xPos, zPos);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void World(ISaveHandler p_i45368_1_, String p_i45368_2_, WorldProvider p_i45368_3_,
        WorldSettings p_i45368_4_, Profiler p_i45368_5_, CallbackInfo ci) {
        childSubWorlds = new ConcurrentHashMap<Integer, World>();
        allWorlds = new UnmodifiableSingleObjPlusCollection<World>((World) (Object) this, this.childSubWorlds.values());
    }

    /**
     * Gets the height to which rain/snow will fall. Calculates it if not already stored.
     */
    @Overwrite
    public int getPrecipitationHeight(int x, int z) {
        int precHeight = this.getChunkFromBlockCoords(x, z)
            .getPrecipitationHeight(x & 15, z & 15);

        double centerX = x + 0.5;
        double centerY = precHeight + 0.5;
        double centerZ = z + 0.5;
        Collection<World> subworlds = ((IMixinWorld) this.getParentWorld()).getSubWorlds();
        if (this.isSubWorld) {
            Vec3 globalCoords = this.transformToGlobal(centerX, centerY, centerZ);
            centerX = globalCoords.xCoord;
            centerY = globalCoords.yCoord;
            centerZ = globalCoords.zCoord;
        }
        for (World subworld : subworlds) {
            // For now - skipping worlds rotated around x/z. Maybe add this later
            if (((SubWorld) subworld).getRotationPitch() != 0 || ((SubWorld) subworld).getRotationRoll() != 0) continue;
            // "The most important part of every investigation is not to find yourself guilty"
            if (subworld == (World) (Object) this) continue;
            AxisAlignedBB worldBB = ((SubWorld) subworld).getMaximumCloseWorldBBRotated();
            if (centerX >= worldBB.minX && centerX <= worldBB.maxX
                && centerZ >= worldBB.minZ
                && centerZ <= worldBB.maxZ) {
                Vec3 localCoords = ((IMixinWorld) subworld)
                    .transformToLocal(Vec3.createVectorHelper(centerX, centerY, centerZ));
                int localX = MathHelper.floor_double(localCoords.xCoord);
                int localY = MathHelper.floor_double(localCoords.yCoord);
                int localZ = MathHelper.floor_double(localCoords.zCoord);
                Chunk subworldChunk = subworld.getChunkFromChunkCoords(localX >> 4, localZ >> 4);
                int subworldHeight = subworldChunk.getPrecipitationHeight(localX & 15, localZ & 15);
                if (subworldHeight > localY) {
                    localCoords.yCoord = subworldHeight + 0.5;
                    Vec3 globalCoords = this.transformToGlobal(localCoords);
                    centerY = globalCoords.yCoord;
                }
            }
        }
        precHeight = MathHelper.floor_double(centerY);

        return precHeight > 255 ? 255 : precHeight;
    }

    public boolean canBlockSeeTheSky(ForgeDirection dir, int x, int y, int z) {
        if (dir == ForgeDirection.UP) {
            return this.canBlockSeeTheSky(x, y, z);
        } else {
            return false;
        }
    }

    /**
     * Checks if the specified block is able to see the sky
     */
    @Overwrite
    public boolean canBlockSeeTheSky(int x, int y, int z) {
        if (this.isSubWorld) {
            return this.getChunkFromChunkCoords(x >> 4, z >> 4)
                .canBlockSeeTheSky(x & 15, y, z & 15);
        } else {
            boolean canSee = this.getChunkFromChunkCoords(x >> 4, z >> 4)
                .canBlockSeeTheSky(x & 15, y, z & 15);
            if (canSee) {
                double centerX = x + 0.5;
                double centerY = y + 0.5;
                double centerZ = z + 0.5;
                for (World subworld : this.getSubWorlds()) {
                    // For now - skipping worlds rotated around x/z. Maybe add this later
                    if (((SubWorld) subworld).getRotationPitch() != 0 || ((SubWorld) subworld).getRotationRoll() != 0)
                        continue;
                    // "The most important part of every investigation is not to find yourself guilty"
                    if (subworld == (World) (Object) this) continue;
                    AxisAlignedBB worldBB = ((SubWorld) subworld).getMaximumCloseWorldBBRotated();
                    if (centerX >= worldBB.minX && centerX <= worldBB.maxX
                        && centerZ >= worldBB.minZ
                        && centerZ <= worldBB.maxZ) {
                        Vec3 localCoords = ((IMixinWorld) subworld).transformToLocal(centerX, centerY, centerZ);
                        int localX = MathHelper.floor_double(localCoords.xCoord);
                        int localY = MathHelper.floor_double(localCoords.yCoord);
                        int localZ = MathHelper.floor_double(localCoords.zCoord);
                        Chunk subworldChunk = subworld.getChunkFromChunkCoords(localX >> 4, localZ >> 4);
                        int subworldHeight = subworldChunk.getHeightValue(localX & 15, localZ & 15);
                        if (subworldHeight > localY) {
                            canSee = false;
                            break;
                        }
                    }
                }
            }
            return canSee;
        }
    }

    @Inject(method = "playSoundEffect", at = @At(value = "HEAD"), cancellable = true)
    public void injectPlaySoundEffect(double x, double y, double z, String soundName, float volume, float pitch,
        CallbackInfo ci) {
        if (this.isSubWorld) {
            World parentWorld = this.getParentWorld();
            Vec3 globalCoords = this.transformToGlobal(x, y, z);
            for (int i = 0; i < parentWorld.worldAccesses.size(); ++i) {
                ((IWorldAccess) parentWorld.worldAccesses.get(i))
                    .playSound(soundName, globalCoords.xCoord, globalCoords.yCoord, globalCoords.zCoord, volume, pitch);
            }
            ci.cancel();
        }
    }

    @Inject(method = "setWorldTime", at = @At("TAIL"), remap = false)
    public void setWorldTime(long time, CallbackInfo ci) {
        for (World subworld : getSubWorlds()) {
            subworld.setWorldTime(time);
        }
    }

    public void markBlocksDirtyDirectional(Direction dir, int coord1, int coord2, int coordVarMin, int coordVarMax) {
        Axis axis = dir.getAxis();
        if (axis == Axis.Y) {
            this.markBlocksDirtyVertical(coord1, coord2, coordVarMin, coordVarMax);
        } else {
            int i1;

            if (coordVarMin > coordVarMax) {
                i1 = coordVarMax;
                coordVarMax = coordVarMin;
                coordVarMin = i1;
            }

            if (!this.provider.hasNoSky) {
                int x = axis == Axis.X ? coordVarMin : coord1;
                int y = coord2;
                int z = axis == Axis.Z ? coordVarMin : coord1;
                for (i1 = coordVarMin; i1 <= coordVarMax; ++i1) {
                    if (axis == Axis.X) {
                        x = i1;
                    } else {
                        z = i1;
                    }
                    this.updateSkyLight(dir, x, y, z);
                }
            }

            int minX;
            int minZ;
            int maxX;
            int maxZ;
            if (axis == Axis.X) {
                minX = coordVarMin;
                maxX = coordVarMax;
                minZ = maxZ = coord1;
            } else {
                minZ = coordVarMin;
                maxZ = coordVarMax;
                minX = maxX = coord1;
            }
            this.markBlockRangeForRenderUpdate(minX, coord2, minZ, maxX, coord2, maxZ);
        }
    }

    public boolean updateSkyLight(Direction dir, int x, int y, int z) {
        if (!this.doChunksNearChunkExist(x, y, z, 17)) {
            return false;
        } else {
            int l = 0;
            int i1 = 0;
            this.theProfiler.startSection("getBrightness" + dir.toString());
            int j1 = this.getSavedSkyLightValue(dir, x, y, z);
            int k1 = this.computeSkyLightValue(dir, x, y, z);
            int l1;
            int i2;
            int j2;
            int k2;
            int l2;
            int i3;
            int j3;
            int k3;
            int l3;

            if (k1 > j1) {
                this.lightUpdateBlockList[i1++] = 133152;
            } else if (k1 < j1) {
                this.lightUpdateBlockList[i1++] = 133152 | j1 << 18;

                while (l < i1) {
                    l1 = this.lightUpdateBlockList[l++];
                    i2 = (l1 & 63) - 32 + x;
                    j2 = (l1 >> 6 & 63) - 32 + y;
                    k2 = (l1 >> 12 & 63) - 32 + z;
                    l2 = l1 >> 18 & 15;
                    i3 = this.getSavedSkyLightValue(dir, i2, j2, k2);

                    if (i3 == l2) {
                        this.setLightValue(dir, EnumSkyBlock.Sky, i2, j2, k2, 0);

                        if (l2 > 0) {
                            j3 = MathHelper.abs_int(i2 - x);
                            k3 = MathHelper.abs_int(j2 - y);
                            l3 = MathHelper.abs_int(k2 - z);

                            if (j3 + k3 + l3 < 17) {
                                for (int i4 = 0; i4 < 6; ++i4) {
                                    int j4 = i2 + Facing.offsetsXForSide[i4];
                                    int k4 = j2 + Facing.offsetsYForSide[i4];
                                    int l4 = k2 + Facing.offsetsZForSide[i4];
                                    int i5 = Math.max(
                                        1,
                                        this.getBlock(j4, k4, l4)
                                            .getLightOpacity((World) (Object) this, j4, k4, l4));
                                    i3 = this.getSavedSkyLightValue(dir, j4, k4, l4);

                                    if (i3 == l2 - i5 && i1 < this.lightUpdateBlockList.length) {
                                        this.lightUpdateBlockList[i1++] = j4 - x + 32 | k4 - y + 32 << 6
                                            | l4 - z + 32 << 12
                                            | l2 - i5 << 18;
                                    }
                                }
                            }
                        }
                    }
                }

                l = 0;
            }

            this.theProfiler.endSection();
            this.theProfiler.startSection("checkedPosition < toCheckCount (" + dir.toString() + ")");

            while (l < i1) {
                l1 = this.lightUpdateBlockList[l++];
                i2 = (l1 & 63) - 32 + x;
                j2 = (l1 >> 6 & 63) - 32 + y;
                k2 = (l1 >> 12 & 63) - 32 + z;
                l2 = this.getSavedSkyLightValue(dir, i2, j2, k2);
                i3 = this.computeSkyLightValue(dir, i2, j2, k2);

                if (i3 != l2) {
                    this.setLightValue(dir, EnumSkyBlock.Sky, i2, j2, k2, i3);

                    if (i3 > l2) {
                        j3 = Math.abs(i2 - x);
                        k3 = Math.abs(j2 - y);
                        l3 = Math.abs(k2 - z);
                        boolean flag = i1 < this.lightUpdateBlockList.length - 6;

                        if (j3 + k3 + l3 < 17 && flag) {
                            if (this.getSavedSkyLightValue(dir, i2 - 1, j2, k2) < i3) {
                                this.lightUpdateBlockList[i1++] = i2 - 1
                                    - x
                                    + 32
                                    + (j2 - y + 32 << 6)
                                    + (k2 - z + 32 << 12);
                            }

                            if (this.getSavedSkyLightValue(dir, i2 + 1, j2, k2) < i3) {
                                this.lightUpdateBlockList[i1++] = i2 + 1
                                    - x
                                    + 32
                                    + (j2 - y + 32 << 6)
                                    + (k2 - z + 32 << 12);
                            }

                            if (this.getSavedSkyLightValue(dir, i2, j2 - 1, k2) < i3) {
                                this.lightUpdateBlockList[i1++] = i2 - x
                                    + 32
                                    + (j2 - 1 - y + 32 << 6)
                                    + (k2 - z + 32 << 12);
                            }

                            if (this.getSavedSkyLightValue(dir, i2, j2 + 1, k2) < i3) {
                                this.lightUpdateBlockList[i1++] = i2 - x
                                    + 32
                                    + (j2 + 1 - y + 32 << 6)
                                    + (k2 - z + 32 << 12);
                            }

                            if (this.getSavedSkyLightValue(dir, i2, j2, k2 - 1) < i3) {
                                this.lightUpdateBlockList[i1++] = i2 - x
                                    + 32
                                    + (j2 - y + 32 << 6)
                                    + (k2 - 1 - z + 32 << 12);
                            }

                            if (this.getSavedSkyLightValue(dir, i2, j2, k2 + 1) < i3) {
                                this.lightUpdateBlockList[i1++] = i2 - x
                                    + 32
                                    + (j2 - y + 32 << 6)
                                    + (k2 + 1 - z + 32 << 12);
                            }
                        }
                    }
                }
            }

            this.theProfiler.endSection();
            return true;
        }
    }

    public int getSavedSkyLightValue(Direction dir, int x, int y, int z) {
        if (!this.isSubWorld) return this.getSavedLightValue(EnumSkyBlock.Sky, x, y, z);
        if (y < 0) {
            y = 0;
        }

        if (y >= 256) {
            y = 255;
        }

        if (x >= -30000000 && z >= -30000000 && x < 30000000 && z < 30000000) {
            int l = x >> 4;
            int i1 = z >> 4;

            if (!this.chunkExists(l, i1)) {
                return EnumSkyBlock.Sky.defaultLightValue;
            } else {
                Chunk testChunk = this.getChunkFromChunkCoords(l, i1);
                if (!(testChunk instanceof ChunkSubWorld)) MetaworldsMod.breakpoint();
                ChunkSubWorld chunk = (ChunkSubWorld) this.getChunkFromChunkCoords(l, i1);
                return chunk.getSavedSkyLightValue(dir, x & 15, y, z & 15);
            }
        } else {
            return EnumSkyBlock.Sky.defaultLightValue;
        }
    }

    private int computeSkyLightValue(Direction dir, int x, int y, int z) {
        if (this.canBlockSeeTheSky(dir.toForgeDirection(), x, y, z)) {
            return 15;
        } else {
            Block block = this.getBlock(x, y, z);
            int blockLight = block.getLightValue((World) (Object) this, x, y, z);
            int l = 0;
            int i1 = block.getLightOpacity((World) (Object) this, x, y, z);

            if (i1 >= 15 && blockLight > 0) {
                i1 = 1;
            }

            if (i1 < 1) {
                i1 = 1;
            }

            if (i1 >= 15) {
                return 0;
            } else {
                for (int j1 = 0; j1 < 6; ++j1) {
                    int k1 = x + Facing.offsetsXForSide[j1];
                    int l1 = y + Facing.offsetsYForSide[j1];
                    int i2 = z + Facing.offsetsZForSide[j1];
                    int j2 = this.getSavedSkyLightValue(dir, k1, l1, i2) - i1;

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

    public void setLightValue(Direction dir, EnumSkyBlock lightType, int x, int y, int z, int value) {
        if (dir == Direction.UP) {
            this.setLightValue(lightType, x, y, z, value);
        } else {
            if (this instanceof SubWorld && x >= -30000000 && z >= -30000000 && x < 30000000 && z < 30000000) {
                if (y >= 0) {
                    if (y < 256) {
                        if (this.chunkExists(x >> 4, z >> 4)) {
                            ChunkSubWorld chunk = (ChunkSubWorld) this.getChunkFromChunkCoords(x >> 4, z >> 4);
                            chunk.setLightValue(dir, lightType, x & 15, y, z & 15, value);

                            for (int i1 = 0; i1 < this.worldAccesses.size(); ++i1) {
                                ((IWorldAccess) this.worldAccesses.get(i1)).markBlockForRenderUpdate(x, y, z);
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean updateSkyLight(int x, int y, int z) {
        if (!this.doChunksNearChunkExist(x, y, z, 17)) {
            return false;
        } else {
            int l = 0;
            int i1 = 0;
            this.theProfiler.startSection("getBrightness");
            int j1 = this.getSavedSkyLightValue(Direction.UP, x, y, z);
            int k1 = this.computeLightValue(x, y, z, EnumSkyBlock.Sky);
            int l1;
            int i2;
            int j2;
            int k2;
            int l2;
            int i3;
            int j3;
            int k3;
            int l3;

            if (k1 > j1) {
                this.lightUpdateBlockList[i1++] = 133152;
            } else if (k1 < j1) {
                this.lightUpdateBlockList[i1++] = 133152 | j1 << 18;

                while (l < i1) {
                    l1 = this.lightUpdateBlockList[l++];
                    i2 = (l1 & 63) - 32 + x;
                    j2 = (l1 >> 6 & 63) - 32 + y;
                    k2 = (l1 >> 12 & 63) - 32 + z;
                    l2 = l1 >> 18 & 15;
                    i3 = this.getSavedSkyLightValue(Direction.UP, i2, j2, k2);

                    if (i3 == l2) {
                        this.setLightValue(EnumSkyBlock.Sky, i2, j2, k2, 0);

                        if (l2 > 0) {
                            j3 = MathHelper.abs_int(i2 - x);
                            k3 = MathHelper.abs_int(j2 - y);
                            l3 = MathHelper.abs_int(k2 - z);

                            if (j3 + k3 + l3 < 17) {
                                for (int i4 = 0; i4 < 6; ++i4) {
                                    int j4 = i2 + Facing.offsetsXForSide[i4];
                                    int k4 = j2 + Facing.offsetsYForSide[i4];
                                    int l4 = k2 + Facing.offsetsZForSide[i4];
                                    int i5 = Math.max(
                                        1,
                                        this.getBlock(j4, k4, l4)
                                            .getLightOpacity((IBlockAccess) this, j4, k4, l4));
                                    i3 = this.getSavedSkyLightValue(Direction.UP, j4, k4, l4);

                                    if (i3 == l2 - i5 && i1 < this.lightUpdateBlockList.length) {
                                        this.lightUpdateBlockList[i1++] = j4 - x + 32 | k4 - y + 32 << 6
                                            | l4 - z + 32 << 12
                                            | l2 - i5 << 18;
                                    }
                                }
                            }
                        }
                    }
                }

                l = 0;
            }

            this.theProfiler.endSection();
            this.theProfiler.startSection("checkedPosition < toCheckCount");

            while (l < i1) {
                l1 = this.lightUpdateBlockList[l++];
                i2 = (l1 & 63) - 32 + x;
                j2 = (l1 >> 6 & 63) - 32 + y;
                k2 = (l1 >> 12 & 63) - 32 + z;
                l2 = this.getSavedSkyLightValue(Direction.UP, i2, j2, k2);
                i3 = this.computeLightValue(i2, j2, k2, EnumSkyBlock.Sky);

                if (i3 != l2) {
                    this.setLightValue(EnumSkyBlock.Sky, i2, j2, k2, i3);

                    if (i3 > l2) {
                        j3 = Math.abs(i2 - x);
                        k3 = Math.abs(j2 - y);
                        l3 = Math.abs(k2 - z);
                        boolean flag = i1 < this.lightUpdateBlockList.length - 6;

                        if (j3 + k3 + l3 < 17 && flag) {
                            if (this.getSavedSkyLightValue(Direction.UP, i2 - 1, j2, k2) < i3) {
                                this.lightUpdateBlockList[i1++] = i2 - 1
                                    - x
                                    + 32
                                    + (j2 - y + 32 << 6)
                                    + (k2 - z + 32 << 12);
                            }

                            if (this.getSavedSkyLightValue(Direction.UP, i2 + 1, j2, k2) < i3) {
                                this.lightUpdateBlockList[i1++] = i2 + 1
                                    - x
                                    + 32
                                    + (j2 - y + 32 << 6)
                                    + (k2 - z + 32 << 12);
                            }

                            if (this.getSavedSkyLightValue(Direction.UP, i2, j2 - 1, k2) < i3) {
                                this.lightUpdateBlockList[i1++] = i2 - x
                                    + 32
                                    + (j2 - 1 - y + 32 << 6)
                                    + (k2 - z + 32 << 12);
                            }

                            if (this.getSavedSkyLightValue(Direction.UP, i2, j2 + 1, k2) < i3) {
                                this.lightUpdateBlockList[i1++] = i2 - x
                                    + 32
                                    + (j2 + 1 - y + 32 << 6)
                                    + (k2 - z + 32 << 12);
                            }

                            if (this.getSavedSkyLightValue(Direction.UP, i2, j2, k2 - 1) < i3) {
                                this.lightUpdateBlockList[i1++] = i2 - x
                                    + 32
                                    + (j2 - y + 32 << 6)
                                    + (k2 - 1 - z + 32 << 12);
                            }

                            if (this.getSavedSkyLightValue(Direction.UP, i2, j2, k2 + 1) < i3) {
                                this.lightUpdateBlockList[i1++] = i2 - x
                                    + 32
                                    + (j2 - y + 32 << 6)
                                    + (k2 + 1 - z + 32 << 12);
                            }
                        }
                    }
                }
            }

            this.theProfiler.endSection();
            return true;
        }
    }
}
