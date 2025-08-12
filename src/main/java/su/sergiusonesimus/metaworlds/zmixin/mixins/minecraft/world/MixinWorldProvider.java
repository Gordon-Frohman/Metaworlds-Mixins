package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.world;

import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

@Mixin(WorldProvider.class)
public class MixinWorldProvider {

    @Shadow(remap = true)
    public World worldObj;

    @Inject(method = "setWorldTime", at = @At("TAIL"), remap = false)
    public void setWorldTime(long time) {
        for (World subworld : ((IMixinWorld) worldObj).getSubWorlds()) {
            subworld.setWorldTime(time);
        }
    }

}
