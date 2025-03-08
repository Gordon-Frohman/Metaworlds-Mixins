package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.server.management;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import su.sergiusonesimus.metaworlds.compat.CompatUtil;

@Mixin(ItemInWorldManager.class)
public abstract class MixinItemInWorldManager {

    @Shadow(remap = true)
    public EntityPlayerMP thisPlayerMP;

    @Shadow(remap = true)
    public World theWorld;

    // TODO

    @Shadow(remap = true)
    public abstract boolean isCreative();

    @Redirect(
        method = "onBlockClicked",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/EntityPlayer;isCurrentToolAdventureModeExempt(III)Z"))
    public boolean isCurrentToolAdventureModeExempt(EntityPlayer player, int p_82246_1_, int p_82246_2_,
        int p_82246_3_) {
        return CompatUtil.isCurrentToolAdventureModeExempt(
            player,
            p_82246_1_,
            p_82246_2_,
            p_82246_3_,
            ((ItemInWorldManager) (Object) this).theWorld);
    }

    public void setPlayerEntity(EntityPlayerMP newPlayer) {
        this.thisPlayerMP = newPlayer;
    }

}
