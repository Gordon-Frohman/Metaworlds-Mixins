package su.sergiusonesimus.metaworlds;

import java.util.Iterator;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import su.sergiusonesimus.metaworlds.client.multiplayer.SubWorldClient;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

@SideOnly(Side.CLIENT)
public class SubWorldClientPreTickHandler {

    @SubscribeEvent
    public void onClientTick(ClientTickEvent event) {
        if (event.phase.equals(Phase.START) && Minecraft.getMinecraft().theWorld != null) {
            Iterator<World> i$ = ((IMixinWorld) Minecraft.getMinecraft().theWorld).getSubWorlds()
                .iterator();

            while (i$.hasNext()) {
                World curSubWorld = (World) i$.next();
                ((SubWorldClient) curSubWorld).onPreTick();
            }
        }
    }
}
