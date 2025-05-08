package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.server.management;

import java.util.Iterator;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
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

import com.google.common.base.Charsets;
import com.mojang.authlib.GameProfile;

import cpw.mods.fml.common.FMLCommonHandler;
import su.sergiusonesimus.metaworlds.compat.packet.SubWorldSpawnPositionPacket;
import su.sergiusonesimus.metaworlds.network.MetaMagicNetwork;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.IMixinEntity;
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
                        new SubWorldSpawnPositionPacket(posXOnSubWorld, posYOnSubWorld, posZOnSubWorld),
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

        Iterator iterator = player.getActivePotionEffects()
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

}
