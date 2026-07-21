package com.ziondev.experiencetweaks.client;

import com.ziondev.experiencetweaks.ExperienceTweaksMod;
import com.ziondev.experiencetweaks.client.gui.ExperienceTweaksConfigScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Handles client input events such as key presses to open the configuration GUI.
 */
@EventBusSubscriber(modid = ExperienceTweaksMod.MODID, value = Dist.CLIENT)
public class ClientInputHandler {

    /**
     * Listens for client tick events and opens the configuration screen when the keybind is pressed.
     *
     * @param event the client tick event
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        while (ClientKeyBinds.OPEN_CONFIG_KEY.consumeClick()) {
            if (minecraft.screen == null) {
                minecraft.setScreen(new ExperienceTweaksConfigScreen(null));
            }
        }
    }
}
