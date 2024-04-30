package su.sergiusonesimus.metaworlds.patcher;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.IExtendedEntityProperties;
import su.sergiusonesimus.metaworlds.api.IMixinEntity;
import su.sergiusonesimus.metaworlds.api.IMixinWorld;
import su.sergiusonesimus.metaworlds.mixin.interfaces.entity.player.IMixinEntityPlayer;

public class EntityPlayerMPSubWorldProxy extends EntityPlayerMP implements EntityPlayerProxy {

    private EntityPlayerMP realPlayer;

    public EntityPlayerMPSubWorldProxy(EntityPlayerMP originalPlayer, World targetSubWorld) {
        super(
            originalPlayer.mcServer,
            (WorldServer) targetSubWorld,
            originalPlayer.getGameProfile(),
            new ItemInWorldManagerForProxy(targetSubWorld, originalPlayer.theItemInWorldManager));
        this.realPlayer = originalPlayer;
        this.dimension = this.realPlayer.dimension;
        this.setEntityId(this.realPlayer.getEntityId());
        this.inventory = this.realPlayer.inventory;
        this.inventoryContainer = this.realPlayer.inventoryContainer;
        this.theItemInWorldManager.setGameType(this.realPlayer.theItemInWorldManager.getGameType());
        this.capabilities = this.realPlayer.capabilities;
        this.openContainer = this.inventoryContainer;
        this.preventEntitySpawning = false;
        ((IMixinEntity) this.realPlayer).getPlayerProxyMap()
            .put(Integer.valueOf(((IMixinWorld) targetSubWorld).getSubWorldID()), this);
        new NetHandlerPlayServer(
            this.mcServer,
            new NetworkManagerSubWorldProxy(
                originalPlayer.playerNetServerHandler.netManager,
                ((IMixinWorld) targetSubWorld).getSubWorldID(),
                false),
            this);
        Vec3 localPos = ((IMixinEntity) this.realPlayer).getLocalPos(this.worldObj);
        this.posX = localPos.xCoord;
        this.posY = localPos.yCoord;
        this.posZ = localPos.zCoord;
    }

    public int hashCode() {
        return this.realPlayer.hashCode();
    }

    public NBTTagCompound getEntityData() {
        return this.realPlayer.getEntityData();
    }

    public String registerExtendedProperties(String identifier, IExtendedEntityProperties properties) {
        return this.realPlayer == null ? super.registerExtendedProperties(identifier, properties)
            : this.realPlayer.registerExtendedProperties(identifier, properties);
    }

    public IExtendedEntityProperties getExtendedProperties(String identifier) {
        return this.realPlayer == null ? super.getExtendedProperties(identifier)
            : this.realPlayer.getExtendedProperties(identifier);
    }

    public NetHandlerPlayServer getNetHandlerProxy() {
        return this.playerNetServerHandler;
    }

    public EntityPlayer getRealPlayer() {
        return this.realPlayer;
    }

    public void travelToDimension(int par1) {
        this.realPlayer.travelToDimension(par1);
    }

    public void setPosition(double par1, double par3, double par5) {
        super.setPosition(par1, par3, par5);
    }

    public void moveEntity(double par1, double par3, double par5) {
        super.moveEntity(par1, par3, par5);
    }

    public void setPositionAndRotation(double par1, double par3, double par5, float par7, float par8) {
        super.setPositionAndRotation(par1, par3, par5, par7, par8);
    }

    public void setLocationAndAngles(double par1, double par3, double par5, float par7, float par8) {
        super.setLocationAndAngles(par1, par3, par5, par7, par8);
    }

    public EntityPlayer.EnumStatus sleepInBedAt(int par1, int par2, int par3) {
        EntityPlayer.EnumStatus result = super.sleepInBedAt(par1, par2, par3);
        if (result == EntityPlayer.EnumStatus.OK) {
            EntityPlayer player = this.getRealPlayer();
            ((IMixinEntityPlayer) player).setSleeping(true);
            player.playerLocation = new ChunkCoordinates(
                MathHelper.floor_double(player.posX),
                MathHelper.floor_double(player.posY),
                MathHelper.floor_double(player.posZ));
            player.worldObj.updateAllPlayersSleepingFlag();
        }

        return result;
    }
}
