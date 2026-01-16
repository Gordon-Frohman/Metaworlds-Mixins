package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.network;

import java.util.Collection;
import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C12PacketUpdateSign;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.llamalad7.mixinextras.sugar.Local;

import su.sergiusonesimus.metaworlds.MetaworldsMod;
import su.sergiusonesimus.metaworlds.api.SubWorldTypeManager;
import su.sergiusonesimus.metaworlds.entity.player.EntityPlayerProxy;
import su.sergiusonesimus.metaworlds.util.OrientedBB;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.IMixinEntity;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.player.IMixinEntityPlayer;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.network.IMixinNetHandlerPlayServer;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.network.play.PacketHandler;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.network.play.client.IMixinC03PacketPlayer;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

@Mixin(NetHandlerPlayServer.class)
public class MixinNetHandlerPlayServer implements IMixinNetHandlerPlayServer {

    @Shadow(remap = true)
    public EntityPlayerMP playerEntity;

    @Shadow(remap = true)
    public MinecraftServer serverController;

    @Shadow(remap = true)
    public boolean field_147366_g;

    @Shadow(remap = true)
    public boolean hasMoved;

    @Shadow(remap = true)
    private static Logger logger;

    /** The last known x position for this connection. */
    @Shadow(remap = true)
    public double lastPosX;
    /** The last known y position for this connection. */
    @Shadow(remap = true)
    public double lastPosY;
    /** The last known z position for this connection. */
    @Shadow(remap = true)
    public double lastPosZ;

    @Shadow(remap = true)
    public int floatingTickCount;

    @Shadow(remap = true)
    public int networkTickCount;

    // TODO

    @Shadow(remap = true)
    public void sendPacket(final Packet packetIn) {}

    @Shadow(remap = true)
    public void kickPlayerFromServer(String string) {}

    private int lastSubworldBelowFeetId;

    private double lastLocalPosX;

    private double lastLocalPosY;

    private double lastLocalPosZ;

    /**
     * Moves the player to the specified destination and rotation
     */
    @Overwrite
    public void setPlayerLocation(double x, double y, double z, float yaw, float pitch) {
        IMixinWorld worldBelowFeet = (IMixinWorld) ((IMixinEntity) this.playerEntity).getWorldBelowFeet();
        Vec3 localPos = worldBelowFeet.transformToLocal(x, y, z);
        this.hasMoved = false;
        this.lastPosX = x;
        this.lastPosY = y;
        this.lastPosZ = z;
        this.lastSubworldBelowFeetId = worldBelowFeet.getSubWorldID();
        this.lastLocalPosX = localPos.xCoord;
        this.lastLocalPosY = localPos.yCoord;
        this.lastLocalPosZ = localPos.zCoord;
        this.playerEntity.setPositionAndRotation(x, y, z, yaw, pitch);
        ((IMixinEntityPlayer) this.playerEntity).setSubworldBelowFeetId(this.lastSubworldBelowFeetId);
        ((IMixinEntityPlayer) this.playerEntity).setCurrentSubworldPosition(localPos);
        this.playerEntity.playerNetServerHandler.sendPacket(
            PacketHandler.getS08PacketPlayerPosLook(
                x,
                y + 1.6200000047683716D,
                z,
                yaw,
                pitch,
                false,
                worldBelowFeet.getSubWorldID(),
                SubWorldTypeManager.getTypeID(worldBelowFeet.getSubWorldType()),
                localPos.xCoord,
                localPos.yCoord + 1.6200000047683716D,
                localPos.zCoord));
    }

    public void setPlayerLocation(double x, double y, double z, float yaw, float pitch, int worldBelowFeetId,
        int worldBelowFeetType, double localPosX, double localPosY, double localPosZ) {
        this.hasMoved = false;
        this.lastPosX = x;
        this.lastPosY = y;
        this.lastPosZ = z;
        this.lastSubworldBelowFeetId = worldBelowFeetId;
        this.lastLocalPosX = localPosX;
        this.lastLocalPosY = localPosY;
        this.lastLocalPosZ = localPosZ;
        this.playerEntity.setPositionAndRotation(x, y, z, yaw, pitch);
        ((IMixinEntityPlayer) this.playerEntity).setSubworldBelowFeetId(worldBelowFeetId);
        ((IMixinEntityPlayer) this.playerEntity).setCurrentSubworldPosition(localPosX, localPosY, localPosZ);
        this.playerEntity.playerNetServerHandler.sendPacket(
            PacketHandler.getS08PacketPlayerPosLook(
                x,
                y + 1.6200000047683716D,
                z,
                yaw,
                pitch,
                false,
                worldBelowFeetId,
                worldBelowFeetType,
                localPosX,
                localPosY + 1.6200000047683716D,
                localPosZ));
    }

