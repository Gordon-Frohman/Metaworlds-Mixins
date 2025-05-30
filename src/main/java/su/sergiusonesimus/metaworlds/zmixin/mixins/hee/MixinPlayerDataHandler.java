package su.sergiusonesimus.metaworlds.zmixin.mixins.hee;

import net.minecraftforge.event.entity.EntityEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import chylex.hee.mechanics.misc.PlayerDataHandler;
import su.sergiusonesimus.metaworlds.entity.player.EntityPlayerProxy;

@Mixin(PlayerDataHandler.class)
public class MixinPlayerDataHandler {

    @Inject(method = "onEntityConstructing", at = @At(value = "HEAD"), cancellable = true, remap = false)
    public void onEntityConstructing(final EntityEvent.EntityConstructing e, CallbackInfo ci) {
        if (e.entity instanceof EntityPlayerProxy) ci.cancel();
    }

}
