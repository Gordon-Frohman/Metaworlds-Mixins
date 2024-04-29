package net.tclproject.metaworlds.core;

import java.io.File;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.tclproject.metaworlds.patcher.ChunkSubWorld;

public class AnvilChunkLoaderSubWorld extends AnvilChunkLoader {

    public AnvilChunkLoaderSubWorld(File par1File) {
        super(par1File);
    }

    protected void writeChunkToNBT(Chunk par1Chunk, World par2World, NBTTagCompound par3NBTTagCompound) {
        super.writeChunkToNBT(par1Chunk, par2World, par3NBTTagCompound);
    }

    public Chunk readChunkFromNBT(World par1World, NBTTagCompound par2NBTTagCompound) {
        /*ChunkSubWorld subWorldChunk = null;

        try {
            subWorldChunk = (ChunkSubWorld) super.readChunkFromNBT(par1World, par2NBTTagCompound);
        } catch (Exception var5) {
            var5.printStackTrace();
        }
        
        return subWorldChunk;
        */
        
        Chunk chunk = super.readChunkFromNBT(par1World, par2NBTTagCompound);
        if (chunk instanceof ChunkSubWorld)
        	return (ChunkSubWorld) chunk;
        else
        	return chunk;
    }
}
