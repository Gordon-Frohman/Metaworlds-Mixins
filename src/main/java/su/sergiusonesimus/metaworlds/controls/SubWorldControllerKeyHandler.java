package su.sergiusonesimus.metaworlds.controls;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import su.sergiusonesimus.metaworlds.MetaworldsMod;
import su.sergiusonesimus.metaworlds.network.MetaMagicNetwork;
import su.sergiusonesimus.metaworlds.network.play.client.C02ControllerKeyUpdatePacket;

public class SubWorldControllerKeyHandler {

    public static final String categoryName = "key.categories.metaworlds";
    public static final String keyMetaworlds = "key.metaworlds.";

    public static boolean down_Pressed = false;
    public static boolean backward_Pressed = false;
    public static boolean forward_Pressed = false;
    public static boolean right_Pressed = false;
    public static boolean left_Pressed = false;
    public static boolean up_Pressed = false;
    public static boolean rollLeft_Pressed = false;
    public static boolean rollRight_Pressed = false;
    public static boolean rollForward_Pressed = false;
    public static boolean rollBackward_Pressed = false;

    public static KeyBinding keyBindRollLeft;
    public static KeyBinding keyBindRollRight;
    public static KeyBinding keyBindRollForward;
    public static KeyBinding keyBindRollBackward;

    public static KeyBinding keyBindLeft;
    public static KeyBinding keyBindRight;
    public static KeyBinding keyBindForward;
    public static KeyBinding keyBindBackward;
    public static KeyBinding keyBindUp;
    public static KeyBinding keyBindDown;

    public SubWorldControllerKeyHandler() {
        this.registerKeys();
    }

