package su.sergiusonesimus.metaworlds.mixin.mixins.server.management;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerManager;
import net.minecraft.util.LongHashMap;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import su.sergiusonesimus.metaworlds.api.IMixinEntity;
import su.sergiusonesimus.metaworlds.api.IMixinWorld;
import su.sergiusonesimus.metaworlds.api.PlayerManagerSuperClass;
import su.sergiusonesimus.metaworlds.patcher.EntityPlayerMPSubWorldProxy;

import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PlayerManager.class)
public abstract class MixinPlayerManager implements PlayerManagerSuperClass {

    @Shadow(remap = true)
    public static Logger field_152627_a;

    @Shadow(remap = true)
    private List players;

    @Shadow(remap = true)
    private int playerViewRadius;

    @Shadow(remap = true)
    private WorldServer theWorldServer;

    @Shadow(remap = true)
    private LongHashMap playerInstances;

    @Shadow(remap = true)
    private List playerInstanceList;

    @Shadow(remap = true)
    public abstract WorldServer getWorldServer();

    @Shadow(remap = true)
    public abstract PlayerManager.PlayerInstance getOrCreateChunkWatcher(int chunkXPos, int chunkZPos, boolean b);

    @Override
    public List getPlayers() {
        return this.players;
    }

    /**
     * Adds an EntityPlayerMP to the PlayerManager.
     */
    @Overwrite
    public void addPlayer(EntityPlayerMP p_72683_1_) {
        int i = (int) p_72683_1_.posX >> 4;
        int j = (int) p_72683_1_.posZ >> 4;
        p_72683_1_.managedPosX = p_72683_1_.posX;
        p_72683_1_.managedPosZ = p_72683_1_.posZ;
        // Load nearby chunks first
        List<ChunkCoordIntPair> chunkList = new ArrayList<ChunkCoordIntPair>();

        for (int k = i - this.playerViewRadius; k <= i + this.playerViewRadius; ++k) {
            for (int l = j - this.playerViewRadius; l <= j + this.playerViewRadius; ++l) {
                chunkList.add(new ChunkCoordIntPair(k, l));
            }
        }

        java.util.Collections.sort(chunkList, new net.minecraftforge.common.util.ChunkCoordComparator(p_72683_1_));

        for (ChunkCoordIntPair pair : chunkList) {
            if (((IMixinWorld) (Object) this.theWorldServer).isChunkWatchable(pair.chunkXPos, pair.chunkZPos))
                this.getOrCreateChunkWatcher(pair.chunkXPos, pair.chunkZPos, true)
                    .addPlayer(p_72683_1_);
        }

        this.players.add(p_72683_1_);
        ((PlayerManager) (Object) this).filterChunkLoadQueue(p_72683_1_);

        for (World curSubWorld : ((IMixinWorld) (Object) this.theWorldServer).getSubWorlds()) {
            EntityPlayer proxyPlayer = ((IMixinEntity) (Object) p_72683_1_).getProxyPlayer(curSubWorld);

            if (proxyPlayer == null) {
                proxyPlayer = new EntityPlayerMPSubWorldProxy(p_72683_1_, curSubWorld);
                // TODO: newManager.setGameType(this.getGameType()); make this synchronized over all proxies and the
                // real player
                ((EntityPlayerMP) proxyPlayer).theItemInWorldManager.setWorld((WorldServer) curSubWorld);
            }

            ((WorldServer) curSubWorld).getPlayerManager()
                .addPlayer((EntityPlayerMP) proxyPlayer);
        }
    }

    @Override
    public void addWatchableChunks(List<ChunkCoordIntPair> chunksToAdd) {
        for (Object curPlayer : this.players) {
            EntityPlayerMP curPlayerEntity = (EntityPlayerMP) curPlayer;
            int playerChunkX = (int) (double) curPlayerEntity.managedPosX >> 4;
            int playerChunkZ = (int) (double) curPlayerEntity.managedPosZ >> 4;
            for (ChunkCoordIntPair curCoordPair : chunksToAdd) {
                if (curCoordPair.chunkXPos >= playerChunkX - this.playerViewRadius
                    && curCoordPair.chunkXPos <= playerChunkX + this.playerViewRadius
                    && curCoordPair.chunkZPos >= playerChunkZ - this.playerViewRadius
                    && curCoordPair.chunkZPos <= playerChunkZ + this.playerViewRadius)
                    this.getOrCreateChunkWatcher(curCoordPair.chunkXPos, curCoordPair.chunkZPos, true)
                        .addPlayer(curPlayerEntity);
            }
        }
    }

    @Override
    public void removeWatchableChunks(List<ChunkCoordIntPair> chunksToRemove) {
        for (Object curPlayer : this.players) {
            EntityPlayerMP curPlayerEntity = (EntityPlayerMP) curPlayer;
            int playerChunkX = (int) (double) curPlayerEntity.managedPosX >> 4;
            int playerChunkZ = (int) (double) curPlayerEntity.managedPosZ >> 4;
            for (ChunkCoordIntPair curCoordPair : chunksToRemove) {
                if (curCoordPair.chunkXPos >= playerChunkX - this.playerViewRadius
                    && curCoordPair.chunkXPos <= playerChunkX + this.playerViewRadius
                    && curCoordPair.chunkZPos >= playerChunkZ - this.playerViewRadius
                    && curCoordPair.chunkZPos <= playerChunkZ + this.playerViewRadius) {
                    PlayerManager.PlayerInstance watchToRemove = this
                        .getOrCreateChunkWatcher(curCoordPair.chunkXPos, curCoordPair.chunkZPos, false);
                    if (watchToRemove != null) watchToRemove.removePlayer(curPlayerEntity);
                }
            }
        }
    }

}
