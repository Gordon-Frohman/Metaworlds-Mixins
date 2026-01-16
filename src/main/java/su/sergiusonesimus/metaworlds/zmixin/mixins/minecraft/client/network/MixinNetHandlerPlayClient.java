package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.client.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook;
import net.minecraft.network.play.server.S05PacketSpawnPosition;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S14PacketEntity;
import net.minecraft.network.play.server.S18PacketEntityTeleport;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import su.sergiusonesimus.metaworlds.EventHookContainer;
import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.IMixinEntity;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.player.IMixinEntityPlayer;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.network.play.PacketHandler;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.network.play.server.IMixinS05PacketSpawnPosition;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.network.play.server.IMixinS08PacketPlayerPosLook;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.network.play.server.IMixinS14PacketEntity;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.network.play.server.IMixinS18PacketEntityTeleport;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.storage.IMixinWorldInfo;

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

    // TODO

    private Packet storedPacket;

    @Inject(method = "handleEntityTeleport", at = @At(value = "HEAD"))
    public void handleEntityTeleport(S18PacketEntityTeleport packetIn, CallbackInfo ci) {
        storedPacket = packetIn;
    }

    @WrapOperation(
        method = "handleEntityTeleport",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setPositionAndRotation2(DDDFFI)V"))
    private void handleEntityTeleportSetPositionAndRotation2(Entity instance, double x, double y, double z, float yaw,
        float pitch, int rotationIncrements, Operation<Void> original) {
        S18PacketEntityTeleport packetIn = (S18PacketEntityTeleport) storedPacket;
        if (((IMixinS18PacketEntityTeleport) packetIn).getSendSubWorldPosFlag() != 0) {
            if (((IMixinS18PacketEntityTeleport) packetIn).getXPosOnSubWorld()
                != ((IMixinEntity) instance).getServerPosXOnSubWorld()
                || ((IMixinS18PacketEntityTeleport) packetIn).getYPosOnSubWorld()
                    != ((IMixinEntity) instance).getServerPosYOnSubWorld()
                || ((IMixinS18PacketEntityTeleport) packetIn).getZPosOnSubWorld()
                    != ((IMixinEntity) instance).getServerPosZOnSubWorld()) {
                ((IMixinEntity) instance)
                    .setServerPosXOnSubWorld(((IMixinS18PacketEntityTeleport) packetIn).getXPosOnSubWorld());
                ((IMixinEntity) instance)
                    .setServerPosYOnSubWorld(((IMixinS18PacketEntityTeleport) packetIn).getYPosOnSubWorld());
                ((IMixinEntity) instance)
                    .setServerPosZOnSubWorld(((IMixinS18PacketEntityTeleport) packetIn).getZPosOnSubWorld());

                Vec3 transformedPos = ((IMixinWorld) ((IMixinEntity) instance).getWorldBelowFeet())
                    .transformLocalToOther(
                        instance.worldObj,
                        (double) ((IMixinEntity) instance).getServerPosXOnSubWorld() / 32.0D,
                        (double) ((IMixinEntity) instance).getServerPosYOnSubWorld() / 32.0D,
                        (double) ((IMixinEntity) instance).getServerPosZOnSubWorld() / 32.0D);
                x = transformedPos.xCoord;
                y = transformedPos.yCoord;
                z = transformedPos.zCoord;
                original.call(instance, x, y, z, yaw, pitch, 1);
            }
        } else original.call(instance, x, y, z, yaw, pitch, 3);
    }

    @Inject(method = "handleEntityMovement", at = @At(value = "HEAD"))
    public void storePacket(S14PacketEntity packetIn, CallbackInfo ci) {
        storedPacket = packetIn;
    }

    @WrapOperation(
        method = "handleEntityMovement",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setPositionAndRotation2(DDDFFI)V"))
    private void handleEntityMovementSetPositionAndRotation2(Entity entity, double x, double y, double z, float yaw,
        float pitch, int rotationIncrements, Operation<Void> original) {
        S14PacketEntity packetIn = (S14PacketEntity) storedPacket;
        if (((IMixinS14PacketEntity) packetIn).getSendSubWorldPosFlag() != 0) {
            if (((IMixinS14PacketEntity) packetIn).getXPosDiffOnSubWorld() != 0
                || ((IMixinS14PacketEntity) packetIn).getYPosDiffOnSubWorld() != 0
                || ((IMixinS14PacketEntity) packetIn).getZPosDiffOnSubWorld() != 0) {
                ((IMixinEntity) entity).setServerPosXOnSubWorld(
                    ((IMixinEntity) entity).getServerPosXOnSubWorld()
                        + ((IMixinS14PacketEntity) packetIn).getXPosDiffOnSubWorld());
                ((IMixinEntity) entity).setServerPosYOnSubWorld(
                    ((IMixinEntity) entity).getServerPosYOnSubWorld()
                        + ((IMixinS14PacketEntity) packetIn).getYPosDiffOnSubWorld());
                ((IMixinEntity) entity).setServerPosZOnSubWorld(
                    ((IMixinEntity) entity).getServerPosZOnSubWorld()
                        + ((IMixinS14PacketEntity) packetIn).getZPosDiffOnSubWorld());
                Vec3 transformedPos = ((IMixinWorld) ((IMixinEntity) entity).getWorldBelowFeet()).transformLocalToOther(
                    entity.worldObj,
                    (double) ((IMixinEntity) entity).getServerPosXOnSubWorld() / 32.0D,
                    (double) ((IMixinEntity) entity).getServerPosYOnSubWorld() / 32.0D,
                    (double) ((IMixinEntity) entity).getServerPosZOnSubWorld() / 32.0D);
                x = transformedPos.xCoord;
                y = transformedPos.yCoord;
                z = transformedPos.zCoord;
                entity.setPositionAndRotation2(x, y, z, yaw, pitch, 1);
            }
        } else entity.setPositionAndRotation2(x, y, z, yaw, pitch, 3);
    }

    @Inject(method = "handlePlayerPosLook", at = @At(value = "HEAD"))
    public void storePacket(S08PacketPlayerPosLook packetIn, CallbackInfo ci) {
        storedPacket = packetIn;
    }

    @WrapOperation(
        method = "handlePlayerPosLook",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/entity/EntityClientPlayerMP;setPositionAndRotation(DDDFF)V"))
    private void setPositionAndRotation(EntityClientPlayerMP instance, double x, double y, double z, float yaw,
        float pitch, Operation<Void> original) {
        int subWorldID = ((IMixinS08PacketPlayerPosLook) storedPacket).getSubWorldBelowFeetID();
        EntityClientPlayerMP entityclientplayermp = this.gameController.thePlayer;
        World subworld = ((IMixinWorld) ((IMixinWorld) entityclientplayermp.worldObj).getParentWorld())
            .getSubWorldsMap()
            .get(subWorldID);
        if (subWorldID == 0 || subworld == null) {
            original.call(instance, x, y, z, yaw, pitch);
        } else {
            Vec3 globalPos = ((IMixinWorld) subworld)
                .transformToGlobal(((IMixinS08PacketPlayerPosLook) storedPacket).getSubWorldPosition());
            original.call(instance, globalPos.xCoord, globalPos.yCoord, globalPos.zCoord, yaw, pitch);
        }
    }

    @Redirect(
        method = "handlePlayerPosLook",
        at = @At(value = "NEW", target = "Lnet/minecraft/network/play/client/C03PacketPlayer$C06PacketPlayerPosLook;"))
    private C06PacketPlayerPosLook getC06PacketPlayerPosLook(double x, double minY, double y, double z, float yaw,
        float pitch, boolean isOnGround, @Local S08PacketPlayerPosLook packetIn,
        @Local EntityClientPlayerMP entityclientplayermp) {
        int subWorldID = ((IMixinS08PacketPlayerPosLook) packetIn).getSubWorldBelowFeetID();
        World subworld = ((IMixinWorld) entityclientplayermp.worldObj).getSubWorld(subWorldID);
        ((IMixinEntity) entityclientplayermp).setWorldBelowFeet(subworld);
        ((IMixinEntityPlayer) entityclientplayermp).setSubworldBelowFeetId(subWorldID);

        World worldBelowFeet = ((IMixinEntity) entityclientplayermp).getWorldBelowFeet();
        double localPosX = ((IMixinS08PacketPlayerPosLook) packetIn).getSubWorldXPosition();
        double localPosY = ((IMixinS08PacketPlayerPosLook) packetIn).getSubWorldYPosition();
        double localPosZ = ((IMixinS08PacketPlayerPosLook) packetIn).getSubWorldZPosition();
        ((IMixinEntityPlayer) entityclientplayermp).setCurrentSubworldPosition(localPosX, localPosY, localPosZ);

        return PacketHandler.getC06PacketPlayerPosLook(
            x,
            minY,
            y,
            z,
            yaw,
            pitch,
            isOnGround,
            ((IMixinWorld) worldBelowFeet).getSubWorldID(),
            ((IMixinEntity) entityclientplayermp).getTractionLossTicks(),
            ((IMixinEntity) entityclientplayermp).isLosingTraction(),
            localPosX,
            localPosY - entityclientplayermp.eyeHeight,
            localPosZ);
    }

    @Inject(method = "handleSpawnPosition", at = @At(value = "TAIL"))
    public void handleSpawnPosition(S05PacketSpawnPosition packetIn, CallbackInfo ci) {
        int spawnWorld = ((IMixinS05PacketSpawnPosition) packetIn).getSpawnWorldID();
        ((IMixinEntityPlayer) this.gameController.thePlayer).setSpawnWorldID(spawnWorld);
        ((IMixinWorldInfo) this.gameController.theWorld.getWorldInfo()).setRespawnWorldID(spawnWorld);
        if (spawnWorld != 0) {
            World worldBelowFeet = ((IMixinWorld) ((IMixinWorld) this.gameController.thePlayer.worldObj)
                .getParentWorld()).getSubWorld(spawnWorld);
            if (worldBelowFeet != null) ((SubWorld) worldBelowFeet).registerEntityToDrag(this.gameController.thePlayer);
            else EventHookContainer.registerSubworldEvent(
                spawnWorld,
                subworld -> { subworld.registerEntityToDrag(this.gameController.thePlayer); });
        }
    }

}
