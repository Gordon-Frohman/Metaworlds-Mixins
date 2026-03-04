package su.sergiusonesimus.metaworlds.zmixin.mixins.littletiles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.creativemd.littletiles.common.items.ItemColorTube;
import com.creativemd.littletiles.common.items.ItemLittleSaw;
import com.creativemd.littletiles.common.items.ItemRubberMallet;

import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.IMixinEntity;

@Mixin(value = { ItemColorTube.class, ItemLittleSaw.class, ItemRubberMallet.class })
public class MixinItemTool {

    private World storedWorld;

    @Inject(
        method = "onItemUse(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;IIIIFFF)Z",
        remap = true,
        at = @At(value = "HEAD"))
    private void storeWorld(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ, CallbackInfoReturnable<Boolean> cir) {
        storedWorld = world;
    }

    @ModifyVariable(
        method = "onItemUse(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;IIIIFFF)Z",
        remap = true,
        at = @At(value = "HEAD"),
        argsOnly = true)
    private EntityPlayer modifyPlayer(EntityPlayer player) {
        return ((IMixinEntity) player).getProxyPlayer(storedWorld);
    }

}
