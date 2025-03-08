package su.sergiusonesimus.metaworlds.item;

import net.minecraft.item.Item;

import cpw.mods.fml.common.registry.GameRegistry;

public class MetaworldsItems {

    public static Item emptyWorldBottleItem;
    public static Item bottledWorldItem;

    public static void registerItems() {
        emptyWorldBottleItem = (new ItemEmptyWorldBottle()).setUnlocalizedName("emptyWorldBottle")
            .setTextureName("potion_bottle_empty");
        bottledWorldItem = (new ItemBottledWorld()).setUnlocalizedName("bottledWorld")
            .setTextureName("metaworlds:item.bottledWorld");

        GameRegistry.registerItem(emptyWorldBottleItem, "emptyWorldBottle");
        GameRegistry.registerItem(bottledWorldItem, "bottledWorld");
    }

}
