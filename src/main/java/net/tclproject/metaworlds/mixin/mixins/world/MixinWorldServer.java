package net.tclproject.metaworlds.mixin.mixins.world;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.tclproject.metaworlds.core.SubWorldServerFactory;
import net.tclproject.metaworlds.patcher.SubWorldFactory;

@Mixin(WorldServer.class)
public class MixinWorldServer {

    public static SubWorldFactory subWorldFactory = null;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void WorldClient(NetHandlerPlayClient p_i45063_1_, WorldSettings p_i45063_2_, int p_i45063_3_,
        EnumDifficulty p_i45063_4_, Profiler p_i45063_5_, CallbackInfo ci) {
        if (subWorldFactory == null)
        	subWorldFactory = new SubWorldServerFactory();
    }

}
