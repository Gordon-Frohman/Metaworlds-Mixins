package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.server.management;

import java.util.Iterator;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.network.play.server.S05PacketSpawnPosition;
import net.minecraft.network.play.server.S09PacketHeldItemChange;
import net.minecraft.network.play.server.S1DPacketEntityEffect;
import net.minecraft.network.play.server.S39PacketPlayerAbilities;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.base.Charsets;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;

import cpw.mods.fml.common.FMLCommonHandler;
import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.network.MetaMagicNetwork;
import su.sergiusonesimus.metaworlds.network.play.server.S04SubWorldSpawnPositionPacket;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.IMixinEntity;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.player.IMixinEntityPlayer;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.network.play.server.IMixinS05PacketSpawnPosition;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

@Mixin(ServerConfigurationManager.class)
public class MixinServerConfigurationManager {

    @Shadow(remap = true)
    private MinecraftServer mcServer;

    @Shadow(remap = true)
    private static Logger logger;

    // TODO

    @Shadow(remap = true)
    public void updateTimeAndWeatherForPlayer(EntityPlayerMP player, WorldServer worldIn) {}

    @Shadow(remap = true)
    public void playerLoggedIn(EntityPlayerMP player) {}

    @Shadow(remap = true)
    public void sendChatMsg(IChatComponent component) {}

    @Shadow(remap = true)
    protected void func_96456_a(ServerScoreboard scoreboardIn, EntityPlayerMP player) {}

    @Shadow(remap = true)
    public int getMaxPlayers() {
        return 0;
    }

    @Shadow(remap = true)
    private void func_72381_a(EntityPlayerMP p_72381_1_, EntityPlayerMP p_72381_2_, World p_72381_3_) {}

    @Shadow(remap = true)
    public NBTTagCompound readPlayerDataFromFile(EntityPlayerMP player) {
        return null;
    }

    @Shadow(remap = true)
    public MinecraftServer getServerInstance() {
        return null;
    }

