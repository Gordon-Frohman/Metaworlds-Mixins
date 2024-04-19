package net.tclproject.metaworlds.admin;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.tclproject.metaworlds.api.IMixinEntity;
import net.tclproject.metaworlds.api.IMixinWorld;

import cpw.mods.fml.client.GuiScrollingList;

public class GuiSubWorldsList extends GuiScrollingList {

    public List<MwAdminContainer.AdminSubWorldInfo> adminSubWorldInfos;
    GuiMwAdmin parent;
    protected int selElement = -1;

    public GuiSubWorldsList(GuiMwAdmin parParent, int width, int height, int top, int bottom, int left,
        int entryHeight) {
        super(Minecraft.getMinecraft(), width, height, top, bottom, left, entryHeight);
        this.parent = parParent;
    }

    protected int getSize() {
        return this.adminSubWorldInfos == null ? 0 : this.adminSubWorldInfos.size();
    }

    protected void elementClicked(int index, boolean doubleClick) {
        this.selElement = index;
    }

    protected boolean isSelected(int index) {
        return index == this.selElement;
    }

    protected void drawBackground() {}

    protected void drawSlot(int listIndex, int var2, int var3, int var4, Tessellator var5) {
        int color = 16777215;
        MwAdminContainer.AdminSubWorldInfo curInfo = (MwAdminContainer.AdminSubWorldInfo) this.adminSubWorldInfos
            .get(listIndex);
        if (curInfo.subWorldId
            == ((IMixinWorld) ((IMixinEntity) this.parent.guiPlayer).getWorldBelowFeet()).getSubWorldID()) {
            color = 65280;
        } else if (!curInfo.isSpawned) {
            color = 4210752;
        }

        String activeString = "Active: ";
        if (curInfo.isSpawned) {
            activeString = activeString + "Yes";
            activeString = activeString + "  Dimension: " + curInfo.dimensionId;
        } else {
            activeString = activeString + "No";
        }

        this.parent.getFontRenderer()
            .drawString(
                this.parent.getFontRenderer()
                    .trimStringToWidth(curInfo.toString(), this.listWidth - 10),
                this.left + 3,
                var3 + 2,
                color);
        this.parent.getFontRenderer()
            .drawString(
                this.parent.getFontRenderer()
                    .trimStringToWidth(activeString, this.listWidth - 10),
                this.left + 3,
                var3 + 12,
                color);
    }
}
