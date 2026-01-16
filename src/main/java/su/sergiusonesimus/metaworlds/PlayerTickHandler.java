package su.sergiusonesimus.metaworlds;

import net.minecraft.world.World;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;
import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.IMixinEntity;

public class PlayerTickHandler {

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event) {
        if (event.phase.equals(Phase.START) && ((IMixinEntity) event.player).isLosingTraction()) {
            byte tractionTicks = ((IMixinEntity) event.player).getTractionLossTicks();
            if (tractionTicks >= 20) {
                ((IMixinEntity) event.player).setWorldBelowFeet((World) null);
            } else if (event.player.isOnLadder()
                || !(((IMixinEntity) event.player).getWorldBelowFeet() instanceof SubWorld)) {
                    ((IMixinEntity) event.player).setTractionTickCount((byte) 0);
                } else {
                    ((IMixinEntity) event.player).setTractionTickCount((byte) (tractionTicks + 1));
                }
        }
    }
}
