package su.sergiusonesimus.metaworlds;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import su.sergiusonesimus.metaworlds.block.BlockSubWorldController;

public class MWControlEventListener {

    private static int count = 0;

    // awful, what I'd call a walking stick for the code, What it essentially does it unsets the shift needed to
    // completely dismount the entity, idk why or how
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onTick(TickEvent.PlayerTickEvent event) {
        // if (event.player.worldBelowFeet instanceof SubWorld) {
        // SubWorld world = (SubWorld)event.player.worldBelowFeet;
        // }
        if (BlockSubWorldController.toMakeFalse && count > 10) {
            KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindSneak.getKeyCode(), false);
            BlockSubWorldController.toMakeFalse = false;
            count = 0;
        } else if (BlockSubWorldController.toMakeFalse) {
            count++;
        }
    }

}