    /**
     * Processes clients perspective on player positioning and/or orientation
     */
    @Overwrite
    public void processPlayer(C03PacketPlayer packetPlayer) {
        // if((packetPlayer instanceof C04PacketPlayerPosition || packetPlayer instanceof C06PacketPlayerPosLook) &&
        // net.minecraft.util.MathHelper.floor_double(packetPlayer.func_149464_c()) != -791 &&
        // net.minecraft.util.MathHelper.floor_double(packetPlayer.func_149467_d()) != 67 &&
        // net.minecraft.util.MathHelper.floor_double(packetPlayer.func_149472_e()) != 873)
        MetaworldsMod.breakpoint2();
        WorldServer worldserver = this.serverController.worldServerForDimension(this.playerEntity.dimension);
        this.field_147366_g = true;

        if (!this.playerEntity.playerConqueredTheEnd) {
            double d0;

            if (!this.hasMoved) {
                if (this.lastSubworldBelowFeetId != ((IMixinC03PacketPlayer) packetPlayer).getSubWorldBelowFeetId()) {
                    // Player has stepped on a different (sub)world
                    this.hasMoved = true;
                } else {
                    // First - checking if the player has moved globally
                    // Or if the world he/she is standing on was moved or rotated
                    d0 = packetPlayer.func_149467_d() - this.lastPosY;

                    if (packetPlayer.func_149464_c() == this.lastPosX && d0 * d0 < 0.01D
                        && packetPlayer.func_149472_e() == this.lastPosZ) {
                        this.hasMoved = true;
                    }
                    if (!this.hasMoved) {
                        // Then - if the player has moved locally
                        d0 = ((IMixinC03PacketPlayer) packetPlayer).getSubWorldYPosition() - this.lastLocalPosY;
                        double d1 = ((IMixinC03PacketPlayer) packetPlayer).getSubWorldXPosition() - this.lastLocalPosX;
                        double d2 = ((IMixinC03PacketPlayer) packetPlayer).getSubWorldZPosition() - this.lastLocalPosZ;

                        if (d0 * d0 < 0.01D && d1 * d1 < 0.01D && d2 * d2 < 0.01D) {
                            this.hasMoved = true;
                        }
                    }
                }
            }

            if (this.hasMoved) {
                double d1;
                double d2;
                double d3;

                if (this.playerEntity.ridingEntity != null) {
                    float f4 = this.playerEntity.rotationYaw;
                    float f = this.playerEntity.rotationPitch;
                    this.playerEntity.ridingEntity.updateRiderPosition();
                    d1 = this.playerEntity.posX;
                    d2 = this.playerEntity.posY;
                    d3 = this.playerEntity.posZ;

                    if (packetPlayer.func_149463_k()) {
                        f4 = packetPlayer.func_149462_g();
                        f = packetPlayer.func_149470_h();
                    }

                    this.playerEntity.onGround = packetPlayer.func_149465_i();
                    this.playerEntity.onUpdateEntity();
                    this.playerEntity.ySize = 0.0F;
                    this.playerEntity.setPositionAndRotation(d1, d2, d3, f4, f);

                    if (this.playerEntity.ridingEntity != null) {
                        this.playerEntity.ridingEntity.updateRiderPosition();
                    }

                    if (!this.hasMoved) // Fixes teleportation kick while riding entities
                    {
                        return;
                    }

                    this.serverController.getConfigurationManager()
                        .updatePlayerPertinentChunks(this.playerEntity);

                    if (this.hasMoved) {
                        this.lastPosX = this.playerEntity.posX;
                        this.lastPosY = this.playerEntity.posY;
                        this.lastPosZ = this.playerEntity.posZ;
                    }

                    worldserver.updateEntity(this.playerEntity);
                    return;
                }

                if (this.playerEntity.isPlayerSleeping()) {
                    this.playerEntity.onUpdateEntity();
                    this.playerEntity.setPositionAndRotation(
                        this.lastPosX,
                        this.lastPosY,
                        this.lastPosZ,
                        this.playerEntity.rotationYaw,
                        this.playerEntity.rotationPitch);
                    worldserver.updateEntity(this.playerEntity);
                    return;
                }

                d0 = this.playerEntity.posY;
                this.lastPosX = this.playerEntity.posX;
                this.lastPosY = this.playerEntity.posY;
                this.lastPosZ = this.playerEntity.posZ;
                d1 = this.playerEntity.posX;
                d2 = this.playerEntity.posY;
                d3 = this.playerEntity.posZ;
                float f1 = this.playerEntity.rotationYaw;
                float f2 = this.playerEntity.rotationPitch;

                if (packetPlayer.func_149466_j() && packetPlayer.func_149467_d() == -999.0D
                    && packetPlayer.func_149471_f() == -999.0D) {
                    packetPlayer.func_149469_a(false);
                }

                int subworldId = ((IMixinC03PacketPlayer) packetPlayer).getSubWorldBelowFeetId();
                World worldBelowFeet = ((IMixinWorld) this.playerEntity.worldObj).getSubWorld(subworldId);
                ((IMixinEntityPlayer) this.playerEntity).setSubworldBelowFeetId(subworldId);
                ((IMixinEntity) this.playerEntity).setWorldBelowFeet(worldBelowFeet);
                if (((IMixinC03PacketPlayer) packetPlayer).getLosingTraction()) {
                    ((IMixinEntity) this.playerEntity).slowlyRemoveWorldBelowFeet();
                    ((IMixinEntity) this.playerEntity)
                        .setTractionTickCount(((IMixinC03PacketPlayer) packetPlayer).getTractionLoss());
                }

                double d4;

                if (packetPlayer.func_149466_j()) {
                    if (subworldId != 0 && worldBelowFeet != null) {
                        double subworldPosX = ((IMixinC03PacketPlayer) packetPlayer).getSubWorldXPosition();
                        double subworldPosY = ((IMixinC03PacketPlayer) packetPlayer).getSubWorldYPosition();
                        double subworldPosZ = ((IMixinC03PacketPlayer) packetPlayer).getSubWorldZPosition();
                        ((IMixinEntityPlayer) this.playerEntity)
                            .setCurrentSubworldPosition(subworldPosX, subworldPosY, subworldPosZ);
                        Vec3 globalPos = ((IMixinWorld) worldBelowFeet)
                            .transformToGlobal(subworldPosX, subworldPosY, subworldPosZ);
                        d1 = globalPos.xCoord;
                        d2 = globalPos.yCoord;
                        d3 = globalPos.zCoord;
                    } else {
                        d1 = packetPlayer.func_149464_c();
                        d2 = packetPlayer.func_149467_d();
                        d3 = packetPlayer.func_149472_e();
                        ((IMixinEntityPlayer) this.playerEntity).setCurrentSubworldPosition(d1, d2, d3);
                    }
                    d4 = packetPlayer.func_149471_f() - packetPlayer.func_149467_d();

                    if (!this.playerEntity.isPlayerSleeping() && (d4 > 1.65D || d4 < 0.1D)) {
                        this.kickPlayerFromServer("Illegal stance");
                        logger.warn(this.playerEntity.getCommandSenderName() + " had an illegal stance: " + d4);
                        return;
                    }

                    if (Math.abs(packetPlayer.func_149464_c()) > 3.2E7D
                        || Math.abs(packetPlayer.func_149472_e()) > 3.2E7D) {
                        this.kickPlayerFromServer("Illegal position");
                        return;
                    }
                }

                if (packetPlayer.func_149463_k()) {
                    f1 = packetPlayer.func_149462_g();
                    f2 = packetPlayer.func_149470_h();
                }

                this.playerEntity.onUpdateEntity();
                this.playerEntity.ySize = 0.0F;
                this.playerEntity.setPositionAndRotation(this.lastPosX, this.lastPosY, this.lastPosZ, f1, f2);

                if (!this.hasMoved) {
                    return;
                }

                d4 = d1 - this.playerEntity.posX;
                double d5 = d2 - this.playerEntity.posY;
                double d6 = d3 - this.playerEntity.posZ;
                // BUGFIX: min -> max, grabs the highest distance
                double d7 = Math.max(Math.abs(d4), Math.abs(this.playerEntity.motionX));
                double d8 = Math.max(Math.abs(d5), Math.abs(this.playerEntity.motionY));
                double d9 = Math.max(Math.abs(d6), Math.abs(this.playerEntity.motionZ));
                double d10 = d7 * d7 + d8 * d8 + d9 * d9;

                if (d10 > 100.0D && (!this.serverController.isSinglePlayer() || !this.serverController.getServerOwner()
                    .equals(this.playerEntity.getCommandSenderName()))) {
                    logger.warn(
                        this.playerEntity.getCommandSenderName() + " moved too quickly! "
                            + d4
                            + ","
                            + d5
                            + ","
                            + d6
                            + " ("
                            + d7
                            + ", "
                            + d8
                            + ", "
                            + d9
                            + ")");
                    IMixinWorld lastSubworldBelowFeet = (IMixinWorld) ((IMixinWorld) this.playerEntity.worldObj)
                        .getSubWorld(lastSubworldBelowFeetId);
                    if (this.lastSubworldBelowFeetId == 0 || lastSubworldBelowFeet == null) {
                        this.setPlayerLocation(
                            this.lastPosX,
                            this.lastPosY,
                            this.lastPosZ,
                            this.playerEntity.rotationYaw,
                            this.playerEntity.rotationPitch);
                    } else {
                        Vec3 globalPos = lastSubworldBelowFeet
                            .transformToGlobal(this.lastLocalPosX, this.lastLocalPosY, this.lastLocalPosZ);
                        this.setPlayerLocation(
                            globalPos.xCoord,
                            globalPos.yCoord,
                            globalPos.zCoord,
                            this.playerEntity.rotationYaw,
                            this.playerEntity.rotationPitch,
                            this.lastSubworldBelowFeetId,
                            SubWorldTypeManager.getTypeID(lastSubworldBelowFeet.getSubWorldType()),
                            this.lastLocalPosX,
                            this.lastLocalPosY,
                            this.lastLocalPosZ);
                    }
                    return;
                }

                float f3 = 0.0625F;
                boolean flag = worldserver
                    .getCollidingBoundingBoxes(
                        this.playerEntity,
                        this.playerEntity.boundingBox.copy()
                            .contract((double) f3, (double) f3, (double) f3))
                    .isEmpty();

                if (this.playerEntity.onGround && !packetPlayer.func_149465_i() && d5 > 0.0D) {
                    this.playerEntity.jump();
                }

                if (!this.hasMoved) // Fixes "Moved Too Fast" kick when being teleported while moving
                {
                    return;
                }

                this.playerEntity.moveEntity(d4, d5, d6);
                this.playerEntity.onGround = packetPlayer.func_149465_i();

                Collection<EntityPlayerProxy> curPlayerProxies = ((IMixinEntity) this.playerEntity).getPlayerProxyMap()
                    .values();

                for (EntityPlayerProxy curPlayerProxy : curPlayerProxies) {
                    ((EntityPlayer) curPlayerProxy).onGround = this.playerEntity.onGround;
                }

                this.playerEntity.addMovementStat(d4, d5, d6);
                double d11 = d5;
                d4 = d1 - this.playerEntity.posX;
                d5 = d2 - this.playerEntity.posY;

                if (d5 > -0.5D || d5 < 0.5D) {
                    d5 = 0.0D;
                }

                d6 = d3 - this.playerEntity.posZ;
                d10 = d4 * d4 + d5 * d5 + d6 * d6;
                boolean flag1 = false;

                if (d10 > 0.0625D && !this.playerEntity.isPlayerSleeping()
                    && !this.playerEntity.theItemInWorldManager.isCreative()) {
                    flag1 = true;
                    logger.warn(this.playerEntity.getCommandSenderName() + " moved wrongly!");
                }

                if (!this.hasMoved) // Fixes "Moved Too Fast" kick when being teleported while moving
                {
                    return;
                }

                this.playerEntity.setPositionAndRotation(d1, d2, d3, f1, f2);

                // Since different algorithms are used when getting colliding BBs and when getting offsets for player
                // movement, we have to check this flag differently
                boolean flag2 = true;
                List<AxisAlignedBB> collidingBBs = worldserver.getCollidingBoundingBoxes(
                    this.playerEntity,
                    this.playerEntity.boundingBox.copy()
                        .contract((double) f3, (double) f3, (double) f3));
                if (!collidingBBs.isEmpty()) {
                    // PLayer's BB intersects with some blocks
                    // Let's check if there're any main world blocks there
                    boolean subWorldsOnly = true;
                    IMixinWorld collidedSubworld = null;
                    for (AxisAlignedBB aabb : collidingBBs) {
                        if (!(aabb instanceof OrientedBB)) {
                            subWorldsOnly = false;
                            break;
                        } else {
                            collidedSubworld = (IMixinWorld) ((OrientedBB) aabb).lastTransformedBy;
                        }
                    }
                    if (!subWorldsOnly) {
                        // Player is collided with at least one main world block
                        // Therefore the collision is valid
                        flag2 = false;
                    } else {
                        // Player is only collided with subworld blocks
                        // Let's check if the collision persists with contracted player's BB
                        double worldRotation = collidedSubworld.getRotationYaw() % 360;
                        if (worldRotation == 0) {
                            // The subworld is parallel with the main world.
                            // Therefore all collisions with it are valid
                            flag2 = false;
                        } else {
                            worldRotation = Math.abs(worldRotation) % 90;
                            if (worldRotation > 45) worldRotation = 90 - worldRotation;
                            worldRotation = Math.toRadians(worldRotation);
                            double modifier = (1 - 1 / (Math.sin(worldRotation) + Math.cos(worldRotation))) / 2;
                            collidingBBs = worldserver.getCollidingBoundingBoxes(
                                this.playerEntity,
                                this.playerEntity.boundingBox.copy()
                                    .contract(
                                        (double) f3
                                            + (this.playerEntity.boundingBox.maxX - this.playerEntity.boundingBox.minX)
                                                * modifier,
                                        (double) f3,
                                        (double) f3
                                            + (this.playerEntity.boundingBox.maxZ - this.playerEntity.boundingBox.minZ)
                                                * modifier));
                            flag2 = collidingBBs.isEmpty();
                        }
                    }
                }

                if (flag && (flag1 || !flag2) && !this.playerEntity.isPlayerSleeping() && !this.playerEntity.noClip) {
                    IMixinWorld lastSubworldBelowFeet = (IMixinWorld) ((IMixinWorld) this.playerEntity.worldObj)
                        .getSubWorld(lastSubworldBelowFeetId);
                    if (this.lastSubworldBelowFeetId == 0 || lastSubworldBelowFeet == null) {
                        this.setPlayerLocation(this.lastPosX, this.lastPosY, this.lastPosZ, f1, f2);
                    } else {
                        Vec3 globalPos = lastSubworldBelowFeet
                            .transformToGlobal(this.lastLocalPosX, this.lastLocalPosY, this.lastLocalPosZ);
                        this.setPlayerLocation(
                            globalPos.xCoord,
                            globalPos.yCoord,
                            globalPos.zCoord,
                            f1,
                            f2,
                            this.lastSubworldBelowFeetId,
                            SubWorldTypeManager.getTypeID(lastSubworldBelowFeet.getSubWorldType()),
                            this.lastLocalPosX,
                            this.lastLocalPosY,
                            this.lastLocalPosZ);
                    }
                    return;
                }

                AxisAlignedBB axisalignedbb = this.playerEntity.boundingBox.copy()
                    .expand((double) f3, (double) f3, (double) f3)
                    .addCoord(0.0D, -0.55D, 0.0D);

                if (!this.serverController.isFlightAllowed() && !this.playerEntity.theItemInWorldManager.isCreative()
                    && !worldserver.checkBlockCollision(axisalignedbb)
                    && !this.playerEntity.capabilities.allowFlying) {
                    if (d11 >= -0.03125D) {
                        ++this.floatingTickCount;

                        if (this.floatingTickCount > 80) {
                            logger
                                .warn(this.playerEntity.getCommandSenderName() + " was kicked for floating too long!");
                            this.kickPlayerFromServer("Flying is not enabled on this server");
                            return;
                        }
                    }
                } else {
                    this.floatingTickCount = 0;
                }

                if (!this.hasMoved) // Fixes "Moved Too Fast" kick when being teleported while moving
                {
                    return;
                }

                this.playerEntity.onGround = packetPlayer.func_149465_i();
                this.serverController.getConfigurationManager()
                    .updatePlayerPertinentChunks(this.playerEntity);
                this.playerEntity.handleFalling(this.playerEntity.posY - d0, packetPlayer.func_149465_i());
            } else if (this.networkTickCount % 20 == 0) {
                IMixinWorld lastSubworldBelowFeet = (IMixinWorld) ((IMixinWorld) this.playerEntity.worldObj)
                    .getSubWorld(lastSubworldBelowFeetId);
                if (this.lastSubworldBelowFeetId == 0 || lastSubworldBelowFeet == null) {
                    this.setPlayerLocation(
                        this.lastPosX,
                        this.lastPosY,
                        this.lastPosZ,
                        this.playerEntity.rotationYaw,
                        this.playerEntity.rotationPitch);
                } else {
                    Vec3 globalPos = lastSubworldBelowFeet
                        .transformToGlobal(this.lastLocalPosX, this.lastLocalPosY, this.lastLocalPosZ);
                    this.setPlayerLocation(
                        globalPos.xCoord,
                        globalPos.yCoord,
                        globalPos.zCoord,
                        this.playerEntity.rotationYaw,
                        this.playerEntity.rotationPitch,
                        this.lastSubworldBelowFeetId,
                        SubWorldTypeManager.getTypeID(lastSubworldBelowFeet.getSubWorldType()),
                        this.lastLocalPosX,
                        this.lastLocalPosY,
                        this.lastLocalPosZ);
                }
            }
        }
    }

