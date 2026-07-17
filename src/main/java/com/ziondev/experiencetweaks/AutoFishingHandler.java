package com.ziondev.experiencetweaks;

import com.ziondev.experiencetweaks.mixin.FishingHookAccessor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = ExperienceTweaksMod.MODID)
public class AutoFishingHandler {
    private static final Map<UUID, RecastData> PENDING_RECASTS = new ConcurrentHashMap<>();

    private record RecastData(InteractionHand hand, int ticksLeft) {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }

        UUID uuid = player.getUUID();

        if (ModConfig.isAutoFishingEnabled() && player.fishing != null) {
            int nibble = ((FishingHookAccessor) player.fishing).getNibble();
            if (nibble > 0) {
                InteractionHand hand = null;
                if (player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof FishingRodItem) {
                    hand = InteractionHand.MAIN_HAND;
                } else if (player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof FishingRodItem) {
                    hand = InteractionHand.OFF_HAND;
                }

                if (hand != null) {
                    ItemStack rod = player.getItemInHand(hand);
                    rod.use(player.level(), player, hand);

                    if (ModConfig.isAutoFishingRecastEnabled()) {
                        PENDING_RECASTS.put(uuid, new RecastData(hand, 10));
                    }
                }
            }
        }

        RecastData data = PENDING_RECASTS.get(uuid);
        if (data != null) {
            if (data.ticksLeft() <= 1) {
                PENDING_RECASTS.remove(uuid);
                experienceTweaks$recast(player, data.hand());
            } else {
                PENDING_RECASTS.put(uuid, new RecastData(data.hand(), data.ticksLeft() - 1));
            }
        }
    }

    private static void experienceTweaks$recast(Player player, InteractionHand hand) {
        if (player.fishing != null) {
            return;
        }

        ItemStack rod = player.getItemInHand(hand);
        if (rod.getItem() instanceof FishingRodItem) {
            rod.use(player.level(), player, hand);
        }
    }
}
