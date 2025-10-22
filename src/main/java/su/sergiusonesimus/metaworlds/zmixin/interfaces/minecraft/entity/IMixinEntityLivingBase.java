package su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.entity;

public interface IMixinEntityLivingBase {

    public void setPositionLocal(double par1, double par3, double par5);

    public void setRotationLocal(float par1, float par2);

    public void setPositionAndRotationLocal(double par1, double par3, double par5, float par7, float par8);

    public void setLocationAndAnglesLocal(double par1, double par3, double par5, float par7, float par8);
}
