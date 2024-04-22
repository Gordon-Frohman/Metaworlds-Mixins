package net.tclproject.metaworlds.mixin.mixins.world;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.storage.ISaveHandler;
import net.tclproject.metaworlds.core.SubWorldServerFactory;
import net.tclproject.metaworlds.patcher.SubWorldFactory;

@Mixin(WorldServer.class)
public abstract class MixinWorldServer extends MixinWorld {

    private static SubWorldFactory subWorldFactory = null;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void WorldClient(MinecraftServer p_i45284_1_, ISaveHandler p_i45284_2_, String p_i45284_3_, int p_i45284_4_, WorldSettings p_i45284_5_, Profiler p_i45284_6_, CallbackInfo ci) {
        if (subWorldFactory == null)
        	subWorldFactory = new SubWorldServerFactory();
    }

}
