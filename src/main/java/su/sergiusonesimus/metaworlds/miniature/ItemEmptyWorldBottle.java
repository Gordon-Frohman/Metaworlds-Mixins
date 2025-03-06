package su.sergiusonesimus.metaworlds.miniature;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.mixin.interfaces.minecraft.world.IMixinWorld;

public class ItemEmptyWorldBottle extends Item {

    public ItemEmptyWorldBottle() {
        this.setMaxStackSize(64);
        this.setCreativeTab(CreativeTabs.tabTransport);
    }

    public boolean onItemUse(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, int par4,
        int par5, int par6, int par7, float par8, float par9, float par10) {
        if (par2EntityPlayer.worldObj.isRemote) {
            return true;
        } else if (par3World != null && ((IMixinWorld) par3World).isSubWorld()) {
            ItemStack newBottledWorld = new ItemStack(MetaworldsMiniatureMod.bottledWorldItem);
            newBottledWorld.setTagCompound(new NBTTagCompound());
            newBottledWorld.getTagCompound()
                .setInteger("subWorldID", ((IMixinWorld) par3World).getSubWorldID());
            if (!par2EntityPlayer.inventory.addItemStackToInventory(newBottledWorld)) {
                return true;
            } else {
                par2EntityPlayer.inventoryContainer.detectAndSendChanges();
                ((SubWorld) par3World).removeSubWorld();
                --par1ItemStack.stackSize;
                if (par1ItemStack.stackSize <= 0) {
                    par2EntityPlayer.inventory
                        .setInventorySlotContents(par2EntityPlayer.inventory.currentItem, (ItemStack) null);
                }

                return true;
            }
        } else {
            return true;
        }
    }
}
