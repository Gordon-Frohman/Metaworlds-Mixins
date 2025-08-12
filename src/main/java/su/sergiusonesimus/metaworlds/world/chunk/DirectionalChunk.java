package su.sergiusonesimus.metaworlds.world.chunk;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;

import net.minecraft.block.Block;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.init.Blocks;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.ReportedException;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.metaworlds.util.Direction.Axis;
import su.sergiusonesimus.metaworlds.util.Direction.AxisDirection;
import su.sergiusonesimus.metaworlds.world.chunk.storage.ExtendedBlockStorageSubWorld;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

public class DirectionalChunk {

    /** Reference to the World object. */
    public World worldObj;
    public SubWorld subWorldObj;
    /** The x coordinate of the chunk. */
    public final int xCoord;
    /** The y coordinate of the chunk. */
    public final int yCoord;
    /** The z coordinate of the chunk. */
    public final int zCoord;

    private Direction dir;
    public int[] heightMap = new int[256];
    /** Lowest value in the heightmap. */
    public int heightMapMinimum;
    private TreeMap<Integer, ExtendedBlockStorageSubWorld> storageArrays;
    /** Which columns need their skylightMaps updated. */
    public boolean[] updateSkylightColumns = new boolean[256];
    public boolean isGapLightingUpdated;

    public DirectionalChunk(World world, int coord1, int coord2, Direction dir) {
        this.worldObj = world;
        this.subWorldObj = (SubWorld) world;
        switch (dir.getAxis()) {
            case X:
                this.xCoord = 0;
                this.yCoord = coord2;
                this.zCoord = coord1;
                break;
            default:
            case Y:
                this.xCoord = coord1;
                this.yCoord = 0;
                this.zCoord = coord2;
                break;
            case Z:
                this.xCoord = coord1;
                this.yCoord = coord2;
                this.zCoord = 0;
                break;
        }
        Arrays
            .fill(heightMap, dir.getAxisDirection() == AxisDirection.POSITIVE ? Integer.MIN_VALUE : Integer.MAX_VALUE);
        this.dir = dir;
        this.storageArrays = new TreeMap<Integer, ExtendedBlockStorageSubWorld>();
    }

    /**
     * Returns the value in the height map at these coordinates in the chunk
     */
    public Integer getHeightValue(int coord1, int coord2) {
        if (storageArrays.isEmpty()) return null;
        Integer highestBlock = this.heightMap[coord2 << 4 | coord1];
        if (highestBlock == Integer.MIN_VALUE)
            this.heightMap[coord2 << 4 | coord1] = highestBlock = storageArrays.firstKey() * 16;
        else if (highestBlock == Integer.MAX_VALUE)
            this.heightMap[coord2 << 4 | coord1] = highestBlock = storageArrays.lastKey() * 16 + 15;
        return highestBlock;
    }

    /**
     * Returns the topmost ExtendedBlockStorage instance for this Chunk that actually contains a block.
     */
    public Integer getTopFilledSegment() {
        if (storageArrays.isEmpty()) return null;
        Map.Entry<Integer, ExtendedBlockStorageSubWorld> result;
        if (isPositive()) {
            result = this.storageArrays.lastEntry();
        } else {
            result = this.storageArrays.firstEntry();
        }
        return result.getKey() * 16;
    }

    public boolean isPositive() {
        return dir.getAxisDirection() == AxisDirection.POSITIVE;
    }

