package net.tclproject.metaworlds.mixins.client.multiplayer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.world.World;

public interface IMixinPlayerControllerMP {

    /**
     * Block dig operation in creative mode (instantly digs the block).
     */
    public default void clickBlockCreative(Minecraft par0Minecraft, PlayerControllerMP par1PlayerControllerMP, int par2,
        int par3, int par4, int par5, World par6World) {
        if (!par6World.extinguishFire(par0Minecraft.thePlayer, par2, par3, par4, par5)) {
            ((IMixinPlayerControllerMP)par1PlayerControllerMP).onPlayerDestroyBlock(par2, par3, par4, par5, par6World);
        }
    }
    
    public boolean onPlayerDestroyBlock(int par1, int par2, int par3, int par4, World par5World);
    
    public void clickBlock(int par1, int par2, int par3, int par4, World par5World);
    
    public void onPlayerDamageBlock(int par1, int par2, int par3, int par4, World par5World);

}
