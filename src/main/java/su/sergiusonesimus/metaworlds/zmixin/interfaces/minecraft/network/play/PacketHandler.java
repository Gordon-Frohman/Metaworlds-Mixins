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
    public static C03PacketPlayer getC03PacketPlayer(boolean p_i45256_1_, int worldBelowFeetId, byte parTractionLoss,
        boolean parLosingTraction) {
        C03PacketPlayer c03 = new C03PacketPlayer(p_i45256_1_);
        return (C03PacketPlayer) ((IMixinC03PacketPlayer) (Object) c03).setSubWorldBelowFeetId(worldBelowFeetId)
            .setTractionLoss(parTractionLoss)
            .setLosingTraction(parLosingTraction);
    }

    @SideOnly(Side.CLIENT)
    public static C04PacketPlayerPosition getC04PacketPlayerPosition(double p_i45253_1_, double p_i45253_3_,
        double p_i45253_5_, double p_i45253_7_, boolean p_i45253_9_, int worldBelowFeetId, byte parTractionLoss,
        boolean parLosingTraction) {
        C04PacketPlayerPosition c04 = new C04PacketPlayerPosition(
            p_i45253_1_,
            p_i45253_3_,
            p_i45253_5_,
            p_i45253_7_,
            p_i45253_9_);
        return (C04PacketPlayerPosition) ((IMixinC03PacketPlayer) (Object) c04).setSubWorldBelowFeetId(worldBelowFeetId)
            .setTractionLoss(parTractionLoss)
            .setLosingTraction(parLosingTraction);
    }

    @SideOnly(Side.CLIENT)
    public static C05PacketPlayerLook getC05PacketPlayerLook(float p_i45255_1_, float p_i45255_2_, boolean p_i45255_3_,
        int worldBelowFeetId, byte parTractionLoss, boolean parLosingTraction) {
        C05PacketPlayerLook c05 = new C05PacketPlayerLook(p_i45255_1_, p_i45255_2_, p_i45255_3_);
        return (C05PacketPlayerLook) ((IMixinC03PacketPlayer) (Object) c05).setSubWorldBelowFeetId(worldBelowFeetId)
            .setTractionLoss(parTractionLoss)
            .setLosingTraction(parLosingTraction);
    }

    @SideOnly(Side.CLIENT)
    public static C06PacketPlayerPosLook getC06PacketPlayerPosLook(double p_i45254_1_, double p_i45254_3_,
        double p_i45254_5_, double p_i45254_7_, float p_i45254_9_, float p_i45254_10_, boolean p_i45254_11_,
        int worldBelowFeetId, byte parTractionLoss, boolean parLosingTraction) {
        C06PacketPlayerPosLook c06 = new C06PacketPlayerPosLook(
            p_i45254_1_,
            p_i45254_3_,
            p_i45254_5_,
            p_i45254_7_,
            p_i45254_9_,
            p_i45254_10_,
            p_i45254_11_);
        if (worldBelowFeetId == 0) {
            int x = 0;
        }
        return (C06PacketPlayerPosLook) ((IMixinC03PacketPlayer) (Object) c06).setSubWorldBelowFeetId(worldBelowFeetId)
            .setTractionLoss(parTractionLoss)
            .setLosingTraction(parLosingTraction);
    }

    public static S08PacketPlayerPosLook getS08PacketPlayerPosLook(double p_i45164_1_, double p_i45164_3_,
        double p_i45164_5_, float p_i45164_7_, float p_i45164_8_, boolean p_i45164_9_, int subWorldBelowFeetID) {
        return ((IMixinS08PacketPlayerPosLook) new S08PacketPlayerPosLook(
            p_i45164_1_,
            p_i45164_3_,
            p_i45164_5_,
            p_i45164_7_,
            p_i45164_8_,
            p_i45164_9_)).setSubWorldBelowFeetID(subWorldBelowFeetID);
    }

    public static S14PacketEntity getS14PacketEntity(int p_i45206_1_, int parSubWorldBelowFeetId, byte parTractionLoss,
        boolean parLosingTraction) {
        return (S14PacketEntity) (Object) ((IMixinS14PacketEntity) new S14PacketEntity(p_i45206_1_))
            .setSubWorldBelowFeetID(parSubWorldBelowFeetId)
            .setTractionLoss(parTractionLoss)
            .setLosingTraction(parLosingTraction);
    }

    public static S15PacketEntityRelMove getS15PacketEntityRelMove(int p_i45203_1_, int worldBelowFeetId,
        byte parTractionLoss, boolean parLosingTraction, byte p_i45203_2_, byte p_i45203_3_, byte p_i45203_4_,
        byte subWorldFlag, byte xDiffOnSubWorld, byte yDiffOnSubWorld, byte zDiffOnSubWorld) {
        S15PacketEntityRelMove s15 = new S15PacketEntityRelMove(p_i45203_1_, p_i45203_2_, p_i45203_3_, p_i45203_4_);
        return (S15PacketEntityRelMove) ((IMixinS14PacketEntity) (Object) s15).setSubWorldBelowFeetID(worldBelowFeetId)
            .setTractionLoss(parTractionLoss)
            .setLosingTraction(parLosingTraction)
            .setSendSubWorldPosFlag(subWorldFlag)
            .setXPosDiffOnSubWorld(xDiffOnSubWorld)
            .setYPosDiffOnSubWorld(yDiffOnSubWorld)
            .setZPosDiffOnSubWorld(zDiffOnSubWorld);
    }

    public static S16PacketEntityLook getS16PacketEntityLook(int p_i45205_1_, int worldBelowFeetId,
        byte parTractionLoss, boolean parLosingTraction, byte p_i45205_2_, byte p_i45205_3_) {
        S16PacketEntityLook s16 = new S16PacketEntityLook(p_i45205_1_, p_i45205_2_, p_i45205_3_);
        return (S16PacketEntityLook) ((IMixinS14PacketEntity) (Object) s16).setSubWorldBelowFeetID(worldBelowFeetId)
            .setTractionLoss(parTractionLoss)
            .setLosingTraction(parLosingTraction);
    }

    public static S17PacketEntityLookMove getS17PacketEntityLookMove(int p_i45204_1_, int worldBelowFeetId,
        byte parTractionLoss, boolean parLosingTraction, byte p_i45204_2_, byte p_i45204_3_, byte p_i45204_4_,
        byte p_i45204_5_, byte p_i45204_6_, byte subWorldFlag, byte xDiffOnSubWorld, byte yDiffOnSubWorld,
        byte zDiffOnSubWorld) {
        S17PacketEntityLookMove s17 = new S17PacketEntityLookMove(
            p_i45204_1_,
            p_i45204_2_,
            p_i45204_3_,
            p_i45204_4_,
            p_i45204_5_,
            p_i45204_6_);
        return (S17PacketEntityLookMove) ((IMixinS14PacketEntity) (Object) s17).setSubWorldBelowFeetID(worldBelowFeetId)
            .setTractionLoss(parTractionLoss)
            .setLosingTraction(parLosingTraction)
            .setSendSubWorldPosFlag(subWorldFlag)
            .setXPosDiffOnSubWorld(xDiffOnSubWorld)
            .setYPosDiffOnSubWorld(yDiffOnSubWorld)
            .setZPosDiffOnSubWorld(zDiffOnSubWorld);
    }

    public static S18PacketEntityTeleport getS18PacketEntityTeleport(int p_i45234_1_, int parSubWorldId,
        byte parTractionLoss, boolean parLosingTraction, int p_i45234_2_, int p_i45234_3_, int p_i45234_4_,
        byte p_i45234_5_, byte p_i45234_6_, byte subWorldFlag, int xOnSubWorld, int yOnSubWorld, int zOnSubWorld) {
        S18PacketEntityTeleport s18 = new S18PacketEntityTeleport(
            p_i45234_1_,
            p_i45234_2_,
            p_i45234_3_,
            p_i45234_4_,
            p_i45234_5_,
            p_i45234_6_);

        return (S18PacketEntityTeleport) ((IMixinS18PacketEntityTeleport) (Object) s18).setSubWorldId(parSubWorldId)
            .setTractionLoss(parTractionLoss)
            .setLosingTraction(parLosingTraction)
            .setSendSubWorldPosFlag(subWorldFlag)
            .setXPosOnSubWorld(xOnSubWorld)
            .setYPosOnSubWorld(yOnSubWorld)
            .setZPosOnSubWorld(zOnSubWorld);
    }

}
