package su.sergiusonesimus.metaworlds.mixin.mixins.minecraft.server.management;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import su.sergiusonesimus.metaworlds.compat.CompatUtil;

@Mixin(ItemInWorldManager.class)
public abstract class MixinItemInWorldManager {
	
	@Shadow(remap = true)
	public EntityPlayerMP thisPlayerMP;

	@Shadow(remap = true)
    public World theWorld;
	
	//TODO

	@Shadow(remap = true)
	public abstract boolean isCreative();
	
	@Redirect(method = "onBlockClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;isCurrentToolAdventureModeExempt(III)Z"))
	public boolean isCurrentToolAdventureModeExempt(EntityPlayer player, int p_82246_1_, int p_82246_2_, int p_82246_3_) {
		return CompatUtil.isCurrentToolAdventureModeExempt(player, p_82246_1_, p_82246_2_, p_82246_3_, ((ItemInWorldManager)(Object)this).theWorld);
	}
	
	public void setPlayerEntity(EntityPlayerMP newPlayer)
	{
	    this.thisPlayerMP = newPlayer;
	}

}
