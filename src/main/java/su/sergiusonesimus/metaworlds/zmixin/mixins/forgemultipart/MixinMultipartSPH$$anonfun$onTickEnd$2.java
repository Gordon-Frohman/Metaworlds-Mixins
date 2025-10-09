package su.sergiusonesimus.metaworlds.zmixin.mixins.forgemultipart;

import java.util.Iterator;
import java.util.Map.Entry;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import codechicken.lib.packet.PacketCustom;
import codechicken.lib.vec.BlockCoord;
import codechicken.multipart.handler.MultipartSPH$;
import codechicken.multipart.handler.MultipartSPH.MCByteStream;
import scala.collection.JavaConverters;
import scala.collection.mutable.Map;
import scala.collection.mutable.Set;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

@Mixin(targets = "codechicken.multipart.handler.MultipartSPH$$anonfun$onTickEnd$2")
public class MixinMultipartSPH$$anonfun$onTickEnd$2 {

    /**
     * @author Sergius Onesimus
     * @reason Unable to implement a cycle for every subworld any other way. Also fuck Scala
     */
    @Overwrite(remap = false)
    public final void apply(EntityPlayerMP player) {

        java.util.Map<World, Map<BlockCoord, MCByteStream>> updateMap = JavaConverters
            .mapAsJavaMapConverter(MultipartSPH$.MODULE$.codechicken$multipart$handler$MultipartSPH$$updateMap())
            .asJava();

        java.util.Map<Object, Set<ChunkCoordIntPair>> chunkWatchers = JavaConverters
            .mapAsJavaMapConverter(MultipartSPH$.MODULE$.codechicken$multipart$handler$MultipartSPH$$chunkWatchers())
            .asJava();

        boolean createPacket = false;
        for (World world : ((IMixinWorld) player.worldObj).getWorlds()) {
            scala.collection.mutable.Map<BlockCoord, MCByteStream> worldUpdates = updateMap.get(world);
            if (worldUpdates != null && worldUpdates.nonEmpty()) {
                createPacket = true;
                break;
            }
        }
        if (!createPacket) return;

        PacketCustom packet = new PacketCustom(MultipartSPH$.MODULE$.channel(), 3).compress();
        boolean send = false;
        java.util.Map<BlockCoord, MCByteStream> worldUpdates;
        java.util.Set<ChunkCoordIntPair> watchedChunks;

        for (World world : ((IMixinWorld) player.worldObj).getWorlds()) {
            Map<BlockCoord, MCByteStream> scalaMap = updateMap.get(world);

            if (scalaMap == null || !scalaMap.nonEmpty()) {
                continue;
            }

            worldUpdates = JavaConverters.mapAsJavaMapConverter(updateMap.get(world))
                .asJava();

            Set<ChunkCoordIntPair> scalaChunks = chunkWatchers.get(player.getEntityId());

            if (scalaChunks == null) {
                continue;
            }

            watchedChunks = JavaConverters.setAsJavaSetConverter(scalaChunks)
                .asJava();

            Iterator<Entry<BlockCoord, MCByteStream>> iterator = worldUpdates.entrySet()
                .iterator();
            while (iterator.hasNext()) {
                Entry<BlockCoord, MCByteStream> entry = iterator.next();
                BlockCoord pos = entry.getKey();
                MCByteStream stream = entry.getValue();

                ChunkCoordIntPair chunkPos = new ChunkCoordIntPair(pos.x >> 4, pos.z >> 4);
                if (watchedChunks.contains(chunkPos)) {
                    send = true;
                    packet.writeInt(((IMixinWorld) world).getSubWorldID());
                    packet.writeByteArray(stream.getBytes());
                    packet.writeByte(255);
                }
            }
        }

        if (send) {
            packet.writeInt(Integer.MAX_VALUE);
            packet.sendToPlayer(player);
        }
    }

}
