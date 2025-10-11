package su.sergiusonesimus.metaworlds.integrations;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;

import su.sergiusonesimus.metaworlds.entity.player.EntityPlayerProxy;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

public class ForgeMultipartIntegration {

    public static MovingObjectPosition currentMOP;

    public static int getSubworldSpecificEntityId(EntityPlayer player) {
        return player.getEntityId()
            | (player instanceof EntityPlayerProxy ? ((IMixinWorld) player.worldObj).getSubWorldID() << 16 : 0);
    }

}
