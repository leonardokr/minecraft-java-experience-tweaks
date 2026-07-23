package com.ziondev.experiencetweaks;

import com.ziondev.experiencetweaks.network.ClientServerConfigCache;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Facade class providing safe, error-tolerant accessors for all mod configuration settings
 * stored in {@link ServerConfig} and {@link ClientConfig}.
 */
public final class ModConfig {

    private ModConfig() {}

    public enum ConfigError {

        INVALID_COST_ITEM("ET-0x001"),
        COST_ITEM_NOT_FOUND("ET-0x002"),
        DIRECT_EXPERIENCE("ET-0x003"),
        KEEP_EXPERIENCE("ET-0x004"),
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
        MILK_BUCKET_NUTRITION("ET-0x018"),
        WANDERING_TRADER_UNLIMITED_TRADES("ET-0x019"),
        VILLAGER_UNLIMITED_TRADES("ET-0x01a"),
        ALL_ARROWS_AFFECTED_BY_INFINITY("ET-0x01b"),
        ALLOW_MULTIPLE_DAMAGE_ENCHANTMENTS("ET-0x01c"),
        ALLOW_MULTIPLE_PROTECTION_ENCHANTMENTS("ET-0x01d"),
        ALLOW_PIERCING_WITH_MULTISHOT("ET-0x01e"),
        ALLOW_MULTIPLE_TRIDENT_ENCHANTMENTS("ET-0x01f"),
        RIPTIDE_ANYWHERE("ET-0x020"),
        ANVIL_DURABILITY_MULTIPLIER("ET-0x021"),
        MOB_ARROWS_COLLECTIBLE("ET-0x022");

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

    /**
     * Returns whether keeping experience on death is enabled for a player.
     *
     * @param player the player to check
     * @return {@code true} if experience is kept on death
     */
    public static boolean isKeepExperienceEnabled(Player player) {
        if (player != null && !player.level().isClientSide()) {
            Boolean cached = com.ziondev.experiencetweaks.network.ServerClientSettingsCache.getKeepExperience(player.getUUID());
            if (cached != null) {
                return cached;
            }
        }
        return isKeepExperienceEnabled();
    }

    /**
     * Returns whether keeping experience on death is enabled.
     *
     * @return {@code true} if experience is kept on death
     */
    public static boolean isKeepExperienceEnabled() {
        try {
            if (ClientConfig.SPEC.isLoaded()) {
                return ClientConfig.KEEP_EXPERIENCE.get();
            }
        } catch (Exception e) {
            broadcastConfigError(ConfigError.KEEP_EXPERIENCE);
        }
        return true;
    }

    /**
     * Returns whether experience points are inserted directly into the player.
     *
     * @param player the player to check
     * @return {@code true} if direct experience is enabled
     */
    public static boolean isDirectExperience(Player player) {
        if (player != null && !player.level().isClientSide()) {
            Boolean cached = com.ziondev.experiencetweaks.network.ServerClientSettingsCache.getDirectExperience(player.getUUID());
            if (cached != null) {
                return cached;
            }
        }
        return isDirectExperience();
    }

    /**
     * Returns whether experience points are inserted directly.
     *
     * @return {@code true} if direct experience is enabled
     */
    public static boolean isDirectExperience() {
        try {
            if (ClientConfig.SPEC.isLoaded()) {
                return ClientConfig.DIRECT_EXPERIENCE.get();
            }
        } catch (Exception e) {
            broadcastConfigError(ConfigError.DIRECT_EXPERIENCE);
        }
        return true;
    }

