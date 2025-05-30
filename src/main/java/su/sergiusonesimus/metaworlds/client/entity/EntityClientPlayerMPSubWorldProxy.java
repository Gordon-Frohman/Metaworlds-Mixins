package su.sergiusonesimus.metaworlds.client.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

import su.sergiusonesimus.metaworlds.client.MinecraftSubWorldProxy;
import su.sergiusonesimus.metaworlds.client.multiplayer.PlayerControllerMPSubWorldProxy;
import su.sergiusonesimus.metaworlds.client.network.NetHandlerPlayClientSubWorldProxy;
import su.sergiusonesimus.metaworlds.client.renderer.RenderGlobalSubWorld;
import su.sergiusonesimus.metaworlds.entity.player.EntityPlayerProxy;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.IMixinEntity;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

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

        this.mc.theWorld.mc = this.mc;

        ((NetHandlerPlayClientSubWorldProxy) this.sendQueue).proxyPlayer = this;

        ((IMixinEntity) this.realPlayer).getPlayerProxyMap()
            .put(((IMixinWorld) targetSubWorld).getSubWorldID(), this);
    }

    public int hashCode() {
        return this.realPlayer == null ? null : this.realPlayer.hashCode();
    }

    public NBTTagCompound getEntityData() {
        return this.realPlayer == null ? null : this.realPlayer.getEntityData();
    }

    public String registerExtendedProperties(String identifier, IExtendedEntityProperties properties) {
        return this.realPlayer == null ? null : this.realPlayer.registerExtendedProperties(identifier, properties);
    }

    public IExtendedEntityProperties getExtendedProperties(String identifier) {
        return this.realPlayer == null ? null : this.realPlayer.getExtendedProperties(identifier);
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
