package net.tclproject.metaworlds.creators.reintegrator;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class BlockDummyReobfTracker extends Block {

    public static String canBlockStayMethodName = null;
    public static String onNeighborBlockChange = null;

    public BlockDummyReobfTracker() {
        super(Material.air);
    }

    public void initialize() {
        this.canBlockStay((World) null, 0, 0, 0);
        this.onNeighborBlockChange((World) null, 0, 0, 0, Blocks.air);
    }

    public boolean canBlockStay(World par1World, int par2, int par3, int par4) {
        canBlockStayMethodName = Thread.currentThread()
            .getStackTrace()[1].getMethodName();
        return true;
    }

    public void onNeighborBlockChange(World par1World, int par2, int par3, int par4, Block par5) {
        onNeighborBlockChange = Thread.currentThread()
            .getStackTrace()[1].getMethodName();
    }
}