    /**
     * Returns the item consumed when enchanting.
     *
     * @return the configured enchantment cost item
     */
    public static Item getEnchantmentCostItem() {
        try {
            if (ServerConfig.SPEC.isLoaded()) {
                String configuredItem = ServerConfig.ENCHANTMENT_COST_ITEM.get();
                if (configuredItem != null && !configuredItem.isBlank()) {
                    return BuiltInRegistries.ITEM
                            .getOptional(Identifier.parse(configuredItem))
                            .orElseGet(() -> {
                                broadcastConfigError(ConfigError.COST_ITEM_NOT_FOUND);
                                return Items.LAPIS_LAZULI;
                            });
                }
            }
        } catch (Exception e) {
            broadcastConfigError(ConfigError.INVALID_COST_ITEM);
        }
        return Items.LAPIS_LAZULI;
    }

    /**
     * Returns the item cost multiplier for enchanting.
     *
     * @return the enchantment item cost multiplier
     */
    public static double getEnchantmentCostMultiplier() {
        try {
            if (ServerConfig.SPEC.isLoaded()) {
                return ServerConfig.ENCHANTMENT_COST_MULTIPLIER.get();
            }
        } catch (Exception e) {
            broadcastConfigError(ConfigError.ENCHANTMENT_COST_MULTIPLIER);
        }
        return 1.5;
    }

    /**
     * Returns the enchantment button cooldown type.
     *
     * @return the enchantment cooldown type string
     */
    public static String getEnchantmentCooldownType() {
        try {
            if (ServerConfig.SPEC.isLoaded()) {
                return ServerConfig.ENCHANTMENT_COOLDOWN_TYPE.get();
            }
        } catch (Exception e) {
            broadcastConfigError(ConfigError.ENCHANTMENT_COOLDOWN_TYPE);
        }
        return "current_level";
    }

    /**
     * Returns the base required level for an enchantment table button index.
     *
     * @param buttonId index of the button (0, 1, or 2)
     * @return the required base level
     */
    public static int getEnchantmentBaseRequiredLevel(int buttonId) {
        try {
            if (ServerConfig.SPEC.isLoaded()) {
                List<? extends Integer> baseLevels = ServerConfig.ENCHANTMENT_BASE_REQUIRED_LEVELS.get();
                if (buttonId >= 0 && buttonId < baseLevels.size()) {
                    return baseLevels.get(buttonId);
                }
            }
        } catch (Exception e) {
            broadcastConfigError(ConfigError.ENCHANTMENT_BASE_REQUIRED_LEVELS);
        }
        return (buttonId + 1) * 10;
    }

    /**
     * Returns the difficulty bias for the enchantment cooldown curve.
     *
     * @return the enchantment required level bias
     */
    public static double getEnchantmentRequiredLevelBias() {
        try {
            if (ServerConfig.SPEC.isLoaded()) {
                return ServerConfig.ENCHANTMENT_REQUIRED_LEVEL_BIAS.get();
            }
        } catch (Exception e) {
            broadcastConfigError(ConfigError.ENCHANTMENT_REQUIRED_LEVEL_BIAS);
        }
        return 0.25;
    }

    /**
     * Returns whether daily experience rewards are enabled for a player.
     *
     * @param player the player to check
     * @return {@code true} if daily experience rewards are enabled
     */
    public static boolean isGiveExperienceEveryDayEnabled(Player player) {
        if (player != null && !player.level().isClientSide()) {
            Boolean cached = com.ziondev.experiencetweaks.network.ServerClientSettingsCache.getGiveExperienceEveryDay(player.getUUID());
            if (cached != null) {
                return cached;
            }
        }
        return isGiveExperienceEveryDayEnabled();
    }

    /**
     * Returns whether daily experience rewards are enabled.
     *
     * @return {@code true} if daily experience rewards are enabled
     */
    public static boolean isGiveExperienceEveryDayEnabled() {
        try {
            if (ClientConfig.SPEC.isLoaded()) {
                return ClientConfig.GIVE_EXPERIENCE_EVERY_DAY.get();
            }
        } catch (Exception e) {
            broadcastConfigError(ConfigError.GIVE_EXPERIENCE_EVERY_DAY);
        }
        return true;
    }

