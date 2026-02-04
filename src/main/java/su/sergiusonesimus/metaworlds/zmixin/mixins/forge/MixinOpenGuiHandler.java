package su.sergiusonesimus.metaworlds.zmixin.mixins.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import cpw.mods.fml.common.network.internal.FMLMessage.OpenGui;
import cpw.mods.fml.common.network.internal.OpenGuiHandler;
import io.netty.channel.ChannelHandlerContext;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.forge.IMixinOpenGui;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.IMixinEntity;

@Mixin(OpenGuiHandler.class)
public class MixinOpenGuiHandler {

    private OpenGui storedMessage;

    @Inject(method = "channelRead0", remap = false, at = @At(value = "HEAD"))
    protected void storeMessage(ChannelHandlerContext ctx, OpenGui msg, CallbackInfo ci) {
        storedMessage = msg;
    }

    @WrapOperation(
        method = "channelRead0",
        remap = false,
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;thePlayer:Lnet/minecraft/client/entity/EntityClientPlayerMP;",
            remap = true))
    protected EntityClientPlayerMP getThePlayer(Minecraft instance, Operation<EntityClientPlayerMP> original) {
        return (EntityClientPlayerMP) ((IMixinEntity) original.call(instance))
            .getProxyPlayer(((IMixinOpenGui) storedMessage).getSubworldId());
    }

}
