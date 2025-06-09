package su.sergiusonesimus.metaworlds.api;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;

import su.sergiusonesimus.metaworlds.world.SubWorldInfoHolder;

public class SubWorldTypeManager {

    public static final String SUBWORLD_TYPE_DEFAULT = "default";
    public static final String SUBWORLD_TYPE_BOAT = "boat";

    public static final SubWorldInfoProvider DEFAULT_INFO_PROVIDER = new SubWorldInfoProvider();

    private static Map<String, SubWorldInfoProvider> subworldTypes = new HashMap<String, SubWorldInfoProvider>();

    public static void registerSubWorldType(String subworldType) {
        subworldTypes.put(subworldType, DEFAULT_INFO_PROVIDER);
    }

    public static void registerSubWorldType(String subworldType, SubWorldInfoProvider subworldInfoProvider) {
        subworldTypes.put(subworldType, subworldInfoProvider);
    }

    public static int getTypeID(String subworldType) {
        int i = 0;
        for (String type : subworldTypes.keySet()) {
            if (type == subworldType) return i;
            i++;
        }
        return 0;
    }

    public static String getTypeByID(int id) {
        int i = 0;
        for (String type : subworldTypes.keySet()) {
            if (i == id) return type;
            i++;
        }
        return SUBWORLD_TYPE_DEFAULT;
    }

    public static SubWorldInfoProvider getSubWorldInfoProvider(String subworldType) {
        SubWorldInfoProvider result = subworldTypes.get(subworldType);
        return result == null ? DEFAULT_INFO_PROVIDER : result;
    }

    public static SubWorldInfoProvider getSubWorldInfoProvider(NBTTagCompound sourceNBT) {
        return getSubWorldInfoProvider(sourceNBT.getString("subWorldType"));
    }

    public static SubWorldInfoProvider getSubWorldInfoProvider(SubWorld sourceWorld) {
        return getSubWorldInfoProvider(sourceWorld.getSubWorldType());
    }

    public static SubWorldInfoHolder getSubWorldInfoHolder(SubWorld sourceWorld) {
        return getSubWorldInfoProvider(sourceWorld).fromSubworld(sourceWorld);
    }

    public static SubWorldInfoHolder getSubWorldInfoHolder(NBTTagCompound sourceNBT) {
        return getSubWorldInfoProvider(sourceNBT.getString("subWorldType")).fromNBT(sourceNBT);
    }

    public static class SubWorldInfoProvider {

        public SubWorldInfoHolder fromSubworld(SubWorld sourceWorld) {
            return new SubWorldInfoHolder(sourceWorld);
        }

        public SubWorldInfoHolder fromNBT(NBTTagCompound sourceNBT) {
            return new SubWorldInfoHolder(sourceNBT);
        }

    }

}
