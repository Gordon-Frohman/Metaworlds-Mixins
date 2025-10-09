package su.sergiusonesimus.metaworlds.zmixin.mixins.forgemultipart;

import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import codechicken.lib.packet.PacketCustom;
import codechicken.lib.vec.BlockCoord;
import codechicken.multipart.TileMultipart$;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

@Mixin(targets = "codechicken.multipart.handler.MultipartCPH$")
public class MixinMultipartCPH {

    private PacketCustom stroredPacket;

    @Inject(method = "handleCompressedTileDesc", remap = false, at = @At(value = "HEAD"))
    private void storePacket(PacketCustom packet, World world, CallbackInfo ci) {
        stroredPacket = packet;
    }

    @ModifyVariable(method = "handleCompressedTileDesc", remap = false, at = @At(value = "HEAD"), argsOnly = true)
    private World modifyWorldParameter(World originalWorld) {
        int worldID = stroredPacket.readInt();
        return ((IMixinWorld) originalWorld).getSubWorld(worldID);
    }

    @Overwrite(remap = false)
    public void handleCompressedTileData(final PacketCustom packet, final World world) {
        for (int subworldID = packet.readInt(); subworldID != Integer.MAX_VALUE; subworldID = packet.readInt()) {
            World subworld = ((IMixinWorld) world).getSubWorld(subworldID);
            final BlockCoord pos = new BlockCoord(packet.readInt(), packet.readInt(), packet.readInt());
            for (short i = packet.readUByte(); i < 255; i = packet.readUByte()) {
                TileMultipart$.MODULE$.handlePacket(pos, subworld, (int) i, packet);
            }
        }
    }

}
