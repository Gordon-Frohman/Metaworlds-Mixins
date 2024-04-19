package net.tclproject.metaworlds.mixins.entity.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.tclproject.metaworlds.api.IMixinWorld;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityItem.class)
public class MixinEntityItem {

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "LEntity;<init>(Lnet/minecraft/world/World;)V"), index = 0)
    private World adjustWorld(World world) {
        return world != null ? ((IMixinWorld) world).getParentWorld() : world;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void EntityItem(World worldIn, double x, double y, double z, CallbackInfo ci) {
        if (worldIn != null) {
            Vec3 transformedPos = ((IMixinWorld) worldIn).transformToGlobal(x, y, z);
            ((Entity) (Object) this).setPosition(transformedPos.xCoord, transformedPos.yCoord, transformedPos.zCoord);
        } else((Entity) (Object) this).setPosition(x, y, z);
    }

}
