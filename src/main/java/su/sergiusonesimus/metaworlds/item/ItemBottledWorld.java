package su.sergiusonesimus.metaworlds.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.util.IMixinAxisAlignedBB;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

public class ItemBottledWorld extends Item {

    public ItemBottledWorld() {
        this.setMaxStackSize(1);
    }

    public boolean onItemUse(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, int par4,
        int par5, int par6, int par7, float par8, float par9, float par10) {
        if (par2EntityPlayer.worldObj.isRemote) {
            return true;
        } else if (!par1ItemStack.hasTagCompound()) {
            return true;
        } else {
            if (par1ItemStack.getTagCompound()
                .hasKey("subWorldID")) {
                int storedSubWorldID = par1ItemStack.getTagCompound()
                    .getInteger("subWorldID");
                World restoredWorld = ((IMixinWorld) par2EntityPlayer.worldObj).createSubWorld(storedSubWorldID);
                SubWorld restoredSubWorld = (SubWorld) restoredWorld;
                Vec3 destPos = ((IMixinWorld) par3World).transformToGlobal(
                    (double) ((float) par4 + par8),
                    (double) ((float) par5 + par9),
                    (double) ((float) par6 + par10));
                AxisAlignedBB newAABB = AxisAlignedBB.getBoundingBox(
                    (double) restoredSubWorld.getMinX(),
                    (double) restoredSubWorld.getMinY(),
                    (double) restoredSubWorld.getMinZ(),
                    (double) restoredSubWorld.getMaxX(),
                    (double) restoredSubWorld.getMaxY(),
                    (double) restoredSubWorld.getMaxZ());
                newAABB = ((IMixinAxisAlignedBB) newAABB).getTransformedToGlobalBoundingBox(restoredWorld);
                Vec3 posDiff = Vec3.createVectorHelper(
                    destPos.xCoord - (newAABB.maxX + newAABB.minX) * 0.5D,
                    destPos.yCoord - newAABB.minY,
                    destPos.zCoord - (newAABB.maxZ + newAABB.minZ) * 0.5D);
                restoredSubWorld.setTranslation(
                    restoredSubWorld.getTranslationX() + posDiff.xCoord,
                    restoredSubWorld.getTranslationY() + posDiff.yCoord,
                    restoredSubWorld.getTranslationZ() + posDiff.zCoord);
                par1ItemStack.stackSize = 0;
                par2EntityPlayer.inventory
                    .setInventorySlotContents(par2EntityPlayer.inventory.currentItem, (ItemStack) null);
            }

            return true;
        }
    }
}