    @Overwrite(remap = false)
    public void initializeConnectionToPlayer(NetworkManager netManager, EntityPlayerMP player,
        NetHandlerPlayServer nethandlerplayserver) {
        GameProfile gameprofile = player.getGameProfile();
        PlayerProfileCache playerprofilecache = this.mcServer.func_152358_ax();
        GameProfile gameprofile1 = playerprofilecache.func_152652_a(gameprofile.getId());
        String s = gameprofile1 == null ? gameprofile.getName() : gameprofile1.getName();
        playerprofilecache.func_152649_a(gameprofile);
        NBTTagCompound nbttagcompound = this.readPlayerDataFromFile(player);

        World playerWorld = this.mcServer.worldServerForDimension(player.dimension);
        if (playerWorld == null) {
            player.dimension = 0;
            playerWorld = this.mcServer.worldServerForDimension(0);
            ChunkCoordinates spawnPoint = playerWorld.provider.getRandomizedSpawnPoint();
            player.setPosition(spawnPoint.posX, spawnPoint.posY, spawnPoint.posZ);
        }

        player.setWorld(playerWorld);
        player.theItemInWorldManager.setWorld((WorldServer) player.worldObj);
        String s1 = "local";

        if (netManager.getSocketAddress() != null) {
            s1 = netManager.getSocketAddress()
                .toString();
        }

        logger.info(
            player.getCommandSenderName() + "["
                + s1
                + "] logged in with entity id "
                + player.getEntityId()
                + " at ("
                + player.posX
                + ", "
                + player.posY
                + ", "
                + player.posZ
                + ")");
        WorldServer worldserver = this.mcServer.worldServerForDimension(player.dimension);
        ChunkCoordinates chunkcoordinates = worldserver.getSpawnPoint();
        this.func_72381_a(player, (EntityPlayerMP) null, worldserver);
        player.playerNetServerHandler = nethandlerplayserver;
        nethandlerplayserver.sendPacket(
            new S01PacketJoinGame(
                player.getEntityId(),
                player.theItemInWorldManager.getGameType(),
                worldserver.getWorldInfo()
                    .isHardcoreModeEnabled(),
                worldserver.provider.dimensionId,
                worldserver.difficultySetting,
                this.getMaxPlayers(),
                worldserver.getWorldInfo()
                    .getTerrainType()));
        nethandlerplayserver.sendPacket(
            new S3FPacketCustomPayload(
                "MC|Brand",
                this.getServerInstance()
                    .getServerModName()
                    .getBytes(Charsets.UTF_8)));
        nethandlerplayserver.sendPacket(
            new S05PacketSpawnPosition(chunkcoordinates.posX, chunkcoordinates.posY, chunkcoordinates.posZ));
        nethandlerplayserver.sendPacket(new S39PacketPlayerAbilities(player.capabilities));
        nethandlerplayserver.sendPacket(new S09PacketHeldItemChange(player.inventory.currentItem));

        if (nbttagcompound != null) {
            NBTTagCompound entityData = nbttagcompound.getCompoundTag("ForgeData");
            if (entityData.hasKey("SubWorldInfo")) {
                NBTTagCompound subWorldData = entityData.getCompoundTag("SubWorldInfo");
                int worldBelowFeetId = subWorldData.getInteger("WorldBelowFeetId");
                World newWorldBelowFeet = ((IMixinWorld) ((IMixinWorld) player.worldObj).getParentWorld())
                    .getSubWorld(worldBelowFeetId);
                if (worldBelowFeetId != 0 && newWorldBelowFeet != null) {
                    double posXOnSubWorld = subWorldData.getDouble("posXOnSubWorld");
                    double posYOnSubWorld = subWorldData.getDouble("posYOnSubWorld");
                    double posZOnSubWorld = subWorldData.getDouble("posZOnSubWorld");
                    MetaMagicNetwork.dispatcher.sendTo(
                        new S04SubWorldSpawnPositionPacket(posXOnSubWorld, posYOnSubWorld, posZOnSubWorld),
                        player);
                    ((IMixinEntity) player).setWorldBelowFeet(newWorldBelowFeet);
                    Vec3 transformedPos = ((IMixinWorld) newWorldBelowFeet)
                        .transformToGlobal(posXOnSubWorld, posYOnSubWorld, posZOnSubWorld);
                    player.setPosition(transformedPos.xCoord, transformedPos.yCoord, transformedPos.zCoord);
                }
            }
        }

        player.func_147099_x()
            .func_150877_d();
        player.func_147099_x()
            .func_150884_b(player);
        this.func_96456_a((ServerScoreboard) worldserver.getScoreboard(), player);
        this.mcServer.func_147132_au();
        ChatComponentTranslation chatcomponenttranslation;

        if (!player.getCommandSenderName()
            .equalsIgnoreCase(s)) {
            chatcomponenttranslation = new ChatComponentTranslation(
                "multiplayer.player.joined.renamed",
                new Object[] { player.func_145748_c_(), s });
        } else {
            chatcomponenttranslation = new ChatComponentTranslation(
                "multiplayer.player.joined",
                new Object[] { player.func_145748_c_() });
        }

        chatcomponenttranslation.getChatStyle()
            .setColor(EnumChatFormatting.YELLOW);
        this.sendChatMsg(chatcomponenttranslation);
        this.playerLoggedIn(player);
        nethandlerplayserver
            .setPlayerLocation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
        this.updateTimeAndWeatherForPlayer(player, worldserver);

        if (this.mcServer.getTexturePack()
            .length() > 0) {
            player.requestTexturePackLoad(this.mcServer.getTexturePack());
        }

        Iterator<PotionEffect> iterator = player.getActivePotionEffects()
            .iterator();

        while (iterator.hasNext()) {
            PotionEffect potioneffect = (PotionEffect) iterator.next();
            nethandlerplayserver.sendPacket(new S1DPacketEntityEffect(player.getEntityId(), potioneffect));
        }

        player.addSelfToInternalCraftingInventory();

        FMLCommonHandler.instance()
            .firePlayerLoggedIn(player);
        if (nbttagcompound != null && nbttagcompound.hasKey("Riding", 10)) {
            Entity entity = EntityList.createEntityFromNBT(nbttagcompound.getCompoundTag("Riding"), worldserver);

            if (entity != null) {
                entity.forceSpawn = true;
                worldserver.spawnEntityInWorld(entity);
                player.mountEntity(entity);
                entity.forceSpawn = false;
            }
        }
    }

