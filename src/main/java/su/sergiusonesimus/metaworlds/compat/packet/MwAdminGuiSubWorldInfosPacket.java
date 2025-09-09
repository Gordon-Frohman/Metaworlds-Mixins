package su.sergiusonesimus.metaworlds.compat.packet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.minecraft.client.Minecraft;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import su.sergiusonesimus.metaworlds.admin.GuiMwAdmin;
import su.sergiusonesimus.metaworlds.admin.MwAdminContainer;
import su.sergiusonesimus.metaworlds.admin.MwAdminContainer.AdminSubWorldInfo;

public class MwAdminGuiSubWorldInfosPacket implements IMessage {

    Collection<MwAdminContainer.AdminSubWorldInfo> adminSubWorldInfos;

    // The basic, no-argument constructor MUST be included to use the new automated handling
    public MwAdminGuiSubWorldInfosPacket() {}

    // We need to initialize our data, so provide a suitable constructor:
    public MwAdminGuiSubWorldInfosPacket(Collection<MwAdminContainer.AdminSubWorldInfo> parAdminSubWorldInfos) {
        this.adminSubWorldInfos = parAdminSubWorldInfos;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.adminSubWorldInfos = new ArrayList<AdminSubWorldInfo>();
        int entriesCount = buf.readInt();

        for (int i = 0; i < entriesCount; ++i) {
            int curSubWorldId = buf.readInt();
            boolean curIsSpawned = buf.readBoolean();
            int curDimensionId = buf.readInt();
            this.adminSubWorldInfos
                .add(new MwAdminContainer.AdminSubWorldInfo(curSubWorldId, curIsSpawned, curDimensionId));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.adminSubWorldInfos.size());
        Iterator<AdminSubWorldInfo> i$ = this.adminSubWorldInfos.iterator();

        while (i$.hasNext()) {
            MwAdminContainer.AdminSubWorldInfo curInfo = (MwAdminContainer.AdminSubWorldInfo) i$.next();
            buf.writeInt(curInfo.subWorldId);
            buf.writeBoolean(curInfo.isSpawned);
            buf.writeInt(curInfo.dimensionId);
        }
    }

    public static class Handler implements IMessageHandler<MwAdminGuiSubWorldInfosPacket, IMessage> {

        @Override
        public IMessage onMessage(MwAdminGuiSubWorldInfosPacket message, MessageContext ctx) {
            // BattlemodeHookContainerClass.interactWith = message.data.getBoolean("intw");
            if (!ctx.side.isServer()) {
                if (Minecraft.getMinecraft().currentScreen instanceof GuiMwAdmin) {
                    ((GuiMwAdmin) Minecraft
                        .getMinecraft().currentScreen).guiSubWorldsList.adminSubWorldInfos = (List<AdminSubWorldInfo>) message.adminSubWorldInfos;
                }
            }
            return null;
        }
    }
}
