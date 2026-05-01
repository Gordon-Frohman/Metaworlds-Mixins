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
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class BlockVolatilityMap {

    private static final Map<Integer, Boolean> blockVolatilityMap = new TreeMap<>();

    // Method descriptors for the two Block methods we inspect
    private static final String CAN_BLOCK_STAY_DESCRIPTOR =
            "(Lnet/minecraft/world/World;III)Z";
    private static final String ON_NEIGHBOR_BLOCK_CHANGE_DESCRIPTOR =
            "(Lnet/minecraft/world/World;IIILnet/minecraft/block/Block;)V";

    @SuppressWarnings("unchecked")
    public static void init() {
        Iterator<Block> iterator = ((FMLControlledNamespacedRegistry<Block>) Block.blockRegistry).typeSafeIterable()
                .iterator();
        while (iterator.hasNext()) checkBlockVolatility(iterator.next());
    }

    public static boolean checkBlockVolatility(Block block) {
        int blockId = Block.getIdFromBlock(block);
        Boolean isVolatile = blockVolatilityMap.get(blockId);
        if (isVolatile != null) {
            return isVolatile;
        }

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
        isVolatile = overridesCanBlockStay || overridesOnNeighborBlockChange;

        blockVolatilityMap.put(blockId, isVolatile);
        return isVolatile;
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
}