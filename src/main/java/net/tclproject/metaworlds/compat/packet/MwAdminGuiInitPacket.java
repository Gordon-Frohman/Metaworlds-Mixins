package net.tclproject.metaworlds.compat.packet;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.tclproject.metaworlds.admin.GuiMwAdmin;
import net.tclproject.metaworlds.admin.MwAdminContainer;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class MwAdminGuiInitPacket implements IMessage {

    public List<MwAdminContainer.SaveGameInfo> saveGameInfos;

    // The basic, no-argument constructor MUST be included to use the new automated handling
    public MwAdminGuiInitPacket() {}

    // We need to initialize our data, so provide a suitable constructor:
    public MwAdminGuiInitPacket(List<MwAdminContainer.SaveGameInfo> parInfosList) {
        this.saveGameInfos = parInfosList;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.saveGameInfos = new ArrayList();
        int entryCount = buf.readInt();

        for (int i = 0; i < entryCount; ++i) {
            MwAdminContainer.SaveGameInfo curGameInfo = new MwAdminContainer.SaveGameInfo(
                ByteBufUtils.readUTF8String(buf),
                (File) null);
            this.saveGameInfos.add(curGameInfo);
            int subWorldsCount = buf.readInt();

            for (int j = 0; j < subWorldsCount; ++j) {
                curGameInfo.subWorldsList
                    .add(new MwAdminContainer.SaveGameSubWorldInfo(ByteBufUtils.readUTF8String(buf), (String) null, 0));
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.saveGameInfos.size());
        Iterator i$ = this.saveGameInfos.iterator();

        while (i$.hasNext()) {
            MwAdminContainer.SaveGameInfo curInfo = (MwAdminContainer.SaveGameInfo) i$.next();
            ByteBufUtils.writeUTF8String(buf, curInfo.worldFileName);
            buf.writeInt(curInfo.subWorldsList.size());
            Iterator i$1 = curInfo.subWorldsList.iterator();

            while (i$1.hasNext()) {
                MwAdminContainer.SaveGameSubWorldInfo curSubWorldInfo = (MwAdminContainer.SaveGameSubWorldInfo) i$1
                    .next();
                ByteBufUtils.writeUTF8String(buf, curSubWorldInfo.subWorldName);
            }
        }
    }

    public static class Handler implements IMessageHandler<MwAdminGuiInitPacket, IMessage> {

        @Override
        public IMessage onMessage(MwAdminGuiInitPacket message, MessageContext ctx) {
            if (!ctx.side.isServer()) {
                EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
                if (Minecraft.getMinecraft().currentScreen instanceof GuiMwAdmin) {
                    ((GuiMwAdmin) Minecraft
                        .getMinecraft().currentScreen).guiImportWorldsList.worldsList = message.saveGameInfos;
                }
            }
            return null;
        }
    }
}
