package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.world;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.storage.ISaveHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import su.sergiusonesimus.metaworlds.util.UnmodifiableSingleObjPlusCollection;

@Mixin(World.class)
public class MixinWorldC {

    @Shadow(remap = false)
    public Map<Integer, World> childSubWorlds;

    @Shadow(remap = false)
    private UnmodifiableSingleObjPlusCollection<World> allWorlds;

    @Inject(
        method = "<init>(Lnet/minecraft/world/storage/ISaveHandler;Ljava/lang/String;Lnet/minecraft/world/WorldProvider;Lnet/minecraft/world/WorldSettings;Lnet/minecraft/profiler/Profiler;)V",
        at = @At("TAIL"))
    public void setSubworlds(ISaveHandler p_i45368_1_, String p_i45368_2_, WorldProvider p_i45368_3_,
        WorldSettings p_i45368_4_, Profiler p_i45368_5_, CallbackInfo ci) {
        childSubWorlds = new ConcurrentHashMap<Integer, World>();
        allWorlds = new UnmodifiableSingleObjPlusCollection<World>((World) (Object) this, this.childSubWorlds.values());
    }

}
