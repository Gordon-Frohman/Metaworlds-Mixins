package net.tclproject.metaworlds.mixin.mixins.client.network;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S09PacketHeldItemChange;
import net.minecraft.network.play.server.S14PacketEntity;
import net.minecraft.network.play.server.S18PacketEntityTeleport;
import net.minecraft.util.Vec3;
import net.tclproject.metaworlds.api.IMixinWorld;
import net.tclproject.metaworlds.mixin.interfaces.network.play.PacketHandler;
import net.tclproject.metaworlds.mixin.interfaces.network.play.server.IMixinS14PacketEntity;
import net.tclproject.metaworlds.mixin.interfaces.network.play.server.IMixinS18PacketEntityTeleport;
import net.tclproject.metaworlds.api.IMixinEntity;

@Mixin(NetHandlerPlayClient.class)
public abstract class MixinNetHandlerPlayClient {

    @Shadow(remap = true)
    public WorldClient clientWorldController;

    @Shadow(remap = true)
    public Minecraft gameController;

    @Shadow(remap = true)
    public NetworkManager netManager;

    @Shadow(remap = true)
    public boolean doneLoadingTerrain;
    
    //TODO

    /**
     * Updates an entity's position and rotation as specified by the packet
     */
	@Overwrite
    public void handleEntityTeleport(S18PacketEntityTeleport p_147275_1_) {
        Entity entity = this.clientWorldController.getEntityByID(p_147275_1_.func_149451_c());

        if (entity != null) {
            entity.serverPosX = p_147275_1_.func_149449_d();
            entity.serverPosY = p_147275_1_.func_149448_e();
            entity.serverPosZ = p_147275_1_.func_149446_f();
            double d0 = (double) entity.serverPosX / 32.0D;
            double d1 = (double) entity.serverPosY / 32.0D + 0.015625D;
            double d2 = (double) entity.serverPosZ / 32.0D;
            float f = (float) (p_147275_1_.func_149450_g() * 360) / 256.0F;
            float f1 = (float) (p_147275_1_.func_149447_h() * 360) / 256.0F;

            ((IMixinEntity)entity).setWorldBelowFeet(((IMixinWorld)this.clientWorldController).getSubWorld(((IMixinS18PacketEntityTeleport)p_147275_1_).getSubWorldBelowFeetId()));
            if (((IMixinS18PacketEntityTeleport)p_147275_1_).getLosingTraction()) {
            	((IMixinEntity)entity).slowlyRemoveWorldBelowFeet();
            	((IMixinEntity)entity).setTractionTickCount(((IMixinS18PacketEntityTeleport)p_147275_1_).getTractionLoss());
            }

            if (((IMixinS18PacketEntityTeleport)p_147275_1_).getSendSubWorldPosFlag() != 0) {
                if (((IMixinS18PacketEntityTeleport)p_147275_1_).getXPosOnSubWorld() != ((IMixinEntity)entity).getServerPosXOnSubWorld()
                    || ((IMixinS18PacketEntityTeleport)p_147275_1_).getYPosOnSubWorld() != ((IMixinEntity)entity).getServerPosYOnSubWorld()
                    || ((IMixinS18PacketEntityTeleport)p_147275_1_).getZPosOnSubWorld() != ((IMixinEntity)entity).getServerPosZOnSubWorld()) {
                	((IMixinEntity)entity).setServerPosXOnSubWorld(((IMixinS18PacketEntityTeleport)p_147275_1_).getXPosOnSubWorld());
                	((IMixinEntity)entity).setServerPosYOnSubWorld(((IMixinS18PacketEntityTeleport)p_147275_1_).getYPosOnSubWorld());
                	((IMixinEntity)entity).setServerPosZOnSubWorld(((IMixinS18PacketEntityTeleport)p_147275_1_).getZPosOnSubWorld());

                    Vec3 transformedPos = ((IMixinWorld)((IMixinEntity)entity).getWorldBelowFeet())
                        .transformLocalToOther(
                            entity.worldObj,
                            (double) ((IMixinEntity)entity).getServerPosXOnSubWorld() / 32.0D,
                            (double) ((IMixinEntity)entity).getServerPosYOnSubWorld() / 32.0D,
                            (double) ((IMixinEntity)entity).getServerPosZOnSubWorld() / 32.0D);
                    d0 = transformedPos.xCoord;
                    d1 = transformedPos.yCoord;
                    d2 = transformedPos.zCoord;
                    entity.setPositionAndRotation2(d0, d1, d2, f, f1, 1);
                }
            } else entity.setPositionAndRotation2(d0, d1, d2, f, f1, 3);
        }
    }

    /**
     * Updates which hotbar slot of the player is currently selected
     */
	@Overwrite
    public void handleHeldItemChange(S09PacketHeldItemChange p_147257_1_) {
        if (p_147257_1_.func_149385_c() >= 0 && p_147257_1_.func_149385_c() < InventoryPlayer.getHotbarSize()) {
            this.gameController.thePlayer.inventory.currentItem = p_147257_1_.func_149385_c();
        }
    }