    /**
     * Processes the player initiating/stopping digging on a particular spot, as well as a player dropping items?. (0:
     * initiated, 1: reinitiated, 2? , 3-4 drop item (respectively without or with player control), 5: stopped; x,y,z,
     * side clicked on;)
     */
    @Overwrite
    public void processPlayerDigging(C07PacketPlayerDigging packetIn) {
        WorldServer worldserver = (WorldServer) this.playerEntity.worldObj;

        this.playerEntity.func_143004_u();

        if (packetIn.func_149506_g() == 4) {
            this.playerEntity.dropOneItem(false);
        } else if (packetIn.func_149506_g() == 3) {
            this.playerEntity.dropOneItem(true);
        } else if (packetIn.func_149506_g() == 5) {
            this.playerEntity.stopUsingItem();
        } else {
            boolean flag = false;

            if (packetIn.func_149506_g() == 0) {
                flag = true;
            }

            if (packetIn.func_149506_g() == 1) {
                flag = true;
            }

            if (packetIn.func_149506_g() == 2) {
                flag = true;
            }

            int i = packetIn.func_149505_c();
            int j = packetIn.func_149503_d();
            int k = packetIn.func_149502_e();

            if (flag) {
                double d0 = this.playerEntity.posX - ((double) i + 0.5D);
                double d1 = this.playerEntity.posY - ((double) j + 0.5D) + 1.5D;
                double d2 = this.playerEntity.posZ - ((double) k + 0.5D);
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;
                d3 *= ((IMixinWorld) worldserver).getScaling() * ((IMixinWorld) worldserver).getScaling();

                double dist = playerEntity.theItemInWorldManager.getBlockReachDistance() + 1;
                dist *= Math.max(1.0d, ((IMixinWorld) worldserver).getScaling());
                dist *= dist;

                if (d3 > dist) {
                    return;
                }

                if (j >= this.serverController.getBuildLimit()) {
                    return;
                }
            }

            if (packetIn.func_149506_g() == 0) {
                if (!this.serverController.isBlockProtected(worldserver, i, j, k, this.playerEntity)) {
                    this.playerEntity.theItemInWorldManager.onBlockClicked(i, j, k, packetIn.func_149501_f());
                } else {
                    this.playerEntity.playerNetServerHandler.sendPacket(new S23PacketBlockChange(i, j, k, worldserver));
                }
            } else if (packetIn.func_149506_g() == 2) {
                this.playerEntity.theItemInWorldManager.uncheckedTryHarvestBlock(i, j, k);

                if (worldserver.getBlock(i, j, k)
                    .getMaterial() != Material.air) {
                    this.playerEntity.playerNetServerHandler.sendPacket(new S23PacketBlockChange(i, j, k, worldserver));
                }
            } else if (packetIn.func_149506_g() == 1) {
                this.playerEntity.theItemInWorldManager.cancelDestroyingBlock(i, j, k);

                if (worldserver.getBlock(i, j, k)
                    .getMaterial() != Material.air) {
                    this.playerEntity.playerNetServerHandler.sendPacket(new S23PacketBlockChange(i, j, k, worldserver));
                }
            }
        }
    }

