package su.sergiusonesimus.metaworlds.controls;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

public class ControllerKeyServerStore implements IExtendedEntityProperties {

    public boolean upPressed = false;
    public boolean downPressed = false;

    public boolean forwardPressed = false;
    public boolean backwardPressed = false;
    public boolean leftPressed = false;
    public boolean rightPressed = false;

    public boolean rollForwardPressed = false;
    public boolean rollBackwardPressed = false;
    public boolean rollLeftPressed = false;
    public boolean rollRightPressed = false;

    public void saveNBTData(NBTTagCompound compound) {}

    public void loadNBTData(NBTTagCompound compound) {}

    public void init(Entity entity, World world) {}
}