    /**
     * Generates the initial skylight map for the chunk upon generation or load.
     */
    public void generateSkylightMap() {
        Integer i = this.getTopFilledSegment();
        if (i == null) return;
        this.heightMapMinimum = this.dir.getAxisDirection() == AxisDirection.POSITIVE ? Integer.MIN_VALUE
            : Integer.MAX_VALUE;

        for (int j = 0; j < 16; ++j) {
            int k = 0;

            while (k < 16) {
                // this.precipitationHeightMap[j + (k << 4)] = -999;
                int l = i;
                int d = 0;
                int min;

                if (this.isPositive()) {
                    l += 15;
                    d = -1;
                    min = storageArrays.firstKey() * 16;
                } else {
                    d = 1;
                    min = storageArrays.lastKey() * 16 + 15;
                }

                while (true) {
                    if ((d < 0 && l > min) || (d > 0 && l < min)) {
                        int nextBlock = this.getNextBlock(l);
                        if (this.func_150808_b(j, nextBlock, k) == 0) {
                            l = nextBlock;
                            continue;
                        }

                        this.heightMap[k << 4 | j] = l;

                        if ((d < 0 && l < this.heightMapMinimum) || (d > 0 && l > this.heightMapMinimum)) {
                            this.heightMapMinimum = l;
                        }
                    }

                    if (!this.worldObj.provider.hasNoSky) {
                        l = 15;
                        int i1 = i + (this.isPositive() ? 15 : 0);

                        do {
                            int j1 = this.func_150808_b(j, i1, k);

                            if (j1 == 0 && l != 15) {
                                j1 = 1;
                            }

                            l -= j1;

                            if (l > 0) {
                                ExtendedBlockStorageSubWorld extendedblockstorage = this.storageArrays.get(i1 >> 4);

                                if (extendedblockstorage != null) {
                                    int x;
                                    int y;
                                    int z;
                                    int chunkX;
                                    int chunkY;
                                    int chunkZ;
                                    switch (dir.getAxis()) {
                                        case X:
                                            x = i1 & 15;
                                            y = k;
                                            z = j;
                                            chunkX = i1;
                                            chunkY = (this.yCoord << 4) + k;
                                            chunkZ = (this.zCoord << 4) + j;
                                            break;
                                        default:
                                        case Y:
                                            x = j;
                                            y = i1 & 15;
                                            z = k;
                                            chunkX = (this.xCoord << 4) + j;
                                            chunkY = i1;
                                            chunkZ = (this.zCoord << 4) + k;
                                            break;
                                        case Z:
                                            x = j;
                                            y = k;
                                            z = i1 & 15;
                                            chunkX = (this.xCoord << 4) + j;
                                            chunkY = (this.yCoord << 4) + k;
                                            chunkZ = i1;
                                            break;
                                    }
                                    extendedblockstorage.setExtSkylightValue(this.dir, x, y, z, l);
                                    this.worldObj.func_147479_m(chunkX, chunkY, chunkZ);
                                }
                            }

                            i1 = this.getNextBlock(i1);
                        } while (((d < 0 && i1 > min) || (d > 0 && i1 < min)) && l > 0);
                    }

                    ++k;
                    break;
                }
            }
        }

        // this.isModified = true;
    }

