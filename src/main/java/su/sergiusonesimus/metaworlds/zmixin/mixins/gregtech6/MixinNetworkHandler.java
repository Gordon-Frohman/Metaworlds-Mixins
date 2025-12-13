package su.sergiusonesimus.metaworlds.zmixin.mixins.gregtech6;

import net.minecraft.entity.player.EntityPlayerMP;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import gregapi.network.NetworkHandler;
import su.sergiusonesimus.metaworlds.entity.player.EntityPlayerProxy;

@Mixin(NetworkHandler.class)
public class MixinNetworkHandler {

    @ModifyVariable(method = "sendToPlayer", remap = false, at = @At("HEAD"), argsOnly = true, ordinal = 0)
    public EntityPlayerMP modifyPlayer(EntityPlayerMP originalPlayer) {
        return originalPlayer instanceof EntityPlayerProxy proxy ? (EntityPlayerMP) proxy.getRealPlayer()
            : originalPlayer;
    }

}
