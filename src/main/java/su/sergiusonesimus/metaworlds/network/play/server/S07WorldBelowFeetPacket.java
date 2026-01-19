package su.sergiusonesimus.metaworlds.network.play.server;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import su.sergiusonesimus.metaworlds.EventHookContainer;
import su.sergiusonesimus.metaworlds.MetaworldsMod;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.IMixinEntity;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.player.IMixinEntityPlayer;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

public class S07WorldBelowFeetPacket implements IMessage {

    public int subworldId = 0;

    public S07WorldBelowFeetPacket() {}

    public S07WorldBelowFeetPacket(int id) {
        this.subworldId = id;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.subworldId = buf.readInt();

    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.subworldId);
    }

    public static class Handler implements IMessageHandler<S07WorldBelowFeetPacket, IMessage> {

        @Override
        public IMessage onMessage(S07WorldBelowFeetPacket message, MessageContext ctx) {
            if (!ctx.side.isServer()) {
                EntityPlayer player = MetaworldsMod.proxy.getClientPlayer();

                World world = ((IMixinWorld) player.getEntityWorld()).getSubWorld(message.subworldId);
                if (world != null) {
                    setWorldBelowFeet(player, world);
                } else {
                    EventHookContainer.registerSubworldEvent(
                        message.subworldId,
                        (subworld) -> { setWorldBelowFeet(player, (World) subworld); });
                }
            }
            return null;
        }

        public void setWorldBelowFeet(EntityPlayer player, World world) {
            ((IMixinEntity) player).setWorldBelowFeet(world);
            ((IMixinEntityPlayer) player).setCurrentSubworldPosition(((IMixinWorld) world).transformToGlobal(player));
        }

    }

}
