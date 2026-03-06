package su.sergiusonesimus.metaworlds.zmixin.mixins.warpdrive;

import net.minecraft.block.Block;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import su.sergiusonesimus.metaworlds.integrations.warpdrive.SubworldChunkHandler;
import su.sergiusonesimus.metaworlds.zmixin.MixinPriorities;

@Mixin(value = WorldClient.class, priority = MixinPriorities.WARPDRIVE)
public class MixinWorldClient {

    @Inject(method = "func_147492_c", at = @At(value = "HEAD"))
    public void storeWorld(int x, int y, int z, Block block, int meta, CallbackInfoReturnable<Boolean> cir) {
        SubworldChunkHandler.currentWorld = (World) (Object) this;
    }

}
