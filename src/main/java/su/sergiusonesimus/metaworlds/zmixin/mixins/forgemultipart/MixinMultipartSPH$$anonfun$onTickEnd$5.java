package su.sergiusonesimus.metaworlds.zmixin.mixins.forgemultipart;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.chunk.Chunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import codechicken.lib.packet.PacketCustom;
import codechicken.multipart.handler.MultipartSPH;
import codechicken.multipart.handler.MultipartSPH$;
import scala.collection.JavaConverters;
import scala.collection.mutable.HashSet;
import scala.collection.mutable.Set;
import su.sergiusonesimus.metaworlds.entity.player.EntityPlayerProxy;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity.IMixinEntity;

@Mixin(targets = "codechicken.multipart.handler.MultipartSPH$$anonfun$onTickEnd$5")
public class MixinMultipartSPH$$anonfun$onTickEnd$5 {

    @Overwrite(remap = false)
    public void apply(EntityPlayerMP player) {
        scala.collection.mutable.Map<Object, LinkedList<ChunkCoordIntPair>> scalaNewWatchers = MultipartSPH$.MODULE$
            .codechicken$multipart$handler$MultipartSPH$$newWatchers();
        Map<Object, LinkedList<ChunkCoordIntPair>> newWatchers = JavaConverters.mapAsJavaMapConverter(scalaNewWatchers)
            .asJava();
        processPlayer(newWatchers, player);
        for (EntityPlayerProxy playerProxy : ((IMixinEntity) player).getPlayerProxyMap()
            .values()) {
            processPlayer(newWatchers, (EntityPlayer) playerProxy, player);
        }
    }

    private void processPlayer(Map<Object, LinkedList<ChunkCoordIntPair>> newWatchers, EntityPlayerMP player) {
        processPlayer(newWatchers, player, player);
    }

    private void processPlayer(Map<Object, LinkedList<ChunkCoordIntPair>> newWatchers, EntityPlayer playerToProcess,
        EntityPlayerMP targetPlayer) {
        if (newWatchers.containsKey(playerToProcess.getEntityId())) {
            LinkedList<ChunkCoordIntPair> newChunks = newWatchers.get(playerToProcess.getEntityId());

            scala.collection.mutable.HashMap<Object, Set<ChunkCoordIntPair>> scalaMap = MultipartSPH$.MODULE$
                .codechicken$multipart$handler$MultipartSPH$$chunkWatchers();
            Map<Object, Set<ChunkCoordIntPair>> chunkWatchers = JavaConverters.mapAsJavaMapConverter(scalaMap)
                .asJava();

            for (ChunkCoordIntPair chunkCoord : newChunks) {
                Chunk chunk = playerToProcess.worldObj
                    .getChunkFromChunkCoords(chunkCoord.chunkXPos, chunkCoord.chunkZPos);

                Iterator<TileEntity> tileIterator = chunk.chunkTileEntityMap.values()
                    .iterator();

                PacketCustom descPacket = MultipartSPH.getDescPacket(chunk, tileIterator);
                if (descPacket != null) {
                    descPacket.sendToPlayer(targetPlayer);
                }

                int entityID = playerToProcess.getEntityId();
                Set<ChunkCoordIntPair> watchersSet = chunkWatchers.get(entityID);
                if (watchersSet == null) {
                    watchersSet = new HashSet<ChunkCoordIntPair>();
                    scalaMap.put(entityID, watchersSet);
                }

                watchersSet.add(chunkCoord);
            }
        }
    }

}
