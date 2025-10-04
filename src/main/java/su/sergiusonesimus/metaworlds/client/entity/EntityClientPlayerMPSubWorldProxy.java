package su.sergiusonesimus.metaworlds.client.entity;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.common.MinecraftForge;

import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.client.MinecraftSubWorldProxy;
import su.sergiusonesimus.metaworlds.client.multiplayer.PlayerControllerMPSubWorldProxy;
import su.sergiusonesimus.metaworlds.client.network.NetHandlerPlayClientSubWorldProxy;
import su.sergiusonesimus.metaworlds.client.renderer.RenderGlobalSubWorld;
import su.sergiusonesimus.metaworlds.entity.player.EntityPlayerProxy;
import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.IMixinEntity;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.player.IMixinEntityPlayer;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

public class EntityClientPlayerMPSubWorldProxy extends EntityClientPlayerMP implements EntityPlayerProxy {

    private EntityClientPlayerMP realPlayer;

    private EntityClientPlayerMPSubWorldProxy(EntityClientPlayerMP originalPlayer, World targetSubWorld,
        MinecraftSubWorldProxy minecraftProxy) {
        super(
            minecraftProxy,
            targetSubWorld,
            Minecraft.getMinecraft()
                .getSession(),
            new NetHandlerPlayClientSubWorldProxy(
                minecraftProxy,
                originalPlayer.sendQueue,
                (WorldClient) targetSubWorld),
            originalPlayer.getStatFileWriter());
    }

    public EntityClientPlayerMPSubWorldProxy(EntityClientPlayerMP originalPlayer, World targetSubWorld) {
        this(originalPlayer, targetSubWorld, new MinecraftSubWorldProxy(Minecraft.getMinecraft()));

        this.realPlayer = originalPlayer;
        this.dimension = this.realPlayer.dimension;
        this.setEntityId(this.realPlayer.getEntityId());
        this.inventory = this.realPlayer.inventory;
        this.inventoryContainer = this.realPlayer.inventoryContainer;
        this.capabilities = this.realPlayer.capabilities;

        this.preventEntitySpawning = false;

        this.mc.thePlayer = this;
        this.mc.theWorld = (WorldClient) targetSubWorld;
        this.mc.playerController = new PlayerControllerMPSubWorldProxy(Minecraft.getMinecraft().playerController, this);
        this.mc.effectRenderer = new EffectRenderer(targetSubWorld, Minecraft.getMinecraft().renderEngine);
        this.mc.renderGlobal = new RenderGlobalSubWorld(this.mc, Minecraft.getMinecraft().renderGlobal);

        this.mc.theWorld.mc = this.mc;

        ((NetHandlerPlayClientSubWorldProxy) this.sendQueue).proxyPlayer = this;

        ((IMixinEntity) this.realPlayer).getPlayerProxyMap()
            .put(((IMixinWorld) targetSubWorld).getSubWorldID(), this);
    }

    public int hashCode() {
        return this.realPlayer == null ? null : this.realPlayer.hashCode();
    }

    public NBTTagCompound getEntityData() {
        return this.realPlayer == null ? null : this.realPlayer.getEntityData();
    }

    public String registerExtendedProperties(String identifier, IExtendedEntityProperties properties) {
        return this.realPlayer == null ? null : this.realPlayer.registerExtendedProperties(identifier, properties);
    }

    public IExtendedEntityProperties getExtendedProperties(String identifier) {
        return this.realPlayer == null ? null : this.realPlayer.getExtendedProperties(identifier);
    }

    @Override
    public NetHandlerPlayClient getNetHandlerProxy() {
        return this.sendQueue;
    }

    public Minecraft getMinecraft() {
        return this.mc;
    }

    @Override
    public EntityPlayer getRealPlayer() {
        return this.realPlayer;
    }

    @Override
    public void travelToDimension(int par1) {
        this.realPlayer.travelToDimension(par1);
    }

    @Override
    public void onUpdate() {
        // super.onUpdate();
    }

