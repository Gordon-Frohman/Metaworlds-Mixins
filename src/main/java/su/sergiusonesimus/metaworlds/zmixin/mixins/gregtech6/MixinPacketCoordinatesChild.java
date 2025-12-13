package su.sergiusonesimus.metaworlds.zmixin.mixins.gregtech6;

import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import gregapi.network.packets.PacketBlockError;
import gregapi.network.packets.PacketBlockEvent;
import gregapi.network.packets.PacketDeathPoint;
import gregapi.network.packets.PacketSound;
import gregapi.network.packets.covers.PacketSyncDataByteAndIDsAndCovers;
import gregapi.network.packets.covers.PacketSyncDataByteArrayAndIDsAndCovers;
import gregapi.network.packets.covers.PacketSyncDataIDsAndCovers;
import gregapi.network.packets.covers.PacketSyncDataIntegerAndIDsAndCovers;
import gregapi.network.packets.covers.PacketSyncDataLongAndIDsAndCovers;
import gregapi.network.packets.covers.PacketSyncDataShortAndIDsAndCovers;
import gregapi.network.packets.covervisuals.PacketSyncDataByteAndCoverVisuals;
import gregapi.network.packets.covervisuals.PacketSyncDataByteArrayAndCoverVisuals;
import gregapi.network.packets.covervisuals.PacketSyncDataCoverVisuals;
import gregapi.network.packets.covervisuals.PacketSyncDataIntegerAndCoverVisuals;
import gregapi.network.packets.covervisuals.PacketSyncDataLongAndCoverVisuals;
import gregapi.network.packets.covervisuals.PacketSyncDataShortAndCoverVisuals;
import gregapi.network.packets.data.PacketSyncDataByte;
import gregapi.network.packets.data.PacketSyncDataByteArray;
import gregapi.network.packets.data.PacketSyncDataInteger;
import gregapi.network.packets.data.PacketSyncDataLong;
import gregapi.network.packets.data.PacketSyncDataName;
import gregapi.network.packets.data.PacketSyncDataShort;
import gregapi.network.packets.ids.PacketSyncDataByteAndIDs;
import gregapi.network.packets.ids.PacketSyncDataByteArrayAndIDs;
import gregapi.network.packets.ids.PacketSyncDataIDs;
import gregapi.network.packets.ids.PacketSyncDataIntegerAndIDs;
import gregapi.network.packets.ids.PacketSyncDataLongAndIDs;
import gregapi.network.packets.ids.PacketSyncDataShortAndIDs;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

@Mixin(
    value = { PacketBlockError.class, PacketBlockEvent.class, PacketDeathPoint.class, PacketSound.class,
        PacketSyncDataByte.class, PacketSyncDataByteAndIDs.class, PacketSyncDataByteArray.class,
        PacketSyncDataByteArrayAndIDs.class, PacketSyncDataCoverVisuals.class, PacketSyncDataIDs.class,
        PacketSyncDataInteger.class, PacketSyncDataIntegerAndIDs.class, PacketSyncDataLong.class,
        PacketSyncDataLongAndIDs.class, PacketSyncDataName.class, PacketSyncDataShort.class,
        PacketSyncDataShortAndIDs.class
        // These are child classes of child classes, may have to move them to a separate mixin
        , PacketSyncDataByteAndCoverVisuals.class, PacketSyncDataByteAndIDsAndCovers.class,
        PacketSyncDataByteArrayAndCoverVisuals.class, PacketSyncDataByteArrayAndIDsAndCovers.class,
        PacketSyncDataIDsAndCovers.class, PacketSyncDataIntegerAndCoverVisuals.class,
        PacketSyncDataIntegerAndIDsAndCovers.class, PacketSyncDataLongAndCoverVisuals.class,
        PacketSyncDataLongAndIDsAndCovers.class, PacketSyncDataShortAndCoverVisuals.class,
        PacketSyncDataShortAndIDsAndCovers.class })
public class MixinPacketCoordinatesChild extends MixinPacketCoordinates {

    @ModifyVariable(method = "process", remap = false, at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private IBlockAccess assignSubworld(IBlockAccess originalWorld) {
        if (originalWorld instanceof World world && this.subworldID != 0) {
            World subworld = ((IMixinWorld) world).getSubWorld(subworldID);
            if (subworld != null) return subworld;
        }
        return originalWorld;
    }

}
