package net.tclproject.metaworlds.serverlist;

import java.lang.reflect.Field;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.RenderTickEvent;

public class ServerListButtonAdder {

    ServerListButton serverListButton;
    Field fieldButtonList;

    public ServerListButtonAdder() {
        Field[] arr$ = GuiScreen.class.getDeclaredFields();
        int len$ = arr$.length;

        for (int i$ = 0; i$ < len$; ++i$) {
            Field curField = arr$[i$];
            if (curField.getType() == List.class) {
                this.fieldButtonList = curField;
                this.fieldButtonList.setAccessible(true);
                break;
            }
        }
    }

    @SubscribeEvent
    public void onRenderTick(RenderTickEvent event) {}
}
