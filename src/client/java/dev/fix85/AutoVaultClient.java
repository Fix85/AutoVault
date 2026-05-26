package dev.fix85;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import dev.fix85.gui.AutoVaultConfigScreen;

public class AutoVaultClient implements ClientModInitializer {
    public static final String MOD_ID = "autovault";

    public static KeyMapping toggleKey;
    public static KeyMapping openGuiKey;

    @Override
    public void onInitializeClient() {
        Config.load();

        toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.autovault.toggle",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                KeyMapping.Category.MISC
        ));

        openGuiKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.autovault.open_gui",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                KeyMapping.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(VaultAutoOpener::onClientTick);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.consumeClick()) {
                Config.get().enabled = !Config.get().enabled;
                Config.save();
                if (client.player != null) {
                    String stateStr = Config.get().enabled ? "§aON§r" : "§cOFF§r";
                    client.player.sendOverlayMessage(
                            Component.translatable("autovault.chat.toggle", stateStr));
                }
            }
            while (openGuiKey.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new AutoVaultConfigScreen(null));
                }
            }
        });
    }
}