    /**
     * Initiates the recalculation of both the block-light and sky-light for a given block inside a chunk.
     */
    public void relightBlock(int coord1, int coordVar, int coord2) {
        if (storageArrays.isEmpty()) return;
        int highestBlock = this.heightMap[coord2 << 4 | coord1];
        int currentBlock = highestBlock;
        int min;
        boolean movingDown = this.isPositive();

        if (movingDown) {
            min = storageArrays.firstKey() * 16;
            if (highestBlock == Integer.MIN_VALUE)
                this.heightMap[coord2 << 4 | coord1] = currentBlock = highestBlock = min;
            if (coordVar > highestBlock) currentBlock = coordVar;
        } else {
            min = storageArrays.lastKey() * 16 + 15;
            if (highestBlock == Integer.MAX_VALUE)
                this.heightMap[coord2 << 4 | coord1] = currentBlock = highestBlock = min;
            if (coordVar < highestBlock) currentBlock = coordVar;
        }

        int nextBlock = this.getNextBlock(currentBlock);
        while (((movingDown && currentBlock > min) || (!movingDown && currentBlock < min))
            && this.func_150808_b(coord1, nextBlock, coord2) == 0) {
            currentBlock = nextBlock;
            nextBlock = this.getNextBlock(currentBlock);
        }

        if (currentBlock != highestBlock) {
            int coord1Global = (dir.getAxis() == Axis.X ? this.zCoord : this.xCoord) * 16;
            int coord2Global = (dir.getAxis() == Axis.Y ? this.zCoord : this.yCoord) * 16;
            ((IMixinWorld) this.worldObj).markBlocksDirtyDirectional(
                dir,
                coord1 + coord1Global,
                coord2 + coord2Global,
                currentBlock,
                highestBlock);
            this.heightMap[coord2 << 4 | coord1] = currentBlock;
            int l1;
            ChunkCoordinates worldCoords;

            if (!this.worldObj.provider.hasNoSky) {
                ExtendedBlockStorageSubWorld extendedblockstorage;

                if (currentBlock < highestBlock) {
                    for (l1 = currentBlock; l1 < highestBlock; ++l1) {
                        extendedblockstorage = this.storageArrays.get(l1 >> 4);

                        if (extendedblockstorage != null) {
                            worldCoords = this.getWorldCoords(coord1, coordVar, coord2);
                            extendedblockstorage.setExtSkylightValue(this.dir, coord1, l1 & 15, coord2, 15);
                            this.worldObj.func_147479_m(worldCoords.posX, worldCoords.posY, worldCoords.posZ);
                        }
                    }
                } else {
                    for (l1 = highestBlock; l1 < currentBlock; ++l1) {
                        extendedblockstorage = this.storageArrays.get(l1 >> 4);

                        if (extendedblockstorage != null) {
                            worldCoords = this.getWorldCoords(coord1, coordVar, coord2);
                            extendedblockstorage.setExtSkylightValue(this.dir, coord1, l1 & 15, coord2, 0);
                            this.worldObj.func_147479_m(worldCoords.posX, worldCoords.posY, worldCoords.posZ);
                        }
                    }
                }

                int light = 15;
                int lightOpacity;

                while (currentBlock > 0 && light > 0) {
                    --currentBlock;
                    lightOpacity = this.func_150808_b(coord1, currentBlock, coord2);

                    if (lightOpacity == 0) {
                        lightOpacity = 1;
                    }

                    light -= lightOpacity;

                    if (light < 0) {
                        light = 0;
                    }

                    ExtendedBlockStorageSubWorld extendedblockstorage1 = this.storageArrays.get(currentBlock >> 4);

                    if (extendedblockstorage1 != null) {
                        extendedblockstorage1.setExtSkylightValue(this.dir, coord1, currentBlock & 15, coord2, light);
                    }
                }
            }

            l1 = this.heightMap[coord2 << 4 | coord1];
            int updateMin = highestBlock;
            int updateMax = l1;

            if (l1 < highestBlock) {
                updateMin = l1;
                updateMax = highestBlock;
            }

            if ((movingDown && l1 < this.heightMapMinimum) || (!movingDown && l1 > this.heightMapMinimum)) {
                this.heightMapMinimum = l1;
            }

            if (!this.worldObj.provider.hasNoSky) {
                this.updateSkylightNeighborHeight(coord1 - 1, coord2, updateMin, updateMax);
                this.updateSkylightNeighborHeight(coord1 + 1, coord2, updateMin, updateMax);
                this.updateSkylightNeighborHeight(coord1, coord2 - 1, updateMin, updateMax);
                this.updateSkylightNeighborHeight(coord1, coord2 + 1, updateMin, updateMax);
                this.updateSkylightNeighborHeight(coord1, coord2, updateMin, updateMax);
            }

            // this.isModified = true;
        }
    }

