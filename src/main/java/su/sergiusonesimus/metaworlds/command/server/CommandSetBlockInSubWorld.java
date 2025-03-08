package su.sergiusonesimus.metaworlds.command.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.command.server.CommandSetBlock;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

public class CommandSetBlockInSubWorld extends CommandSetBlock {

    public String getCommandName() {
        return "setblockinsubworld";
    }

    /*
     * public String getCommandUsage(ICommandSender sender)
     * {
     * return "commands.setblockinsubworld.usage";
     * }
     */

    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length >= 5) {
            World world = ((IMixinWorld) sender.getEntityWorld()).getSubWorld(Integer.parseInt(args[0]));

            if (world == null) {
                throw new CommandException("commands.setblockinsubworld.nosuchworld", new Object[0]);
            } else {
                int i = sender.getPlayerCoordinates().posX;
                int j = sender.getPlayerCoordinates().posY;
                int k = sender.getPlayerCoordinates().posZ;

                Vec3 playerCoords = ((IMixinWorld) world).transformToLocal(Vec3.createVectorHelper(i, j, k));

                i = (int) playerCoords.xCoord;
                j = (int) playerCoords.yCoord;
                k = (int) playerCoords.zCoord;

                i = MathHelper.floor_double(func_110666_a(sender, (double) i, args[1]));
                j = MathHelper.floor_double(func_110666_a(sender, (double) j, args[2]));
                k = MathHelper.floor_double(func_110666_a(sender, (double) k, args[3]));
                Block block = CommandBase.getBlockByText(sender, args[4]);
                int l = 0;

                if (args.length >= 6) {
                    l = parseIntBounded(sender, args[5], 0, 15);
                }

                if (!world.blockExists(i, j, k)) {
                    throw new CommandException("commands.setblock.outOfWorld", new Object[0]);
                } else {
                    NBTTagCompound nbttagcompound = new NBTTagCompound();
                    boolean flag = false;

                    if (args.length >= 8 && block.hasTileEntity()) {
                        String s = func_147178_a(sender, args, 7).getUnformattedText();

                        try {
                            NBTBase nbtbase = JsonToNBT.func_150315_a(s);

                            if (!(nbtbase instanceof NBTTagCompound)) {
                                throw new CommandException(
                                    "commands.setblock.tagError",
                                    new Object[] { "Not a valid tag" });
                            }

                            nbttagcompound = (NBTTagCompound) nbtbase;
                            flag = true;
                        } catch (NBTException nbtexception) {
                            throw new CommandException(
                                "commands.setblock.tagError",
                                new Object[] { nbtexception.getMessage() });
                        }
                    }

                    if (args.length >= 7) {
                        if (args[6].equals("destroy")) {
                            world.func_147480_a(i, j, k, true);
                        } else if (args[6].equals("keep") && !world.isAirBlock(i, j, k)) {
                            throw new CommandException("commands.setblock.noChange", new Object[0]);
                        }
                    }

                    if (!world.setBlock(i, j, k, block, l, 3)) {
                        throw new CommandException("commands.setblock.noChange", new Object[0]);
                    } else {
                        if (flag) {
                            TileEntity tileentity = world.getTileEntity(i, j, k);

                            if (tileentity != null) {
                                nbttagcompound.setInteger("x", i);
                                nbttagcompound.setInteger("y", j);
                                nbttagcompound.setInteger("z", k);
                                tileentity.readFromNBT(nbttagcompound);
                            }
                        }

                        func_152373_a(sender, this, "commands.setblock.success", new Object[0]);
                    }
                }
            }
        } else {
            throw new WrongUsageException("commands.setblock.usage", new Object[0]);
        }
    }

    /**
     * Adds the strings available in this command to the given list of tab completion options.
     */
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        return args.length == 1 ? getListOfSubWorldsInWorld(sender.getEntityWorld())
            : args.length == 5 ? getListOfStringsFromIterableMatchingLastWord(args, Block.blockRegistry.getKeys())
                : (args.length == 7
                    ? getListOfStringsMatchingLastWord(args, new String[] { "replace", "destroy", "keep" })
                    : null);
    }

    public List getListOfSubWorldsInWorld(World world) {
        Set<Integer> subworlds = ((IMixinWorld) world).getSubWorldsMap()
            .keySet();
        ArrayList arraylist = new ArrayList();
        for (Integer subworld : subworlds) {
            arraylist.add(subworld.toString());
        }
        return arraylist;
    }

}
