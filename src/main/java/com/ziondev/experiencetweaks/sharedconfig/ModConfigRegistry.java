package com.ziondev.experiencetweaks.sharedconfig;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.function.Function;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.KeyMapping;

/**
 * ModConfigRegistry acts as a shared, mod-independent registry where each
 * individual mod registers its configuration screen factory in the JVM system properties.
 */
public class ModConfigRegistry {
    private static final String SCREENS_KEY = "ziondev.shared_config_screens";
    private static final String KEY_KEY = "ziondev.shared_config_key";

    /**
     * Retrieves the map of registered configuration screens from the JVM system properties.
     *
     * @return the map of registered config screens
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Function<Screen, Screen>> getConfigScreens() {
        Map<String, Function<Screen, Screen>> screens = (Map<String, Function<Screen, Screen>>) System.getProperties().get(SCREENS_KEY);
        if (screens == null) {
            screens = new LinkedHashMap<>();
            System.getProperties().put(SCREENS_KEY, screens);
        }
        return screens;
    }

    /**
     * Gets the shared open configuration key mapping.
     *
     * @return the shared KeyMapping instance
     */
    public static KeyMapping getOpenConfigKey() {
        return (KeyMapping) System.getProperties().get(KEY_KEY);
    }

    /**
     * Sets the shared open configuration key mapping in the JVM system properties.
     *
     * @param key the KeyMapping instance to share
     */
    public static void setOpenConfigKey(KeyMapping key) {
        System.getProperties().put(KEY_KEY, key);
    }

    /**
     * Registers a configuration screen factory for a given mod.
     *
     * @param modId         The unique identifier of the mod (String)
     * @param screenFactory The function creating the screen given a parent screen (Function)
     */
    public static void register(String modId, Function<Screen, Screen> screenFactory) {
        getConfigScreens().put(modId, screenFactory);
    }
}