    /**
     * Updates the specified entity's position by the specified relative moment and absolute rotation. Note that
     * subclassing of the packet allows for the specification of a subset of this data (e.g. only rel. position, abs.
     * rotation or both).
     */
	@Overwrite
    public void handleEntityMovement(S14PacketEntity p_147259_1_) {
        Entity entity = p_147259_1_.func_149065_a(this.clientWorldController);

        if (entity != null) {
            entity.serverPosX += p_147259_1_.func_149062_c();
            entity.serverPosY += p_147259_1_.func_149061_d();
            entity.serverPosZ += p_147259_1_.func_149064_e();
            double d0 = (double) entity.serverPosX / 32.0D;
            double d1 = (double) entity.serverPosY / 32.0D;
            double d2 = (double) entity.serverPosZ / 32.0D;
            float f = p_147259_1_.func_149060_h() ? (float) (p_147259_1_.func_149066_f() * 360) / 256.0F
                : entity.rotationYaw;
            float f1 = p_147259_1_.func_149060_h() ? (float) (p_147259_1_.func_149063_g() * 360) / 256.0F
                : entity.rotationPitch;

            ((IMixinEntity)entity).setWorldBelowFeet(((IMixinWorld)this.clientWorldController).getSubWorld(((IMixinS14PacketEntity)p_147259_1_).getSubWorldBelowFeetId()));

            if (((IMixinS14PacketEntity)p_147259_1_).getLosingTraction()) {
                ((IMixinEntity)entity).slowlyRemoveWorldBelowFeet();
                ((IMixinEntity)entity).setTractionTickCount(((IMixinS14PacketEntity)p_147259_1_).getTractionLoss());
            }

            if (((IMixinS14PacketEntity)p_147259_1_).getSendSubWorldPosFlag() != 0) {
                if (((IMixinS14PacketEntity)p_147259_1_).getXPosDiffOnSubWorld() != 0 || ((IMixinS14PacketEntity)p_147259_1_).getYPosDiffOnSubWorld() != 0
                    || ((IMixinS14PacketEntity)p_147259_1_).getZPosDiffOnSubWorld() != 0) {
                    ((IMixinEntity)entity).setServerPosXOnSubWorld(((IMixinEntity)entity).getServerPosXOnSubWorld() + ((IMixinS14PacketEntity)p_147259_1_).getXPosDiffOnSubWorld());
                    ((IMixinEntity)entity).setServerPosYOnSubWorld(((IMixinEntity)entity).getServerPosYOnSubWorld() + ((IMixinS14PacketEntity)p_147259_1_).getYPosDiffOnSubWorld());
                    ((IMixinEntity)entity).setServerPosZOnSubWorld(((IMixinEntity)entity).getServerPosZOnSubWorld() + ((IMixinS14PacketEntity)p_147259_1_).getZPosDiffOnSubWorld());
                    Vec3 deleteMePos = ((IMixinEntity)entity).getLocalPos(((IMixinEntity)entity).getWorldBelowFeet());
                    Vec3 transformedPos = ((IMixinWorld)((IMixinEntity)entity).getWorldBelowFeet())
                        .transformLocalToOther(
                            entity.worldObj,
                            (double) ((IMixinEntity)entity).getServerPosXOnSubWorld() / 32.0D,
                            (double) ((IMixinEntity)entity).getServerPosYOnSubWorld() / 32.0D,
                            (double) ((IMixinEntity)entity).getServerPosZOnSubWorld() / 32.0D);
                    d0 = transformedPos.xCoord;
                    d1 = transformedPos.yCoord;
                    d2 = transformedPos.zCoord;
                    entity.setPositionAndRotation2(d0, d1, d2, f, f1, 1);
                }
            } else entity.setPositionAndRotation2(d0, d1, d2, f, f1, 3);
        }
    }

    /**
     * Handles changes in player positioning and rotation such as when travelling to a new dimension, (re)spawning,
     * mounting horses etc. Seems to immediately reply to the server with the clients post-processing perspective on the
     * player positioning
     */
	@Overwrite
    public void handlePlayerPosLook(S08PacketPlayerPosLook p_147258_1_) {
        EntityClientPlayerMP entityclientplayermp = this.gameController.thePlayer;
        double d0 = p_147258_1_.func_148932_c();
        double d1 = p_147258_1_.func_148928_d();
        double d2 = p_147258_1_.func_148933_e();
        float f = p_147258_1_.func_148931_f();
        float f1 = p_147258_1_.func_148930_g();
        entityclientplayermp.ySize = 0.0F;
        entityclientplayermp.motionX = entityclientplayermp.motionY = entityclientplayermp.motionZ = 0.0D;
        entityclientplayermp.setPositionAndRotation(d0, d1, d2, f, f1);
        this.netManager.scheduleOutboundPacket(
        		PacketHandler.getC06PacketPlayerPosLook(
                entityclientplayermp.posX,
                entityclientplayermp.boundingBox.minY,
                entityclientplayermp.posY,
                entityclientplayermp.posZ,
                p_147258_1_.func_148931_f(),
                p_147258_1_.func_148930_g(),
                p_147258_1_.func_148929_h(),
                /*
                 * entityclientplayermp.getWorldBelowFeet().getSubWorldID() COORDS WOULD NEED TO BE IN SUBWORLD
                 * COORDSYSTEM
                 */
                ((IMixinWorld)entityclientplayermp.worldObj).getSubWorldID(),
                ((IMixinEntity)entityclientplayermp).getTractionLossTicks(),
                ((IMixinEntity)entityclientplayermp).isLosingTraction()),
            new GenericFutureListener[0]);

        if (!this.doneLoadingTerrain) {
            this.gameController.thePlayer.prevPosX = this.gameController.thePlayer.posX;
            this.gameController.thePlayer.prevPosY = this.gameController.thePlayer.posY;
            this.gameController.thePlayer.prevPosZ = this.gameController.thePlayer.posZ;
            this.doneLoadingTerrain = true;
            this.gameController.displayGuiScreen((GuiScreen) null);
        }
    }

}
