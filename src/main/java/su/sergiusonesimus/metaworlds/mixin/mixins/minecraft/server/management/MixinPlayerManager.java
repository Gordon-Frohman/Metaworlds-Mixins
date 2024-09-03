package su.sergiusonesimus.metaworlds.mixin.mixins.minecraft.server.management;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerManager;
import net.minecraft.util.LongHashMap;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import su.sergiusonesimus.metaworlds.api.IMixinEntity;
import su.sergiusonesimus.metaworlds.api.IMixinWorld;
import su.sergiusonesimus.metaworlds.api.PlayerManagerSuperClass;
import su.sergiusonesimus.metaworlds.patcher.EntityPlayerMPSubWorldProxy;

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

    /**
     * Removes an EntityPlayerMP from the PlayerManager.
     */
    @Overwrite
    public void removePlayer(EntityPlayerMP p_72695_1_) {
        int i = (int) p_72695_1_.managedPosX >> 4;
        int j = (int) p_72695_1_.managedPosZ >> 4;

        for (int k = i - this.playerViewRadius; k <= i + this.playerViewRadius; ++k) {
            for (int l = j - this.playerViewRadius; l <= j + this.playerViewRadius; ++l) {
                PlayerManager.PlayerInstance playerinstance = this.getOrCreateChunkWatcher(k, l, false);

                if (playerinstance != null) {
                    playerinstance.removePlayer(p_72695_1_);
                }
            }
        }

        this.players.remove(p_72695_1_);

        for (World curSubWorld : ((IMixinWorld) this.theWorldServer).getSubWorlds()) {
            EntityPlayer proxyPlayer = ((IMixinEntity) p_72695_1_).getProxyPlayer(curSubWorld);
            ((WorldServer) curSubWorld).getPlayerManager()
                .removePlayer((EntityPlayerMP) proxyPlayer);
            // Sadly we need to do this check because minecraft in inconsequentian in the order it removes the player
            // from a)the World and b)the PlayerManager
            if (!curSubWorld.playerEntities.contains(proxyPlayer)) ((IMixinEntity) p_72695_1_).getPlayerProxyMap()
                .remove(((IMixinWorld) curSubWorld).getSubWorldID());
        }
    }

    /**
     * Update which chunks the player needs info on.
     */
    @Overwrite
    public void updatePlayerPertinentChunks(EntityPlayerMP p_72685_1_) {
        int i = (int) p_72685_1_.posX >> 4;
        int j = (int) p_72685_1_.posZ >> 4;
        double d0 = p_72685_1_.managedPosX - p_72685_1_.posX;
        double d1 = p_72685_1_.managedPosZ - p_72685_1_.posZ;
        double d2 = d0 * d0 + d1 * d1;

        if (d2 >= 64.0D) {
            int k = (int) p_72685_1_.managedPosX >> 4;
            int l = (int) p_72685_1_.managedPosZ >> 4;
            int i1 = this.playerViewRadius;
            int j1 = i - k;
            int k1 = j - l;
            List<ChunkCoordIntPair> chunksToLoad = new ArrayList<ChunkCoordIntPair>();

            if (j1 != 0 || k1 != 0) {
                for (int l1 = i - i1; l1 <= i + i1; ++l1) {
                    for (int i2 = j - i1; i2 <= j + i1; ++i2) {
                        if (!((PlayerManager) (Object) this).overlaps(l1, i2, k, l, i1)
                            && ((IMixinWorld) this.theWorldServer).isChunkWatchable(l1, i2)) {
                            chunksToLoad.add(new ChunkCoordIntPair(l1, i2));
                        }

                        if (!((PlayerManager) (Object) this).overlaps(l1 - j1, i2 - k1, i, j, i1)
                            && ((IMixinWorld) this.theWorldServer).isChunkWatchable(l1 - j1, i2 - k1)) {
                            PlayerManager.PlayerInstance playerinstance = this
                                .getOrCreateChunkWatcher(l1 - j1, i2 - k1, false);

                            if (playerinstance != null) {
                                playerinstance.removePlayer(p_72685_1_);
                            }
                        }
                    }
                }

                ((PlayerManager) (Object) this).filterChunkLoadQueue(p_72685_1_);
                p_72685_1_.managedPosX = p_72685_1_.posX;
                p_72685_1_.managedPosZ = p_72685_1_.posZ;
                // send nearest chunks first
                java.util.Collections
                    .sort(chunksToLoad, new net.minecraftforge.common.util.ChunkCoordComparator(p_72685_1_));

                for (ChunkCoordIntPair pair : chunksToLoad) {
                    this.getOrCreateChunkWatcher(pair.chunkXPos, pair.chunkZPos, true)
                        .addPlayer(p_72685_1_);
                }

                if (i1 > 1 || i1 < -1 || j1 > 1 || j1 < -1) {
                    java.util.Collections.sort(
                        p_72685_1_.loadedChunks,
                        new net.minecraftforge.common.util.ChunkCoordComparator(p_72685_1_));
                }
            }
        }

        for (World curSubWorld : ((IMixinWorld) this.theWorldServer).getSubWorlds()) {
            ((WorldServer) curSubWorld).getPlayerManager()
                .updatePlayerPertinentChunks((EntityPlayerMP) ((IMixinEntity) p_72685_1_).getProxyPlayer(curSubWorld));
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
