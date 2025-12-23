package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.client.entity;

import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition;
import net.minecraft.network.play.client.C03PacketPlayer.C05PacketPlayerLook;
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook;
import net.minecraft.util.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.network.play.PacketHandler;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;
import su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.entity.player.MixinEntityPlayer;

@Mixin(EntityClientPlayerMP.class)
public abstract class MixinEntityClientPlayerMP extends MixinEntityPlayer {

    @Shadow(remap = true)
    public boolean wasSprinting;

    @Shadow(remap = true)
    public NetHandlerPlayClient sendQueue;

    @Shadow(remap = true)
    public boolean wasSneaking;

    @Shadow(remap = true)
    public double oldPosX;

    @Shadow(remap = true)
    public double oldMinY;

    @Shadow(remap = true)
    public double oldPosY;

    @Shadow(remap = true)
    public double oldPosZ;

    @Shadow(remap = true)
    public float oldRotationYaw;

    @Shadow(remap = true)
    public float oldRotationPitch;

    @Shadow(remap = true)
    public int ticksSinceMovePacket;

    @Shadow(remap = true)
    public boolean wasOnGround;

    // TODO

    @Redirect(
        method = "onUpdate()V",
        at = @At(value = "NEW", target = "Lnet/minecraft/network/play/client/C03PacketPlayer$C05PacketPlayerLook;"))
    private C05PacketPlayerLook getC05PacketPlayerLook(float p_i45255_1_, float p_i45255_2_, boolean p_i45255_3_) {
        return PacketHandler.getC05PacketPlayerLook(
            p_i45255_1_,
            p_i45255_2_,
            p_i45255_3_,
            ((IMixinWorld) this.getWorldBelowFeet()).getSubWorldID(),
            this.getTractionLossTicks(),
            this.isLosingTraction());
    }

    @Redirect(
        method = "sendMotionUpdates()V",
        at = @At(value = "NEW", target = "Lnet/minecraft/network/play/client/C03PacketPlayer;"))
    private C03PacketPlayer redirectC03PacketPlayer(boolean isOnGround) {
        return PacketHandler.getC03PacketPlayer(
            isOnGround,
            ((IMixinWorld) this.getWorldBelowFeet()).getSubWorldID(),
            this.getTractionLossTicks(),
            this.isLosingTraction());
    }

    @Redirect(
        method = "sendMotionUpdates()V",
        at = @At(value = "NEW", target = "Lnet/minecraft/network/play/client/C03PacketPlayer$C04PacketPlayerPosition;"))
    private C04PacketPlayerPosition redirectC04PacketPlayerPosition(double x, double minY, double y, double z,
        boolean isOnGround) {
        Vec3 localPos = ((IMixinWorld) this.getWorldBelowFeet()).transformToLocal(x, minY, z);
        return PacketHandler.getC04PacketPlayerPosition(
            x,
            minY,
            y,
            z,
            isOnGround,
            ((IMixinWorld) this.getWorldBelowFeet()).getSubWorldID(),
            this.getTractionLossTicks(),
            this.isLosingTraction(),
            localPos.xCoord,
            localPos.yCoord,
            localPos.zCoord);
    }

    @Redirect(
        method = "sendMotionUpdates()V",
        at = @At(value = "NEW", target = "Lnet/minecraft/network/play/client/C03PacketPlayer$C05PacketPlayerLook;"))
    private C05PacketPlayerLook redirectC05PacketPlayerLook(float yaw, float pitch, boolean isOnGround) {
        return PacketHandler.getC05PacketPlayerLook(
            yaw,
            pitch,
            isOnGround,
            ((IMixinWorld) this.getWorldBelowFeet()).getSubWorldID(),
            this.getTractionLossTicks(),
            this.isLosingTraction());
    }

    @Redirect(
        method = "sendMotionUpdates()V",
        at = @At(value = "NEW", target = "Lnet/minecraft/network/play/client/C03PacketPlayer$C06PacketPlayerPosLook;"))
    private C06PacketPlayerPosLook redirectC06PacketPlayerPosLook(double x, double minY, double y, double z, float yaw,
        float pitch, boolean isOnGround) {
        Vec3 localPos = ((IMixinWorld) this.getWorldBelowFeet()).transformToLocal(x, minY, z);
        return PacketHandler.getC06PacketPlayerPosLook(
            x,
            minY,
            y,
            z,
            yaw,
            pitch,
            isOnGround,
            ((IMixinWorld) this.getWorldBelowFeet()).getSubWorldID(),
            this.getTractionLossTicks(),
            this.isLosingTraction(),
            localPos.xCoord,
            localPos.yCoord,
            localPos.zCoord);
    }

}
