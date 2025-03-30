package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.client.entity;

import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.client.C03PacketPlayer.C05PacketPlayerLook;
import net.minecraft.network.play.client.C0BPacketEntityAction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.client.entity.IMixinEntityClientPlayerMP;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.network.play.PacketHandler;
import su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.entity.player.MixinEntityPlayer;

@Mixin(EntityClientPlayerMP.class)
public abstract class MixinEntityClientPlayerMP extends MixinEntityPlayer implements IMixinEntityClientPlayerMP {

    // These variables only used on world load to ensure player spawns correctly
    private double subworldSpawnX;

    private double subworldSpawnY;

    private double subworldSpawnZ;

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

    /**
     * Send updated motion and position information to the server
     */
    @Overwrite
    public void sendMotionUpdates() {
        boolean flag = this.isSprinting();

        if (flag != this.wasSprinting) {
            if (flag) {
                this.sendQueue.addToSendQueue(new C0BPacketEntityAction((EntityClientPlayerMP) (Object) this, 4));
            } else {
                this.sendQueue.addToSendQueue(new C0BPacketEntityAction((EntityClientPlayerMP) (Object) this, 5));
            }

            this.wasSprinting = flag;
        }

        boolean flag1 = this.isSneaking();

        if (flag1 != this.wasSneaking) {
            if (flag1) {
                this.sendQueue.addToSendQueue(new C0BPacketEntityAction((EntityClientPlayerMP) (Object) this, 1));
            } else {
                this.sendQueue.addToSendQueue(new C0BPacketEntityAction((EntityClientPlayerMP) (Object) this, 2));
            }

            this.wasSneaking = flag1;
        }

        double d0 = this.posX - this.oldPosX;
        double d1 = this.boundingBox.minY - this.oldMinY;
        double d2 = this.posZ - this.oldPosZ;
        double d3 = (double) (this.rotationYaw - this.oldRotationYaw);
        double d4 = (double) (this.rotationPitch - this.oldRotationPitch);
        boolean flag2 = d0 * d0 + d1 * d1 + d2 * d2 > 9.0E-4D || this.ticksSinceMovePacket >= 20;
        boolean flag3 = d3 != 0.0D || d4 != 0.0D;

        if (this.ridingEntity != null) {
            this.sendQueue.addToSendQueue(
                PacketHandler.getC06PacketPlayerPosLook(
                    this.motionX,
                    -999.0D,
                    -999.0D,
                    this.motionZ,
                    this.rotationYaw,
                    this.rotationPitch,
                    this.onGround,
                    ((IMixinWorld) this.getWorldBelowFeet()).getSubWorldID(),
                    this.getTractionLossTicks(),
                    this.isLosingTraction()));
            flag2 = false;
        } else if (flag2 && flag3) {
            this.sendQueue.addToSendQueue(
                PacketHandler.getC06PacketPlayerPosLook(
                    this.posX,
                    this.boundingBox.minY,
                    this.posY,
                    this.posZ,
                    this.rotationYaw,
                    this.rotationPitch,
                    this.onGround,
                    ((IMixinWorld) this.getWorldBelowFeet()).getSubWorldID(),
                    this.getTractionLossTicks(),
                    this.isLosingTraction()));
        } else if (flag2) {
            this.sendQueue.addToSendQueue(
                PacketHandler.getC04PacketPlayerPosition(
                    this.posX,
                    this.boundingBox.minY,
                    this.posY,
                    this.posZ,
                    this.onGround,
                    ((IMixinWorld) this.getWorldBelowFeet()).getSubWorldID(),
                    this.getTractionLossTicks(),
                    this.isLosingTraction()));
        } else if (flag3) {
            this.sendQueue.addToSendQueue(
                PacketHandler.getC05PacketPlayerLook(
                    this.rotationYaw,
                    this.rotationPitch,
                    this.onGround,
                    ((IMixinWorld) this.getWorldBelowFeet()).getSubWorldID(),
                    this.getTractionLossTicks(),
                    this.isLosingTraction()));
        } else {
            this.sendQueue.addToSendQueue(
                PacketHandler.getC03PacketPlayer(
                    this.onGround,
                    ((IMixinWorld) this.getWorldBelowFeet()).getSubWorldID(),
                    this.getTractionLossTicks(),
                    this.isLosingTraction()));
        }

        ++this.ticksSinceMovePacket;
        this.wasOnGround = this.onGround;

        if (flag2) {
            this.oldPosX = this.posX;
            this.oldMinY = this.boundingBox.minY;
            this.oldPosY = this.posY;
            this.oldPosZ = this.posZ;
            this.ticksSinceMovePacket = 0;
        }

        if (flag3) {
            this.oldRotationYaw = this.rotationYaw;
            this.oldRotationPitch = this.rotationPitch;
        }
    }

    public double getSubworldSpawnX() {
        return this.subworldSpawnX;
    }

    public double getSubworldSpawnY() {
        return this.subworldSpawnY;
    }

    public double getSubworldSpawnZ() {
        return this.subworldSpawnZ;
    }

    public void setSubworldSpawnX(double x) {
        this.subworldSpawnX = x;
    }

    public void setSubworldSpawnY(double y) {
        this.subworldSpawnY = y;
    }

    public void setSubworldSpawnZ(double z) {
        this.subworldSpawnZ = z;
    }

}
