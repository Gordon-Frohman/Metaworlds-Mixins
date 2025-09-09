package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.entity;

import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S19PacketEntityHeadLook;
import net.minecraft.network.play.server.S1BPacketEntityAttach;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.MapData;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.IMixinEntity;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.network.play.PacketHandler;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

@Mixin(EntityTrackerEntry.class)
public abstract class MixinEntityTrackerEntry {

    /** specific for subworlds */
    public int lastScaledXPositionOnSubWorld;
    public int lastScaledYPositionOnSubWorld;
    public int lastScaledZPositionOnSubWorld;

    @Shadow(remap = true)
    private boolean ridingEntity;

    @Shadow(remap = true)
    public boolean playerEntitiesUpdated;

    @Shadow(remap = true)
    private double posX;

    @Shadow(remap = true)
    private double posY;

    @Shadow(remap = true)
    private double posZ;

    @Shadow(remap = true)
    private boolean isDataInitialized;

    @Shadow(remap = true)
    public Entity myEntity;

    @Shadow(remap = true)
    private Entity field_85178_v;

    @Shadow(remap = true)
    public int ticks;

    @Shadow(remap = true)
    public int updateFrequency;

    @Shadow(remap = true)
    private int ticksSinceLastForcedTeleport;

    @Shadow(remap = true)
    public int lastScaledXPosition;

    @Shadow(remap = true)
    public int lastScaledYPosition;

    @Shadow(remap = true)
    public int lastScaledZPosition;

    @Shadow(remap = true)
    public int lastYaw;

    @Shadow(remap = true)
    public int lastPitch;

    @Shadow(remap = true)
    public int lastHeadMotion;

    @Shadow(remap = true)
    private boolean sendVelocityUpdates;

    @Shadow(remap = true)
    public double motionX;

    @Shadow(remap = true)
    public double motionY;

    @Shadow(remap = true)
    public double motionZ;

    // TODO

    @Shadow(remap = true)
    public abstract void func_151261_b(Packet p_151261_1_);

    @Shadow(remap = true)
    protected abstract void sendMetadataToAllAssociatedPlayers();

    @Shadow(remap = true)
    public abstract void func_151259_a(Packet p_151259_1_);

    @SuppressWarnings("rawtypes")
    @Shadow(remap = true)
    public abstract void sendEventsToPlayers(List p_73125_1_);

    // Creating this variables outside of Overwrite not to mess with other mods' Injects
    int subWorldScaledX;
    int subWorldScaledY;
    int subWorldScaledZ;

    int subWorldScaledXDiff;
    int subWorldScaledYDiff;
    int subWorldScaledZDiff;
    boolean sendSubWorldPos;
    boolean subWorldFlag;
    boolean subWorldTeleportRequired;

