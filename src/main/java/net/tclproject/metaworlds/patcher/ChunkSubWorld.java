package net.tclproject.metaworlds.patcher;

import net.minecraft.block.Block;
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
    public byte[] collisionLimitsMapYPos;
    public byte[] collisionLimitsMapYNeg;
    public byte[] collisionLimitsMapZPos;
    public byte[] collisionLimitsMapZNeg;
    public boolean isEmpty = true;

    public ChunkSubWorld(World par1World, int par2, int par3) {
        super(par1World, par2, par3);
    }

    public ChunkSubWorld(World par1World, Block[] par2blocks, int par3, int par4) {
        super(par1World, par2blocks, par3, par4);
    }

    public ChunkSubWorld(World world, Block[] par2blocks, byte[] metadata, int chunkX, int chunkZ) {
        super(world, par2blocks, metadata, chunkX, chunkZ);
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
}
