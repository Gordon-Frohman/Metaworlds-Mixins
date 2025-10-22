package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.util;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MovingObjectPosition.class)
public class MixinMovingObjectPositionC {

    @Shadow(remap = false)
    public World worldObj;

    @Inject(method = "<init>(IIIILnet/minecraft/util/Vec3;)V", at = @At("TAIL"))
    public void MovingObjectPosition(int p_i45481_1_, int p_i45481_2_, int p_i45481_3_, int p_i45481_4_,
        Vec3 p_i45481_5_, CallbackInfo ci) {
        this.worldObj = Minecraft.getMinecraft().theWorld;
    }

    @Inject(method = "<init>(IIIILnet/minecraft/util/Vec3;Z)V", at = @At("TAIL"))
    public void MovingObjectPosition(int p_i45481_1_, int p_i45481_2_, int p_i45481_3_, int p_i45481_4_,
        Vec3 p_i45481_5_, boolean p_i45481_6_, CallbackInfo ci) {
        this.worldObj = Minecraft.getMinecraft().theWorld;
    }

}
