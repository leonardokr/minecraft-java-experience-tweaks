package com.ziondev.experiencetweaks.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.ziondev.experiencetweaks.ExperienceTweaksMod;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

/**
 * Registers client-side key mappings for the mod, such as opening the in-game
 * configuration screen.
 */
@EventBusSubscriber(modid = ExperienceTweaksMod.MODID, value = Dist.CLIENT)
public class ClientKeyBinds {

    /**
     * Registers key mappings on the client mod event bus.
     *
     * @param event the key mappings registration event
     */
    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        com.ziondev.experiencetweaks.sharedconfig.ModConfigRegistry.register("Experience Tweaks",
                parent -> new com.ziondev.experiencetweaks.client.gui.ExperienceTweaksConfigScreen(parent));

        KeyMapping key = com.ziondev.experiencetweaks.sharedconfig.ModConfigRegistry.getOpenConfigKey();
        if (key == null) {
            key = new KeyMapping("key.metalions.open_config",
                    InputConstants.Type.KEYSYM,
                    InputConstants.KEY_K,
                    KeyMapping.Category.MISC);
            com.ziondev.experiencetweaks.sharedconfig.ModConfigRegistry.setOpenConfigKey(key);
            event.register(key);
        }
    }
}