    @ModifyVariable(method = "processPlayerBlockPlacement", at = @At(value = "STORE"), ordinal = 0)
    private WorldServer modifyWorldServer(WorldServer worldServer) {
        return (WorldServer) this.playerEntity.worldObj;
    }

    @ModifyVariable(method = "processPlayerBlockPlacement", at = @At(value = "STORE"), ordinal = 0)
    private double modifyDist(double dist, @Local WorldServer worldserver) {
        return dist * Math.max(1.0d, 1.0d / ((IMixinWorld) worldserver).getScaling());
    }

    /**
     * Processes interactions ((un)leashing, opening command block GUI) and attacks on an entity with players currently
     * equipped item
     */
    @Overwrite
    public void processUseEntity(C02PacketUseEntity packetIn) {
        WorldServer worldserver = (WorldServer) this.playerEntity.worldObj;
        Entity entity = packetIn.func_149564_a(worldserver);
        this.playerEntity.func_143004_u();

        if (entity != null) {
            boolean flag = this.playerEntity.canEntityBeSeen(entity);
            double d0 = 36.0D;

            if (!flag) {
                d0 = 9.0D;
            }

            if (this.playerEntity.getDistanceSqToEntity(entity) < d0) {
                if (packetIn.func_149565_c() == C02PacketUseEntity.Action.INTERACT) {
                    this.playerEntity.interactWith(entity);
                } else if (packetIn.func_149565_c() == C02PacketUseEntity.Action.ATTACK) {
                    if (entity instanceof EntityItem || entity instanceof EntityXPOrb
                        || entity instanceof EntityArrow
                        || entity == this.playerEntity) {
                        this.kickPlayerFromServer("Attempting to attack an invalid entity");
                        this.serverController.logWarning(
                            "Player " + this.playerEntity.getCommandSenderName()
                                + " tried to attack an invalid entity");
                        return;
                    }

                    this.playerEntity.attackTargetEntityWithCurrentItem(entity);
                }
            }
        }
    }

