package su.sergiusonesimus.metaworlds.zmixin.interfaces.forge;

import cpw.mods.fml.common.network.internal.FMLMessage.OpenGui;

public interface IMixinOpenGui {

    public OpenGui setSubworldId(int id);

    public int getSubworldId();

}
