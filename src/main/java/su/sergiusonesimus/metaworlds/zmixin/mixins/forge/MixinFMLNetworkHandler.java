package su.sergiusonesimus.metaworlds.zmixin.mixins.forge;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.llamalad7.mixinextras.sugar.Local;

import cpw.mods.fml.common.network.internal.FMLMessage.OpenGui;
import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.forge.IMixinOpenGui;

@Mixin(FMLNetworkHandler.class)
public class MixinFMLNetworkHandler {

    @Inject(
        method = "openGui",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lio/netty/channel/embedded/EmbeddedChannel;writeOutbound([Ljava/lang/Object;)Z",
            remap = false,
            shift = Shift.BEFORE),
        locals = LocalCapture.CAPTURE_FAILHARD)
    private static void setSubworldId(EntityPlayer entityPlayer, Object mod, int modGuiId, World world, int x, int y,
        int z, CallbackInfo ci, @Local(name = "openGui") OpenGui openGui) {
        if (world instanceof SubWorld subworld)
            openGui = ((IMixinOpenGui) openGui).setSubworldId(subworld.getSubWorldID());
    }

}
