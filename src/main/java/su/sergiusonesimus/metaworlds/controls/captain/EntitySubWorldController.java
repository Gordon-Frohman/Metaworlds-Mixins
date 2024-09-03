package su.sergiusonesimus.metaworlds.controls.captain;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import su.sergiusonesimus.metaworlds.api.IMixinWorld;
import su.sergiusonesimus.metaworlds.api.SubWorld;

public class EntitySubWorldController extends Entity {

    private boolean riseSubWorld;
    private boolean wasJumping;
    public World controlledWorld;
    public double startingYaw;

    public EntitySubWorldController(World par1World) {
        this(par1World, (World) null);
    }

    public EntitySubWorldController(World par1World, World par2ControlledWorld) {
        super(par1World);
        this.riseSubWorld = true;
        this.wasJumping = false;
        this.startingYaw = 0.0D;
        this.preventEntitySpawning = true;
        this.setSize(1.5F, 0.6F);
        this.yOffset = 0.0F;
        this.controlledWorld = par2ControlledWorld;
    }

    public EntitySubWorldController(World par1World, World par2ControlledWorld, double par2, double par4, double par6) {
        this(par1World, par2ControlledWorld);
        this.setPosition(par2, par4, par6);
        this.motionX = 0.0D;
        this.motionY = 0.0D;
        this.motionZ = 0.0D;
        this.prevPosX = par2;
        this.prevPosY = par4;
        this.prevPosZ = par6;
    }

    protected void writeEntityToNBT(NBTTagCompound par1NBTTagCompound) {}

    protected void readEntityFromNBT(NBTTagCompound par1NBTTagCompound) {}

    protected void entityInit() {
        this.dataWatcher.addObject(21, new Integer(0));
        this.dataWatcher.addObject(22, new Float(0.0F));
    }

    public void setControlledWorld(World newControlledWorld) {
        this.controlledWorld = newControlledWorld;
        if (this.controlledWorld != null) {
            this.dataWatcher.updateObject(21, Integer.valueOf(((IMixinWorld) this.controlledWorld).getSubWorldID()));
        } else {
            this.dataWatcher.updateObject(21, Integer.valueOf(0));
        }
    }

    public void setStartingYaw(float newStartingYaw) {
        this.startingYaw = (double) newStartingYaw;
        this.dataWatcher.updateObject(22, Float.valueOf(newStartingYaw));
    }

    public boolean interactFirst(EntityPlayer par1EntityPlayer) {
        if (this.riddenByEntity != null && this.riddenByEntity instanceof EntityPlayer
            && this.riddenByEntity != par1EntityPlayer) {
            return true;
        } else {
            if (!this.worldObj.isRemote) {
                par1EntityPlayer.mountEntity(this);
            }

            return true;
        }
    }

