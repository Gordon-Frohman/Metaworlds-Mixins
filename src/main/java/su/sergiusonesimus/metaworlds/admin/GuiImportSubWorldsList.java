package su.sergiusonesimus.metaworlds.admin;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;

import cpw.mods.fml.client.GuiScrollingList;

public class GuiImportSubWorldsList extends GuiScrollingList {

    public List<MwAdminContainer.SaveGameSubWorldInfo> subWorldsList;
    GuiMwAdmin parent;
    protected int selElement = -1;

    public GuiImportSubWorldsList(GuiMwAdmin parParent, int width, int height, int top, int bottom, int left,
        int entryHeight) {
        super(Minecraft.getMinecraft(), width, height, top, bottom, left, entryHeight);
        this.parent = parParent;
    }

    protected int getSize() {
        return this.subWorldsList == null ? 0 : this.subWorldsList.size();
    }

    protected void elementClicked(int index, boolean doubleClick) {
        this.selElement = index;
    }

    protected boolean isSelected(int index) {
        return index == this.selElement;
    }

    protected void drawBackground() {}

    protected void drawSlot(int listIndex, int var2, int var3, int var4, Tessellator var5) {
        this.parent.getFontRenderer()
            .drawString(
                this.parent.getFontRenderer()
                    .trimStringToWidth(
                        ((MwAdminContainer.SaveGameSubWorldInfo) this.subWorldsList.get(listIndex)).subWorldName,
                        this.listWidth - 10),
                this.left + 3,
                var3 + 2,
                16777215);
    }
}
