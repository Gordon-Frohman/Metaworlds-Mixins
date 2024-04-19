package net.tclproject.metaworlds.patcher;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.tclproject.metaworlds.api.IMixinEntity;
import net.tclproject.metaworlds.api.IMixinWorld;
import net.tclproject.metaworlds.mixin.interfaces.client.multiplayer.IMixinWorldClient;

public class EntityClientPlayerMPSubWorldProxy extends EntityClientPlayerMP implements EntityPlayerProxy {

    private EntityClientPlayerMP realPlayer;

    private EntityClientPlayerMPSubWorldProxy(EntityClientPlayerMP originalPlayer, World targetSubWorld,
        MinecraftSubWorldProxy minecraftProxy) {
        super(
            minecraftProxy,
            targetSubWorld,
            Minecraft.getMinecraft()
                .getSession(),
            new NetHandlerPlayClientSubWorldProxy(
                minecraftProxy,
                originalPlayer.sendQueue,
                (WorldClient) targetSubWorld),
            originalPlayer.getStatFileWriter());
    }

    public EntityClientPlayerMPSubWorldProxy(EntityClientPlayerMP originalPlayer, World targetSubWorld) {
        this(originalPlayer, targetSubWorld, new MinecraftSubWorldProxy(Minecraft.getMinecraft()));

        this.realPlayer = originalPlayer;
        this.dimension = this.realPlayer.dimension;
        this.setEntityId(this.realPlayer.getEntityId());
        this.inventory = this.realPlayer.inventory;
        this.inventoryContainer = this.realPlayer.inventoryContainer;
        this.capabilities = this.realPlayer.capabilities;

        this.preventEntitySpawning = false;

        this.mc.thePlayer = this;
        this.mc.theWorld = (WorldClient) targetSubWorld;
        this.mc.playerController = new PlayerControllerMPSubWorldProxy(Minecraft.getMinecraft().playerController, this);
        this.mc.effectRenderer = new EffectRenderer(targetSubWorld, Minecraft.getMinecraft().renderEngine);
        this.mc.renderGlobal = new RenderGlobalSubWorld(this.mc, Minecraft.getMinecraft().renderGlobal);

        ((IMixinWorldClient)this.mc.theWorld).setMinecraft(this.mc);

        ((NetHandlerPlayClientSubWorldProxy) this.sendQueue).proxyPlayer = this;

        ((IMixinEntity)this.realPlayer).getPlayerProxyMap()
            .put(((IMixinWorld) targetSubWorld).getSubWorldID(), this);
    }

    public int hashCode() {
        return this.realPlayer.hashCode();
    }

    public NBTTagCompound getEntityData() {
        return this.realPlayer.getEntityData();
    }

    public String registerExtendedProperties(String identifier, IExtendedEntityProperties properties) {
        return this.realPlayer.registerExtendedProperties(identifier, properties);
    }

    public IExtendedEntityProperties getExtendedProperties(String identifier) {
        return this.realPlayer.getExtendedProperties(identifier);
    }

    @Override
    public NetHandlerPlayClient getNetHandlerProxy() {
        return this.sendQueue;
    }

    public Minecraft getMinecraft() {
        return this.mc;
    }

    @Override
    public EntityPlayer getRealPlayer() {
        return this.realPlayer;
    }

    @Override
    public void travelToDimension(int par1) {
        this.realPlayer.travelToDimension(par1);
    }

    @Override
    public void onUpdate() {
        // super.onUpdate();
    }
}
