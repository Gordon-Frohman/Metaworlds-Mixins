package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.entity.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

@Mixin(EntityItem.class)
public class MixinEntityItem {

    @ModifyArg(
        method = "<init>",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;<init>(Lnet/minecraft/world/World;)V"),
        index = 0)
    private static World adjustWorld(World world) {
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
