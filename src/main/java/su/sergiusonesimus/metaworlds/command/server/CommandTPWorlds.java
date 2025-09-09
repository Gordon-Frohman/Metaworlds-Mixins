package su.sergiusonesimus.metaworlds.command.server;

import java.util.Iterator;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

public class CommandTPWorlds extends CommandBase {

    public String getCommandName() {
        return "tpworlds";
    }

    public int getRequiredPermissionLevel() {
        return 3;
    }

    public String getCommandUsage(ICommandSender icommandsender) {
        return "commands.tpworlds.usage";
    }

    public void processCommand(ICommandSender icommandsender, String[] astring) {
        if (icommandsender instanceof EntityPlayer) {
            EntityPlayer senderPlayer = (EntityPlayer) icommandsender;
            World baseWorld = ((IMixinWorld) senderPlayer.worldObj).getParentWorld();
            Iterator<World> i$ = ((IMixinWorld) baseWorld).getSubWorlds()
                .iterator();

            while (i$.hasNext()) {
                World curWorld = (World) i$.next();
                SubWorld curSubWorld = (SubWorld) curWorld;
                double bbCenterX = (double) (curSubWorld.getMaxX() + curSubWorld.getMinX()) / 2.0D;
                double bbCenterY = (double) curSubWorld.getMaxY();
                double bbCenterZ = (double) (curSubWorld.getMaxZ() + curSubWorld.getMinZ()) / 2.0D;
                Vec3 transformedPos = curSubWorld.transformToGlobal(bbCenterX, bbCenterY, bbCenterZ);
                curSubWorld.setTranslation(
                    curSubWorld.getTranslationX() + senderPlayer.posX - transformedPos.xCoord,
                    curSubWorld.getTranslationY() + senderPlayer.posY - transformedPos.yCoord,
                    curSubWorld.getTranslationZ() + senderPlayer.posZ - transformedPos.zCoord);
            }
        }
    }
}
