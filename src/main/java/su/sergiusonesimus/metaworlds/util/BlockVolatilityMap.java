package su.sergiusonesimus.metaworlds.util;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.common.registry.FMLControlledNamespacedRegistry;
import su.sergiusonesimus.metaworlds.MetaworldsMod;
import su.sergiusonesimus.metaworlds.block.BlockDummyReobfTracker;
import su.sergiusonesimus.metaworlds.integrations.ForgeMultipartIntegration;

public class BlockVolatilityMap {

    private static Map<Integer, Boolean> blockVolatilityMap = new TreeMap<Integer, Boolean>();

    @SuppressWarnings("unchecked")
    public static void init() {
        Iterator<Block> iterator = ((FMLControlledNamespacedRegistry<Block>) Block.blockRegistry).typeSafeIterable()
            .iterator();
        while (iterator.hasNext()) checkBlockVolatility(iterator.next());
    }

    public static boolean checkBlockVolatility(Block block) {
        int blockId = Block.getIdFromBlock(block);
        Boolean isVolatile = (Boolean) blockVolatilityMap.get(Integer.valueOf(blockId));
        if (isVolatile == null) {
            if (MetaworldsMod.isForgeMultipartLoaded && ForgeMultipartIntegration.isBlockMultipart(block)) {
                isVolatile = true;
            } else {
                try {
                    if (block.getClass()
                        .getMethod(
                            BlockDummyReobfTracker.canBlockStayMethodName,
                            new Class[] { World.class, Integer.TYPE, Integer.TYPE, Integer.TYPE })
                        .getDeclaringClass()
                        .equals(Block.class)
                        && block.getClass()
                            .getMethod(
                                BlockDummyReobfTracker.onNeighborBlockChange,
                                new Class[] { World.class, Integer.TYPE, Integer.TYPE, Integer.TYPE, Block.class })
                            .getDeclaringClass()
                            .equals(Block.class)) {
                        isVolatile = false;
                    } else {
                        isVolatile = true;
                    }
                } catch (Exception e) {

                }
            }

            blockVolatilityMap.put(Integer.valueOf(blockId), isVolatile);
        }
        return isVolatile;
    }

    public static boolean isBlockSolid(Block block, IBlockAccess world, int x, int y, int z) {
        for (ForgeDirection dir : ForgeDirection.values()) {
            if (block.isSideSolid(world, x, y, z, dir)) return true;
        }
        return false;
    }

}
