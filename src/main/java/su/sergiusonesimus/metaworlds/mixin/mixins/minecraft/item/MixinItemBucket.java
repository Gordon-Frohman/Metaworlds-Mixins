package su.sergiusonesimus.metaworlds.mixin.mixins.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.FillBucketEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import cpw.mods.fml.common.eventhandler.Event;
import su.sergiusonesimus.metaworlds.mixin.interfaces.util.IMixinMovingObjectPosition;

@Mixin(ItemBucket.class)
public abstract class MixinItemBucket {

    @Shadow(remap = true)
    private Block isFull;

    @Shadow(remap = true)
    public abstract boolean tryPlaceContainedLiquid(World worldIn, int i, int j, int k);

    @Shadow(remap = true)
    public abstract ItemStack func_150910_a(ItemStack itemStackIn, EntityPlayer player, Item waterBucket);

    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
    @Overwrite
    public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer player) {
        boolean flag = this.isFull == Blocks.air;
        MovingObjectPosition movingobjectposition = ((ItemBucket) (Object) this)
            .getMovingObjectPositionFromPlayer(worldIn, player, flag);
        if (movingobjectposition != null) worldIn = ((IMixinMovingObjectPosition) movingobjectposition).getWorld();

        if (movingobjectposition == null) {
            return itemStackIn;
        } else {
            FillBucketEvent event = new FillBucketEvent(player, itemStackIn, worldIn, movingobjectposition);
            if (MinecraftForge.EVENT_BUS.post(event)) {
                return itemStackIn;
            }

            if (event.getResult() == Event.Result.ALLOW) {
                if (player.capabilities.isCreativeMode) {
                    return itemStackIn;
                }

                if (--itemStackIn.stackSize <= 0) {
                    return event.result;
                }

                if (!player.inventory.addItemStackToInventory(event.result)) {
                    player.dropPlayerItemWithRandomChoice(event.result, false);
                }

                return itemStackIn;
            }
            if (movingobjectposition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                int i = movingobjectposition.blockX;
                int j = movingobjectposition.blockY;
                int k = movingobjectposition.blockZ;

                if (!worldIn.canMineBlock(player, i, j, k)) {
                    return itemStackIn;
                }

                if (flag) {
                    if (!player.canPlayerEdit(i, j, k, movingobjectposition.sideHit, itemStackIn)) {
                        return itemStackIn;
                    }

                    Material material = worldIn.getBlock(i, j, k)
                        .getMaterial();
                    int l = worldIn.getBlockMetadata(i, j, k);

                    if (material == Material.water && l == 0) {
                        worldIn.setBlockToAir(i, j, k);
                        return this.func_150910_a(itemStackIn, player, Items.water_bucket);
                    }

                    if (material == Material.lava && l == 0) {
                        worldIn.setBlockToAir(i, j, k);
                        return this.func_150910_a(itemStackIn, player, Items.lava_bucket);
                    }
                } else {
                    if (this.isFull == Blocks.air) {
                        return new ItemStack(Items.bucket);
                    }

                    if (movingobjectposition.sideHit == 0) {
                        --j;
                    }

                    if (movingobjectposition.sideHit == 1) {
                        ++j;
                    }

                    if (movingobjectposition.sideHit == 2) {
                        --k;
                    }

                    if (movingobjectposition.sideHit == 3) {
                        ++k;
                    }

                    if (movingobjectposition.sideHit == 4) {
                        --i;
                    }

                    if (movingobjectposition.sideHit == 5) {
                        ++i;
                    }

                    if (!player.canPlayerEdit(i, j, k, movingobjectposition.sideHit, itemStackIn)) {
                        return itemStackIn;
                    }

                    if (this.tryPlaceContainedLiquid(worldIn, i, j, k) && !player.capabilities.isCreativeMode) {
                        return new ItemStack(Items.bucket);
                    }
                }
            }

            return itemStackIn;
        }
    }

}
