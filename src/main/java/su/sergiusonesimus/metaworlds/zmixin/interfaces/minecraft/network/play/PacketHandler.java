package su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.network.play;

import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition;
import net.minecraft.network.play.client.C03PacketPlayer.C05PacketPlayerLook;
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S14PacketEntity;
import net.minecraft.network.play.server.S14PacketEntity.S15PacketEntityRelMove;
import net.minecraft.network.play.server.S14PacketEntity.S16PacketEntityLook;
import net.minecraft.network.play.server.S14PacketEntity.S17PacketEntityLookMove;
import net.minecraft.network.play.server.S18PacketEntityTeleport;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.network.play.client.IMixinC03PacketPlayer;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.network.play.server.IMixinS08PacketPlayerPosLook;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.network.play.server.IMixinS14PacketEntity;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.network.play.server.IMixinS18PacketEntityTeleport;

public class PacketHandler {

    @SideOnly(Side.CLIENT)
    public static C03PacketPlayer getC03PacketPlayer(boolean isOnGround, int worldBelowFeetId, byte tractionLoss,
        boolean isLosingTraction) {
        C03PacketPlayer c03 = new C03PacketPlayer(isOnGround);
        return (C03PacketPlayer) ((IMixinC03PacketPlayer) (Object) c03).setSubWorldBelowFeetId(worldBelowFeetId)
            .setTractionLoss(tractionLoss)
            .setLosingTraction(isLosingTraction);
    }

    @SideOnly(Side.CLIENT)
    public static C04PacketPlayerPosition getC04PacketPlayerPosition(double x, double y, double headY, double z,
        boolean p_i45253_9_, int worldBelowFeetId, byte tractionLoss, boolean isLosingTraction, double subworldPosX,
        double subworldPosY, double subworldPosZ) {
        C04PacketPlayerPosition c04 = new C04PacketPlayerPosition(x, y, headY, z, p_i45253_9_);
        return (C04PacketPlayerPosition) ((IMixinC03PacketPlayer) (Object) c04).setSubWorldBelowFeetId(worldBelowFeetId)
            .setTractionLoss(tractionLoss)
            .setLosingTraction(isLosingTraction)
            .setSubWorldXPosition(subworldPosX)
            .setSubWorldYPosition(subworldPosY)
            .setSubWorldZPosition(subworldPosZ);
    }

    @SideOnly(Side.CLIENT)
    public static C05PacketPlayerLook getC05PacketPlayerLook(float yaw, float pitch, boolean isOnGround,
        int worldBelowFeetId, byte tractionLoss, boolean isLosingTraction) {
        C05PacketPlayerLook c05 = new C05PacketPlayerLook(yaw, pitch, isOnGround);
        return (C05PacketPlayerLook) ((IMixinC03PacketPlayer) (Object) c05).setSubWorldBelowFeetId(worldBelowFeetId)
            .setTractionLoss(tractionLoss)
            .setLosingTraction(isLosingTraction);
    }

    @SideOnly(Side.CLIENT)
    public static C06PacketPlayerPosLook getC06PacketPlayerPosLook(double x, double y, double headY, double z,
        float yaw, float pitch, boolean isOnGround, int worldBelowFeetId, byte tractionLoss, boolean isLosingTraction,
        double subworldPosX, double subworldPosY, double subworldPosZ) {
        C06PacketPlayerPosLook c06 = new C06PacketPlayerPosLook(x, y, headY, z, yaw, pitch, isOnGround);
        return (C06PacketPlayerPosLook) ((IMixinC03PacketPlayer) (Object) c06).setSubWorldBelowFeetId(worldBelowFeetId)
            .setTractionLoss(tractionLoss)
            .setLosingTraction(isLosingTraction)
            .setSubWorldXPosition(subworldPosX)
            .setSubWorldYPosition(subworldPosY)
            .setSubWorldZPosition(subworldPosZ);
    }

    public static S08PacketPlayerPosLook getS08PacketPlayerPosLook(double x, double y, double z, float yaw, float pitch,
        boolean isOnGround, int subWorldBelowFeetID, int subWorldBelowFeetType, double subworldPosX,
        double subworldPosY, double subworldPosZ) {
        return (S08PacketPlayerPosLook) ((IMixinS08PacketPlayerPosLook) new S08PacketPlayerPosLook(
            x,
            y,
            z,
            yaw,
            pitch,
            isOnGround)).setSubWorldBelowFeetID(subWorldBelowFeetID)
                .setSubWorldBelowFeetType(subWorldBelowFeetType)
                .setSubWorldXPosition(subworldPosX)
                .setSubWorldYPosition(subworldPosY)
                .setSubWorldZPosition(subworldPosZ);
    }