    /**
     * Returns base experience points awarded per day survived from server configuration.
     *
     * @return base daily experience points
     */
    public static int getGiveExperienceEveryDayBase() {
        try {
            if (ServerConfig.SPEC.isLoaded()) {
                return ServerConfig.GIVE_EXPERIENCE_EVERY_DAY_BASE.get();
            }
        } catch (Exception e) {
            broadcastConfigError(ConfigError.GIVE_EXPERIENCE_EVERY_DAY_BASE);
        }
        return 5;
    }

    /**
     * Returns growth percentage per consecutive day survived from server configuration.
     *
     * @return daily experience growth multiplier
     */
    public static double getGiveExperienceEveryDayGrowth() {
        try {
            if (ServerConfig.SPEC.isLoaded()) {
                return ServerConfig.GIVE_EXPERIENCE_EVERY_DAY_GROWTH.get();
            }
        } catch (Exception e) {
            broadcastConfigError(ConfigError.GIVE_EXPERIENCE_EVERY_DAY_GROWTH);
        }
        return 0.1;
    }

    /**
     * Returns whether auto-fishing is enabled for the client.
     *
     * @return {@code true} if auto-fishing is enabled
     */
    public static boolean isAutoFishingEnabled() {
        try {
            if (ClientConfig.SPEC.isLoaded()) {
                return ClientConfig.AUTO_FISHING.get();
            }
        } catch (Exception e) {
            broadcastConfigError(ConfigError.AUTO_FISHING);
        }
        return true;
    }

    /**
     * Returns whether auto-recasting fishing rod is enabled for the client.
     *
     * @return {@code true} if auto-recasting is enabled
     */
    public static boolean isAutoFishingRecastEnabled() {
        try {
            if (ClientConfig.SPEC.isLoaded()) {
                return ClientConfig.AUTO_FISHING_RECAST.get();
            }
        } catch (Exception e) {
            broadcastConfigError(ConfigError.AUTO_FISHING_RECAST);
        }
        return true;
    }

    /**
     * Returns whether the anvil 40-level cost limit is bypassed.
     *
     * @return {@code true} if anvil cost limit is bypassed
     */
    public static boolean isAnvilBypassTooExpensive() {
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            Boolean cached = ClientServerConfigCache.getAnvilBypassTooExpensive();
            if (cached != null) {
                return cached;
            }
        }
        try {
            if (ServerConfig.SPEC.isLoaded()) {
                return ServerConfig.ANVIL_BYPASS_TOO_EXPENSIVE.get();
            }
        } catch (Exception e) {
            broadcastConfigError(ConfigError.ANVIL_BYPASS_TOO_EXPENSIVE);
        }
        return true;
    }

