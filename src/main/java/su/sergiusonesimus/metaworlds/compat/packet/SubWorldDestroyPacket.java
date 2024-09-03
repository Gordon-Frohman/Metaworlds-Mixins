package su.sergiusonesimus.metaworlds.compat.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import su.sergiusonesimus.metaworlds.api.IMixinEntity;
import su.sergiusonesimus.metaworlds.api.IMixinWorld;
import su.sergiusonesimus.metaworlds.api.SubWorld;

public class SubWorldDestroyPacket implements IMessage {

    public int subWorldsCount;
    public Integer[] subWorldIDs;

    public SubWorldDestroyPacket() {}

    public SubWorldDestroyPacket(int numSubWorldsToDestroy, Integer[] subWorldIDsArray) {
        this.subWorldsCount = numSubWorldsToDestroy;
        this.subWorldIDs = subWorldIDsArray;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.subWorldsCount = buf.readInt();
        if (this.subWorldsCount != -1) {
            this.subWorldIDs = new Integer[this.subWorldsCount];

            for (int i = 0; i < this.subWorldsCount; ++i) {
                this.subWorldIDs[i] = Integer.valueOf(buf.readInt());
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.subWorldsCount);
        if (this.subWorldsCount != -1) {
            Integer[] arr$ = this.subWorldIDs;
            int len$ = arr$.length;

            for (int i$ = 0; i$ < len$; ++i$) {
                Integer curSubWorldID = arr$[i$];
                buf.writeInt(curSubWorldID.intValue());
            }
        }
    }

    public static class Handler implements IMessageHandler<SubWorldDestroyPacket, IMessage> {

        @Override
        public IMessage onMessage(SubWorldDestroyPacket message, MessageContext ctx) {
            if (!ctx.side.isServer()) {
                EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
                if (message.subWorldsCount == -1) {
                    while (!((IMixinWorld) player.worldObj).getSubWorldsMap()
                        .isEmpty()) {
                        SubWorld arr$ = (SubWorld) ((IMixinWorld) player.worldObj).getSubWorlds()
                            .iterator()
                            .next();
                        arr$.removeSubWorld();
                        ((IMixinEntity) player).getPlayerProxyMap()
                            .remove(Integer.valueOf(arr$.getSubWorldID()));
                    }
                } else {
                    Integer[] var10 = message.subWorldIDs;
                    int len$ = var10.length;

                    for (int i$ = 0; i$ < len$; ++i$) {
                        Integer curSubWorldID = var10[i$];
                        SubWorld curSubWorld = (SubWorld) ((IMixinWorld) player.worldObj)
                            .getSubWorld(curSubWorldID.intValue());
                        curSubWorld.removeSubWorld();
                        ((IMixinEntity) player).getPlayerProxyMap()
                            .remove(Integer.valueOf(curSubWorld.getSubWorldID()));
                    }
                }
            }
            return null;
        }
    }
}
