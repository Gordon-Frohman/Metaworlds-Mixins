package su.sergiusonesimus.metaworlds.world.chunk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.metaworlds.world.chunk.storage.ExtendedBlockStorageSubWorld;

public class ChunkSubWorld extends Chunk {

    public SubWorld subWorldObj;
    public short collisionLimitXPosPlane;
    public short collisionLimitXNegPlane;
    public short collisionLimitYPosPlane;
    public short collisionLimitYNegPlane;
    public short collisionLimitZPosPlane;
    public short collisionLimitZNegPlane;
    public byte[] collisionLimitXPosLineY;
    public byte[] collisionLimitXPosLineZ;
    public byte[] collisionLimitXNegLineY;
    public byte[] collisionLimitXNegLineZ;
    public byte[] collisionLimitYPosLineX;
    public byte[] collisionLimitYPosLineZ;
    public byte[] collisionLimitYNegLineX;
    public byte[] collisionLimitYNegLineZ;
    public byte[] collisionLimitZPosLineX;
    public byte[] collisionLimitZPosLineY;
    public byte[] collisionLimitZNegLineX;
    public byte[] collisionLimitZNegLineY;
    // public int[] collisionLimitsMapXPos = new int[4096];
    // public int[] collisionLimitsMapXNeg = new int[4096];
    public int[] collisionLimitsMapYPos = new int[256];
    // public int[] collisionLimitsMapYNeg = new int[256];
    // public int[] collisionLimitsMapZPos = new int[4096];
    // public int[] collisionLimitsMapZNeg = new int[4096];
    public boolean isEmpty = true;

    public ChunkSubWorld(Chunk chunk) {
        super(chunk.worldObj, chunk.xPosition, chunk.zPosition);

        subWorldObj = (SubWorld) worldObj;

        storageArrays = chunk.storageArrays;
        for (int i = 0; i < storageArrays.length; i++) {
            if (storageArrays[i] != null) {
                if (!(storageArrays[i] instanceof ExtendedBlockStorageSubWorld)) {
                    storageArrays[i] = new ExtendedBlockStorageSubWorld(storageArrays[i]);
                }
                if (!this.storageArrays[i].isEmpty()) this.subWorldObj.registerExtendedBlockStorage(
                    (ExtendedBlockStorageSubWorld) this.storageArrays[i],
                    this.xPosition,
                    i,
                    this.zPosition);
            }
        }

        blockBiomeArray = chunk.blockBiomeArray;
        precipitationHeightMap = chunk.precipitationHeightMap;
        updateSkylightColumns = chunk.updateSkylightColumns;
        isChunkLoaded = chunk.isChunkLoaded;
        heightMap = chunk.heightMap;
        isGapLightingUpdated = chunk.isGapLightingUpdated;
        chunkTileEntityMap = chunk.chunkTileEntityMap;
        entityLists = chunk.entityLists;
        isTerrainPopulated = chunk.isTerrainPopulated;
        isLightPopulated = chunk.isLightPopulated;
        field_150815_m = chunk.field_150815_m;
        isModified = chunk.isModified;
        hasEntities = chunk.hasEntities;
        lastSaveTime = chunk.lastSaveTime;
        sendUpdates = chunk.sendUpdates;
        heightMapMinimum = chunk.heightMapMinimum;
        inhabitedTime = chunk.inhabitedTime;
        queuedLightChecks = chunk.queuedLightChecks;
    }

    public ChunkSubWorld(World par1World, int par2, int par3) {
        super(par1World, par2, par3);

        Arrays.fill(this.collisionLimitsMapYPos, -999);

        subWorldObj = (SubWorld) worldObj;
    }

