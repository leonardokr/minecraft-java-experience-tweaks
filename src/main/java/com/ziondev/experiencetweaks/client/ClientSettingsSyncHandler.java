package com.ziondev.experiencetweaks.client;

import com.ziondev.experiencetweaks.ClientConfig;
import com.ziondev.experiencetweaks.ExperienceTweaksMod;
import com.ziondev.experiencetweaks.network.SyncClientSettingsPacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

/**
 * Handles client-side configuration synchronization with the server.
 */
@EventBusSubscriber(modid = ExperienceTweaksMod.MODID, value = Dist.CLIENT)
public class ClientSettingsSyncHandler {

    /**
     * Sends settings to the server when the player logs in.
     *
     * @param event the logging in event
     */
    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        sync(event);
    }

    /**
     * Sends settings to the server when the player respawns.
     *
     * @param event the clone event
     */
    @SubscribeEvent
    public static void onClone(ClientPlayerNetworkEvent.Clone event) {
        sync(event);
    }

    private static void sync(ClientPlayerNetworkEvent event) {
        if (event.getConnection() != null) {
            event.getConnection().send(new net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket(
                    new SyncClientSettingsPacket(
                            ClientConfig.KEEP_EXPERIENCE.get(),
                            ClientConfig.DIRECT_EXPERIENCE.get(),
                            ClientConfig.GIVE_EXPERIENCE_EVERY_DAY.get(),
                            ClientConfig.RIPTIDE_ANYWHERE.get(),
                            ClientConfig.MOB_ARROWS_COLLECTIBLE.get(),
                            ClientConfig.NEVER_REPEAT_TREASURE_MAPS.get()
                    )
            ));
        }
    }
}