    @Overwrite
    public void processUpdateSign(C12PacketUpdateSign packetIn) {
        this.playerEntity.func_143004_u();
        WorldServer worldserver = (WorldServer) this.playerEntity.worldObj;// this.serverController.worldServerForDimension(this.playerEntity.dimension);

        if (worldserver.blockExists(packetIn.func_149588_c(), packetIn.func_149586_d(), packetIn.func_149585_e())) {
            TileEntity tileentity = worldserver
                .getTileEntity(packetIn.func_149588_c(), packetIn.func_149586_d(), packetIn.func_149585_e());

            if (tileentity instanceof TileEntitySign) {
                TileEntitySign tileentitysign = (TileEntitySign) tileentity;

                if (!tileentitysign.func_145914_a() || tileentitysign.func_145911_b() != this.playerEntity) {
                    this.serverController.logWarning(
                        "Player " + this.playerEntity.getCommandSenderName()
                            + " just tried to change non-editable sign");
                    return;
                }
            }

            int i;
            int j;

            for (j = 0; j < 4; ++j) {
                boolean flag = true;

                if (packetIn.func_149589_f()[j].length() > 15) {
                    flag = false;
                } else {
                    for (i = 0; i < packetIn.func_149589_f()[j].length(); ++i) {
                        if (!ChatAllowedCharacters.isAllowedCharacter(packetIn.func_149589_f()[j].charAt(i))) {
                            flag = false;
                        }
                    }
                }

                if (!flag) {
                    packetIn.func_149589_f()[j] = "!?";
                }
            }

            if (tileentity instanceof TileEntitySign) {
                j = packetIn.func_149588_c();
                int k = packetIn.func_149586_d();
                i = packetIn.func_149585_e();
                TileEntitySign tileentitysign1 = (TileEntitySign) tileentity;
                System.arraycopy(packetIn.func_149589_f(), 0, tileentitysign1.signText, 0, 4);
                tileentitysign1.markDirty();
                worldserver.markBlockForUpdate(j, k, i);
            }
        }
    }

}
