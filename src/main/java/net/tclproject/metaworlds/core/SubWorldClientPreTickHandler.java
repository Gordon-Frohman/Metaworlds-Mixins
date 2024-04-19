package net.tclproject.metaworlds.core;

import java.util.Iterator;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.tclproject.metaworlds.api.IMixinWorld;
import net.tclproject.metaworlds.core.client.SubWorldClient;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;

public class SubWorldClientPreTickHandler {

    @SubscribeEvent
    public void onClientTick(ClientTickEvent event) {
        if (event.phase.equals(Phase.START) && Minecraft.getMinecraft().theWorld != null) {
            Iterator i$ = ((IMixinWorld) Minecraft.getMinecraft().theWorld).getSubWorlds()
                .iterator();

            while (i$.hasNext()) {
                World curSubWorld = (World) i$.next();
                ((SubWorldClient) curSubWorld).onPreTick();
            }
        }
    }
}
