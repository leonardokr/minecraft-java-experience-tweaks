package com.ziondev.experiencetweaks;

import com.ziondev.experiencetweaks.network.SyncServerConfigPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Dedicated event handler responsible for syncing server-side configuration settings
 * to clients whenever a player logs into the server.
 */
@EventBusSubscriber(modid = ExperienceTweaksMod.MODID)
public class ServerConfigSyncHandler {

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new SyncServerConfigPacket(
                    ModConfig.isAnvilUseItemCost(),
                    ServerConfig.ANVIL_COST_ITEM.get(),
                    ModConfig.getAnvilItemCostMultiplier(),
                    ModConfig.isAnvilBypassTooExpensive()
            ));
        }
    }
}
