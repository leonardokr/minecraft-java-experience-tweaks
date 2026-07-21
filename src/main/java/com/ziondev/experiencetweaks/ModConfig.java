package com.ziondev.experiencetweaks;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class ModConfig {

    private ModConfig() {}

    public enum ConfigError {

        INVALID_COST_ITEM("ET-0x001"),
        COST_ITEM_NOT_FOUND("ET-0x002"),
        DIRECT_EXPERIENCE("ET-0x003"),
        DONT_KEEP_EXPERIENCE("ET-0x004"),
        ENCHANTMENT_COST_MULTIPLIER("ET-0x005"),
        ENCHANTMENT_COOLDOWN_TYPE("ET-0x006"),
        ENCHANTMENT_BASE_REQUIRED_LEVELS("ET-0x007"),
        ENCHANTMENT_REQUIRED_LEVEL_BIAS("ET-0x008"),
        GIVE_EXPERIENCE_EVERY_DAY("ET-0x009"),
        GIVE_EXPERIENCE_EVERY_DAY_BASE("ET-0x00a"),
        GIVE_EXPERIENCE_EVERY_DAY_GROWTH("ET-0x00b"),
        AUTO_FISHING("ET-0x00c"),
        AUTO_FISHING_RECAST("ET-0x00d"),
        ANVIL_BYPASS_TOO_EXPENSIVE("ET-0x00e"),
        ANVIL_USE_ITEM_COST("ET-0x00f"),
        INVALID_ANVIL_COST_ITEM("ET-0x010"),
        ANVIL_COST_ITEM_NOT_FOUND("ET-0x011"),
        ANVIL_ITEM_COST_MULTIPLIER("ET-0x012"),
        ALLOW_MENDING_WITH_INFINITY("ET-0x013"),
        ANVIL_ENCHANTMENT_EXTRACTION("ET-0x014"),
        ANVIL_ENCHANTMENT_EXTRACTION_DESTROY_SOURCE("ET-0x015"),
        WATER_BELOW_HYDRATES_FARMLAND("ET-0x016"),
        WATER_HYDRATION_RADIUS("ET-0x017"),
        MILK_BUCKET_NUTRITION("ET-0x018");

        private final String code;

        ConfigError(String code) {
            this.code = code;
        }

        public String code()          { return code; }
        public String playerMessageKey() {
            return "experiencetweaks.config.error." + code.toLowerCase().replace("-", "_");
        }
    }

    private static final Set<ConfigError> REPORTED = Collections.synchronizedSet(
            EnumSet.noneOf(ConfigError.class));

    private static void broadcastConfigError(ConfigError error) {
        String localizedLog = net.minecraft.locale.Language.getInstance().getOrDefault(error.playerMessageKey());
        ExperienceTweaksMod.LOGGER.warn("[ModConfig] [{}] {}", error.code(), localizedLog);

        if (!REPORTED.add(error)) {
            return;
        }

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }

        Component alert = Component.empty()
                .append(Component.literal("[ExperienceTweaks] ").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .append(Component.literal("[" + error.code() + "] ").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .append(Component.translatable(error.playerMessageKey()).withStyle(ChatFormatting.RED));

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (server.getPlayerList().getOps().get(new NameAndId(player.getGameProfile())) != null) {
                player.sendSystemMessage(alert);
            }
        }
    }

    public static boolean isDontKeepExperience(String playerName) {
        try {
            List<? extends String> list = Config.DONT_KEEP_EXPERIENCE.get();
            return list.contains(playerName);
        } catch (Exception e) {
            broadcastConfigError(ConfigError.DONT_KEEP_EXPERIENCE);
            return false;
        }
    }

    public static boolean isDirectExperience() {
        try {
            return Config.DIRECT_EXPERIENCE.get();
        } catch (Exception e) {
            broadcastConfigError(ConfigError.DIRECT_EXPERIENCE);
            return true;
        }
    }

    public static Item getEnchantmentCostItem() {
        try {
            String configuredItem = Config.ENCHANTMENT_COST_ITEM.get();
            if (!configuredItem.isBlank()) {
                return BuiltInRegistries.ITEM
                        .getOptional(Identifier.parse(configuredItem))
                        .orElseGet(() -> {
                            broadcastConfigError(ConfigError.COST_ITEM_NOT_FOUND);
                            return Items.LAPIS_LAZULI;
                        });
            }
        } catch (Exception e) {
            broadcastConfigError(ConfigError.INVALID_COST_ITEM);
        }
        return Items.LAPIS_LAZULI;
    }

    public static double getEnchantmentCostMultiplier() {
        try {
            return Config.ENCHANTMENT_COST_MULTIPLIER.get();
        } catch (Exception e) {
            broadcastConfigError(ConfigError.ENCHANTMENT_COST_MULTIPLIER);
            return 1.5;
        }
    }

    public static String getEnchantmentCooldownType() {
        try {
            return Config.ENCHANTMENT_COOLDOWN_TYPE.get();
        } catch (Exception e) {
            broadcastConfigError(ConfigError.ENCHANTMENT_COOLDOWN_TYPE);
            return "current_level";
        }
    }

    public static int getEnchantmentBaseRequiredLevel(int buttonId) {
        try {
            List<? extends Integer> baseLevels = Config.ENCHANTMENT_BASE_REQUIRED_LEVELS.get();
            if (buttonId < baseLevels.size()) {
                return baseLevels.get(buttonId);
            }
        } catch (Exception e) {
            broadcastConfigError(ConfigError.ENCHANTMENT_BASE_REQUIRED_LEVELS);
        }
        return (buttonId + 1) * 10;
    }

    public static double getEnchantmentRequiredLevelBias() {
        try {
            return Config.ENCHANTMENT_REQUIRED_LEVEL_BIAS.get();
        } catch (Exception e) {
            broadcastConfigError(ConfigError.ENCHANTMENT_REQUIRED_LEVEL_BIAS);
            return 0.25;
        }
    }

    public static boolean isGiveExperienceEveryDayEnabled() {
        try {
            return Config.GIVE_EXPERIENCE_EVERY_DAY.get();
        } catch (Exception e) {
            broadcastConfigError(ConfigError.GIVE_EXPERIENCE_EVERY_DAY);
            return false;
        }
    }

    public static int getGiveExperienceEveryDayBase() {
        try {
            return Config.GIVE_EXPERIENCE_EVERY_DAY_BASE.get();
        } catch (Exception e) {
            broadcastConfigError(ConfigError.GIVE_EXPERIENCE_EVERY_DAY_BASE);
            return 5;
        }
    }

    public static double getGiveExperienceEveryDayGrowth() {
        try {
            return Config.GIVE_EXPERIENCE_EVERY_DAY_GROWTH.get();
        } catch (Exception e) {
            broadcastConfigError(ConfigError.GIVE_EXPERIENCE_EVERY_DAY_GROWTH);
            return 0.1;
        }
    }

    public static boolean isAutoFishingEnabled() {
        try {
            return Config.AUTO_FISHING.get();
        } catch (Exception e) {
            broadcastConfigError(ConfigError.AUTO_FISHING);
            return false;
        }
    }

    public static boolean isAutoFishingRecastEnabled() {
        try {
            return Config.AUTO_FISHING_RECAST.get();
        } catch (Exception e) {
            broadcastConfigError(ConfigError.AUTO_FISHING_RECAST);
            return true;
        }
    }

    public static boolean isAnvilBypassTooExpensive() {
        try {
            return Config.ANVIL_BYPASS_TOO_EXPENSIVE.get();
        } catch (Exception e) {
            broadcastConfigError(ConfigError.ANVIL_BYPASS_TOO_EXPENSIVE);
            return true;
        }
    }

    public static boolean isAnvilUseItemCost() {
        try {
            return Config.ANVIL_USE_ITEM_COST.get();
        } catch (Exception e) {
            broadcastConfigError(ConfigError.ANVIL_USE_ITEM_COST);
            return false;
        }
    }

    public static Item getAnvilCostItem() {
        try {
            String configuredItem = Config.ANVIL_COST_ITEM.get();
            if (!configuredItem.isBlank()) {
                return BuiltInRegistries.ITEM
                        .getOptional(Identifier.parse(configuredItem))
                        .orElseGet(() -> {
                            broadcastConfigError(ConfigError.ANVIL_COST_ITEM_NOT_FOUND);
                            return Items.EMERALD;
                        });
            }
        } catch (Exception e) {
            broadcastConfigError(ConfigError.INVALID_ANVIL_COST_ITEM);
        }
        return Items.EMERALD;
    }

    public static double getAnvilItemCostMultiplier() {
        try {
            return Config.ANVIL_ITEM_COST_MULTIPLIER.get();
        } catch (Exception e) {
            broadcastConfigError(ConfigError.ANVIL_ITEM_COST_MULTIPLIER);
            return 0.5;
        }
    }

    public static boolean isAllowMendingWithInfinity() {
        try {
            return Config.ALLOW_MENDING_WITH_INFINITY.get();
        } catch (Exception e) {
            broadcastConfigError(ConfigError.ALLOW_MENDING_WITH_INFINITY);
            return false;
        }
    }

    public static boolean isAnvilEnchantmentExtractionEnabled() {
        try {
            return Config.ANVIL_ENCHANTMENT_EXTRACTION.get();
        } catch (Exception e) {
            broadcastConfigError(ConfigError.ANVIL_ENCHANTMENT_EXTRACTION);
            return true;
        }
    }

    public static boolean isAnvilEnchantmentExtractionDestroySource() {
        try {
            return Config.ANVIL_ENCHANTMENT_EXTRACTION_DESTROY_SOURCE.get();
        } catch (Exception e) {
            broadcastConfigError(ConfigError.ANVIL_ENCHANTMENT_EXTRACTION_DESTROY_SOURCE);
            return true;
        }
    }

    /**
     * Returns {@code true} if water located directly underneath a farmland block or slab hydrates it.
     *
     * @return {@code true} if water under farmland hydrates it
     */
    public static boolean isWaterBelowHydratesFarmlandEnabled() {
        try {
            return Config.WATER_BELOW_HYDRATES_FARMLAND.get();
        } catch (Exception e) {
            broadcastConfigError(ConfigError.WATER_BELOW_HYDRATES_FARMLAND);
            return true;
        }
    }

    /**
     * Returns the horizontal block radius to check for water to hydrate farmland.
     *
     * @return the horizontal water hydration radius in blocks
     */
    public static int getWaterHydrationRadius() {
        try {
            return Config.WATER_HYDRATION_RADIUS.get();
        } catch (Exception e) {
            broadcastConfigError(ConfigError.WATER_HYDRATION_RADIUS);
            return 4;
        }
    }

    /**
     * Returns the number of food points (hunger markers) restored when drinking a milk bucket.
     *
     * @return food points restored by milk bucket
     */
    public static int getMilkBucketNutrition() {
        try {
            return Config.MILK_BUCKET_NUTRITION.get();
        } catch (Exception e) {
            broadcastConfigError(ConfigError.MILK_BUCKET_NUTRITION);
            return 2;
        }
    }
}