    public void setSkyLightValue(int coord1, int coordVar, int coord2, int light) {
        ExtendedBlockStorageSubWorld extendedblockstorage = this.storageArrays.get(coordVar >> 4);

        if (extendedblockstorage == null) {
            ChunkCoordinates worldCoords = this.getWorldCoords(coord1, coordVar, coord2);
            ChunkSubWorld actualChunk = (ChunkSubWorld) this.worldObj
                .getChunkFromBlockCoords(worldCoords.posX, worldCoords.posZ);
            if (actualChunk.storageArrays[worldCoords.posY >> 4] == null)
                actualChunk.storageArrays[worldCoords.posY >> 4] = new ExtendedBlockStorageSubWorld(
                    worldCoords.posY >> 4 << 4,
                    !this.worldObj.provider.hasNoSky);
            extendedblockstorage = (ExtendedBlockStorageSubWorld) actualChunk.storageArrays[worldCoords.posY >> 4];
            this.storageArrays.put(coordVar >> 4, extendedblockstorage);
            this.generateSkylightMap();
        }

        // this.isModified = true;

        if (!this.worldObj.provider.hasNoSky) {
            extendedblockstorage.setExtSkylightValue(this.dir, coord1, coordVar & 15, coord2, light);
        }
    }

    private void updateSkylightNeighborHeight(int coord1, int coord2, int coordVarMin, int coordVarMax) {
        ChunkCoordinates worldCoords = this.getWorldCoords(coord1, coordVarMin, coord2);
        if (coordVarMax > coordVarMin
            && this.worldObj.doChunksNearChunkExist(worldCoords.posX, 0, worldCoords.posZ, 16)) {
            int x = worldCoords.posX;
            int y = worldCoords.posY;
            int z = worldCoords.posZ;
            for (int i1 = coordVarMin; i1 < coordVarMax; ++i1) {
                switch (dir.getAxis()) {
                    case X:
                        x = i1;
                        break;
                    case Y:
                        y = i1;
                        break;
                    case Z:
                        z = i1;
                        break;
                }
                this.worldObj.updateLightByType(EnumSkyBlock.Sky, x, y, z);
            }

            // this.isModified = true;
        }
    }

    public int getNextBlock(int currentBlock) {
        int borderNumber;
        int d = 0;
        Set<Entry<Integer, ExtendedBlockStorageSubWorld>> setToParse;
        if (this.isPositive()) {
            borderNumber = 0;
            d = -1;
            setToParse = this.storageArrays.descendingMap()
                .entrySet();
        } else {
            borderNumber = 15;
            d = 1;
            setToParse = this.storageArrays.entrySet();
        }
        if (currentBlock % 16 == borderNumber) {
            int storageID = Math.floorDiv(currentBlock, 16);
            Iterator<Entry<Integer, ExtendedBlockStorageSubWorld>> i$ = setToParse.iterator();
            while (i$.hasNext()) {
                Entry<Integer, ExtendedBlockStorageSubWorld> entry = i$.next();
                if (entry.getKey() == storageID) {
                    if (i$.hasNext()) return i$.next()
                        .getKey() * 16 + (15 - borderNumber);
                    else return (storageID + d) * 16 + (15 - borderNumber);
                }
            }
        }
        return currentBlock + d;
    }

    public int func_150808_b(int coord1, int coordVar, int coord2) {
        int x;
        int y;
        int z;
        switch (dir.getAxis()) {
            case X:
                x = coordVar;
                y = (this.yCoord << 4) + coord2;
                z = (this.zCoord << 4) + coord1;
                break;
            default:
            case Y:
                x = (this.xCoord << 4) + coord1;
                y = coordVar;
                z = (this.zCoord << 4) + coord2;
                break;
            case Z:
                x = (this.xCoord << 4) + coord1;
                y = (this.yCoord << 4) + coord2;
                z = coordVar;
                break;
        }
        return this.getBlock(coord1, coordVar, coord2)
            .getLightOpacity(worldObj, x, y, z);
    }

    public ChunkCoordinates getEBSCoords(int coord1, int coordVar, int coord2) {
        int x;
        int y;
        int z;
        switch (dir.getAxis()) {
            case X:
                x = coordVar & 15;
                y = coord2;
                z = coord1;
                break;
            default:
            case Y:
                x = coord1;
                y = coordVar & 15;
                z = coord2;
                break;
            case Z:
                x = coord1;
                y = coord2;
                z = coordVar & 15;
                break;
        }
        return new ChunkCoordinates(x, y, z);
    }

