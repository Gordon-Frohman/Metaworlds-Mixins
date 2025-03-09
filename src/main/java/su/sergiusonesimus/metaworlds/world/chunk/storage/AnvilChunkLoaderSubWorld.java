package su.sergiusonesimus.metaworlds.world.chunk.storage;

import java.io.File;
import java.util.Arrays;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;

import su.sergiusonesimus.metaworlds.world.chunk.ChunkSubWorld;

public class AnvilChunkLoaderSubWorld extends AnvilChunkLoader {

    public AnvilChunkLoaderSubWorld(File file) {
        super(file);
    }

    protected void writeChunkToNBT(Chunk chunk, World world, NBTTagCompound tagCompound) {
        super.writeChunkToNBT(chunk, world, tagCompound);

        ChunkSubWorld subworldChunk = (ChunkSubWorld) chunk;
        tagCompound.setIntArray("CollisionLimitsMapYPos", subworldChunk.collisionLimitsMapYPos);
    }

    public Chunk readChunkFromNBT(World world, NBTTagCompound tagCompound) {
        Chunk chunk = super.readChunkFromNBT(world, tagCompound);
        ChunkSubWorld subworldChunk = chunk instanceof ChunkSubWorld ? (ChunkSubWorld) chunk : new ChunkSubWorld(chunk);

        if (tagCompound.hasKey("CollisionLimitsMapYPos"))
            subworldChunk.collisionLimitsMapYPos = tagCompound.getIntArray("CollisionLimitsMapYPos");
        else {
            subworldChunk.collisionLimitsMapYPos = new int[256];
            Arrays.fill(subworldChunk.collisionLimitsMapYPos, -999);
        }
        return subworldChunk;
    }
}
