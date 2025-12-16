package su.sergiusonesimus.metaworlds.network.play.server;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.IMixinEntity;
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
                EntityPlayer player = Minecraft.getMinecraft().thePlayer;

                ((IMixinEntity) player)
                    .setWorldBelowFeet(((IMixinWorld) player.getEntityWorld()).getSubWorld(message.subworldId));
            }
            return null;
        }
    }

}
