package com.ziondev.experiencetweaks;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * Single access point for all Experience Tweaks configuration values.
 * <p>
 * Every getter in this class:
 * <ul>
 *   <li>reads the underlying {@link Config} spec value,</li>
 *   <li>validates/parses where needed.</li>
 *   <li>logs a warning via {@link ExperienceTweaksMod#LOGGER} on bad input,</li>
 *   <li>and returns a safe fallback so callers never receive a broken value.</li>
 * </ul>
 *
 * <p>No other class in the mod should access {@link Config} fields directly,
 * except {@link ExperienceTweaksMod} which registers the spec itself.
 */
public final class ModConfig {

    private ModConfig() {}

    /**
     * Returns {@code true} if the given player name is on the opt-out list
     * (i.e., they do NOT keep their experience on death).
     *
     * @param playerName the player's display name
     * @return {@code true} if the player should lose XP on death; {@code false} otherwise
     */
    public static boolean isDontKeepExperience(String playerName) {
        try {
            List<? extends String> list = Config.DONT_KEEP_EXPERIENCE.get();
            return list.contains(playerName);
        } catch (Exception e) {
            ExperienceTweaksMod.LOGGER.warn(
                    "[ModConfig] Failed to read dontKeepExperience, defaulting to false. Cause: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Returns {@code true} if XP should be delivered directly to the player
     * instead of spawning orbs.
     * <p>Fallback: {@code true}
     */
    public static boolean isDirectExperience() {
        try {
            return Config.DIRECT_EXPERIENCE.get();
        } catch (Exception e) {
            ExperienceTweaksMod.LOGGER.warn(
                    "[ModConfig] Failed to read directExperience, defaulting to true. Cause: {}", e.getMessage());
            return true;
        }
    }

    /**
     * Returns the {@link Item} used as currency when enchanting.
     * <p>Falls back to {@link Items#LAPIS_LAZULI} if the configured registry name
     * is blank, invalid, or not found.
     */
    public static Item getEnchantmentCostItem() {
        try {
            String configuredItem = Config.ENCHANTMENT_COST_ITEM.get();
            if (!configuredItem.isBlank()) {
                return BuiltInRegistries.ITEM
                        .getOptional(Identifier.parse(configuredItem))
                        .orElseGet(() -> {
                            ExperienceTweaksMod.LOGGER.warn(
                                    "[ModConfig] enchantmentCostItem '{}' not found in registry, falling back to minecraft:lapis_lazuli.",
                                    configuredItem);
                            return Items.LAPIS_LAZULI;
                        });
            }
        } catch (Exception e) {
            ExperienceTweaksMod.LOGGER.warn(
                    "[ModConfig] Invalid enchantmentCostItem config, falling back to minecraft:lapis_lazuli. Cause: {}", e.getMessage());
        }
        return Items.LAPIS_LAZULI;
    }

    /**
     * Returns the item-cost multiplier applied to the enchantment button index.
     * <p>Fallback: {@code 1.5}
     */
    public static double getEnchantmentCostMultiplier() {
        try {
            return Config.ENCHANTMENT_COST_MULTIPLIER.get();
        } catch (Exception e) {
            ExperienceTweaksMod.LOGGER.warn(
                    "[ModConfig] Failed to read enchantmentCostMultiplier, defaulting to 1.5. Cause: {}", e.getMessage());
            return 1.5;
        }
    }

    /**
     * Returns the cooldown progression mode for enchantment buttons.
     * Valid values: {@code "current_level"}, {@code "last_level"}.
     * <p>Fallback: {@code "current_level"}
     */
    public static String getEnchantmentCooldownType() {
        try {
            return Config.ENCHANTMENT_COOLDOWN_TYPE.get();
        } catch (Exception e) {
            ExperienceTweaksMod.LOGGER.warn(
                    "[ModConfig] Failed to read enchantmentCooldownType, defaulting to 'current_level'. Cause: {}", e.getMessage());
            return "current_level";
        }
    }

    /**
     * Returns the configured base required level for the given 0-indexed button.
     * <p>Falls back to {@code (buttonId + 1) * 10} (i.e., 10 / 20 / 30) if the list
     * is too short or the config cannot be read.
     *
     * @param buttonId 0-based enchantment button index (0, 1, 2)
     */
    public static int getEnchantmentBaseRequiredLevel(int buttonId) {
        try {
            List<? extends Integer> baseLevels = Config.ENCHANTMENT_BASE_REQUIRED_LEVELS.get();
            if (buttonId < baseLevels.size()) {
                return baseLevels.get(buttonId);
            }
        } catch (Exception e) {
            ExperienceTweaksMod.LOGGER.warn(
                    "[ModConfig] Failed to read enchantmentBaseRequiredLevels[{}], using default. Cause: {}", buttonId, e.getMessage());
        }
        return (buttonId + 1) * 10;
    }

    /**
     * Returns the difficulty weight (0.0–1.0) used in the cooldown curve formula.
     * <p>Fallback: {@code 0.25}
     */
    public static double getEnchantmentRequiredLevelBias() {
        try {
            return Config.ENCHANTMENT_REQUIRED_LEVEL_BIAS.get();
        } catch (Exception e) {
            ExperienceTweaksMod.LOGGER.warn(
                    "[ModConfig] Failed to read enchantmentRequiredLevelBias, defaulting to 0.25. Cause: {}", e.getMessage());
            return 0.25;
        }
    }
}
