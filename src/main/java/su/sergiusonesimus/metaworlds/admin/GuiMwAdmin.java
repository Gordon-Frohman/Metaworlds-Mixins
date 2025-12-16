package su.sergiusonesimus.metaworlds.admin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;

import su.sergiusonesimus.metaworlds.network.MetaMagicNetwork;
import su.sergiusonesimus.metaworlds.network.play.client.C03MwAdminClientActionPacket;

public class GuiMwAdmin extends GuiScreen {

    protected EntityPlayer guiPlayer;
    public GuiImportWorldsList guiImportWorldsList;
    protected GuiImportSubWorldsList guiImportSubWorldsList;
    public GuiSubWorldsList guiSubWorldsList;
    protected GuiButton buttonTeleportToSubWorld;
    protected GuiButton buttonTeleportSubWorldHere;
    protected GuiButton buttonSpawnSubWorld;
    protected GuiButton buttonDespawnSubWorld;
    protected GuiButton buttonStopMovement;
    protected GuiButton buttonResetScale;
    protected GuiButton buttonImportSelectedSubWorld;
    protected int currentTab = 1;

    public GuiMwAdmin(EntityPlayer playerPar) {
        this.guiPlayer = playerPar;
        MetaMagicNetwork.dispatcher.sendToServer(new C03MwAdminClientActionPacket(1));
    }

    public void initGui() {
        this.buttonList.add(new GuiButton(1, this.width / 2 - 180, this.height / 16, 70, 20, "SubWorlds"));
        this.buttonList.add(new GuiButton(2, this.width / 2 - 180 + 70 + 4, this.height / 16, 70, 20, "Import"));
        int listTop = this.height / 16 + 20 + 4;
        int listBottom = this.height - 38;
        this.buttonList.add(
            this.buttonTeleportToSubWorld = new GuiButton(
                101,
                this.width / 2 - 140 - 2,
                this.height - 30,
                140,
                20,
                "Teleport to SubWorld"));
        this.buttonList.add(
            this.buttonTeleportSubWorldHere = new GuiButton(
                102,
                this.width / 2 + 2,
                this.height - 30,
                140,
                20,
                "Teleport SubWorld here"));
        this.buttonList.add(
            this.buttonSpawnSubWorld = new GuiButton(
                103,
                this.width / 2 + 28,
                (listTop + listBottom) / 2 - 22,
                110,
                20,
                "Spawn SubWorld"));
        this.buttonList.add(
            this.buttonDespawnSubWorld = new GuiButton(
                104,
                this.width / 2 + 28,
                (listTop + listBottom) / 2 + 2,
                110,
                20,
                "Despawn SubWorld"));
        this.buttonList.add(
            this.buttonStopMovement = new GuiButton(
                105,
                this.width / 2 + 28,
                (listTop + listBottom) / 2 + 34,
                110,
                20,
                "Stop Movement"));
        this.buttonList.add(
            this.buttonResetScale = new GuiButton(
                106,
                this.width / 2 + 28,
                (listTop + listBottom) / 2 + 58,
                110,
                20,
                "Reset Scale"));
        this.buttonList.add(
            this.buttonImportSelectedSubWorld = new GuiButton(
                201,
                this.width / 2 - 75,
                this.height - 30,
                150,
                20,
                "Import selected SubWorld"));
        this.buttonImportSelectedSubWorld.visible = false;
        this.guiImportWorldsList = new GuiImportWorldsList(
            this,
            158,
            Minecraft.getMinecraft().displayHeight,
            listTop,
            listBottom,
            this.width / 2 - 180,
            20);
        this.guiImportSubWorldsList = new GuiImportSubWorldsList(
            this,
            178,
            Minecraft.getMinecraft().displayHeight,
            listTop,
            listBottom,
            this.width / 2 - 18,
            20);
        this.guiSubWorldsList = new GuiSubWorldsList(
            this,
            200,
            Minecraft.getMinecraft().displayHeight,
            listTop,
            listBottom,
            this.width / 2 - 180,
            30);
    }

    public boolean doesGuiPauseGame() {
        return false;
    }

    protected void actionPerformed(GuiButton buttonPar) {
        if (buttonPar.id == 1) {
            MetaMagicNetwork.dispatcher.sendToServer(new C03MwAdminClientActionPacket(1));
            this.currentTab = 1;
            this.buttonTeleportToSubWorld.visible = true;
            this.buttonTeleportSubWorldHere.visible = true;
            this.buttonSpawnSubWorld.visible = true;
            this.buttonDespawnSubWorld.visible = true;
            this.buttonStopMovement.visible = true;
            this.buttonResetScale.visible = true;
            this.buttonImportSelectedSubWorld.visible = false;
        }

        if (buttonPar.id == 2) {
            MetaMagicNetwork.dispatcher.sendToServer(new C03MwAdminClientActionPacket(2));
            this.currentTab = 2;
            this.buttonTeleportToSubWorld.visible = false;
            this.buttonTeleportSubWorldHere.visible = false;
            this.buttonSpawnSubWorld.visible = false;
            this.buttonDespawnSubWorld.visible = false;
            this.buttonStopMovement.visible = false;
            this.buttonResetScale.visible = false;
            this.buttonImportSelectedSubWorld.visible = true;
        }

        int actionPar;
        if (buttonPar.id >= 101 && buttonPar.id <= 106 && this.guiSubWorldsList.selElement != -1) {
            actionPar = ((MwAdminContainer.AdminSubWorldInfo) this.guiSubWorldsList.adminSubWorldInfos
                .get(this.guiSubWorldsList.selElement)).subWorldId;
            MetaMagicNetwork.dispatcher.sendToServer(new C03MwAdminClientActionPacket(buttonPar.id, actionPar));
        }

        if (buttonPar.id == 201 && this.guiImportWorldsList.selElement != -1
            && this.guiImportSubWorldsList.selElement != -1
            && this.guiImportSubWorldsList.selElement < this.guiImportSubWorldsList.subWorldsList.size()) {
            actionPar = this.guiImportWorldsList.selElement & 4095 | this.guiImportSubWorldsList.selElement << 12;
            MetaMagicNetwork.dispatcher.sendToServer(new C03MwAdminClientActionPacket(buttonPar.id, actionPar));
        }
    }

    public void drawScreen(int par1, int par2, float par3) {
        if (this.currentTab == 1) {
            this.guiSubWorldsList.drawScreen(par1, par2, par3);
        } else if (this.currentTab == 2) {
            this.guiImportWorldsList.drawScreen(par1, par2, par3);
            this.guiImportSubWorldsList.drawScreen(par1, par2, par3);
        }

        super.drawScreen(par1, par2, par3);
    }

    public FontRenderer getFontRenderer() {
        return this.fontRendererObj;
    }
}
