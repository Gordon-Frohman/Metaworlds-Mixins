package net.tclproject.metaworlds.patcher;

import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;

public class ItemInWorldManagerForProxy extends ItemInWorldManager {

    private ItemInWorldManager realItemInWorldManager;

    public ItemInWorldManagerForProxy(World par1World, ItemInWorldManager realPlayerItemInWorldManager) {
        super(par1World);
        this.realItemInWorldManager = realPlayerItemInWorldManager;
    }

    public WorldSettings.GameType getGameType() {
        WorldSettings.GameType thisGameType = super.getGameType();
        if (thisGameType != this.realItemInWorldManager.getGameType()) {
            this.setGameType(this.realItemInWorldManager.getGameType());
            return super.getGameType();
        } else {
            return thisGameType;
        }
    }

    public boolean isCreative() {
        return this.getGameType()
            .isCreative();
    }

    public void onBlockClicked(int par1, int par2, int par3, int par4) {
        this.getGameType();
        super.onBlockClicked(par1, par2, par3, par4);
    }

    public boolean tryHarvestBlock(int par1, int par2, int par3) {
        this.getGameType();
        return super.tryHarvestBlock(par1, par2, par3);
    }
}
