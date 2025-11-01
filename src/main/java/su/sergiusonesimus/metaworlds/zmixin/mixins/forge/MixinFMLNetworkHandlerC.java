package su.sergiusonesimus.metaworlds.zmixin.mixins.forge;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.llamalad7.mixinextras.sugar.Local;

import cpw.mods.fml.common.network.internal.FMLNetworkHandler;

@Mixin(FMLNetworkHandler.class)
public class MixinFMLNetworkHandlerC {

    @Inject(
        method = "openGui",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lcpw/mods/fml/common/FMLCommonHandler;showGuiScreen(Ljava/lang/Object;)V",
            remap = false,
            shift = Shift.AFTER),
        locals = LocalCapture.CAPTURE_FAILHARD)
    private static void openGui(EntityPlayer entityPlayer, Object mod, int modGuiId, World world, int x, int y, int z,
        CallbackInfo ci, @Local(name = "guiContainer") Object guiContainer) {
        if (guiContainer instanceof GuiContainer container && entityPlayer instanceof EntityPlayerSP clientPlayer) {
            container.mc = clientPlayer.mc;
        }
    }

}
