package su.sergiusonesimus.metaworlds.controls;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

public class ControllerKeyServerStore implements IExtendedEntityProperties {

    public boolean ctrlDown = false;
    public boolean sDown = false;
    public boolean wDown = false;
    public boolean dDown = false;
    public boolean aDown = false;
    public boolean spaceDown = false;
    public boolean rlDown = false;
    public boolean rrDown = false;

    public void saveNBTData(NBTTagCompound compound) {}

    public void loadNBTData(NBTTagCompound compound) {}

    public void init(Entity entity, World world) {}
}
