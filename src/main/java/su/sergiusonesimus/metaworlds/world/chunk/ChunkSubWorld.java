package su.sergiusonesimus.metaworlds.world.chunk;

import java.util.Arrays;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;

public class ChunkSubWorld extends Chunk {

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
    public byte[] collisionLimitsMapXPos;
    public byte[] collisionLimitsMapXNeg;
    public int[] collisionLimitsMapYPos = new int[256];
    public byte[] collisionLimitsMapYNeg;
    public byte[] collisionLimitsMapZPos;
    public byte[] collisionLimitsMapZNeg;
    public boolean isEmpty = true;

    public ChunkSubWorld(Chunk chunk) {
        super(chunk.worldObj, chunk.xPosition, chunk.zPosition);

        storageArrays = chunk.storageArrays;
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
    }

    public ChunkSubWorld(World par1World, Block[] par2blocks, int par3, int par4) {
        super(par1World, par2blocks, par3, par4);

        Arrays.fill(this.collisionLimitsMapYPos, -999);
    }

    public ChunkSubWorld(World world, Block[] par2blocks, byte[] metadata, int chunkX, int chunkZ) {
        super(world, par2blocks, metadata, chunkX, chunkZ);

        Arrays.fill(this.collisionLimitsMapYPos, -999);
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
     * Gets the height of the topmost block that isn't air
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
     * Sets the height of the topmost block that isn't air
     */
    public void setCollisionLimitYPos(int x, int z, int height) {
        this.collisionLimitsMapYPos[x | z << 4] = height;
    }
}
