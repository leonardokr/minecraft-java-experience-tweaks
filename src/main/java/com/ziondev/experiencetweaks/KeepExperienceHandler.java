package com.ziondev.experiencetweaks;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Keeps the player's experience level, total experience, and progress bar
 * intact after death when {@link ModConfig#isKeepExperienceEnabled()} is enabled.
 */
@EventBusSubscriber(modid = ExperienceTweaksMod.MODID)
public class KeepExperienceHandler {

    /**
     * Copies experience level, total experience, and progress from the old player
     * instance to the new one upon respawning after death if keep experience is enabled.
     *
     * @param event the player clone event
     */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) {
            return;
        }

        if (!ModConfig.isKeepExperienceEnabled()) {
            return;
        }

        Player oldPlayer = event.getOriginal();
        event.getEntity().experienceLevel = oldPlayer.experienceLevel;
        event.getEntity().totalExperience = oldPlayer.totalExperience;
        event.getEntity().experienceProgress = oldPlayer.experienceProgress;
    }
}
