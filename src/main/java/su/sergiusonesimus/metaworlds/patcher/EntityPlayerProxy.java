package su.sergiusonesimus.metaworlds.patcher;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;

public interface EntityPlayerProxy {

    INetHandler getNetHandlerProxy();

    EntityPlayer getRealPlayer();
}
