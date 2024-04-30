package su.sergiusonesimus.metaworlds.compat;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/** To make combat better and easier. */
public class CompatUtil {

    public static boolean isCurrentToolAdventureModeExempt(EntityPlayer p, int x, int y, int z, World world) {
        if (p.capabilities.allowEdit) {
            return true;
        } else {
            // we changed this line from p.worldObj.getBlock(x,y,z) in the original to allow for metaworlds, as far as I
            // can understand
            Block block = world.getBlock(x, y, z);

            if (block.getMaterial() != Material.air) {
                if (block.getMaterial()
                    .isAdventureModeExempt()) {
                    return true;
                }

                if (p.getCurrentEquippedItem() != null) {
                    ItemStack itemstack = p.getCurrentEquippedItem();

                    if (itemstack.func_150998_b(block) || itemstack.func_150997_a(block) > 1.0F) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    // Since this does not exist anymore
    public static void cleanPool() {}
}
