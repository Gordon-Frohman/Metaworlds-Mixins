package su.sergiusonesimus.metaworlds.zmixin.mixins.littletiles;

import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.creativemd.creativecore.common.utils.HashMapList;
import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTileBlockPos;
import com.creativemd.littletiles.common.utils.PlacementHelper;
import com.creativemd.littletiles.utils.PreviewTile;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import su.sergiusonesimus.metaworlds.zmixin.interfaces.littletiles.IMixinLittleTileBlockPos;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.IMixinEntity;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.util.IMixinMovingObjectPosition;

@Mixin(ItemBlockTiles.class)
public class MixinItemBlockTiles {

    private static LittleTileBlockPos storedPos;

    @Inject(method = "placeBlockAt", remap = false, at = @At(value = "HEAD"))
    private void placeBlockAt(EntityPlayer player, ItemStack stack, World world, LittleTileBlockPos pos,
        PlacementHelper helper, boolean customPlacement, CallbackInfoReturnable<Boolean> cir) {
        storedPos = pos;
    }

    @WrapOperation(
        method = "placeTiles",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lcom/creativemd/littletiles/common/items/ItemBlockTiles;canPlaceTiles(Lnet/minecraft/world/World;Lcom/creativemd/creativecore/common/utils/HashMapList;Ljava/util/ArrayList;)Z",
            remap = false))
    private static boolean canPlaceTiles(World world, HashMapList<ChunkCoordinates, PreviewTile> splitted,
        ArrayList<ChunkCoordinates> coordsToCheck, Operation<Boolean> original) {
        return original.call(((IMixinLittleTileBlockPos) storedPos).getWorld(), splitted, coordsToCheck);
    }

    @WrapOperation(
        method = "placeBlockAt",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lcom/creativemd/littletiles/common/items/ItemBlockTiles;placeTiles(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;Ljava/util/ArrayList;Lcom/creativemd/littletiles/common/structure/LittleStructure;IIILnet/minecraft/item/ItemStack;Ljava/util/ArrayList;)Z"))
    private boolean placeTiles(World world, EntityPlayer player, ArrayList<PreviewTile> previews,
        LittleStructure structure, int x, int y, int z, ItemStack stack, ArrayList<LittleTile> unplaceableTiles,
        Operation<Boolean> original) {
        return original.call(
            ((IMixinLittleTileBlockPos) storedPos).getWorld(),
            player,
            previews,
            structure,
            x,
            y,
            z,
            stack,
            unplaceableTiles);
    }

    @WrapOperation(
        method = { "onItemUse", "func_150936_a" },
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lcom/creativemd/littletiles/common/utils/PlacementHelper;getInstance(Lnet/minecraft/entity/player/EntityPlayer;)Lcom/creativemd/littletiles/common/utils/PlacementHelper;"))
    private PlacementHelper getInstance(EntityPlayer player, Operation<PlacementHelper> original) {
        return original.call(
            ((IMixinEntity) player)
                .getProxyPlayer(((IMixinMovingObjectPosition) Minecraft.getMinecraft().objectMouseOver).getWorld()));
    }

}
