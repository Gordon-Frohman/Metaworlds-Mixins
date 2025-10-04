package su.sergiusonesimus.metaworlds.zmixin.mixins.minecraft.network.play.server;

import java.io.IOException;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S05PacketSpawnPosition;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.network.play.server.IMixinS05PacketSpawnPosition;

@Mixin(S05PacketSpawnPosition.class)
public class MixinS05PacketSpawnPosition implements IMixinS05PacketSpawnPosition {

    // TODO

    private int spawnWorldID = 0;

    public int getSpawnWorldID() {
        return spawnWorldID;
    }

    public S05PacketSpawnPosition setSpawnWorldID(int ID) {
        spawnWorldID = ID;
        return (S05PacketSpawnPosition) (Object) this;
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    @Inject(method = "readPacketData", at = @At("TAIL"))
    public void readPacketData(PacketBuffer data, CallbackInfo ci) throws IOException {
        this.spawnWorldID = data.readInt();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    @Inject(method = "writePacketData", at = @At("TAIL"))
    public void writePacketData(PacketBuffer data, CallbackInfo ci) throws IOException {
        data.writeInt(this.spawnWorldID);
    }

}