    public static S14PacketEntity getS14PacketEntity(int entityID, int subWorldBelowFeetId, byte tractionLoss,
        boolean isLosingTraction) {
        return (S14PacketEntity) (Object) ((IMixinS14PacketEntity) new S14PacketEntity(entityID))
            .setSubWorldBelowFeetID(subWorldBelowFeetId)
            .setTractionLoss(tractionLoss)
            .setLosingTraction(isLosingTraction);
    }

    public static S15PacketEntityRelMove getS15PacketEntityRelMove(int entityID, int worldBelowFeetId,
        byte tractionLoss, boolean isLosingTraction, byte relativeX, byte relativeY, byte relativeZ, byte subWorldFlag,
        byte xDiffOnSubWorld, byte yDiffOnSubWorld, byte zDiffOnSubWorld) {
        S15PacketEntityRelMove s15 = new S15PacketEntityRelMove(entityID, relativeX, relativeY, relativeZ);
        return (S15PacketEntityRelMove) ((IMixinS14PacketEntity) (Object) s15).setSubWorldBelowFeetID(worldBelowFeetId)
            .setTractionLoss(tractionLoss)
            .setLosingTraction(isLosingTraction)
            .setSendSubWorldPosFlag(subWorldFlag)
            .setXPosDiffOnSubWorld(xDiffOnSubWorld)
            .setYPosDiffOnSubWorld(yDiffOnSubWorld)
            .setZPosDiffOnSubWorld(zDiffOnSubWorld);
    }

    public static S16PacketEntityLook getS16PacketEntityLook(int entityID, int worldBelowFeetId, byte tractionLoss,
        boolean isLosingTraction, byte absoluteYaw, byte absolutePitch) {
        S16PacketEntityLook s16 = new S16PacketEntityLook(entityID, absoluteYaw, absolutePitch);
        return (S16PacketEntityLook) ((IMixinS14PacketEntity) (Object) s16).setSubWorldBelowFeetID(worldBelowFeetId)
            .setTractionLoss(tractionLoss)
            .setLosingTraction(isLosingTraction);
    }

    public static S17PacketEntityLookMove getS17PacketEntityLookMove(int entityID, int worldBelowFeetId,
        byte tractionLoss, boolean isLosingTraction, byte relativeX, byte relativeY, byte relativeZ, byte absoluteYaw,
        byte absolutePitch, byte subWorldFlag, byte xDiffOnSubWorld, byte yDiffOnSubWorld, byte zDiffOnSubWorld) {
        S17PacketEntityLookMove s17 = new S17PacketEntityLookMove(
            entityID,
            relativeX,
            relativeY,
            relativeZ,
            absoluteYaw,
            absolutePitch);
        return (S17PacketEntityLookMove) ((IMixinS14PacketEntity) (Object) s17).setSubWorldBelowFeetID(worldBelowFeetId)
            .setTractionLoss(tractionLoss)
            .setLosingTraction(isLosingTraction)
            .setSendSubWorldPosFlag(subWorldFlag)
            .setXPosDiffOnSubWorld(xDiffOnSubWorld)
            .setYPosDiffOnSubWorld(yDiffOnSubWorld)
            .setZPosDiffOnSubWorld(zDiffOnSubWorld);
    }

    public static S18PacketEntityTeleport getS18PacketEntityTeleport(int entityID, int subWorldId, byte tractionLoss,
        boolean isLosingTraction, int x, int y, int z, byte absoluteYaw, byte absolutePitch, byte subWorldFlag,
        int xOnSubWorld, int yOnSubWorld, int zOnSubWorld) {
        S18PacketEntityTeleport s18 = new S18PacketEntityTeleport(entityID, x, y, z, absoluteYaw, absolutePitch);

        return (S18PacketEntityTeleport) ((IMixinS18PacketEntityTeleport) (Object) s18).setSubWorldId(subWorldId)
            .setTractionLoss(tractionLoss)
            .setLosingTraction(isLosingTraction)
            .setSendSubWorldPosFlag(subWorldFlag)
            .setXPosOnSubWorld(xOnSubWorld)
            .setYPosOnSubWorld(yOnSubWorld)
            .setZPosOnSubWorld(zOnSubWorld);
    }

}
