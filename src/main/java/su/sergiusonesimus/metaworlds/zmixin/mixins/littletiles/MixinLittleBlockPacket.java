package su.sergiusonesimus.metaworlds.zmixin.mixins.littletiles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.creativemd.littletiles.common.packet.LittleBlockPacket;

import io.netty.buffer.ByteBuf;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.littletiles.IMixinMixinLittleBlockPacket;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.IMixinEntity;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

@Mixin(LittleBlockPacket.class)
public class MixinLittleBlockPacket implements IMixinMixinLittleBlockPacket {

    private int subworldId;

    public LittleBlockPacket setSubworld(World world) {
        return setSubworld(((IMixinWorld) world).getSubWorldID());
    }

    public LittleBlockPacket setSubworld(int id) {
        subworldId = id;
        return (LittleBlockPacket) (Object) this;
    }

    @Inject(
        method = "<init>(IIILnet/minecraft/entity/player/EntityPlayer;ILnet/minecraft/nbt/NBTTagCompound;)V",
        remap = false,
        at = @At(value = "TAIL"))
    private void init(int x, int y, int z, EntityPlayer player, int action, NBTTagCompound nbt, CallbackInfo ci) {
        setSubworld(player.worldObj);
    }

    @Inject(method = "writeBytes", remap = false, at = @At(value = "HEAD"))
    private void writeBytes(ByteBuf buf, CallbackInfo ci) {
        buf.writeInt(subworldId);
    }

    @Inject(method = "readBytes", remap = false, at = @At(value = "HEAD"))
    private void readBytes(ByteBuf buf, CallbackInfo ci) {
        subworldId = buf.readInt();
    }

    @ModifyVariable(method = "executeServer", remap = false, at = @At(value = "HEAD"), argsOnly = true)
    private EntityPlayer modifyPlayer(EntityPlayer player) {
        return ((IMixinEntity) player).getProxyPlayer(subworldId);
    }

}
