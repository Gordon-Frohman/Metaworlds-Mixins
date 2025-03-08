package su.sergiusonesimus.metaworlds.command.server;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import cpw.mods.fml.common.network.internal.FMLNetworkHandler;

public class CommandMWAdmin extends CommandBase {

    public String getCommandName() {
        return "mwc";
    }

    public int getRequiredPermissionLevel() {
        return 3;
    }

    public String getCommandUsage(ICommandSender var1) {
        return "/mwc Opens a MetaWorlds administration GUI";
    }

    public void processCommand(ICommandSender icommandsender, String[] var2) {
        if (icommandsender instanceof EntityPlayer) {
            EntityPlayer senderPlayer = (EntityPlayer) icommandsender;
            FMLNetworkHandler.openGui(senderPlayer, "MetaworldsMod", 0, (World) null, 0, 0, 0);
        }
    }
}