    public ChunkSubWorld(World p_i45446_1_, Block[] p_i45446_2_, int p_i45446_3_, int p_i45446_4_) {
        this(p_i45446_1_, p_i45446_3_, p_i45446_4_);
        int k = p_i45446_2_.length / 256;
        boolean flag = !p_i45446_1_.provider.hasNoSky;

        for (int l = 0; l < 16; ++l) {
            for (int i1 = 0; i1 < 16; ++i1) {
                for (int j1 = 0; j1 < k; ++j1) {
                    Block block = p_i45446_2_[l << 11 | i1 << 7 | j1];

                    if (block != null && block.getMaterial() != Material.air) {
                        int k1 = j1 >> 4;

                        if (this.storageArrays[k1] == null) {
                            this.storageArrays[k1] = new ExtendedBlockStorageSubWorld(k1 << 4, flag);
                        }

                        boolean wasEmpty = this.storageArrays[k1].isEmpty();
                        this.storageArrays[k1].func_150818_a(l, j1 & 15, i1, block);
                        boolean isEmpty = this.storageArrays[k1].isEmpty();
                        if (wasEmpty != isEmpty) {
                            if (!isEmpty) {
                                this.subWorldObj.registerExtendedBlockStorage(
                                    (ExtendedBlockStorageSubWorld) this.storageArrays[k1],
                                    this.xPosition,
                                    k1,
                                    this.zPosition);
                            } else {
                                this.subWorldObj.unregisterExtendedBlockStorage(this.xPosition, k1, this.zPosition);
                            }
                        }
                    }
                }
            }
        }
    }

