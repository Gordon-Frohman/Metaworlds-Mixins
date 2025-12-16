package su.sergiusonesimus.metaworlds.api;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import su.sergiusonesimus.metaworlds.network.play.server.S01SubWorldCreatePacket;
import su.sergiusonesimus.metaworlds.world.SubWorldInfoHolder;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;

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

    public static IMessage getSubWorldCreatePacket(SubWorld sourceWorld) {
        return getSubWorldInfoProvider(sourceWorld.getSubWorldType()).getCreatePacket(sourceWorld);
    }

    public static SubWorldInfoHolder getSubWorldInfoHolder(SubWorld sourceWorld) {
        return getSubWorldInfoProvider(sourceWorld).fromSubworld(sourceWorld);
    }

    public static SubWorldInfoHolder getSubWorldInfoHolder(NBTTagCompound sourceNBT) {
        return getSubWorldInfoProvider(sourceNBT.getString("subWorldType")).fromNBT(sourceNBT);
    }

    public static class SubWorldInfoProvider {

        public World create(World parentWorld, int id) {
            return ((IMixinWorld) parentWorld).createSubWorld(id);
        }

        public IMessage getCreatePacket(SubWorld sourceWorld) {
            return new S01SubWorldCreatePacket(
                1,
                new Integer[] { Integer.valueOf(sourceWorld.getSubWorldID()) },
                new Integer[] { Integer.valueOf(SubWorldTypeManager.getTypeID(sourceWorld.getSubWorldType())) });
        }

        public SubWorldInfoHolder fromSubworld(SubWorld sourceWorld) {
            return new SubWorldInfoHolder(sourceWorld);
        }

        public SubWorldInfoHolder fromNBT(NBTTagCompound sourceNBT) {
            return new SubWorldInfoHolder(sourceNBT);
        }

    }

}
