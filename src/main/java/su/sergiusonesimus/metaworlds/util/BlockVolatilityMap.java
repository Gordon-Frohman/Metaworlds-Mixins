package su.sergiusonesimus.metaworlds.util;

import cpw.mods.fml.common.registry.FMLControlledNamespacedRegistry;
import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import su.sergiusonesimus.metaworlds.MetaworldsMod;
import su.sergiusonesimus.metaworlds.block.BlockDummyReobfTracker;
import su.sergiusonesimus.metaworlds.integrations.ForgeMultipartIntegration;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;

public class BlockVolatilityMap {

    /**
     * 4096 is default Forge max id for blocks. If any id-extending mod is used this case is covered by
     * calling {@link #ensureCapacity(int)} in {@link #checkBlockVolatility(Block)}
     * In future we can switch from storing 8 blocks per 1 byte in an underlying array for 8x compression,
     * but now memory difference is negligible (64Kb vs 8Kb)
     */
    private static boolean[] volatileBlocksFlags = new boolean[4096];
    private static boolean[] initializedBlocks = new boolean[4096];

    // Method descriptors for the two Block methods we inspect
    private static final String CAN_BLOCK_STAY_DESCRIPTOR =
            "(Lnet/minecraft/world/World;III)Z";
    private static final String ON_NEIGHBOR_BLOCK_CHANGE_DESCRIPTOR =
            "(Lnet/minecraft/world/World;IIILnet/minecraft/block/Block;)V";

    @SuppressWarnings("unchecked")
    public static void init() {
        Iterator<Block> iterator = ((FMLControlledNamespacedRegistry<Block>) Block.blockRegistry).typeSafeIterable()
                .iterator();
        int maxId = 0;
        while (iterator.hasNext()) {
            Block block = iterator.next();
            int id = Block.getIdFromBlock(block);
            if (id > maxId) maxId = id;
        }
        volatileBlocksFlags = new boolean[maxId + 1];
        initializedBlocks = new boolean[maxId + 1];

        iterator = ((FMLControlledNamespacedRegistry<Block>) Block.blockRegistry).typeSafeIterable().iterator();
        while (iterator.hasNext()) checkBlockVolatility(iterator.next());
    }

    public static boolean checkBlockVolatility(Block block) {
        int blockId = Block.getIdFromBlock(block);
        if (!initializedBlocks[blockId]) {
            if (MetaworldsMod.isForgeMultipartLoaded && ForgeMultipartIntegration.isBlockMultipart(block)) {
                return true;
            }

            boolean overridesCanBlockStay = overridesInSubclass(
                    block.getClass(),
                    BlockDummyReobfTracker.canBlockStayMethodName,
                    CAN_BLOCK_STAY_DESCRIPTOR);
            boolean overridesOnNeighborBlockChange = overridesInSubclass(
                    block.getClass(),
                    BlockDummyReobfTracker.onNeighborBlockChange,
                    ON_NEIGHBOR_BLOCK_CHANGE_DESCRIPTOR);
            boolean isVolatile = overridesCanBlockStay || overridesOnNeighborBlockChange;


            ensureCapacity(blockId);
            volatileBlocksFlags[blockId] = isVolatile;
            initializedBlocks[blockId] = true;
        }
        return volatileBlocksFlags[blockId];
    }

    private static boolean overridesInSubclass(Class<?> cls, String methodName, String methodDescriptor) {
        Class<?> cur = cls;
        while (cur != null && cur != Block.class && cur != Object.class) {
            if (declaresMethod(cur, methodName, methodDescriptor)) return true;
            cur = cur.getSuperclass();
        }
        return false;
    }

    private static boolean declaresMethod(Class<?> cls, final String methodName, final String methodDescriptor) {
        ClassLoader loader = cls.getClassLoader();
        if (loader == null) return false;
        String resourceName = cls.getName().replace('.', '/') + ".class";

        try (InputStream in = loader.getResourceAsStream(resourceName);) {
            try {
                if (in == null) return false;
                ClassReader reader = new ClassReader(in);
                final boolean[] found = new boolean[]{false};
                // Currently using ASM5 since almost all 1.7.10 mods targeting Java 8. If higher versions are used,
                // we'll need to bump to ASM9, but at first increase project target version
                // Project's Gradle plugin (gtnhconvention) also targets 1.8 by default
                // https://github.com/GTNewHorizons/GTNHGradle/blob/master/src/main/java/com/gtnewhorizons/gtnhgradle/modules/IdeIntegrationModule.java#L61
                reader.accept(new ClassVisitor(Opcodes.ASM5) {
                    @Override
                    public MethodVisitor visitMethod(int access, String name, String desc,
                                                     String signature, String[] exceptions) {
                        if (!found[0] && methodName.equals(name) && methodDescriptor.equals(desc)) {
                            found[0] = true;
                        }
                        return null;
                    }
                }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                return found[0];
            } catch (Exception e) {
                return false;
            }
        } catch (Exception ignored) {
            return false;
        }
    }

    public static boolean isBlockSolid(Block block, IBlockAccess world, int x, int y, int z) {
        for (ForgeDirection dir : ForgeDirection.values()) {
            if (block.isSideSolid(world, x, y, z, dir)) return true;
        }
        return false;
    }

    private static void ensureCapacity(int blockId) {
        if (blockId >= volatileBlocksFlags.length) {
            int newSize = Math.max(blockId + 1, volatileBlocksFlags.length * 2);
            volatileBlocksFlags = Arrays.copyOf(volatileBlocksFlags, newSize);
            initializedBlocks = Arrays.copyOf(initializedBlocks, newSize);
        }
    }
}