    public ChunkCoordinates getWorldCoords(int coord1, int coordVar, int coord2) {
        int x = this.xCoord * 16;
        int y = this.yCoord * 16;
        int z = this.zCoord * 16;
        switch (dir.getAxis()) {
            case X:
                x += coordVar;
                y += coord2;
                z += coord1;
                break;
            default:
            case Y:
                x += coord1;
                y += coordVar;
                z += coord2;
                break;
            case Z:
                x += coord1;
                y += coord2;
                z += coordVar;
                break;
        }
        return new ChunkCoordinates(x, y, z);
    }

    public ChunkCoordinates getChunkCoords(int x, int y, int z) {
        int coord1;
        int coord2;
        int coordVar;
        switch (dir.getAxis()) {
            case X:
                coord1 = z & 15;
                coord2 = y & 15;
                coordVar = x;
                break;
            default:
            case Y:
                coord1 = x & 15;
                coord2 = z & 15;
                coordVar = y;
                break;
            case Z:
                coord1 = x & 15;
                coord2 = y & 15;
                coordVar = z;
                break;
        }
        return new ChunkCoordinates(coord1, coordVar, coord2);
    }

    public Block getBlock(final int coord1, final int coordVar, final int coord2) {
        Block block = Blocks.air;

        ExtendedBlockStorage extendedblockstorage = this.storageArrays.get(coordVar >> 4);

        if (extendedblockstorage != null) {
            try {
                ChunkCoordinates ebsCoords = this.getEBSCoords(coord1, coordVar, coord2);
                block = extendedblockstorage.getBlockByExtId(ebsCoords.posX, ebsCoords.posY, ebsCoords.posZ);
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Getting block");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being got");
                crashreportcategory.addCrashSectionCallable("Location", new Callable() {

                    public String call() {
                        return CrashReportCategory.getLocationInfo(coord1, coordVar, coord2);
                    }
                });
                throw new ReportedException(crashreport);
            }
        }

