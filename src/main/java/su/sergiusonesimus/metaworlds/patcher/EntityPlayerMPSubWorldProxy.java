package su.sergiusonesimus.metaworlds.patcher;

import java.util.ArrayList;
import java.util.Iterator;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.server.S13PacketDestroyEntities;
import net.minecraft.network.play.server.S26PacketMapChunkBulk;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkWatchEvent;
import su.sergiusonesimus.metaworlds.api.IMixinEntity;
import su.sergiusonesimus.metaworlds.api.IMixinWorld;
import su.sergiusonesimus.metaworlds.api.SubWorld;
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

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void onUpdate()
    {
        this.theItemInWorldManager.updateBlockRemoving();
        --this.field_147101_bU;

        if (this.hurtResistantTime > 0)
        {
            --this.hurtResistantTime;
        }

        this.openContainer.detectAndSendChanges();

        if (!this.worldObj.isRemote && !ForgeHooks.canInteractWith(this, this.openContainer))
        {
            this.closeScreen();
            this.openContainer = this.inventoryContainer;
        }

        while (!this.destroyedItemsNetCache.isEmpty())
        {
            int i = Math.min(this.destroyedItemsNetCache.size(), 127);
            int[] aint = new int[i];
            Iterator iterator = this.destroyedItemsNetCache.iterator();
            int j = 0;

            while (iterator.hasNext() && j < i)
            {
                aint[j++] = ((Integer)iterator.next()).intValue();
                iterator.remove();
            }

            this.playerNetServerHandler.sendPacket(new S13PacketDestroyEntities(aint));
        }

        if (!this.loadedChunks.isEmpty())
        {
            ArrayList arraylist = new ArrayList();
            Iterator iterator1 = this.loadedChunks.iterator();
            ArrayList arraylist1 = new ArrayList();
            Chunk chunk;

            while (iterator1.hasNext() && arraylist.size() < S26PacketMapChunkBulk.func_149258_c())
            {
                ChunkCoordIntPair chunkcoordintpair = (ChunkCoordIntPair)iterator1.next();

                if (chunkcoordintpair != null)
                {
                    if (this.worldObj.blockExists(chunkcoordintpair.chunkXPos << 4, 0, chunkcoordintpair.chunkZPos << 4))
                    {
                        chunk = this.worldObj.getChunkFromChunkCoords(chunkcoordintpair.chunkXPos, chunkcoordintpair.chunkZPos);

                        if (chunk.func_150802_k())
                        {
                            arraylist.add(chunk);
                            arraylist1.addAll(((WorldServer)this.worldObj).func_147486_a(chunkcoordintpair.chunkXPos * 16, 0, chunkcoordintpair.chunkZPos * 16, chunkcoordintpair.chunkXPos * 16 + 15, 256, chunkcoordintpair.chunkZPos * 16 + 15));
                            //BugFix: 16 makes it load an extra chunk, which isn't associated with a player, which makes it not unload unless a player walks near it.
                            iterator1.remove();
                        }
                    }
                }
                else
                {
                    iterator1.remove();
                }
            }

            if (!arraylist.isEmpty())
            {
            	this.playerNetServerHandler.sendPacket(new S26PacketMapChunkBulk(arraylist));
                Iterator iterator2 = arraylist1.iterator();

                while (iterator2.hasNext())
                {
                    TileEntity tileentity = (TileEntity)iterator2.next();
                    this.func_147097_b(tileentity);
                }

                iterator2 = arraylist.iterator();

                while (iterator2.hasNext())
                {
                    chunk = (Chunk)iterator2.next();
                    this.getServerForPlayer().getEntityTracker().func_85172_a(this, chunk);
                    MinecraftForge.EVENT_BUS.post(new ChunkWatchEvent.Watch(chunk.getChunkCoordIntPair(), this));
                }
            }
        }
    }
}