    public void onUpdate() {
        super.onUpdate();
        this.controlledWorld = ((IMixinWorld) this.worldObj).getSubWorld(this.dataWatcher.getWatchableObjectInt(21));
        this.startingYaw = (double) this.dataWatcher.getWatchableObjectFloat(22);
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        if (this.riddenByEntity != null && ((IMixinWorld) this.controlledWorld).isSubWorld()) {
            SubWorld subWorldObj = (SubWorld) this.controlledWorld;
            double sourceYaw = MathHelper
                .wrapAngleTo180_double(subWorldObj.getRotationYaw() + (double) this.riddenByEntity.rotationYaw);
            double destYaw = MathHelper.wrapAngleTo180_double(this.startingYaw);
            double rotationSpeed = MathHelper.wrapAngleTo180_double(destYaw - sourceYaw) * 0.05D;
            if (rotationSpeed > -0.25D && rotationSpeed < 0.25D) {
                rotationSpeed = 0.0D;
            }

            subWorldObj.setRotationYawSpeed(rotationSpeed);

            boolean jumping1 = false;
            boolean sinking = false;

            boolean left = false;
            boolean back1 = false;
            boolean forward = false;
            boolean right = false;

            boolean rollright = false;
            boolean rollleft = false;

            if (this.controlledWorld.isRemote) {
                sinking = SubWorldControllerKeyHandler.ctrl_down;
                jumping1 = SubWorldControllerKeyHandler.space_Down;

                left = SubWorldControllerKeyHandler.a_Down;
                back1 = SubWorldControllerKeyHandler.d_Down;
                forward = SubWorldControllerKeyHandler.w_Down;
                right = SubWorldControllerKeyHandler.s_Down;

                rollright = SubWorldControllerKeyHandler.rl_Down;
                rollleft = SubWorldControllerKeyHandler.rr_Down;
            } else {
                ControllerKeyServerStore newMotionY1 = (ControllerKeyServerStore) this.riddenByEntity
                    .getExtendedProperties("LCTRL");
                if (newMotionY1 != null) {
                    sinking = newMotionY1.ctrlDown;
                    jumping1 = newMotionY1.spaceDown;

                    left = newMotionY1.aDown;
                    back1 = newMotionY1.dDown;
                    forward = newMotionY1.wDown;
                    right = newMotionY1.sDown;

                    rollright = newMotionY1.rlDown;
                    rollleft = newMotionY1.rrDown;
                }
            }

            // System.out.println(sinking + "1");
            // System.out.println(jumping1 + "2");
            //
            // System.out.println(left + "3");
            // System.out.println(back1 + "4");
            // System.out.println(forward + "5");
            // System.out.println(right + "6");
            //
            // System.out.println(decelerating1 + "7");
            // System.out.println(accelerating + "8");

            double curStrafe = (double) (left ? (right ? 0 : 1) : (right ? -1 : 0));
            double curForward = (forward ? (back1 ? 0 : 1D) : (back1 ? -1D : 0));
            double curRoll = (rollright ? (rollleft ? 0 : 1D) : (rollleft ? -1D : 0));

            subWorldObj.setRotationRollSpeed(curRoll);

            boolean shouldCancelAccel = false;

            // System.out.println(subWorldObj.hasCollision());

            if (subWorldObj.hasCollision()) {
                // curStrafe = -curStrafe;
                // curForward = -curForward;
                // boolean tempSink = sinking;
                // sinking = jumping1;
                // jumping1 = tempSink;

                shouldCancelAccel = true;
            }

            double newMotionX = subWorldObj.getMotionX();
            double newMotionZ = subWorldObj.getMotionZ();
            double accelerationY;
            double newMotionY;
            double newVel;
            if (curForward == 0.0D && curStrafe == 0.0D) {
                newMotionX -= subWorldObj.getMotionX() * 0.1D;
                newMotionZ -= subWorldObj.getMotionZ() * 0.1D;
                if (newMotionX * newMotionX + newMotionZ * newMotionZ < 2.5E-5D) {
                    newMotionX = 0.0D;
                    newMotionZ = 0.0D;
                }
            } else {
                accelerationY = Math.cos((double) this.riddenByEntity.rotationYaw * Math.PI / 180.0D);
                double jumping = Math.sin((double) this.riddenByEntity.rotationYaw * Math.PI / 180.0D);
                newMotionY = Math.sqrt(curForward * curForward + curStrafe * curStrafe);
                newVel = curForward / newMotionY * 0.01D;
                double accelerationStrafe = curStrafe / newMotionY * 0.01D;
                newMotionX += -newVel * jumping + accelerationStrafe * accelerationY;
                newMotionZ += newVel * accelerationY + accelerationStrafe * jumping;
            }

            accelerationY = 0.0D;
            if (jumping1) {
                accelerationY = 0.01D;
            } else if (sinking) {
                accelerationY = -0.01D;
            } else {
                accelerationY = -subWorldObj.getMotionY() * 0.1D;
                if (Math.abs(accelerationY) < 0.005D) {
                    accelerationY = -subWorldObj.getMotionY();
                }
            }

            newMotionY = subWorldObj.getMotionY() + accelerationY;
            newVel = newMotionX * newMotionX + newMotionY * newMotionY + newMotionZ * newMotionZ;
            if (newVel > 0.36D) {
                newVel = Math.sqrt(newVel);
                newMotionX *= 0.6D / newVel;
                newMotionY *= 0.6D / newVel;
                newMotionZ *= 0.6D / newVel;
            }

            // This is also a very bad mechanism to cancel acceleration, but it's the only one I could come up with so
            // far.
            // If you can improve this to be proper collision mechanics and not just bouncing off, please, do, I'd
            // greatly appreciate it :)
            if (shouldCancelAccel) {
                newMotionX = -newMotionX;
                newMotionY = -newMotionY;
                newMotionZ = -newMotionZ;
            }

            subWorldObj.setMotion(newMotionX, newMotionY, newMotionZ);
        }

        if (this.riddenByEntity == null && !this.worldObj.isRemote) {
            this.setDead();
        }
    }

    public void updateRiderPosition() {
        if (this.riddenByEntity != null && this.riddenByEntity instanceof EntityPlayer) {
            // ((EntityPlayer)this.riddenByEntity).dismountEntity(this);
            // this.riddenByEntity.setPosition(this.posX, this.posY + 1.0D, this.posZ);
        }
    }

    public boolean shouldRenderInPass(int pass) {
        return false;
    }

    public void func_70056_a(double par1, double par3, double par5, float par7, float par8, int par9) {
        this.setPosition(par1, par3, par5);
        this.setRotation(par7, par8);
    }
}