    public void registerKeys() {
        boolean resetBinding = false;
        if (!MetaworldsMod.usePlayerControls && (keyBindLeft == null || keyBindRight == null
            || keyBindForward == null
            || keyBindBackward == null
            || keyBindUp == null
            || keyBindDown == null)) {
            keyBindUp = new KeyBinding(keyMetaworlds + "up", Keyboard.KEY_ADD, categoryName);
            ClientRegistry.registerKeyBinding(keyBindUp);
            keyBindDown = new KeyBinding(keyMetaworlds + "down", Keyboard.KEY_SUBTRACT, categoryName);
            ClientRegistry.registerKeyBinding(keyBindDown);
            keyBindForward = new KeyBinding(keyMetaworlds + "forward", Keyboard.KEY_UP, categoryName);
            ClientRegistry.registerKeyBinding(keyBindForward);
            keyBindBackward = new KeyBinding(keyMetaworlds + "backward", Keyboard.KEY_DOWN, categoryName);
            ClientRegistry.registerKeyBinding(keyBindBackward);
            keyBindLeft = new KeyBinding(keyMetaworlds + "left", Keyboard.KEY_LEFT, categoryName);
            ClientRegistry.registerKeyBinding(keyBindLeft);
            keyBindRight = new KeyBinding(keyMetaworlds + "right", Keyboard.KEY_RIGHT, categoryName);
            ClientRegistry.registerKeyBinding(keyBindRight);
            resetBinding = true;
        }
        if (keyBindRollLeft == null || keyBindRollRight == null
            || keyBindRollForward == null
            || keyBindRollBackward == null) {
            keyBindRollForward = new KeyBinding(keyMetaworlds + "rollForward", Keyboard.KEY_NUMPAD8, categoryName);
            ClientRegistry.registerKeyBinding(keyBindRollForward);
            keyBindRollBackward = new KeyBinding(keyMetaworlds + "rollBackward", Keyboard.KEY_NUMPAD2, categoryName);
            ClientRegistry.registerKeyBinding(keyBindRollBackward);
            keyBindRollLeft = new KeyBinding(keyMetaworlds + "rollLeft", Keyboard.KEY_NUMPAD4, categoryName);
            ClientRegistry.registerKeyBinding(keyBindRollLeft);
            keyBindRollRight = new KeyBinding(keyMetaworlds + "rollRight", Keyboard.KEY_NUMPAD6, categoryName);
            ClientRegistry.registerKeyBinding(keyBindRollRight);
            resetBinding = true;
        }
        if (resetBinding) KeyBinding.resetKeyBindingArrayAndHash();
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent event) {
        if (event.phase.equals(Phase.START)) {
            boolean upKeyPressedNow;
            boolean downKeyPressedNow;
            boolean forwardKeyPressedNow;
            boolean backwardKeyPressedNow;
            boolean leftKeyPressedNow;
            boolean rightKeyPressedNow;
            if (MetaworldsMod.usePlayerControls) {
                upKeyPressedNow = Minecraft.getMinecraft().gameSettings.keyBindJump.getIsKeyPressed();
                downKeyPressedNow = Minecraft.getMinecraft().gameSettings.keyBindSprint.getIsKeyPressed();
                forwardKeyPressedNow = Minecraft.getMinecraft().gameSettings.keyBindForward.getIsKeyPressed();
                backwardKeyPressedNow = Minecraft.getMinecraft().gameSettings.keyBindBack.getIsKeyPressed();
                leftKeyPressedNow = Minecraft.getMinecraft().gameSettings.keyBindLeft.getIsKeyPressed();
                rightKeyPressedNow = Minecraft.getMinecraft().gameSettings.keyBindRight.getIsKeyPressed();
            } else {
                upKeyPressedNow = keyBindUp.getIsKeyPressed();
                downKeyPressedNow = keyBindDown.getIsKeyPressed();
                forwardKeyPressedNow = keyBindForward.getIsKeyPressed();
                backwardKeyPressedNow = keyBindBackward.getIsKeyPressed();
                leftKeyPressedNow = keyBindLeft.getIsKeyPressed();
                rightKeyPressedNow = keyBindRight.getIsKeyPressed();
            }
            boolean rollForwardKeyPressedNow = keyBindRollForward.getIsKeyPressed();
            boolean rollBackwardKeyPressedNow = keyBindRollBackward.getIsKeyPressed();
            boolean rollLeftKeyPressedNow = keyBindRollLeft.getIsKeyPressed();
            boolean rollRightKeyPressedNow = keyBindRollRight.getIsKeyPressed();
            boolean updateRequired = false;

            if (!up_Pressed && upKeyPressedNow) {
                up_Pressed = true;
                updateRequired = true;
            } else if (up_Pressed && !upKeyPressedNow) {
                up_Pressed = false;
                updateRequired = true;
            }

            if (!down_Pressed && downKeyPressedNow) {
                down_Pressed = true;
                updateRequired = true;
            } else if (down_Pressed && !downKeyPressedNow) {
                down_Pressed = false;
                updateRequired = true;
            }

            if (!forward_Pressed && forwardKeyPressedNow) {
                forward_Pressed = true;
                updateRequired = true;
            } else if (forward_Pressed && !forwardKeyPressedNow) {
                forward_Pressed = false;
                updateRequired = true;
            }

            if (!backward_Pressed && backwardKeyPressedNow) {
                backward_Pressed = true;
                updateRequired = true;
            } else if (backward_Pressed && !backwardKeyPressedNow) {
                backward_Pressed = false;
                updateRequired = true;
            }

            if (!left_Pressed && leftKeyPressedNow) {
                left_Pressed = true;
                updateRequired = true;
            } else if (left_Pressed && !leftKeyPressedNow) {
                left_Pressed = false;
                updateRequired = true;
            }

            if (!right_Pressed && rightKeyPressedNow) {
                right_Pressed = true;
                updateRequired = true;
            } else if (right_Pressed && !rightKeyPressedNow) {
                right_Pressed = false;
                updateRequired = true;
            }

            if (!rollForward_Pressed && rollForwardKeyPressedNow) {
                rollForward_Pressed = true;
                updateRequired = true;
            } else if (rollForward_Pressed && !rollForwardKeyPressedNow) {
                rollForward_Pressed = false;
                updateRequired = true;
            }

            if (!rollBackward_Pressed && rollBackwardKeyPressedNow) {
                rollBackward_Pressed = true;
                updateRequired = true;
            } else if (rollBackward_Pressed && !rollBackwardKeyPressedNow) {
                rollBackward_Pressed = false;
                updateRequired = true;
            }

            if (!rollLeft_Pressed && rollLeftKeyPressedNow) {
                rollLeft_Pressed = true;
                updateRequired = true;
            } else if (rollLeft_Pressed && !rollLeftKeyPressedNow) {
                rollLeft_Pressed = false;
                updateRequired = true;
            }

            if (!rollRight_Pressed && rollRightKeyPressedNow) {
                rollRight_Pressed = true;
                updateRequired = true;
            } else if (rollRight_Pressed && !rollRightKeyPressedNow) {
                rollRight_Pressed = false;
                updateRequired = true;
            }

            if (updateRequired) {
                MetaMagicNetwork.dispatcher.sendToServer(
                    new C02ControllerKeyUpdatePacket(
                        up_Pressed,
                        down_Pressed,
                        forward_Pressed,
                        backward_Pressed,
                        left_Pressed,
                        right_Pressed,
                        rollForward_Pressed,
                        rollBackward_Pressed,
                        rollLeft_Pressed,
                        rollRight_Pressed));
            }
        }
    }
}
