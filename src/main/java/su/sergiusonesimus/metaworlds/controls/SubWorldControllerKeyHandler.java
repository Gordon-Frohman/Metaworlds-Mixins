package su.sergiusonesimus.metaworlds.controls;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import su.sergiusonesimus.metaworlds.network.MetaMagicNetwork;
import su.sergiusonesimus.metaworlds.network.play.client.C02ControllerKeyUpdatePacket;

public class SubWorldControllerKeyHandler {

    public static boolean ctrl_down = false;
    public static boolean s_Down = false;
    public static boolean w_Down = false;
    public static boolean d_Down = false;
    public static boolean a_Down = false;
    public static boolean space_Down = false;
    public static boolean rl_Down = false;
    public static boolean rr_Down = false;

    public static KeyBinding keyBindRollLeft;
    public static KeyBinding keyBindRollRight;

    public SubWorldControllerKeyHandler() {
        this.registerKeys();
    }

    public void registerKeys() {
        if (keyBindRollLeft == null) {
            keyBindRollLeft = new KeyBinding("Roll Left Subworld", Keyboard.KEY_NUMPAD8, "key.categories.movement");
            ClientRegistry.registerKeyBinding(keyBindRollLeft);
            keyBindRollRight = new KeyBinding("Roll Right Subworld", Keyboard.KEY_NUMPAD2, "key.categories.movement");
            ClientRegistry.registerKeyBinding(keyBindRollRight);
            KeyBinding.resetKeyBindingArrayAndHash();
        }
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent event) {
        if (event.phase.equals(Phase.START)) {
            boolean ctrlKeyPressedNow = Minecraft.getMinecraft().gameSettings.keyBindSprint.getIsKeyPressed();
            boolean sPressedNow = Minecraft.getMinecraft().gameSettings.keyBindRight.getIsKeyPressed();
            boolean dPressedNow = Minecraft.getMinecraft().gameSettings.keyBindBack.getIsKeyPressed();
            boolean aPressedNow = Minecraft.getMinecraft().gameSettings.keyBindLeft.getIsKeyPressed();
            boolean wPressedNow = Minecraft.getMinecraft().gameSettings.keyBindForward.getIsKeyPressed();
            boolean spacePressedNow = Minecraft.getMinecraft().gameSettings.keyBindJump.getIsKeyPressed();
            boolean plusPressedNow = keyBindRollRight.getIsKeyPressed();
            boolean minusPressedNow = keyBindRollLeft.getIsKeyPressed();
            boolean updateRequired = false;

            if (!ctrl_down && ctrlKeyPressedNow) {
                ctrl_down = true;
                updateRequired = true;
            } else if (ctrl_down && !ctrlKeyPressedNow) {
                ctrl_down = false;
                updateRequired = true;
            }

            if (!s_Down && sPressedNow) {
                s_Down = true;
                updateRequired = true;
            } else if (s_Down && !sPressedNow) {
                s_Down = false;
                updateRequired = true;
            }

            if (!d_Down && dPressedNow) {
                d_Down = true;
                updateRequired = true;
            } else if (d_Down && !dPressedNow) {
                d_Down = false;
                updateRequired = true;
            }

            if (!a_Down && aPressedNow) {
                a_Down = true;
                updateRequired = true;
            } else if (a_Down && !aPressedNow) {
                a_Down = false;
                updateRequired = true;
            }

            if (!w_Down && wPressedNow) {
                w_Down = true;
                updateRequired = true;
            } else if (w_Down && !wPressedNow) {
                w_Down = false;
                updateRequired = true;
            }

            if (!space_Down && spacePressedNow) {
                space_Down = true;
                updateRequired = true;
            } else if (space_Down && !spacePressedNow) {
                space_Down = false;
                updateRequired = true;
            }

            if (!rl_Down && plusPressedNow) {
                rl_Down = true;
                updateRequired = true;
            } else if (rl_Down && !plusPressedNow) {
                rl_Down = false;
                updateRequired = true;
            }

            if (!rr_Down && minusPressedNow) {
                rr_Down = true;
                updateRequired = true;
            } else if (rr_Down && !minusPressedNow) {
                rr_Down = false;
                updateRequired = true;
            }

            if (updateRequired) {
                MetaMagicNetwork.dispatcher.sendToServer(
                    new C02ControllerKeyUpdatePacket(
                        ctrl_down,
                        s_Down,
                        d_Down,
                        a_Down,
                        w_Down,
                        space_Down,
                        rl_Down,
                        rr_Down));
            }
        }
    }
}
