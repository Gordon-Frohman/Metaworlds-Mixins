package su.sergiusonesimus.metaworlds.mixin.mixins.minecraft.client.renderer;

import net.minecraft.client.renderer.EntitySorter;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.Vec3;
import su.sergiusonesimus.metaworlds.mixin.interfaces.minecraft.world.IMixinWorld;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntitySorter.class)
public class MixinEntitySorter {

    @Shadow(remap = true)
    private double entityPosX;

    @Shadow(remap = true)
    private double entityPosY;

    @Shadow(remap = true)
    private double entityPosZ;

    @Overwrite
    public int compare(WorldRenderer p_compare_1_, WorldRenderer p_compare_2_) {
        Vec3 transformedPos1 = ((IMixinWorld) p_compare_1_.worldObj)
            .transformToLocal(-this.entityPosX, -this.entityPosY, -this.entityPosZ);
        Vec3 transformedPos2 = ((IMixinWorld) p_compare_2_.worldObj)
            .transformToLocal(-this.entityPosX, -this.entityPosY, -this.entityPosZ);

        double d0 = (double) p_compare_1_.posXPlus - transformedPos1.xCoord;
        double d1 = (double) p_compare_1_.posYPlus - transformedPos1.yCoord;
        double d2 = (double) p_compare_1_.posZPlus - transformedPos1.zCoord;
        double d3 = (double) p_compare_2_.posXPlus - transformedPos2.xCoord;
        double d4 = (double) p_compare_2_.posYPlus - transformedPos2.yCoord;
        double d5 = (double) p_compare_2_.posZPlus - transformedPos2.zCoord;
        return (int) ((d0 * d0 + d1 * d1 + d2 * d2 - (d3 * d3 + d4 * d4 + d5 * d5)) * 1024.0D);
    }

}
