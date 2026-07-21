package com.ziondev.experiencetweaks;

import com.ziondev.experiencetweaks.mixin.FishingHookAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side event handler for auto-fishing and auto-recasting.
 * Detects fish bites via synched entity data (DATA_BITING) and fishing bobber splash sound events on the client.
 */
@EventBusSubscriber(modid = ExperienceTweaksMod.MODID, value = Dist.CLIENT)
public class AutoFishingHandler {

    private static final Map<UUID, RecastData> PENDING_RECASTS = new ConcurrentHashMap<>();

    private record RecastData(InteractionHand hand, int ticksLeft) {}

    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        if (!ModConfig.isAutoFishingEnabled()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || player.fishing == null || minecraft.gameMode == null) {
            return;
        }

        SoundInstance sound = event.getSound();
        if (sound == null || sound.getSound() == null) {
            return;
        }

        Identifier loc = sound.getSound().getLocation();
        if (loc != null) {
            String path = loc.getPath();
            if (path.contains("fishing_bobber.splash") || path.contains("splash")) {
                FishingHook hook = player.fishing;
                double dx = sound.getX() - hook.getX();
                double dy = sound.getY() - hook.getY();
                double dz = sound.getZ() - hook.getZ();
                if ((dx * dx + dy * dy + dz * dz) <= 16.0) {
                    experienceTweaks$reelIn(player, minecraft);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null || player != minecraft.player || minecraft.gameMode == null) {
            return;
        }

        UUID uuid = player.getUUID();

        if (ModConfig.isAutoFishingEnabled() && player.fishing != null) {
            FishingHook hook = player.fishing;
            boolean isBiting = false;
            try {
                isBiting = ((FishingHookAccessor) hook).isBiting()
                        || hook.getEntityData().get(FishingHookAccessor.getDataBiting())
                        || ((FishingHookAccessor) hook).getNibble() > 0;
            } catch (Exception ignored) {
            }

            if (isBiting) {
                experienceTweaks$reelIn(player, minecraft);
            }
        }

        RecastData data = PENDING_RECASTS.get(uuid);
        if (data != null) {
            if (data.ticksLeft() <= 1) {
                PENDING_RECASTS.remove(uuid);
                experienceTweaks$recast(player, minecraft, data.hand());
            } else {
                PENDING_RECASTS.put(uuid, new RecastData(data.hand(), data.ticksLeft() - 1));
            }
        }
    }

    private static void experienceTweaks$reelIn(Player player, Minecraft minecraft) {
        InteractionHand hand = null;
        if (player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof FishingRodItem) {
            hand = InteractionHand.MAIN_HAND;
        } else if (player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof FishingRodItem) {
            hand = InteractionHand.OFF_HAND;
        }

        if (hand != null) {
            minecraft.gameMode.useItem(player, hand);
            if (ModConfig.isAutoFishingRecastEnabled()) {
                PENDING_RECASTS.put(player.getUUID(), new RecastData(hand, 10));
            }
        }
    }

    private static void experienceTweaks$recast(Player player, Minecraft minecraft, InteractionHand hand) {
        if (player.fishing != null) {
            return;
        }

        ItemStack rod = player.getItemInHand(hand);
        if (rod.getItem() instanceof FishingRodItem) {
            minecraft.gameMode.useItem(player, hand);
        }
    }
}
