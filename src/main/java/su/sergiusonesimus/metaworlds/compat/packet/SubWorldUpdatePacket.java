package su.sergiusonesimus.metaworlds.compat.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import su.sergiusonesimus.metaworlds.client.multiplayer.SubWorldClient;
import su.sergiusonesimus.metaworlds.world.SubWorldServer;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

public class SubWorldUpdatePacket implements IMessage {

    public int subWorldId;
    public int flags;
    public int serverTick;
    public double positionX;
    public double positionY;
    public double positionZ;
    public double rotationYaw;
    public double rotationPitch;
    public double rotationRoll;
    public double scaling;
    public double centerX;
    public double centerY;
    public double centerZ;
    public double motionX;
    public double motionY;
    public double motionZ;
    public double rotationYawFrequency;
    public double rotationPitchFrequency;
    public double rotationRollFrequency;
    public double scaleChangeRate;
    public int minX;
    public int minY;
    public int minZ;
    public int maxX;
    public int maxY;
    public int maxZ;
    public int subWorldType;

    public SubWorldUpdatePacket() {}

    public SubWorldUpdatePacket(SubWorldServer par1SubWorldServer, int updateFlags) {
        this.subWorldId = par1SubWorldServer.getSubWorldID();
        this.flags = updateFlags;
        this.serverTick = MinecraftServer.getServer()
            .getTickCounter();
        this.positionX = par1SubWorldServer.getTranslationX();
        this.positionY = par1SubWorldServer.getTranslationY();
        this.positionZ = par1SubWorldServer.getTranslationZ();
        this.rotationYaw = par1SubWorldServer.getRotationYaw();
        this.rotationPitch = par1SubWorldServer.getRotationPitch();
        this.rotationRoll = par1SubWorldServer.getRotationRoll();
        this.scaling = par1SubWorldServer.getScaling();
        this.centerX = par1SubWorldServer.getCenterX();
        this.centerY = par1SubWorldServer.getCenterY();
        this.centerZ = par1SubWorldServer.getCenterZ();
        this.motionX = par1SubWorldServer.getMotionX();
        this.motionY = par1SubWorldServer.getMotionY();
        this.motionZ = par1SubWorldServer.getMotionZ();
        this.rotationYawFrequency = par1SubWorldServer.getRotationYawSpeed();
        this.rotationPitchFrequency = par1SubWorldServer.getRotationPitchSpeed();
        this.rotationRollFrequency = par1SubWorldServer.getRotationRollSpeed();
        this.scaleChangeRate = par1SubWorldServer.getScaleChangeRate();
        this.minX = par1SubWorldServer.getMinX();
        this.minY = par1SubWorldServer.getMinY();
        this.minZ = par1SubWorldServer.getMinZ();
        this.maxX = par1SubWorldServer.getMaxX();
        this.maxY = par1SubWorldServer.getMaxY();
        this.maxZ = par1SubWorldServer.getMaxZ();
        this.subWorldType = par1SubWorldServer.getSubWorldType();
    }