    @Override
    public EntityPlayer.EnumStatus sleepInBedAt(int x, int y, int z) {
        Vec3 worldDir = ((SubWorld) this.worldObj).rotateToGlobal(0, -1, 0);
        if (Direction.getNearest(worldDir) != Direction.DOWN) return EntityPlayer.EnumStatus.OTHER_PROBLEM;

        EntityPlayer.EnumStatus result = super.sleepInBedAt(x, y, z);
        if (result == EntityPlayer.EnumStatus.OK) {
            EntityPlayer player = this.getRealPlayer();

            if (player.isRiding()) {
                player.mountEntity(null);
            }

            player.setSize(0.2F, 0.2F);
            player.yOffset = 0.2F;

            Vec3 globalPos;
            if (this.worldObj.blockExists(x, y, z)) {
                int l = worldObj.getBlock(x, y, z)
                    .getBedDirection(worldObj, x, y, z);
                float f1 = 0.5F;
                float f = 0.5F;

                switch (l) {
                    case 0:
                        f = 0.9F;
                        break;
                    case 1:
                        f1 = 0.1F;
                        break;
                    case 2:
                        f = 0.1F;
                        break;
                    case 3:
                        f1 = 0.9F;
                }

                player.func_71013_b(l);
                globalPos = ((IMixinWorld) this.worldObj)
                    .transformToGlobal((float) x + f1, (float) y + 0.9375F, (float) z + f);
            } else {
                globalPos = ((IMixinWorld) this.worldObj)
                    .transformToGlobal((float) x + 0.5F, (float) y + 0.9375F, (float) z + 0.5F);
            }
            player.setPosition(globalPos.xCoord, globalPos.yCoord, globalPos.zCoord);

            player.sleeping = true;
            player.sleepTimer = 0;
            player.playerLocation = ((SubWorld) this.worldObj).transformBlockToGlobal(x, y, z);
            player.motionX = player.motionZ = player.motionY = 0.0D;
        }

        return result;
    }

    public void wakeUpPlayer(boolean wakeImmediatly, boolean updateWorldFlag, boolean setSpawn) {
        MinecraftForge.EVENT_BUS.post(
            new net.minecraftforge.event.entity.player.PlayerWakeUpEvent(
                this,
                wakeImmediatly,
                updateWorldFlag,
                setSpawn));
        this.setSize(0.6F, 1.8F);
        this.resetHeight();
        ChunkCoordinates chunkcoordinates = this.playerLocation;
        ChunkCoordinates chunkcoordinates1 = this.playerLocation;
        Block block = (chunkcoordinates == null ? null
            : worldObj.getBlock(chunkcoordinates.posX, chunkcoordinates.posY, chunkcoordinates.posZ));

        if (chunkcoordinates != null
            && block.isBed(worldObj, chunkcoordinates.posX, chunkcoordinates.posY, chunkcoordinates.posZ, this)) {
            block.setBedOccupied(
                this.worldObj,
                chunkcoordinates.posX,
                chunkcoordinates.posY,
                chunkcoordinates.posZ,
                this,
                false);
            chunkcoordinates1 = block.getBedSpawnPosition(
                this.worldObj,
                chunkcoordinates.posX,
                chunkcoordinates.posY,
                chunkcoordinates.posZ,
                this);

            if (chunkcoordinates1 == null) {
                chunkcoordinates1 = new ChunkCoordinates(
                    chunkcoordinates.posX,
                    chunkcoordinates.posY + 1,
                    chunkcoordinates.posZ);
            }

            Vec3 globalPos = ((IMixinWorld) this.worldObj).transformToGlobal(
                (double) ((float) chunkcoordinates1.posX + 0.5F),
                (double) ((float) chunkcoordinates1.posY + this.yOffset + 0.1F),
                (double) ((float) chunkcoordinates1.posZ + 0.5F));
            this.getRealPlayer()
                .setPosition(globalPos.xCoord, globalPos.yCoord, globalPos.zCoord);

            if (setSpawn) {
                ((IMixinEntityPlayer) this.getRealPlayer())
                    .setSpawnWorldID(((IMixinWorld) this.worldObj).getSubWorldID());
                this.getRealPlayer()
                    .setSpawnChunk(this.playerLocation, false);
            }
        }

        this.sleeping = false;

        if (!this.worldObj.isRemote && updateWorldFlag) {
            this.worldObj.updateAllPlayersSleepingFlag();
        }

        if (wakeImmediatly) {
            this.sleepTimer = 0;
        } else {
            this.sleepTimer = 100;
        }

        if (setSpawn) {
            this.setSpawnChunk(this.playerLocation, false);
        }
    }
}
