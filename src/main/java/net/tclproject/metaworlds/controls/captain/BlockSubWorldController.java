package net.tclproject.metaworlds.controls.captain;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.tclproject.metaworlds.api.IMixinEntity;
import net.tclproject.metaworlds.api.IMixinWorld;
import net.tclproject.metaworlds.patcher.EntityPlayerProxy;

public class BlockSubWorldController extends Block {

    public static boolean toMakeFalse = false;

    public BlockSubWorldController() {
        super(Material.ground);
        this.setCreativeTab(CreativeTabs.tabBlock);
    }

    public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer,
        int par6, float par7, float par8, float par9) {
        if (par5EntityPlayer.ridingEntity instanceof EntitySubWorldController) {
            par5EntityPlayer.dismountEntity(par5EntityPlayer.ridingEntity);
            par5EntityPlayer.ridingEntity.setDead();
            par5EntityPlayer.ridingEntity = null;
            Minecraft.getMinecraft().gameSettings.keyBindSneak
                .setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindSneak.getKeyCode(), true);
            toMakeFalse = true;
            return true;
        }
        if (par1World.isRemote) {
            return true;
        } else {
            // if (par1World instanceof SubWorld) {
            if (par5EntityPlayer instanceof EntityPlayerProxy) {
                par5EntityPlayer = ((EntityPlayerProxy) par5EntityPlayer).getRealPlayer();
            }

            World entityParentWorld = par5EntityPlayer.worldObj;
            // double posX = (((SubWorld)par1World).getMaximumCloseWorldBB().maxX +
            // ((SubWorld)par1World).getMaximumCloseWorldBB().minX) / 2;
            // double posZ = (((SubWorld)par1World).getMaximumCloseWorldBB().maxZ +
            // ((SubWorld)par1World).getMaximumCloseWorldBB().minZ) / 2;
            EntitySubWorldController controllerEntity = new EntitySubWorldController(
                entityParentWorld,
                par1World,
                par5EntityPlayer.posX,
                par5EntityPlayer.posY + 0.6D,
                par5EntityPlayer.posZ);
            controllerEntity.setStartingYaw((float) ((IMixinWorld)par1World).getRotationYaw() + par5EntityPlayer.rotationYaw);
            controllerEntity.setControlledWorld(par1World);
            ((IMixinEntity)controllerEntity).setWorldBelowFeet(((IMixinEntity)par5EntityPlayer).getWorldBelowFeet());
            if (!entityParentWorld.isRemote) {
                entityParentWorld.spawnEntityInWorld(controllerEntity);
            }

            controllerEntity.interactFirst(par5EntityPlayer);
            // }
            return true;
        }
    }
}