    @Override
    public void fromBytes(ByteBuf par1DataInput) {
        this.subWorldId = par1DataInput.readUnsignedShort();
        this.flags = par1DataInput.readUnsignedByte();
        this.serverTick = par1DataInput.readInt();
        if ((this.flags & 1) != 0) {
            this.positionX = (double) par1DataInput.readFloat();
            this.positionY = (double) par1DataInput.readFloat();
            this.positionZ = (double) par1DataInput.readFloat();
            this.rotationYaw = (double) par1DataInput.readFloat();
            this.rotationPitch = (double) par1DataInput.readFloat();
            this.rotationRoll = (double) par1DataInput.readFloat();
            this.scaling = (double) par1DataInput.readFloat();
        }

        if ((this.flags & 2) != 0) {
            this.motionX = (double) par1DataInput.readFloat();
            this.motionY = (double) par1DataInput.readFloat();
            this.motionZ = (double) par1DataInput.readFloat();
            this.rotationYawFrequency = (double) par1DataInput.readFloat();
            this.rotationPitchFrequency = (double) par1DataInput.readFloat();
            this.rotationRollFrequency = (double) par1DataInput.readFloat();
            this.scaleChangeRate = (double) par1DataInput.readFloat();
        }

        if ((this.flags & 4) != 0) {
            this.centerX = (double) par1DataInput.readFloat();
            this.centerY = (double) par1DataInput.readFloat();
            this.centerZ = (double) par1DataInput.readFloat();
        }

        if ((this.flags & 8) != 0) {
            this.minX = par1DataInput.readInt();
            this.minY = par1DataInput.readInt();
            this.minZ = par1DataInput.readInt();
            this.maxX = par1DataInput.readInt();
            this.maxY = par1DataInput.readInt();
            this.maxZ = par1DataInput.readInt();
        }

        if ((this.flags & 16) != 0) {
            this.subWorldType = par1DataInput.readInt();
        }
    }

    @Override
    public void toBytes(ByteBuf par1DataOutput) {
        par1DataOutput.writeShort(this.subWorldId);
        par1DataOutput.writeByte(this.flags);
        par1DataOutput.writeInt(this.serverTick);
        if ((this.flags & 1) != 0) {
            par1DataOutput.writeFloat((float) this.positionX);
            par1DataOutput.writeFloat((float) this.positionY);
            par1DataOutput.writeFloat((float) this.positionZ);
            par1DataOutput.writeFloat((float) this.rotationYaw);
            par1DataOutput.writeFloat((float) this.rotationPitch);
            par1DataOutput.writeFloat((float) this.rotationRoll);
            par1DataOutput.writeFloat((float) this.scaling);
        }

        if ((this.flags & 2) != 0) {
            par1DataOutput.writeFloat((float) this.motionX);
            par1DataOutput.writeFloat((float) this.motionY);
            par1DataOutput.writeFloat((float) this.motionZ);
            par1DataOutput.writeFloat((float) this.rotationYawFrequency);
            par1DataOutput.writeFloat((float) this.rotationPitchFrequency);
            par1DataOutput.writeFloat((float) this.rotationRollFrequency);
            par1DataOutput.writeFloat((float) this.scaleChangeRate);
        }

        if ((this.flags & 4) != 0) {
            par1DataOutput.writeFloat((float) this.centerX);
            par1DataOutput.writeFloat((float) this.centerY);
            par1DataOutput.writeFloat((float) this.centerZ);
        }

        if ((this.flags & 8) != 0) {
            par1DataOutput.writeInt(this.minX);
            par1DataOutput.writeInt(this.minY);
            par1DataOutput.writeInt(this.minZ);
            par1DataOutput.writeInt(this.maxX);
            par1DataOutput.writeInt(this.maxY);
            par1DataOutput.writeInt(this.maxZ);
        }

        if ((this.flags & 16) != 0) {
            par1DataOutput.writeInt(this.subWorldType);
        }
    }

    public boolean containsSameEntityIDAs(SubWorldUpdatePacket par1Packet) {
        return par1Packet.subWorldId == this.subWorldId;
    }