    // respawnPlayer

    private int spawnWorld;
    private EntityPlayerMP storedPlayer;

    @Inject(
        method = "respawnPlayer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/EntityTracker;removePlayerFromTrackers(Lnet/minecraft/entity/player/EntityPlayerMP;)V",
            shift = Shift.BEFORE))
    public void storeVariables(EntityPlayerMP player, int dimension, boolean conqueredEnd,
        CallbackInfoReturnable<EntityPlayerMP> ci) {
        spawnWorld = ((IMixinEntityPlayer) player).getSpawnWorldID(dimension);
        storedPlayer = player;
    }

    @WrapOperation(
        method = "respawnPlayer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/EntityPlayer;verifyRespawnCoordinates(Lnet/minecraft/world/World;Lnet/minecraft/util/ChunkCoordinates;Z)Lnet/minecraft/util/ChunkCoordinates;"))
    public ChunkCoordinates verifyRespawnCoordinates(World world, ChunkCoordinates spawnCoordinates,
        boolean forcedSpawn, Operation<ChunkCoordinates> original) {
        if (spawnWorld == 0) return original.call(world, spawnCoordinates, forcedSpawn);
        else {
            ChunkCoordinates result = EntityPlayer.verifyRespawnCoordinates(
                ((IMixinWorld) ((IMixinWorld) world).getParentWorld()).getSubWorld(spawnWorld),
                spawnCoordinates,
                forcedSpawn);
            if (result == null) {
                ((IMixinEntityPlayer) storedPlayer).setSpawnWorldID(0);
                spawnWorld = 0;
            }
            return result;
        }
    }

    @WrapOperation(
        method = "respawnPlayer",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayerMP;setLocationAndAngles(DDDFF)V"))
    public void setLocationAndAngles(EntityPlayerMP player, double x, double y, double z, float yaw, float pitch,
        Operation<Void> original) {
        if (spawnWorld == 0) original.call(player, x, y, z, yaw, pitch);
        else {
            World worldBelowFeet = ((IMixinWorld) ((IMixinWorld) player.worldObj).getParentWorld())
                .getSubWorld(spawnWorld);
            SubWorld subworld = (SubWorld) worldBelowFeet;
            Vec3 globalPos = subworld.transformToGlobal(x, y, z);
            player.setLocationAndAngles(
                globalPos.xCoord,
                globalPos.yCoord,
                globalPos.zCoord,
                yaw - (float) (subworld.getRotationYaw() % 360 / 180 * Math.PI),
                pitch);
            ((IMixinEntityPlayer) player).setSpawnWorldID(spawnWorld);
            ((IMixinEntity) player).setWorldBelowFeet(worldBelowFeet);
            subworld.registerEntityToDrag(player);
        }
    }

    @WrapOperation(
        method = "respawnPlayer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/NetHandlerPlayServer;sendPacket(Lnet/minecraft/network/Packet;)V",
            ordinal = 2))
    public void sendPacket(NetHandlerPlayServer handler, Packet packetIn, Operation<Void> original) {
        original.call(handler, ((IMixinS05PacketSpawnPosition) packetIn).setSpawnWorldID(spawnWorld));
    }

}
