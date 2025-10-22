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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.GameRules;
import net.minecraft.world.IWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

import org.jblas.DoubleMatrix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.command.ISubWorldSelector;
import su.sergiusonesimus.metaworlds.util.UnmodifiableSingleObjPlusCollection;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.server.IMixinMinecraftServer;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

@Mixin(World.class)
public abstract class MixinWorld implements IMixinWorld {

    private final boolean isSubWorld = this instanceof SubWorld;
    public Map<Integer, World> childSubWorlds;
    private UnmodifiableSingleObjPlusCollection<World> allWorlds;

    @Shadow(remap = false)
    public static double MAX_ENTITY_RADIUS;

    @SuppressWarnings("rawtypes")
    @Shadow(remap = true)
    public ArrayList collidingBoundingBoxes;

    @Shadow(remap = false)
    public boolean restoringBlockSnapshots;

    @Shadow(remap = true)
    public List<EntityPlayer> playerEntities;

    @Shadow(remap = true)
    public List<Entity> loadedEntityList;

    @Shadow(remap = true)
    public WorldInfo worldInfo;

    @Shadow(remap = true)
    protected List<IWorldAccess> worldAccesses;

    // TODO

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
    public <T> List<T> selectEntitiesWithinAABB(Class<T> clazz, AxisAlignedBB bb, IEntitySelector selector) {
        return null;
    }

    @Shadow(remap = true)
    public List<Entity> getEntitiesWithinAABBExcludingEntity(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB,
        IEntitySelector par3IEntitySelector) {
        return null;
    }

    @Shadow(remap = true)
    public boolean isMaterialInBB(AxisAlignedBB par1AxisAlignedBB, Material par2Material) {
        return false;
    }

    @Shadow(remap = true)
    public List<AxisAlignedBB> getCollidingBoundingBoxes(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB) {
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
    public List<Entity> getEntitiesWithinAABBExcludingEntity(Entity p_72839_1_, AxisAlignedBB p_72839_2_) {
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
        return this.allWorlds;
    }

    public Collection<World> getSubWorlds() {
        return this.childSubWorlds.values();
    }

    public Map<Integer, World> getSubWorldsMap() {
        return this.childSubWorlds;
    }

    public int getWorldsCount() {
        return this.childSubWorlds.size() + 1;
    }

    public int getUnoccupiedSubworldID() {
        Map<Integer, World> subworlds = ((IMixinMinecraftServer) MinecraftServer.mcServer).getExistingSubWorlds();
        int maxID = subworlds.size() + 1;
        for (int i = 1; i < maxID; i++) if (!subworlds.containsKey(i)) return i;
        return maxID;
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

    public List<AxisAlignedBB> getCollidingBoundingBoxesGlobal(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB) {
        return this.getCollidingBoundingBoxes(par1Entity, par2AxisAlignedBB);
    }

    public List<Entity> getEntitiesWithinAABBExcludingEntityLocal(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB) {
        return this.getEntitiesWithinAABBExcludingEntityLocal(par1Entity, par2AxisAlignedBB, (IEntitySelector) null);
    }

    public List<Entity> getEntitiesWithinAABBExcludingEntityLocal(Entity par1Entity, AxisAlignedBB par2AxisAlignedBB,
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

    @Inject(
        method = "<init>(Lnet/minecraft/world/storage/ISaveHandler;Ljava/lang/String;Lnet/minecraft/world/WorldSettings;Lnet/minecraft/world/WorldProvider;Lnet/minecraft/profiler/Profiler;)V",
        at = @At("TAIL"))
    public void setSubworlds(ISaveHandler p_i45369_1_, String p_i45369_2_, WorldSettings p_i45369_3_,
        WorldProvider p_i45369_4_, Profiler p_i45369_5_, CallbackInfo ci) {
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

    /**
     * Checks if the specified block is able to see the sky
     */
    @Overwrite
    public boolean canBlockSeeTheSky(int x, int y, int z) {
        boolean canSee = this.getChunkFromChunkCoords(x >> 4, z >> 4)
            .canBlockSeeTheSky(x & 15, y, z & 15);
        if (canSee) {
            double centerX = x + 0.5;
            double centerY = y + 0.5;
            double centerZ = z + 0.5;
            Collection<World> subworlds = ((IMixinWorld) this.getParentWorld()).getSubWorlds();
            if (this.isSubWorld) {
                Vec3 globalCoords = this.transformToGlobal(Vec3.createVectorHelper(centerX, centerY, centerZ));
                centerX = globalCoords.xCoord;
                centerY = globalCoords.yCoord;
                centerZ = globalCoords.zCoord;
            }
            for (World subworld : subworlds) {
                // For now - skipping worlds rotated around x/z. Maybe add this later
                if (((SubWorld) subworld).getRotationPitch() != 0 || ((SubWorld) subworld).getRotationRoll() != 0)
                    continue;
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

    @Override
    public List<World> getSubworldsWithinAABBExcludingSubworld(SubWorld subworld, AxisAlignedBB bb) {
        return getSubworldsWithinAABBExcludingSubworld(subworld, bb, null);
    }

    @Override
    public List<World> getSubworldsWithinAABBExcludingSubworld(SubWorld subworld, AxisAlignedBB bb,
        ISubWorldSelector selector) {
        ArrayList<World> result = new ArrayList<World>();

        for (World currentSubworld : getSubWorlds()) {
            if (currentSubworld == subworld) continue;
            if (selector == null || selector.isSubWorldApplicable(currentSubworld)) {
                if (((SubWorld) currentSubworld).getMaximumStretchedWorldBB(false, false)
                    .intersectsWith(bb)) result.add(currentSubworld);
            }
        }

        return result;
    }

    @Override
    public <T> List<T> getSubworldsWithinAABB(Class<T> subworldClass, AxisAlignedBB bb) {
        return getSubworldsWithinAABB(subworldClass, bb, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> getSubworldsWithinAABB(Class<T> subworldClass, AxisAlignedBB bb, ISubWorldSelector selector) {
        if (subworldClass == null) return null;
        ArrayList<T> result = new ArrayList<T>();

        for (World currentSubworld : getSubWorlds()) {
            if (subworldClass.isInstance(currentSubworld)) {
                if (selector == null || selector.isSubWorldApplicable(currentSubworld)) {
                    if (((SubWorld) currentSubworld).getMaximumStretchedWorldBB(false, false)
                        .intersectsWith(bb)) result.add((T) currentSubworld);
                }
            }
        }

        return result;
    }
}