        return block;
    }

    public boolean canBlockSeeTheSky(int x, int y, int z) {
        ChunkCoordinates chunkCoords = this.getChunkCoords(x, y, z);
        int highestBlock = this.heightMap[chunkCoords.posZ << 4 | chunkCoords.posX];
        AxisDirection direction = this.dir.getAxisDirection();
        return (direction == AxisDirection.POSITIVE && chunkCoords.posY >= highestBlock)
            || (direction == AxisDirection.NEGATIVE && chunkCoords.posY <= highestBlock);
    }

    public void registerExtendedBlockStorage(ExtendedBlockStorageSubWorld ebs, int index) {
        this.storageArrays.put(index, ebs);
    }

    public void unregisterExtendedBlockStorage(int index) {
        this.storageArrays.remove(index);
    }

    /**
     * Propagates a given sky-visible block's light value downward and upward to neighboring blocks as necessary.
     */
    public void propagateSkylightOcclusion(int coord1, int coord2) {
        this.updateSkylightColumns[coord1 + coord2 * 16] = true;
        this.isGapLightingUpdated = true;
    }

    private void recheckGaps(boolean isClient) {
        this.worldObj.theProfiler.startSection("recheckGaps" + this.dir.toString());

        // if (this.worldObj.doChunksNearChunkExist(this.xCoord * 16 + 8, this.yCoord + 8, this.zCoord * 16 + 8, 16))
        // {
        for (int i = 0; i < 16; ++i) {
            for (int j = 0; j < 16; ++j) {
                if (this.updateSkylightColumns[i + j * 16]) {
                    this.updateSkylightColumns[i + j * 16] = false;
                    Integer highestBlock = this.getHeightValue(i, j);
                    if (highestBlock == null) continue;
                    if (highestBlock == Integer.MIN_VALUE) highestBlock = storageArrays.firstKey() * 16;
                    else if (highestBlock == Integer.MAX_VALUE) highestBlock = storageArrays.lastKey() * 16 + 15;
                    ChunkCoordinates neighbourCoords1 = this.getWorldCoords(i - 1, 0, j);
                    ChunkCoordinates neighbourCoords2 = this.getWorldCoords(i + 1, 0, j);
                    ChunkCoordinates neighbourCoords3 = this.getWorldCoords(i, 0, j - 1);
                    ChunkCoordinates neighbourCoords4 = this.getWorldCoords(i, 0, j + 1);
                    int j1 = this.subWorldObj.getDirectionalChunk(
                        dir,
                        neighbourCoords1.posX,
                        neighbourCoords1.posY,
                        neighbourCoords1.posZ).heightMapMinimum;
                    int k1 = this.subWorldObj.getDirectionalChunk(
                        dir,
                        neighbourCoords2.posX,
                        neighbourCoords2.posY,
                        neighbourCoords2.posZ).heightMapMinimum;
                    int l1 = this.subWorldObj.getDirectionalChunk(
                        dir,
                        neighbourCoords3.posX,
                        neighbourCoords3.posY,
                        neighbourCoords3.posZ).heightMapMinimum;
                    int i2 = this.subWorldObj.getDirectionalChunk(
                        dir,
                        neighbourCoords4.posX,
                        neighbourCoords4.posY,
                        neighbourCoords4.posZ).heightMapMinimum;

                    if (k1 < j1) {
                        j1 = k1;
                    }

                    if (l1 < j1) {
                        j1 = l1;
                    }

                    if (i2 < j1) {
                        j1 = i2;
                    }

                    this.checkSkylightNeighborHeight(i, j, j1);
                    this.checkSkylightNeighborHeight(i - 1, j, highestBlock);
                    this.checkSkylightNeighborHeight(i + 1, j, highestBlock);
                    this.checkSkylightNeighborHeight(i, j - 1, highestBlock);
                    this.checkSkylightNeighborHeight(i, j + 1, highestBlock);

                    if (isClient) {
                        this.worldObj.theProfiler.endSection();
                        return;
                    }
                }
            }
        }

        this.isGapLightingUpdated = false;
        // }

        this.worldObj.theProfiler.endSection();
    }

    /**
     * Checks the height of a block next to a sky-visible block and schedules a lighting update as necessary.
     */
    private void checkSkylightNeighborHeight(int coord1, int coord2, int coordVar) {
        if (storageArrays.isEmpty()) return;
        ChunkCoordinates worldCoords = this.getWorldCoords(coord1, coordVar, coord2);
        int localCoord1 = (coord1 + 16) % 16;
        int localCoord2 = (coord2 + 16) % 16;
        DirectionalChunk localChunk = this.subWorldObj
            .getDirectionalChunk(dir, worldCoords.posX, worldCoords.posY, worldCoords.posZ);
        Integer highestBlock = localChunk.getHeightValue(localCoord1, localCoord2);
        if (highestBlock == null) return;
        if (highestBlock == Integer.MIN_VALUE) highestBlock = storageArrays.firstKey() * 16;
        else if (highestBlock == Integer.MAX_VALUE) highestBlock = storageArrays.lastKey() * 16 + 15;
        if (coordVar == Integer.MIN_VALUE) coordVar = storageArrays.firstKey() * 16;
        else if (coordVar == Integer.MAX_VALUE) coordVar = storageArrays.lastKey() * 16 + 15;

        if (highestBlock > coordVar) {
            localChunk.updateSkylightNeighborHeight(localCoord1, localCoord2, coordVar, highestBlock + 1);
        } else if (highestBlock < coordVar) {
            localChunk.updateSkylightNeighborHeight(localCoord1, localCoord2, highestBlock, coordVar + 1);
        }
    }

    public void func_150804_b(boolean shouldRecheckGaps) {
        if (this.isGapLightingUpdated && !this.worldObj.provider.hasNoSky && !shouldRecheckGaps) {
            this.recheckGaps(this.worldObj.isRemote);
        }

        // this.field_150815_m = true;
        //
        // if (!this.isLightPopulated && this.isTerrainPopulated)
        // {
        // this.func_150809_p();
        // }
    }

    public int getSegmentsCount() {
        return this.storageArrays.size();
    }

}
