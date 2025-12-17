package su.sergiusonesimus.metaworlds.event;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityEvent;

public class EntityDisplacementEvent extends EntityEvent {

    /** The world from which the entity is being removed */
    public final World sourceWorld;
    /** The world in which the entity is being spawned */
    public final World targetWorld;

    /**
     * An event fired whenever an entity is displaced from main world to a subworld and vice versa. <br>
     * Used for entity rotation on subworld reintegration. <br>
     * Fire this event if you are implementing a custom subworld creation/reintegration technique.
     * 
     * @param entity
     * @param sourceWorld - The world from which the entity is being removed
     * @param targetWorld - The world in which the entity is being spawned
     */
    public EntityDisplacementEvent(Entity entity, World sourceWorld, World targetWorld) {
        super(entity);
        this.sourceWorld = sourceWorld;
        this.targetWorld = targetWorld;
    }

}
