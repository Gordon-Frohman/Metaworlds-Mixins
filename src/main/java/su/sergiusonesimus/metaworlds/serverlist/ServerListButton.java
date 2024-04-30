package su.sergiusonesimus.metaworlds.serverlist;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class ServerListButton extends GuiButton {

    public ServerListButton(int par1, int par2, int par3, int par4, int par5, String par6Str) {
        super(par1, par2, par3, par4, par5, par6Str);
    }

    public boolean mousePressed(Minecraft p_146116_1_, int p_146116_2_, int p_146116_3_) {
        boolean result = super.mousePressed(p_146116_1_, p_146116_2_, p_146116_3_);
        if (result) {
            ;
        }

        return result;
    }
}
