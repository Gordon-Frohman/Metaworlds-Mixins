package su.sergiusonesimus.metaworlds.util;

import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import codechicken.lib.math.MathHelper;
import su.sergiusonesimus.metaworlds.EventHookContainer;
import su.sergiusonesimus.metaworlds.MetaworldsMod;
import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.event.BlockDisplacementEvent;
import su.sergiusonesimus.metaworlds.event.EntityDisplacementEvent;
import su.sergiusonesimus.metaworlds.event.MetaworldsEventFactory;
import su.sergiusonesimus.metaworlds.integrations.ForgeMultipartIntegration;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

public class DisplacementHelper {

    /**
     * Creates a copy of a source world block on specified coordinates in a corresponding place in target world. <br>
     * Original block is not being removed. You should remove it separately. <br>
     * Also displaces tile entities, minecarts and hanging entities. Those are being removed, so there's no need to do
     * it separately.
     * 
     * @param x
     * @param y
     * @param z
     * @param sourceWorld
     * @param targetWorld
     */
    public static void displaceBlock(int x, int y, int z, World sourceWorld, World targetWorld) {
        displaceBlock(x, y, z, sourceWorld, targetWorld, null);
    }

    /**
     * Creates a copy of a source world block on specified coordinates in a corresponding place in target world. <br>
     * Original block is not being removed. You should remove it separately. <br>
     * Also displaces tile entities, minecarts and hanging entities. Those are being removed, so there's no need to do
     * it separately.
     * 
     * @param x
     * @param y
     * @param z
     * @param sourceWorld
     * @param targetWorld
     * @param customDisplacement - Functional interface in case if you need to implement custom displacement logic. Used
     *                           by ReCreate.
     */
    public static void displaceBlock(int x, int y, int z, World sourceWorld, World targetWorld,
        CustomBlockDisplacement customDisplacement) {
        Block block = sourceWorld.getBlock(x, y, z);
        int oldMeta = sourceWorld.getBlockMetadata(x, y, z);

        if (customDisplacement != null
            && customDisplacement.tryDisplaceBlock(sourceWorld, targetWorld, x, y, z, block, oldMeta)) return;

        BlockDisplacementEvent event = MetaworldsEventFactory.onBlockDisplacement(x, y, z, sourceWorld, targetWorld);
        Vec3 newCoords = ((IMixinWorld) sourceWorld).transformLocalToOther(targetWorld, x + 0.5D, y + 0.5D, z + 0.5D);
        int newX = MathHelper.floor_double(newCoords.xCoord);
        int newY = MathHelper.floor_double(newCoords.yCoord);
        int newZ = MathHelper.floor_double(newCoords.zCoord);
        int newMeta = event.blockMetadata;
        targetWorld.setBlock(newX, newY, newZ, block, newMeta, 3);
        targetWorld.setBlockMetadataWithNotify(newX, newY, newZ, newMeta, 3);
        if (block.hasTileEntity(oldMeta)) {
            TileEntity oldTE = event.tileEntity;
            NBTTagCompound nbttag = new NBTTagCompound();
            oldTE.writeToNBT(nbttag);
            oldTE.invalidate();
            boolean blockIsMultipart = MetaworldsMod.isForgeMultipartLoaded
                && ForgeMultipartIntegration.isBlockMultipart(block);
            TileEntity newTE;
            if (blockIsMultipart) {
                newTE = ForgeMultipartIntegration.createTileEntityFromNBT(nbttag);
            } else {
                newTE = TileEntity.createAndLoadEntity(nbttag);
            }
            targetWorld.setTileEntity(newX, newY, newZ, newTE);
            if (blockIsMultipart) {
                if (targetWorld instanceof SubWorld) {
                    final TileEntity teToSend = newTE;
                    EventHookContainer.scheduleTask(targetWorld, new Runnable() {

                        @Override
                        public void run() {
                            ForgeMultipartIntegration.sendMultipartUpdate(targetWorld, teToSend);
                        }

                    }, 30); // Providing enough delay to let the client world load all the chunks
                } else {
                    ForgeMultipartIntegration.sendMultipartUpdate(targetWorld, newTE);
                }
            }
        }
        List<Entity> entities = sourceWorld.getEntitiesWithinAABBExcludingEntity(
            null,
            AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1, z + 1)
                .expand(0.25, 0.25, 0.25));
        Iterator<Entity> j$ = entities.iterator();
        Entity entity;
        while (j$.hasNext()) {
            entity = j$.next();
            if ((entity instanceof EntityMinecart && entity.posX >= x
                && entity.posX <= (x + 1)
                && entity.posZ >= z
                && entity.posZ <= (z + 1)
                && entity.posY >= (y + block.getBlockBoundsMaxY()))
                || (entity instanceof EntityHanging hanging
                    && (hanging.field_146063_b == x && hanging.field_146064_c == y && hanging.field_146062_d == z))) {
                displaceEntity(entity, sourceWorld, targetWorld);
            }
        }
    }

    public static void displaceEntity(Entity entity, World sourceWorld, World targetWorld) {
        if (entity instanceof EntityPlayer) return;
        Entity newEntity = EntityList.createEntityByName(EntityList.getEntityString(entity), targetWorld);
        newEntity.copyDataFrom(entity, true);
        Vec3 newCoords = ((IMixinWorld) sourceWorld).transformLocalToOther(targetWorld, entity);
        newEntity.setLocationAndAngles(
            newCoords.xCoord,
            newCoords.yCoord,
            newCoords.zCoord,
            newEntity.rotationYaw,
            newEntity.rotationPitch);
        MinecraftForge.EVENT_BUS.post(new EntityDisplacementEvent(newEntity, sourceWorld, targetWorld));
        targetWorld.spawnEntityInWorld(newEntity);
        entity.setDead();
    }

    @FunctionalInterface
    public static interface CustomBlockDisplacement {

        boolean tryDisplaceBlock(World sourceWorld, World targetWorld, int x, int y, int z, Block block, int metadata);

    }

}
