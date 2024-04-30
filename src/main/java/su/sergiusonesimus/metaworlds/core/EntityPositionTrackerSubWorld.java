package su.sergiusonesimus.metaworlds.core;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EntityPositionTrackerSubWorld extends Entity {

    Entity trackedEntity = null;

    public EntityPositionTrackerSubWorld(World par1World) {
        super(par1World);
    }

    protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {}

    protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {}

    protected void entityInit() {
        this.dataWatcher.addObject(23, new Integer(0));
    }

    public void onUpdate() {
        super.onUpdate();
        if (this.trackedEntity == null) {
            ;
        }
    }
}
