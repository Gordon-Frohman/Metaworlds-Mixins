package su.sergiusonesimus.metaworlds.zmixin.mixins.littletiles;

import net.minecraft.entity.player.EntityPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.creativemd.littletiles.common.packet.LittlePlacePacket;
import com.creativemd.littletiles.common.utils.LittleTileBlockPos;

import io.netty.buffer.ByteBuf;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.littletiles.IMixinLittleTileBlockPos;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

@Mixin(LittlePlacePacket.class)
public class MixinLittlePlacePacket {

    @Shadow(remap = false)
    public LittleTileBlockPos pos;

    private int subworldId = 0;

    @Inject(method = "writeBytes", remap = false, at = @At(value = "TAIL"))
    public void writeBytes(ByteBuf buf, CallbackInfo ci) {
        buf.writeInt(((IMixinWorld) ((IMixinLittleTileBlockPos) pos).getWorld()).getSubWorldID());
    }

    @Inject(method = "readBytes", remap = false, at = @At(value = "TAIL"))
    public void readBytes(ByteBuf buf, CallbackInfo ci) {
        subworldId = buf.readInt();
    }

    @Inject(
        method = "executeServer",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lcom/creativemd/littletiles/common/items/ItemBlockTiles;placeBlockAt(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lcom/creativemd/littletiles/common/utils/LittleTileBlockPos;Lcom/creativemd/littletiles/common/utils/PlacementHelper;Z)Z",
            remap = false))
    public void executeServer(EntityPlayer player, CallbackInfo ci) {
        ((IMixinLittleTileBlockPos) pos).setWorld(((IMixinWorld) player.worldObj).getSubWorld(subworldId));
    }

}
