package su.sergiusonesimus.metaworlds.zmixin.interfaces.littletiles;

import net.minecraft.world.World;

import com.creativemd.littletiles.common.packet.LittleBlockPacket;

public interface IMixinMixinLittleBlockPacket {

    LittleBlockPacket setSubworld(World world);

    LittleBlockPacket setSubworld(int id);

}
