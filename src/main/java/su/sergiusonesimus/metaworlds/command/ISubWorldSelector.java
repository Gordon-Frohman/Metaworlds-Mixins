package su.sergiusonesimus.metaworlds.command;

import net.minecraft.world.World;

import su.sergiusonesimus.metaworlds.api.SubWorld;

public interface ISubWorldSelector {

    ISubWorldSelector selectAnything = new ISubWorldSelector() {

        /**
         * Return whether the specified entity is applicable to this filter.
         */
        public boolean isSubWorldApplicable(World world) {
            return world instanceof SubWorld;
        }
    };

    /**
     * Return whether the specified entity is applicable to this filter.
     */
    boolean isSubWorldApplicable(World world);

    public static class SubWorldOfType implements ISubWorldSelector {

        private final String subworldType;

        public SubWorldOfType(String type) {
            this.subworldType = type;
        }

        /**
         * Return whether the specified entity is applicable to this filter.
         */
        public boolean isSubWorldApplicable(World world) {
            if (!(world instanceof SubWorld subworld)) return false;
            return subworld.getSubWorldType()
                .equals(subworldType);
        }
    }
}