    public ChunkSubWorld(World p_i45447_1_, Block[] p_i45447_2_, byte[] p_i45447_3_, int p_i45447_4_, int p_i45447_5_) {
        this(p_i45447_1_, p_i45447_4_, p_i45447_5_);
        int k = p_i45447_2_.length / 256;
        boolean flag = !p_i45447_1_.provider.hasNoSky;

        for (int l = 0; l < 16; ++l) {
            for (int i1 = 0; i1 < 16; ++i1) {
                for (int j1 = 0; j1 < k; ++j1) {
                    int k1 = l * k * 16 | i1 * k | j1;
                    Block block = p_i45447_2_[k1];

                    if (block != null && block != Blocks.air) {
                        int l1 = j1 >> 4;

                        if (this.storageArrays[l1] == null) {
                            this.storageArrays[l1] = new ExtendedBlockStorageSubWorld(l1 << 4, flag);
                        }

                        boolean wasEmpty = this.storageArrays[l1].isEmpty();
                        this.storageArrays[l1].func_150818_a(l, j1 & 15, i1, block);
                        this.storageArrays[l1].setExtBlockMetadata(l, j1 & 15, i1, p_i45447_3_[k1]);
                        boolean isEmpty = this.storageArrays[l1].isEmpty();
                        if (wasEmpty != isEmpty) {
                            if (!isEmpty) {
                                this.subWorldObj.registerExtendedBlockStorage(
                                    (ExtendedBlockStorageSubWorld) this.storageArrays[l1],
                                    this.xPosition,
                                    l1,
                                    this.zPosition);
                            } else {
                                this.subWorldObj.unregisterExtendedBlockStorage(this.xPosition, l1, this.zPosition);
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean needsSaving(boolean par1) {
        if (this.hasEntities || this.isModified) {
            this.isEmpty = false;
        }

        return this.isEmpty ? false : super.needsSaving(par1);
    }

    public void populateChunk(IChunkProvider par1IChunkProvider, IChunkProvider par2IChunkProvider, int par3,
        int par4) {
        super.populateChunk(par1IChunkProvider, par2IChunkProvider, par3, par4);
        this.isTerrainPopulated = true;
        this.isLightPopulated = true;
    }

    /**
     * Gets the height of the topmost block, whether it is solid or not
     */
    public int getCollisionLimitYPos(int x, int z) {
        int k = x | z << 4;
        int l = this.collisionLimitsMapYPos[k];

        if (l == -999) {
            int i1 = this.getTopFilledSegment() + 15;
            l = -1;

            while (i1 > 0 && l == -1) {
                Block block = this.getBlock(x, i1, z);

                if (block == Blocks.air) {
                    --i1;
                } else {
                    l = i1 + 1;
                }
            }

            this.collisionLimitsMapYPos[k] = l;
        }

        return l;
    }

    /**
     * Gets the amount of light saved in this block (doesn't adjust for daylight)
     */
    public int getSavedLightValue(EnumSkyBlock lightType, int x, int y, int z) {
        ExtendedBlockStorageSubWorld extendedblockstorage = (ExtendedBlockStorageSubWorld) this.storageArrays[y >> 4];
        if (extendedblockstorage == null) {
            return this.canBlockSeeTheSky(x, y, z) ? lightType.defaultLightValue : 0;
        } else {
            if (lightType == EnumSkyBlock.Sky) {
                if (this.worldObj.provider.hasNoSky) {
                    return 0;
                } else {
                    Vec3 lightVec = subWorldObj.getLightVector();
                    double xPart = Math.abs(lightVec.xCoord);
                    double yPart = Math.abs(lightVec.yCoord);
                    double zPart = Math.abs(lightVec.zCoord);
                    double sum = xPart + yPart + zPart;
                    xPart /= sum;
                    yPart /= sum;
                    zPart /= sum;
                    int lightX = extendedblockstorage
                        .getExtSkylightValue(lightVec.xCoord > 0 ? Direction.EAST : Direction.WEST, x, y & 15, z);
                    int lightY = extendedblockstorage
                        .getExtSkylightValue(lightVec.yCoord > 0 ? Direction.UP : Direction.DOWN, x, y & 15, z);
                    int lightZ = extendedblockstorage
                        .getExtSkylightValue(lightVec.zCoord > 0 ? Direction.SOUTH : Direction.NORTH, x, y & 15, z);
                    return (int) (Math.round(lightX * xPart + lightY * yPart + lightZ * zPart));
                }
            } else {
                return lightType == EnumSkyBlock.Block ? extendedblockstorage.getExtBlocklightValue(x, y & 15, z)
                    : lightType.defaultLightValue;
            }
        }
    }

    public int getSavedSkyLightValue(Direction dir, int x, int y, int z) {
        ExtendedBlockStorageSubWorld extendedblockstorage = (ExtendedBlockStorageSubWorld) this.storageArrays[y >> 4];
        return extendedblockstorage == null ? (this.canBlockSeeTheSky(x, y, z) ? EnumSkyBlock.Sky.defaultLightValue : 0)
            : (this.worldObj.provider.hasNoSky ? 0 : extendedblockstorage.getExtSkylightValue(dir, x, y & 15, z));
    }

    /**
     * Initialise this chunk with new binary data
     */
    @SideOnly(Side.CLIENT)
    public void fillChunk(byte[] p_76607_1_, int p_76607_2_, int p_76607_3_, boolean p_76607_4_) {
        Iterator iterator = chunkTileEntityMap.values()
            .iterator();
        while (iterator.hasNext()) {
            TileEntity tileEntity = (TileEntity) iterator.next();
            tileEntity.updateContainingBlockInfo();
            tileEntity.getBlockMetadata();
            tileEntity.getBlockType();
        }

        int k = 0;
        boolean flag1 = !this.worldObj.provider.hasNoSky;
        int l;

        for (l = 0; l < this.storageArrays.length; ++l) {
            if ((p_76607_2_ & 1 << l) != 0) {
                if (this.storageArrays[l] == null) {
                    this.storageArrays[l] = new ExtendedBlockStorageSubWorld(l << 4, flag1);
                }

                byte[] abyte1 = this.storageArrays[l].getBlockLSBArray();
                System.arraycopy(p_76607_1_, k, abyte1, 0, abyte1.length);
                k += abyte1.length;
            } else if (p_76607_4_ && this.storageArrays[l] != null) {
                this.storageArrays[l] = null;
            }
        }

        NibbleArray nibblearray;

        for (l = 0; l < this.storageArrays.length; ++l) {
            if ((p_76607_2_ & 1 << l) != 0 && this.storageArrays[l] != null) {
                nibblearray = this.storageArrays[l].getMetadataArray();
                System.arraycopy(p_76607_1_, k, nibblearray.data, 0, nibblearray.data.length);
                k += nibblearray.data.length;
            }
        }

        for (l = 0; l < this.storageArrays.length; ++l) {
            if ((p_76607_2_ & 1 << l) != 0 && this.storageArrays[l] != null) {
                nibblearray = this.storageArrays[l].getBlocklightArray();
                System.arraycopy(p_76607_1_, k, nibblearray.data, 0, nibblearray.data.length);
                k += nibblearray.data.length;
            }
        }

        if (flag1) {
            for (l = 0; l < this.storageArrays.length; ++l) {
                if ((p_76607_2_ & 1 << l) != 0 && this.storageArrays[l] != null) {
                    nibblearray = this.storageArrays[l].getSkylightArray();
                    System.arraycopy(p_76607_1_, k, nibblearray.data, 0, nibblearray.data.length);
                    k += nibblearray.data.length;
                }
            }
        }

        for (l = 0; l < this.storageArrays.length; ++l) {
            if ((p_76607_3_ & 1 << l) != 0) {
                if (this.storageArrays[l] == null) {
                    k += 2048;
                } else {
                    nibblearray = this.storageArrays[l].getBlockMSBArray();

                    if (nibblearray == null) {
                        nibblearray = this.storageArrays[l].createBlockMSBArray();
                    }

                    System.arraycopy(p_76607_1_, k, nibblearray.data, 0, nibblearray.data.length);
                    k += nibblearray.data.length;
                }
            } else
                if (p_76607_4_ && this.storageArrays[l] != null && this.storageArrays[l].getBlockMSBArray() != null) {
                    this.storageArrays[l].clearMSBArray();
                }
        }

        if (p_76607_4_) {
            System.arraycopy(p_76607_1_, k, this.blockBiomeArray, 0, this.blockBiomeArray.length);
            int i1 = k + this.blockBiomeArray.length;
        }

        for (l = 0; l < this.storageArrays.length; ++l) {
            if (this.storageArrays[l] != null && (p_76607_2_ & 1 << l) != 0) {
                boolean wasEmpty = this.storageArrays[l].isEmpty();
                this.storageArrays[l].removeInvalidBlocks();
                boolean isEmpty = this.storageArrays[l].isEmpty();
                if (wasEmpty != isEmpty) {
                    if (!isEmpty) {
                        this.subWorldObj.registerExtendedBlockStorage(
                            (ExtendedBlockStorageSubWorld) this.storageArrays[l],
                            this.xPosition,
                            l,
                            this.zPosition);
                    } else {
                        this.subWorldObj.unregisterExtendedBlockStorage(this.xPosition, l, this.zPosition);
                    }
                }
            }
        }

        this.isLightPopulated = true;
        this.isTerrainPopulated = true;
        this.generateHeightMap();
        List<TileEntity> invalidList = new ArrayList<TileEntity>();
        iterator = this.chunkTileEntityMap.values()
            .iterator();

        while (iterator.hasNext()) {
            TileEntity tileentity = (TileEntity) iterator.next();
            int x = tileentity.xCoord & 15;
            int y = tileentity.yCoord;
            int z = tileentity.zCoord & 15;
            Block block = tileentity.getBlockType();
            if ((block != getBlock(x, y, z) || tileentity.blockMetadata != this.getBlockMetadata(x, y, z))
                && tileentity.shouldRefresh(
                    block,
                    getBlock(x, y, z),
                    tileentity.blockMetadata,
                    this.getBlockMetadata(x, y, z),
                    worldObj,
                    x,
                    y,
                    z)) {
                invalidList.add(tileentity);
            }
            tileentity.updateContainingBlockInfo();
        }

        for (TileEntity te : invalidList) {
            te.invalidate();
        }
    }

    public boolean func_150807_a(int x, int y, int z, Block block, int meta) {
        int blockKey = z << 4 | x;

        if (y >= this.precipitationHeightMap[blockKey] - 1) {
            this.precipitationHeightMap[blockKey] = -999;
        }

        int worldX = this.xPosition * 16 + x;
        int worldZ = this.zPosition * 16 + z;

        DirectionalChunk dirChunkYNeg = this.subWorldObj.getDirectionalChunk(Direction.DOWN, worldX, y, worldZ);
        DirectionalChunk dirChunkXPos = this.subWorldObj.getDirectionalChunk(Direction.EAST, worldX, y, worldZ);
        DirectionalChunk dirChunkXNeg = this.subWorldObj.getDirectionalChunk(Direction.WEST, worldX, y, worldZ);
        DirectionalChunk dirChunkZPos = this.subWorldObj.getDirectionalChunk(Direction.SOUTH, worldX, y, worldZ);
        DirectionalChunk dirChunkZNeg = this.subWorldObj.getDirectionalChunk(Direction.NORTH, worldX, y, worldZ);

        int highestBlockYPos = this.heightMap[blockKey];
        Integer highestBlockYNeg = dirChunkYNeg.getHeightValue(x, z);
        Integer highestBlockXPos = dirChunkXPos.getHeightValue(z, y & 15);
        Integer highestBlockXNeg = dirChunkXNeg.getHeightValue(z, y & 15);
        Integer highestBlockZPos = dirChunkZPos.getHeightValue(x, y & 15);
        Integer highestBlockZNeg = dirChunkZNeg.getHeightValue(x, y & 15);
        Block block1 = this.getBlock(x, y, z);
        int meta1 = this.getBlockMetadata(x, y, z);

        if (block1 == block && meta1 == meta) {
            return false;
        } else {
            ExtendedBlockStorage extendedblockstorage = this.storageArrays[y >> 4];
            boolean regenSkyLightMapYPos = false;
            boolean regenSkyLightMapYNeg = highestBlockYNeg == null;
            boolean regenSkyLightMapXPos = highestBlockXPos == null;
            boolean regenSkyLightMapXNeg = highestBlockXNeg == null;
            boolean regenSkyLightMapZPos = highestBlockZPos == null;
            boolean regenSkyLightMapZNeg = highestBlockZNeg == null;

            if (extendedblockstorage == null) {
                if (block == Blocks.air) {
                    return false;
                }

                extendedblockstorage = this.storageArrays[y >> 4] = new ExtendedBlockStorageSubWorld(
                    y >> 4 << 4,
                    !this.worldObj.provider.hasNoSky);
                regenSkyLightMapYPos = y >= highestBlockYPos;
                regenSkyLightMapYNeg = regenSkyLightMapYNeg || y <= highestBlockYNeg;
                regenSkyLightMapXPos = regenSkyLightMapXPos || x >= highestBlockXPos;
                regenSkyLightMapXNeg = regenSkyLightMapXNeg || x <= highestBlockXNeg;
                regenSkyLightMapZPos = regenSkyLightMapZPos || z >= highestBlockZPos;
                regenSkyLightMapZNeg = regenSkyLightMapZNeg || z <= highestBlockZNeg;
            }

            int blockOpacity = block1.getLightOpacity(this.worldObj, worldX, y, worldZ);

            if (!this.worldObj.isRemote) {
                block1.onBlockPreDestroy(this.worldObj, worldX, y, worldZ, meta1);
            }

            boolean wasEmpty = this.storageArrays[y >> 4].isEmpty();
            extendedblockstorage.func_150818_a(x, y & 15, z, block);
            extendedblockstorage.setExtBlockMetadata(x, y & 15, z, meta); // This line duplicates the one below, so
                                                                          // breakBlock fires with valid worldstate
            boolean isEmpty = this.storageArrays[y >> 4].isEmpty();
            if (wasEmpty != isEmpty) {
                if (!isEmpty) {
                    this.subWorldObj.registerExtendedBlockStorage(
                        (ExtendedBlockStorageSubWorld) this.storageArrays[y >> 4],
                        this.xPosition,
                        y >> 4,
                        this.zPosition);
                } else {
                    this.subWorldObj.unregisterExtendedBlockStorage(this.xPosition, y >> 4, this.zPosition);
                }
            }

            if (!this.worldObj.isRemote) {
                block1.breakBlock(this.worldObj, worldX, y, worldZ, block1, meta1);
                // After breakBlock a phantom TE might have been created with incorrect meta. This attempts to kill that
                // phantom TE so the normal one can be create properly later
                TileEntity te = this.getTileEntityUnsafe(x & 0x0F, y, z & 0x0F);
                if (te != null && te.shouldRefresh(
                    block1,
                    getBlock(x & 0x0F, y, z & 0x0F),
                    meta1,
                    getBlockMetadata(x & 0x0F, y, z & 0x0F),
                    worldObj,
                    worldX,
                    y,
                    worldZ)) {
                    this.removeTileEntity(x & 0x0F, y, z & 0x0F);
                }
            } else if (block1.hasTileEntity(meta1)) {
                TileEntity te = this.getTileEntityUnsafe(x & 0x0F, y, z & 0x0F);
                if (te != null && te.shouldRefresh(block1, block, meta1, meta, worldObj, worldX, y, worldZ)) {
                    this.worldObj.removeTileEntity(worldX, y, worldZ);
                }
            }

            if (extendedblockstorage.getBlockByExtId(x, y & 15, z) != block) {
                return false;
            } else {
                extendedblockstorage.setExtBlockMetadata(x, y & 15, z, meta);

                int newBlockOpacity = 0;
                if (!regenSkyLightMapYPos || !regenSkyLightMapYNeg
                    || !regenSkyLightMapXPos
                    || !regenSkyLightMapXNeg
                    || !regenSkyLightMapZPos
                    || !regenSkyLightMapZNeg) newBlockOpacity = block.getLightOpacity(this.worldObj, worldX, y, worldZ);

                if (regenSkyLightMapYPos) {
                    this.generateSkylightMap();
                } else {
                    if (newBlockOpacity > 0) {
                        if (y >= highestBlockYPos) {
                            this.relightBlock(x, y + 1, z);
                        }
                    } else if (y == highestBlockYPos - 1) {
                        this.relightBlock(x, y, z);
                    }

                    if (newBlockOpacity != blockOpacity
                        && (newBlockOpacity < blockOpacity || this.getSavedSkyLightValue(Direction.UP, x, y, z) > 0
                            || this.getSavedLightValue(EnumSkyBlock.Block, x, y, z) > 0)) {
                        this.propagateSkylightOcclusion(x, z);
                    }
                }

                if (regenSkyLightMapYNeg) {
                    dirChunkYNeg.generateSkylightMap();
                } else {
                    if (newBlockOpacity > 0) {
                        if (y <= highestBlockYNeg) {
                            dirChunkYNeg.relightBlock(x, y - 1, z);
                        }
                    } else if (y == highestBlockYNeg + 1) {
                        dirChunkYNeg.relightBlock(x, y, z);
                    }

                    if (newBlockOpacity != blockOpacity && (newBlockOpacity < blockOpacity
                        || this.getSavedSkyLightValue(Direction.DOWN, x, y, z) > 0)) {
                        dirChunkYNeg.propagateSkylightOcclusion(x, z);
                    }
                }

                if (regenSkyLightMapXPos) {
                    dirChunkXPos.generateSkylightMap();
                } else {
                    if (newBlockOpacity > 0) {
                        if (x >= highestBlockXPos) {
                            dirChunkXPos.relightBlock(z, this.xPosition * 16 + x + 1, y & 15);
                        }
                    } else if (x == highestBlockXPos - 1) {
                        dirChunkXPos.relightBlock(z, this.xPosition * 16 + x, y & 15);
                    }

                    if (newBlockOpacity != blockOpacity && (newBlockOpacity < blockOpacity
                        || this.getSavedSkyLightValue(Direction.EAST, x, y, z) > 0)) {
                        dirChunkXPos.propagateSkylightOcclusion(z, y & 15);
                    }
                }

                if (regenSkyLightMapXNeg) {
                    dirChunkXNeg.generateSkylightMap();
                } else {
                    if (newBlockOpacity > 0) {
                        if (x <= highestBlockXNeg) {
                            dirChunkXNeg.relightBlock(z, this.xPosition * 16 + x - 1, y & 15);
                        }
                    } else if (x == highestBlockXNeg + 1) {
                        dirChunkXNeg.relightBlock(z, this.xPosition * 16 + x, y & 15);
                    }

                    if (newBlockOpacity != blockOpacity && (newBlockOpacity < blockOpacity
                        || this.getSavedSkyLightValue(Direction.WEST, x, y, z) > 0)) {
                        dirChunkXNeg.propagateSkylightOcclusion(z, y & 15);
                    }
                }

                if (regenSkyLightMapZPos) {
                    dirChunkZPos.generateSkylightMap();
                } else {
                    if (newBlockOpacity > 0) {
                        if (z >= highestBlockZPos) {
                            dirChunkZPos.relightBlock(x, this.zPosition * 16 + z + 1, y & 15);
                        }
                    } else if (z == highestBlockZPos - 1) {
                        dirChunkZPos.relightBlock(x, this.zPosition * 16 + z, y & 15);
                    }

                    if (newBlockOpacity != blockOpacity && (newBlockOpacity < blockOpacity
                        || this.getSavedSkyLightValue(Direction.SOUTH, x, y, z) > 0)) {
                        dirChunkZPos.propagateSkylightOcclusion(x, y & 15);
                    }
                }

                if (regenSkyLightMapZNeg) {
                    dirChunkZNeg.generateSkylightMap();
                } else {
                    if (newBlockOpacity > 0) {
                        if (z <= highestBlockZNeg) {
                            dirChunkZNeg.relightBlock(x, this.zPosition * 16 + z - 1, y & 15);
                        }
                    } else if (z == highestBlockZNeg + 1) {
                        dirChunkZNeg.relightBlock(x, this.zPosition * 16 + z, y & 15);
                    }

                    if (newBlockOpacity != blockOpacity && (newBlockOpacity < blockOpacity
                        || this.getSavedSkyLightValue(Direction.NORTH, x, y, z) > 0)) {
                        dirChunkZNeg.propagateSkylightOcclusion(x, y & 15);
                    }
                }

                TileEntity tileentity;

                if (!this.worldObj.isRemote) {
                    block.onBlockAdded(this.worldObj, worldX, y, worldZ);
                }

                if (block.hasTileEntity(meta)) {
                    tileentity = this.func_150806_e(x, y, z);

                    if (tileentity != null) {
                        tileentity.updateContainingBlockInfo();
                        tileentity.blockMetadata = meta;
                    }
                }

                this.isModified = true;
                return true;
            }
        }
    }

    public void setLightValue(EnumSkyBlock lightType, int x, int y, int z, int minValue) {
        ExtendedBlockStorageSubWorld extendedblockstorage = (ExtendedBlockStorageSubWorld) this.storageArrays[y >> 4];

        if (extendedblockstorage == null) {
            this.storageArrays[y >> 4] = extendedblockstorage = new ExtendedBlockStorageSubWorld(
                y >> 4 << 4,
                !this.worldObj.provider.hasNoSky);
            if (!extendedblockstorage.isEmpty()) this.subWorldObj.registerExtendedBlockStorage(
                (ExtendedBlockStorageSubWorld) extendedblockstorage,
                this.xPosition,
                y >> 4,
                this.zPosition);
            this.generateSkylightMap();
        }

        this.isModified = true;

        if (lightType == EnumSkyBlock.Sky) {
            if (!this.worldObj.provider.hasNoSky) {
                extendedblockstorage.setExtSkylightValue(x, y & 15, z, minValue);
            }
        } else if (lightType == EnumSkyBlock.Block) {
            extendedblockstorage.setExtBlocklightValue(x, y & 15, z, minValue);
        }
    }

    public void setLightValue(Direction dir, EnumSkyBlock lightType, int x, int y, int z, int minValue) {
        ExtendedBlockStorageSubWorld extendedblockstorage = (ExtendedBlockStorageSubWorld) this.storageArrays[y >> 4];

        if (extendedblockstorage == null) {
            this.storageArrays[y >> 4] = extendedblockstorage = new ExtendedBlockStorageSubWorld(
                y >> 4 << 4,
                !this.worldObj.provider.hasNoSky);
            if (!extendedblockstorage.isEmpty()) this.subWorldObj.registerExtendedBlockStorage(
                (ExtendedBlockStorageSubWorld) this.storageArrays[y >> 4],
                this.xPosition,
                y >> 4,
                this.zPosition);
            if (dir == Direction.UP) this.generateSkylightMap();
            else this.subWorldObj.getDirectionalChunk(dir, x, y, z)
                .generateSkylightMap();
        }

        this.isModified = true;

        if (lightType == EnumSkyBlock.Sky) {
            if (!this.worldObj.provider.hasNoSky) {
                extendedblockstorage.setExtSkylightValue(dir, x, y & 15, z, minValue);
            }
        } else if (lightType == EnumSkyBlock.Block) {
            extendedblockstorage.setExtBlocklightValue(x, y & 15, z, minValue);
        }
    }
}
