package com.ziondev.experiencetweaks;

import net.neoforged.neoforge.common.ModConfigSpec;
import java.util.List;

/**
 * Defines server and common configuration specifications for world mechanics, block rules,
 * anvil mechanics, enchantment table settings, and server-side daily experience reward amounts.
 */
public class ServerConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    /** Base experience points awarded to players each day they survive. */
    public static final ModConfigSpec.IntValue GIVE_EXPERIENCE_EVERY_DAY_BASE = BUILDER
            .comment("\n Base experience points awarded to players each day they survive.")
            .defineInRange("giveExperienceEveryDayBase", 5, 0, 100000);

    /** Growth multiplier (percentage) for consecutive days survived. */
    public static final ModConfigSpec.DoubleValue GIVE_EXPERIENCE_EVERY_DAY_GROWTH = BUILDER
            .comment("\n Growth multiplier (percentage) for consecutive days survived. Example: 0.1 = 10% more experience per consecutive day survived.")
            .defineInRange("giveExperienceEveryDayGrowth", 0.1, 0.0, 100.0);

    /** Item consumed instead of experience when enchanting. */
    public static final ModConfigSpec.ConfigValue<String> ENCHANTMENT_COST_ITEM = BUILDER
            .comment("\n Item consumed instead of experience when enchanting. Use registry name like 'minecraft:diamond'. If empty or invalid, emerald is used.")
            .define("enchantmentCostItem", "minecraft:emerald");

    /** Multiplier for the item cost based on the required enchantment level. */
    public static final ModConfigSpec.DoubleValue ENCHANTMENT_COST_MULTIPLIER = BUILDER
            .comment("\n Multiplier for the item cost based on the required enchantment level. (e.g., 30 levels * 0.1 = 3 items)")
            .defineInRange("enchantmentCostMultiplier", 1.5, 0.0, 100.0);

    /** Type of cooldown for enchantment buttons. */
    public static final ModConfigSpec.ConfigValue<String> ENCHANTMENT_COOLDOWN_TYPE = BUILDER
            .comment("\n Type of cooldown for enchantment buttons. Options: 'current_level' (default), 'last_level'.")
            .define("enchantmentCooldownType", "current_level");

    /** Initial player experience levels required for enchantment table buttons. */
    public static final ModConfigSpec.ConfigValue<List<? extends Integer>> ENCHANTMENT_BASE_REQUIRED_LEVELS = BUILDER
            .comment("\n Initial player experience levels required for enchantment table buttons 1, 2 and 3. Player progression is stored separately per player.")
            .defineList("enchantmentBaseRequiredLevels", List.of(10, 15, 20), () -> 0, obj -> obj instanceof Integer integer && integer >= 0);

    /** Difficulty weight for the enchantment cooldown curve. */
    public static final ModConfigSpec.DoubleValue ENCHANTMENT_REQUIRED_LEVEL_BIAS = BUILDER
            .comment(
                "\n Difficulty weight for the enchantment cooldown curve. Range: 0.0 to 1.0.",
                " Formula: ceil(buttonIndex x bias x 50 / sqrt(currentPlayerLevel))",
                " Uses a square-root curve so the cooldown stays meaningful at high levels (200+).",
                "  0.0 = minimum difficulty (always +1 level)",
                "  0.5 = balanced default",
                "  1.0 = maximum difficulty"
            )
            .defineInRange("enchantmentRequiredLevelBias", 0.25, 0.0, 1.0);

    /** If true, the 'Too Expensive!' level cost cap (40 levels) in the anvil is disabled. */
    public static final ModConfigSpec.BooleanValue ANVIL_BYPASS_TOO_EXPENSIVE = BUILDER
            .comment("\n If true, the 'Too Expensive!' level cost cap (40 levels) in the anvil is disabled.")
            .define("anvilBypassTooExpensive", true);

    /** If true, anvil repairs, combinations, and renames will consume items instead of experience levels. */
    public static final ModConfigSpec.BooleanValue ANVIL_USE_ITEM_COST = BUILDER
            .comment("\n If true, anvil repairs, combinations, and renames will consume items instead of experience levels.")
            .define("anvilUseItemCost", true);

    /** Item consumed instead of experience when using the anvil. */
    public static final ModConfigSpec.ConfigValue<String> ANVIL_COST_ITEM = BUILDER
            .comment("\n Item consumed instead of experience when using the anvil (if anvilUseItemCost is true). Use registry name like 'minecraft:emerald'. If empty or invalid, emerald is used.")
            .define("anvilCostItem", "minecraft:emerald");

    /** Multiplier for the item cost based on the vanilla experience level cost. */
    public static final ModConfigSpec.DoubleValue ANVIL_ITEM_COST_MULTIPLIER = BUILDER
            .comment("\n Multiplier for the item cost based on the vanilla experience level cost. (e.g., 10 levels * 0.5 = 5 items). Minimum cost is always 1 item.")
            .defineInRange("anvilItemCostMultiplier", 0.5, 0.0, 100.0);

    /** If true, Mending and Infinity enchantments can be combined on the same item via the anvil. */
    public static final ModConfigSpec.BooleanValue ALLOW_MENDING_WITH_INFINITY = BUILDER
            .comment("\n If true, Mending and Infinity enchantments can be combined on the same item via the anvil.")
            .define("allowMendingWithInfinity", false);

    /** If true, placing an enchanted item in the left slot and a blank book in the right slot extracts enchantment. */
    public static final ModConfigSpec.BooleanValue ANVIL_ENCHANTMENT_EXTRACTION = BUILDER
            .comment("\n If true, placing an enchanted item in the left slot and a blank book in the right slot of the anvil extracts the first enchantment into an enchanted book.")
            .define("anvilEnchantmentExtraction", true);

    /** If true, the source item is destroyed when its last enchantment is extracted. */
    public static final ModConfigSpec.BooleanValue ANVIL_ENCHANTMENT_EXTRACTION_DESTROY_SOURCE = BUILDER
            .comment("\n If true, the source item is destroyed when its last enchantment is extracted. If false, it is returned without enchantments.")
            .define("anvilEnchantmentExtractionDestroySource", true);

    /** If true, water located directly underneath a farmland block (or farmland slab) will hydrate it. */
    public static final ModConfigSpec.BooleanValue WATER_BELOW_HYDRATES_FARMLAND = BUILDER
            .comment("\n If true, water located directly underneath a farmland block (or farmland slab) will hydrate it.")
            .define("waterBelowHydratesFarmland", true);

    /** Horizontal radius (in blocks) to check for water to hydrate farmland blocks and slabs. */
    public static final ModConfigSpec.IntValue WATER_HYDRATION_RADIUS = BUILDER
            .comment("\n Horizontal radius (in blocks) to check for water to hydrate farmland blocks and slabs. Vanilla default is 4.")
            .defineInRange("waterHydrationRadius", 4, 0, 16);

    /** Food points (hunger level) restored when drinking a milk bucket. */
    public static final ModConfigSpec.IntValue MILK_BUCKET_NUTRITION = BUILDER
            .comment("\n Food points (hunger level) restored when drinking a milk bucket. Default is 2 (1 full hunger shank).")
            .defineInRange("milkBucketNutrition", 2, 0, 20);

    /** If true, trade limits for Wandering Traders are disabled. */
    public static final ModConfigSpec.BooleanValue WANDERING_TRADER_UNLIMITED_TRADES = BUILDER
            .comment("\n If true, trade limits for Wandering Traders are disabled.")
            .define("wanderingTraderUnlimitedTrades", true);

    /** If true, daily trade limits for Villagers are disabled. */
    public static final ModConfigSpec.BooleanValue VILLAGER_UNLIMITED_TRADES = BUILDER
            .comment("\n If true, daily trade limits for Villagers are disabled.")
            .define("villagerUnlimitedTrades", true);

    /** If true, all types of arrows are affected by the Infinity enchantment. */
    public static final ModConfigSpec.BooleanValue ALL_ARROWS_AFFECTED_BY_INFINITY = BUILDER
            .comment("\n If true, all types of arrows (spectral, tipped, etc.) will be affected by the Infinity enchantment.")
            .define("allArrowsAffectedByInfinity", false);

    /** If true, Sharpness, Smite, and Bane of Arthropods can be combined on the same weapon. */
    public static final ModConfigSpec.BooleanValue ALLOW_MULTIPLE_DAMAGE_ENCHANTMENTS = BUILDER
            .comment("\n If true, Sharpness, Smite, and Bane of Arthropods can be combined on the same weapon.")
            .define("allowMultipleDamageEnchantments", false);

    /** If true, Protection, Projectile Protection, Fire Protection, and Blast Protection can be combined. */
    public static final ModConfigSpec.BooleanValue ALLOW_MULTIPLE_PROTECTION_ENCHANTMENTS = BUILDER
            .comment("\n If true, Protection, Projectile Protection, Fire Protection, and Blast Protection can be combined on the same armor item.")
            .define("allowMultipleProtectionEnchantments", false);

    /** If true, Piercing and Multishot enchantments can be combined on the same crossbow. */
    public static final ModConfigSpec.BooleanValue ALLOW_PIERCING_WITH_MULTISHOT = BUILDER
            .comment("\n If true, Piercing and Multishot enchantments can be combined on the same crossbow.")
            .define("allowPiercingWithMultishot", false);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