    @SideOnly(Side.CLIENT)
    public void executeOnTick() {
        EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
        World targetWorld = ((IMixinWorld) player.worldObj).getSubWorld(this.subWorldId);
        if (targetWorld == null) {
            ;
        }

        if (((IMixinWorld) targetWorld).isSubWorld()) {
            SubWorldClient targetSubWorld = (SubWorldClient) targetWorld;
            float newTickDiff = (float) this.serverTick - (float) targetSubWorld.localTickCounter;
            if (targetSubWorld.lastServerTickReceived == -1) {
                targetSubWorld.serverTickDiff = newTickDiff;
            } else if (targetSubWorld.localTickCounter < 8) {
                targetSubWorld.serverTickDiff = (float) MathHelper.ceiling_float_int(
                    (targetSubWorld.serverTickDiff * (float) targetSubWorld.localTickCounter + newTickDiff)
                        / (float) (targetSubWorld.localTickCounter + 1));
            } else {
                targetSubWorld.serverTickDiff = (float) MathHelper
                    .ceiling_float_int((targetSubWorld.serverTickDiff * 7.0F + newTickDiff) / 8.0F);
            }

            targetSubWorld.lastServerTickReceived = this.serverTick;
            if ((this.flags & 4) != 0) {
                targetSubWorld.setCenter(this.centerX, this.centerY, this.centerZ);
            }

            if ((this.flags & 1) != 0) {
                targetSubWorld.UpdatePositionAndRotation(
                    this.positionX,
                    this.positionY,
                    this.positionZ,
                    this.rotationYaw,
                    this.rotationPitch,
                    this.rotationRoll,
                    this.scaling);
            }

            double oldScaleChangeRate = targetSubWorld.getScaleChangeRate();
            double oldRotationYawFreq = targetSubWorld.getRotationYawSpeed();
            double oldRotationPitchFreq = targetSubWorld.getRotationPitchSpeed();
            double oldRotationRollFreq = targetSubWorld.getRotationRollSpeed();
            double oldMotionX = targetSubWorld.getMotionX();
            double oldMotionY = targetSubWorld.getMotionY();
            double oldMotionZ = targetSubWorld.getMotionZ();
            targetSubWorld.setScaleChangeRate((this.scaleChangeRate + oldScaleChangeRate) * 0.5D);
            targetSubWorld.setRotationYawSpeed((this.rotationYawFrequency + oldRotationYawFreq) * 0.5D);
            targetSubWorld.setRotationPitchSpeed((this.rotationPitchFrequency + oldRotationPitchFreq) * 0.5D);
            targetSubWorld.setRotationRollSpeed((this.rotationRollFrequency + oldRotationRollFreq) * 0.5D);
            targetSubWorld.setMotion(
                (this.motionX + oldMotionX) * 0.5D,
                (this.motionY + oldMotionY) * 0.5D,
                (this.motionZ + oldMotionZ) * 0.5D);
            targetSubWorld.tickPosition((int) (targetSubWorld.serverTickDiff - newTickDiff) - 1);
            targetSubWorld.setScaleChangeRate(this.scaleChangeRate);
            targetSubWorld.setRotationYawSpeed(this.rotationYawFrequency);
            targetSubWorld.setRotationPitchSpeed(this.rotationPitchFrequency);
            targetSubWorld.setRotationRollSpeed(this.rotationRollFrequency);
            targetSubWorld.setMotion(this.motionX, this.motionY, this.motionZ);
            if ((this.flags & 8) != 0) {
                targetSubWorld.setBoundaries(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
            }

            if ((this.flags & 16) != 0) {
                targetSubWorld.setSubWorldType(this.subWorldType);
            }
        }
    }

    public static class Handler implements IMessageHandler<SubWorldUpdatePacket, IMessage> {

        @Override
        public IMessage onMessage(SubWorldUpdatePacket message, MessageContext ctx) {
            if (!ctx.side.isServer()) {
                EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
                World targetWorld = ((IMixinWorld) player.worldObj).getSubWorld(message.subWorldId);
                if (targetWorld == null) {
                    return null;
                }

                if (((IMixinWorld) targetWorld).isSubWorld()) {
                    SubWorldClient targetSubWorld = (SubWorldClient) targetWorld;
                    if (targetSubWorld.lastServerTickReceived < message.serverTick
                        && (targetSubWorld.getUpdatePacketToHandle() == null
                            || targetSubWorld.getUpdatePacketToHandle().serverTick <= message.serverTick)) {
                        targetSubWorld.setUpdatePacketToHandle(message);
                    }
                }
            }
            return null;
        }
    }
}
