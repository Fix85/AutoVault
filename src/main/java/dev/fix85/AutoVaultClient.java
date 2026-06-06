package dev.fix85;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import dev.fix85.gui.AutoVaultConfigScreen;

public class AutoVaultClient implements ClientModInitializer {
    public static final String MOD_ID = "autovault";

    public static KeyBinding toggleKey;
    public static KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {
        Config.load();

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.autovault.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                KeyBinding.MISC_CATEGORY
        ));

        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.autovault.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                KeyBinding.MISC_CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(VaultAutoOpener::onClientTick);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                Config.get().enabled = !Config.get().enabled;
                Config.save();
                if (client.player != null) {
                    String stateStr = Config.get().enabled ? "§aON§r" : "§cOFF§r";
                    client.player.sendMessage(
                            Text.translatable("autovault.chat.toggle", stateStr),
                            true);
                }
            }
            while (openGuiKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new AutoVaultConfigScreen(null));
                }
            }
        });
    }
}