    /**
     * also sends velocity, rotation, and riding info.
     */
    @SuppressWarnings("rawtypes")
    @Overwrite
    public void sendLocationToAllClients(List p_73122_1_) {
        this.playerEntitiesUpdated = false;

        if (!this.isDataInitialized || this.myEntity.getDistanceSq(this.posX, this.posY, this.posZ) > 16.0D) {
            this.posX = this.myEntity.posX;
            this.posY = this.myEntity.posY;
            this.posZ = this.myEntity.posZ;
            this.isDataInitialized = true;
            this.playerEntitiesUpdated = true;
            this.sendEventsToPlayers(p_73122_1_);
        }

        if (this.field_85178_v != this.myEntity.ridingEntity
            || this.myEntity.ridingEntity != null && this.ticks % 60 == 0) {
            this.field_85178_v = this.myEntity.ridingEntity;
            this.func_151259_a(new S1BPacketEntityAttach(0, this.myEntity, this.myEntity.ridingEntity));
        }

        if (this.myEntity instanceof EntityItemFrame && this.ticks % 10 == 0) {
            EntityItemFrame entityitemframe = (EntityItemFrame) this.myEntity;
            ItemStack itemstack = entityitemframe.getDisplayedItem();

            if (itemstack != null && itemstack.getItem() instanceof ItemMap) {
                MapData mapdata = Items.filled_map.getMapData(itemstack, this.myEntity.worldObj);
                Iterator iterator = p_73122_1_.iterator();

                while (iterator.hasNext()) {
                    EntityPlayer entityplayer = (EntityPlayer) iterator.next();
                    EntityPlayerMP entityplayermp = (EntityPlayerMP) entityplayer;
                    mapdata.updateVisiblePlayers(entityplayermp, itemstack);
                    Packet packet = Items.filled_map.func_150911_c(itemstack, this.myEntity.worldObj, entityplayermp);

                    if (packet != null) {
                        entityplayermp.playerNetServerHandler.sendPacket(packet);
                    }
                }
            }

            this.sendMetadataToAllAssociatedPlayers();
        } else if (this.ticks % this.updateFrequency == 0 || this.myEntity.isAirBorne
            || this.myEntity.getDataWatcher()
                .hasChanges()) {
                    int i;
                    int j;

                    if (this.myEntity.ridingEntity == null) {
                        subWorldScaledX = 0;
                        subWorldScaledY = 0;
                        subWorldScaledZ = 0;

                        subWorldScaledXDiff = 0;
                        subWorldScaledYDiff = 0;
                        subWorldScaledZDiff = 0;
                        sendSubWorldPos = false;
                        subWorldFlag = false;
                        subWorldTeleportRequired = false;

                        if (((IMixinWorld) ((IMixinEntity) this.myEntity).getWorldBelowFeet()).getSubWorldID()
                            != ((IMixinWorld) this.myEntity.worldObj).getSubWorldID()) {
                            Vec3 localPos = ((IMixinWorld) ((IMixinEntity) this.myEntity).getWorldBelowFeet())
                                .transformOtherToLocal(this.myEntity.worldObj, this.myEntity);
                            subWorldScaledX = this.myEntity.myEntitySize.multiplyBy32AndRound(localPos.xCoord);
                            subWorldScaledY = MathHelper.floor_double(localPos.yCoord * 32.0D);
                            subWorldScaledZ = this.myEntity.myEntitySize.multiplyBy32AndRound(localPos.zCoord);
                            subWorldScaledXDiff = subWorldScaledX - this.lastScaledXPositionOnSubWorld;
                            subWorldScaledYDiff = subWorldScaledY - this.lastScaledYPositionOnSubWorld;
                            subWorldScaledZDiff = subWorldScaledZ - this.lastScaledZPositionOnSubWorld;
                            sendSubWorldPos = true;
                            subWorldFlag = Math.abs(subWorldScaledXDiff) >= 2 || Math.abs(subWorldScaledYDiff) >= 2
                                || Math.abs(subWorldScaledZDiff) >= 2;
                            subWorldTeleportRequired = !(subWorldScaledXDiff >= -128 && subWorldScaledXDiff < 128
                                && subWorldScaledYDiff >= -128
                                && subWorldScaledYDiff < 128
                                && subWorldScaledZDiff >= -128
                                && subWorldScaledZDiff < 128);
                        }

                        ++this.ticksSinceLastForcedTeleport;
                        i = this.myEntity.myEntitySize.multiplyBy32AndRound(this.myEntity.posX);
                        j = MathHelper.floor_double(this.myEntity.posY * 32.0D);
                        int k = this.myEntity.myEntitySize.multiplyBy32AndRound(this.myEntity.posZ);
                        int l = MathHelper.floor_float(this.myEntity.rotationYaw * 256.0F / 360.0F);
                        int i1 = MathHelper.floor_float(this.myEntity.rotationPitch * 256.0F / 360.0F);
                        int j1 = i - this.lastScaledXPosition;
                        int k1 = j - this.lastScaledYPosition;
                        int l1 = k - this.lastScaledZPosition;
                        Object object = null;
                        boolean flag = subWorldFlag || Math.abs(j1) >= 4
                            || Math.abs(k1) >= 4
                            || Math.abs(l1) >= 4
                            || this.ticks % 60 == 0;
                        boolean flag1 = Math.abs(l - this.lastYaw) >= 4 || Math.abs(i1 - this.lastPitch) >= 4;

                        if (this.ticks > 0 || this.myEntity instanceof EntityArrow) {
                            if (j1 >= -128 && j1 < 128
                                && k1 >= -128
                                && k1 < 128
                                && l1 >= -128
                                && l1 < 128
                                && this.ticksSinceLastForcedTeleport <= 400
                                && !this.ridingEntity
                                && !subWorldTeleportRequired) {
                                if (flag && flag1) {
                                    object = PacketHandler.getS17PacketEntityLookMove(
                                        this.myEntity.getEntityId(),
                                        ((IMixinWorld) ((IMixinEntity) this.myEntity).getWorldBelowFeet())
                                            .getSubWorldID(),
                                        ((IMixinEntity) this.myEntity).getTractionLossTicks(),
                                        ((IMixinEntity) this.myEntity).isLosingTraction(),
                                        (byte) j1,
                                        (byte) k1,
                                        (byte) l1,
                                        (byte) l,
                                        (byte) i1,
                                        (byte) (sendSubWorldPos ? 1 : 0),
                                        (byte) subWorldScaledXDiff,
                                        (byte) subWorldScaledYDiff,
                                        (byte) subWorldScaledZDiff);
                                } else if (flag) {
                                    object = PacketHandler.getS15PacketEntityRelMove(
                                        this.myEntity.getEntityId(),
                                        ((IMixinWorld) ((IMixinEntity) this.myEntity).getWorldBelowFeet())
                                            .getSubWorldID(),
                                        ((IMixinEntity) this.myEntity).getTractionLossTicks(),
                                        ((IMixinEntity) this.myEntity).isLosingTraction(),
                                        (byte) j1,
                                        (byte) k1,
                                        (byte) l1,
                                        (byte) (sendSubWorldPos ? 1 : 0),
                                        (byte) subWorldScaledXDiff,
                                        (byte) subWorldScaledYDiff,
                                        (byte) subWorldScaledZDiff);
                                } else if (flag1) {
                                    object = PacketHandler.getS16PacketEntityLook(
                                        this.myEntity.getEntityId(),
                                        ((IMixinWorld) ((IMixinEntity) this.myEntity).getWorldBelowFeet())
                                            .getSubWorldID(),
                                        ((IMixinEntity) this.myEntity).getTractionLossTicks(),
                                        ((IMixinEntity) this.myEntity).isLosingTraction(),
                                        (byte) l,
                                        (byte) i1);
                                }
                            } else {
                                this.ticksSinceLastForcedTeleport = 0;
                                object = PacketHandler.getS18PacketEntityTeleport(
                                    this.myEntity.getEntityId(),
                                    ((IMixinWorld) ((IMixinEntity) this.myEntity).getWorldBelowFeet()).getSubWorldID(),
                                    ((IMixinEntity) this.myEntity).getTractionLossTicks(),
                                    ((IMixinEntity) this.myEntity).isLosingTraction(),
                                    i,
                                    j,
                                    k,
                                    (byte) l,
                                    (byte) i1,
                                    (byte) (sendSubWorldPos ? 1 : 0),
                                    subWorldScaledX,
                                    subWorldScaledY,
                                    subWorldScaledZ);
                            }
                        }

                        if (this.sendVelocityUpdates) {
                            double d0 = this.myEntity.motionX - this.motionX;
                            double d1 = this.myEntity.motionY - this.motionY;
                            double d2 = this.myEntity.motionZ - this.motionZ;
                            double d3 = 0.02D;
                            double d4 = d0 * d0 + d1 * d1 + d2 * d2;

                            if (d4 > d3 * d3 || d4 > 0.0D && this.myEntity.motionX == 0.0D
                                && this.myEntity.motionY == 0.0D
                                && this.myEntity.motionZ == 0.0D) {
                                this.motionX = this.myEntity.motionX;
                                this.motionY = this.myEntity.motionY;
                                this.motionZ = this.myEntity.motionZ;
                                this.func_151259_a(
                                    new S12PacketEntityVelocity(
                                        this.myEntity.getEntityId(),
                                        this.motionX,
                                        this.motionY,
                                        this.motionZ));
                            }
                        }

                        if (object != null) {
                            this.func_151259_a((Packet) object);
                        }

                        this.sendMetadataToAllAssociatedPlayers();

                        if (flag) {
                            this.lastScaledXPosition = i;
                            this.lastScaledYPosition = j;
                            this.lastScaledZPosition = k;

                            if (sendSubWorldPos && this.ticks > 0) {
                                this.lastScaledXPositionOnSubWorld = subWorldScaledX;
                                this.lastScaledYPositionOnSubWorld = subWorldScaledY;
                                this.lastScaledZPositionOnSubWorld = subWorldScaledZ;
                            }
                        }

                        if (flag1) {
                            this.lastYaw = l;
                            this.lastPitch = i1;
                        }

                        this.ridingEntity = false;
                    } else {
                        i = MathHelper.floor_float(this.myEntity.rotationYaw * 256.0F / 360.0F);
                        j = MathHelper.floor_float(this.myEntity.rotationPitch * 256.0F / 360.0F);
                        boolean flag2 = Math.abs(i - this.lastYaw) >= 4 || Math.abs(j - this.lastPitch) >= 4;

                        if (flag2) {
                            this.func_151259_a(
                                PacketHandler.getS16PacketEntityLook(
                                    this.myEntity.getEntityId(),
                                    ((IMixinWorld) ((IMixinEntity) this.myEntity).getWorldBelowFeet()).getSubWorldID(),
                                    ((IMixinEntity) this.myEntity).getTractionLossTicks(),
                                    ((IMixinEntity) this.myEntity).isLosingTraction(),
                                    (byte) i,
                                    (byte) j));
                            this.lastYaw = i;
                            this.lastPitch = j;
                        }

                        this.lastScaledXPosition = this.myEntity.myEntitySize.multiplyBy32AndRound(this.myEntity.posX);
                        this.lastScaledYPosition = MathHelper.floor_double(this.myEntity.posY * 32.0D);
                        this.lastScaledZPosition = this.myEntity.myEntitySize.multiplyBy32AndRound(this.myEntity.posZ);
                        this.sendMetadataToAllAssociatedPlayers();
                        this.ridingEntity = true;
                    }

                    i = MathHelper.floor_float(this.myEntity.getRotationYawHead() * 256.0F / 360.0F);

                    if (Math.abs(i - this.lastHeadMotion) >= 4) {
                        this.func_151259_a(new S19PacketEntityHeadLook(this.myEntity, (byte) i));
                        this.lastHeadMotion = i;
                    }

                    this.myEntity.isAirBorne = false;
                }

        ++this.ticks;

        if (this.myEntity.velocityChanged) {
            this.func_151261_b(new S12PacketEntityVelocity(this.myEntity));
            this.myEntity.velocityChanged = false;
        }
    }

    @Overwrite
    private boolean isPlayerWatchingThisChunk(EntityPlayerMP p_73121_1_) {
        return ((WorldServer) this.myEntity.worldObj).getPlayerManager()
            .isPlayerWatchingChunk(p_73121_1_, this.myEntity.chunkCoordX, this.myEntity.chunkCoordZ);
    }

}