    /**
     * Returns whether anvil operations consume items instead of experience.
     *
     * @return {@code true} if anvil uses item cost
     */
    public static boolean isAnvilUseItemCost() {
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            Boolean cached = ClientServerConfigCache.getAnvilUseItemCost();
            if (cached != null) {
                return cached;
            }
        }
        try {
            if (ServerConfig.SPEC.isLoaded()) {
                return ServerConfig.ANVIL_USE_ITEM_COST.get();
            }
        } catch (Exception e) {
            broadcastConfigError(ConfigError.ANVIL_USE_ITEM_COST);
        }
        return true;
    }

    /**
     * Returns the item type consumed by anvil operations.
     *
     * @return the configured anvil cost item
     */
    public static Item getAnvilCostItem() {
        String configuredItem = null;
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            configuredItem = ClientServerConfigCache.getAnvilCostItem();
        }
        if (configuredItem == null || configuredItem.isBlank()) {
            try {
                if (ServerConfig.SPEC.isLoaded()) {
                    configuredItem = ServerConfig.ANVIL_COST_ITEM.get();
                }
            } catch (Exception e) {
                broadcastConfigError(ConfigError.INVALID_ANVIL_COST_ITEM);
            }
        }

        if (configuredItem != null && !configuredItem.isBlank()) {
            try {
                return BuiltInRegistries.ITEM
                        .getOptional(Identifier.parse(configuredItem))
                        .orElseGet(() -> {
                            broadcastConfigError(ConfigError.ANVIL_COST_ITEM_NOT_FOUND);
                            return Items.EMERALD;
                        });
            } catch (Exception e) {
                broadcastConfigError(ConfigError.INVALID_ANVIL_COST_ITEM);
            }
        }
        return Items.EMERALD;
    }

    /**
     * Returns the item cost multiplier for anvil operations.
     *
     * @return the anvil item cost multiplier
     */
    public static double getAnvilItemCostMultiplier() {
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            Double cached = ClientServerConfigCache.getAnvilItemCostMultiplier();
            if (cached != null) {
                return cached;
            }
        }
        try {
            if (ServerConfig.SPEC.isLoaded()) {
                return ServerConfig.ANVIL_ITEM_COST_MULTIPLIER.get();
            }
        } catch (Exception e) {
            broadcastConfigError(ConfigError.ANVIL_ITEM_COST_MULTIPLIER);
        }
        return 0.5;
    }

    /**
     * Returns whether Mending and Infinity can be combined on the same item.
     *
     * @return {@code true} if Mending and Infinity can be combined
     */
    public static boolean isAllowMendingWithInfinity() {
        try {
            if (ServerConfig.SPEC.isLoaded()) {
                return ServerConfig.ALLOW_MENDING_WITH_INFINITY.get();
            }
        } catch (Exception e) {
            broadcastConfigError(ConfigError.ALLOW_MENDING_WITH_INFINITY);
        }
        return false;
    }

    /**
     * Returns whether anvil enchantment extraction is enabled.
     *
     * @return {@code true} if enchantment extraction is enabled
     */
    public static boolean isAnvilEnchantmentExtractionEnabled() {
        try {
            if (ServerConfig.SPEC.isLoaded()) {
                return ServerConfig.ANVIL_ENCHANTMENT_EXTRACTION.get();
            }
        } catch (Exception e) {
            broadcastConfigError(ConfigError.ANVIL_ENCHANTMENT_EXTRACTION);
        }
        return true;
    }

    /**
     * Returns whether the source item is destroyed when its last enchantment is extracted.
     *
     * @return {@code true} if source item is destroyed
     */
    public static boolean isAnvilEnchantmentExtractionDestroySource() {
        try {
            if (ServerConfig.SPEC.isLoaded()) {
                return ServerConfig.ANVIL_ENCHANTMENT_EXTRACTION_DESTROY_SOURCE.get();
            }
        } catch (Exception e) {
            broadcastConfigError(ConfigError.ANVIL_ENCHANTMENT_EXTRACTION_DESTROY_SOURCE);
        }
        return true;
    }

    /**
     * Returns {@code true} if water located directly underneath a farmland block or slab hydrates it.
     *
     * @return {@code true} if water under farmland hydrates it
     */
    public static boolean isWaterBelowHydratesFarmlandEnabled() {
        try {
            if (ServerConfig.SPEC.isLoaded()) {
                return ServerConfig.WATER_BELOW_HYDRATES_FARMLAND.get();
            }
        } catch (Exception e) {
            broadcastConfigError(ConfigError.WATER_BELOW_HYDRATES_FARMLAND);
        }
        return true;
    }

    /**
     * Returns the horizontal block radius to check for water to hydrate farmland.
     *
     * @return the horizontal water hydration radius in blocks
     */
    public static int getWaterHydrationRadius() {
        try {
            if (ServerConfig.SPEC.isLoaded()) {
                return ServerConfig.WATER_HYDRATION_RADIUS.get();
            }
        } catch (Exception e) {
            broadcastConfigError(ConfigError.WATER_HYDRATION_RADIUS);
        }
        return 4;
    }

    /**
     * Returns the number of food points (hunger markers) restored when drinking milk.
     *
     * @return food points restored by milk
     */
    public static int getMilkBucketNutrition() {
        try {
            if (ServerConfig.SPEC.isLoaded()) {
                return ServerConfig.MILK_BUCKET_NUTRITION.get();
            }
        } catch (Exception e) {
            broadcastConfigError(ConfigError.MILK_BUCKET_NUTRITION);
        }
        return 2;
    }

    /**
     * Returns whether trade limits for Wandering Traders are disabled.
     *
     * @return {@code true} if Wandering Trader trade limits are disabled
     */
    public static boolean isWanderingTraderUnlimitedTrades() {
        try {
            if (ServerConfig.SPEC.isLoaded()) {
                return ServerConfig.WANDERING_TRADER_UNLIMITED_TRADES.get();
            }
        } catch (Exception e) {
            broadcastConfigError(ConfigError.WANDERING_TRADER_UNLIMITED_TRADES);
        }
        return true;
    }

    /**
     * Returns whether daily trade limits for Villagers are disabled.
     *
     * @return {@code true} if Villager trade limits are disabled
     */
    public static boolean isVillagerUnlimitedTrades() {
        try {
            if (ServerConfig.SPEC.isLoaded()) {
                return ServerConfig.VILLAGER_UNLIMITED_TRADES.get();
            }
        } catch (Exception e) {
            broadcastConfigError(ConfigError.VILLAGER_UNLIMITED_TRADES);
        }
        return true;
    }

    /**
     * Returns whether all types of arrows are affected by the Infinity enchantment.
     *
     * @return {@code true} if all arrows are affected by Infinity
     */
    public static boolean isAllArrowsAffectedByInfinity() {
        try {
            if (ServerConfig.SPEC.isLoaded()) {
                return ServerConfig.ALL_ARROWS_AFFECTED_BY_INFINITY.get();
            }
        } catch (Exception e) {
            broadcastConfigError(ConfigError.ALL_ARROWS_AFFECTED_BY_INFINITY);
        }
        return false;
    }

    /**
     * Returns whether multiple damage enchantments (Sharpness, Smite, Bane of Arthropods) can coexist.
     *
     * @return {@code true} if multiple damage enchantments are allowed
     */
    public static boolean isAllowMultipleDamageEnchantments() {
        try {
            if (ServerConfig.SPEC.isLoaded()) {
                return ServerConfig.ALLOW_MULTIPLE_DAMAGE_ENCHANTMENTS.get();
            }
        } catch (Exception e) {
            broadcastConfigError(ConfigError.ALLOW_MULTIPLE_DAMAGE_ENCHANTMENTS);
        }
        return false;
    }

    /**
     * Returns whether multiple protection enchantments can coexist.
     *
     * @return {@code true} if multiple protection enchantments are allowed
     */
    public static boolean isAllowMultipleProtectionEnchantments() {
        try {
            if (ServerConfig.SPEC.isLoaded()) {
                return ServerConfig.ALLOW_MULTIPLE_PROTECTION_ENCHANTMENTS.get();
            }
        } catch (Exception e) {
            broadcastConfigError(ConfigError.ALLOW_MULTIPLE_PROTECTION_ENCHANTMENTS);
        }
        return false;
    }

    /**
     * Returns whether Piercing and Multishot enchantments can coexist on the same crossbow.
     *
     * @return {@code true} if Piercing and Multishot can be combined
     */
    public static boolean isAllowPiercingWithMultishot() {
        try {
            if (ServerConfig.SPEC.isLoaded()) {
                return ServerConfig.ALLOW_PIERCING_WITH_MULTISHOT.get();
            }
        } catch (Exception e) {
            broadcastConfigError(ConfigError.ALLOW_PIERCING_WITH_MULTISHOT);
        }
        return false;
    }

    /**
     * Returns whether Channeling, Loyalty, and Riptide enchantments can coexist on the same trident.
     *
     * @return {@code true} if Channeling, Loyalty, and Riptide can be combined
     */
    public static boolean isAllowMultipleTridentEnchantments() {
        try {
            if (ServerConfig.SPEC.isLoaded()) {
                return ServerConfig.ALLOW_MULTIPLE_TRIDENT_ENCHANTMENTS.get();
            }
        } catch (Exception e) {
            broadcastConfigError(ConfigError.ALLOW_MULTIPLE_TRIDENT_ENCHANTMENTS);
        }
        return false;
    }

    /**
     * Returns whether the Riptide enchantment can be used anywhere, regardless of weather or water.
     * This overload is used on the logical server to read a player's synchronized client-side preferences.
     *
     * @param player the player to check
     * @return {@code true} if Riptide can be used anywhere
     */
    public static boolean isRiptideAnywhere(Player player) {
        if (player != null && !player.level().isClientSide()) {
            Boolean cached = com.ziondev.experiencetweaks.network.ServerClientSettingsCache.getRiptideAnywhere(player.getUUID());
            if (cached != null) {
                return cached;
            }
        }
        return isRiptideAnywhere();
    }

    /**
     * Returns whether the Riptide enchantment can be used anywhere, regardless of weather or water.
     * This overload is used on the logical client to check the local player's client configuration directly.
     *
     * @return {@code true} if Riptide can be used anywhere
     */
    public static boolean isRiptideAnywhere() {
        try {
            if (ClientConfig.SPEC.isLoaded()) {
                return ClientConfig.RIPTIDE_ANYWHERE.get();
            }
        } catch (Exception e) {
            broadcastConfigError(ConfigError.RIPTIDE_ANYWHERE);
        }
        return false;
    }

    /**
     * Returns whether arrows shot by mobs are collectible by a player.
     *
     * @param player the player to check
     * @return {@code true} if mob arrows are collectible
     */
    public static boolean isMobArrowsCollectible(Player player) {
        if (player != null && !player.level().isClientSide()) {
            Boolean cached = com.ziondev.experiencetweaks.network.ServerClientSettingsCache.getMobArrowsCollectible(player.getUUID());
            if (cached != null) {
                return cached;
            }
        }
        return isMobArrowsCollectible();
    }

    /**
     * Returns whether arrows shot by mobs are collectible.
     *
     * @return {@code true} if mob arrows are collectible
     */
    public static boolean isMobArrowsCollectible() {
        try {
            if (ClientConfig.SPEC.isLoaded()) {
                return ClientConfig.MOB_ARROWS_COLLECTIBLE.get();
            }
        } catch (Exception e) {
            broadcastConfigError(ConfigError.MOB_ARROWS_COLLECTIBLE);
        }
        return false;
    }

    /**
     * Returns the anvil durability multiplier percentage.
     *
     * @return the anvil durability multiplier percentage
     */
    public static int getAnvilDurabilityMultiplier() {
        try {
            if (ServerConfig.SPEC.isLoaded()) {
                return ServerConfig.ANVIL_DURABILITY_MULTIPLIER.get();
            }
        } catch (Exception e) {
            broadcastConfigError(ConfigError.ANVIL_DURABILITY_MULTIPLIER);
        }
        return 100;
    }

    /**
     * Returns whether client-side debug logging is enabled.
     *
     * @return {@code true} if client-side debug logging is enabled
     */
    public static boolean isDebugModeEnabled() {
        try {
            if (ClientConfig.SPEC.isLoaded()) {
                return ClientConfig.ENABLE_DEBUG_MODE.get();
            }
        } catch (Exception e) {
            ExperienceTweaksMod.LOGGER.error("Failed to read client debug mode config", e);
        }
        return false;
    }

    /**
     * Returns whether server-side debug logging is enabled.
     *
     * @return {@code true} if server-side debug logging is enabled
     */
    public static boolean isServerDebugModeEnabled() {
        try {
            if (ServerConfig.SPEC.isLoaded()) {
                return ServerConfig.ENABLE_DEBUG_MODE.get();
            }
        } catch (Exception e) {
            ExperienceTweaksMod.LOGGER.error("Failed to read server debug mode config", e);
        }
        return false;
    }
}
