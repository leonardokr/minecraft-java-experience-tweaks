package com.ziondev.experiencetweaks.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.ziondev.experiencetweaks.ExperienceTweaksMod;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

/**
 * Registers client-side key mappings for the mod, such as opening the in-game configuration screen.
 */
@EventBusSubscriber(modid = ExperienceTweaksMod.MODID, value = Dist.CLIENT)
public class ClientKeyBinds {

    /** Key mapping to open the Experience Tweaks in-game configuration screen. Default is key 'K'. */
    public static final KeyMapping OPEN_CONFIG_KEY = new KeyMapping(
            "key.experiencetweaks.open_config",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_K,
            KeyMapping.Category.MISC
    );

    /**
     * Registers key mappings on the client mod event bus.
     *
     * @param event the key mappings registration event
     */
    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_CONFIG_KEY);
    }
}
