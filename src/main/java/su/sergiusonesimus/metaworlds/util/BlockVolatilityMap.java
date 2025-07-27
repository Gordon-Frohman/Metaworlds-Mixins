package su.sergiusonesimus.metaworlds.util;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.common.registry.FMLControlledNamespacedRegistry;
import su.sergiusonesimus.metaworlds.block.BlockDummyReobfTracker;

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
                    isVolatile = Boolean.valueOf(false);
                } else {
                    isVolatile = Boolean.valueOf(true);
                }
            } catch (SecurityException var15) {
                ;
            } catch (NoSuchMethodException var16) {
                ;
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
