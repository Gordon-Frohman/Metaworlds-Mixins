package su.sergiusonesimus.metaworlds.zmixin.mixins.forge;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cpw.mods.fml.common.network.internal.FMLMessage.OpenGui;
import io.netty.buffer.ByteBuf;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.forge.IMixinOpenGui;

@Mixin(OpenGui.class)
public class MixinOpenGui implements IMixinOpenGui {

    public int subworldId = 0;

    public OpenGui setSubworldId(int id) {
        subworldId = id;
        return (OpenGui) (Object) this;
    }

    public int getSubworldId() {
        return subworldId;
    }

    @Inject(method = "toBytes", remap = false, at = @At(value = "TAIL"))
    void toBytes(ByteBuf buf, CallbackInfo ci) {
        buf.writeInt(subworldId);
    }

    @Inject(method = "fromBytes", remap = false, at = @At(value = "TAIL"))
    void fromBytes(ByteBuf buf, CallbackInfo ci) {
        subworldId = buf.readInt();
    }

}
