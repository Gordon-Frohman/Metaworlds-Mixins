package su.sergiusonesimus.metaworlds.core;

import java.util.Iterator;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import su.sergiusonesimus.metaworlds.core.client.SubWorldClient;
import su.sergiusonesimus.metaworlds.mixin.interfaces.minecraft.world.IMixinWorld;

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
