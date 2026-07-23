package com.ziondev.experiencetweaks.client;

import com.ziondev.experiencetweaks.ExperienceTweaksMod;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Handles client input events such as key presses to open the configuration
 * GUI.
 */
@EventBusSubscriber(modid = ExperienceTweaksMod.MODID, value = Dist.CLIENT)
public class ClientInputHandler {

    /**
     * Listens for client tick events and opens the configuration screen when the
     * keybind is pressed.
     *
     * @param event the client tick event
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        net.minecraft.client.KeyMapping key = com.ziondev.experiencetweaks.sharedconfig.ModConfigRegistry.getOpenConfigKey();
        if (key != null) {
            while (key.consumeClick()) {
                if (minecraft.screen == null) {
                    java.util.Map<String, java.util.function.Function<net.minecraft.client.gui.screens.Screen, net.minecraft.client.gui.screens.Screen>> screens =
                            com.ziondev.experiencetweaks.sharedconfig.ModConfigRegistry.getConfigScreens();
                    if (screens.size() == 1) {
                        minecraft.setScreen(screens.values().iterator().next().apply(null));
                    } else {
                        minecraft.setScreen(new com.ziondev.experiencetweaks.sharedconfig.UnifiedConfigScreen(null));
                    }
                }
            }
        }
    }
}
