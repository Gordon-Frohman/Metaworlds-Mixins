package su.sergiusonesimus.metaworlds.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

import su.sergiusonesimus.metaworlds.block.MetaworldsBlocks;

public class CreativeTabMetaworlds extends CreativeTabs {

    public CreativeTabMetaworlds(String label) {
        super(label);
    }

    /**
     * Get the ItemStack that will be rendered to the tab.
     */
    @Override
    public Item getTabIconItem() {
        return Item.getItemFromBlock(MetaworldsBlocks.blankSubWorldCreator);
    }
}
