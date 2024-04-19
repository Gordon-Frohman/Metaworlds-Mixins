package net.tclproject.metaworlds.mixin.interfaces.client.multiplayer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.world.World;
import net.tclproject.metaworlds.patcher.SubWorldFactory;

public interface IMixinWorldClient {

    public World CreateSubWorld();

    public World CreateSubWorld(int newSubWorldID);

    public Minecraft getMinecraft();

    public void setMinecraft(Minecraft newMinecraft);

    public NetHandlerPlayClient getSendQueue();
    
    public void setSubworldFactory(SubWorldFactory subWorldFactory);
    
    public SubWorldFactory getSubworldFactory();
}
