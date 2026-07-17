package com.ziondev.experiencetweaks;

import net.neoforged.neoforge.common.ModConfigSpec;
import java.util.Collections;
import java.util.List;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<List<? extends String>> DONT_KEEP_EXPERIENCE = BUILDER
            .comment(" List of player names who do NOT want keep their experience after death.")
            .defineList("dontKeepExperience", Collections.emptyList(), () -> "", obj -> obj instanceof String);

    public static final ModConfigSpec.BooleanValue DIRECT_EXPERIENCE = BUILDER
            .comment("\n If true, experience points will be inserted directly into the player instead of dropping as orbs.")
            .define("directExperience", true);

    public static final ModConfigSpec.ConfigValue<String> ENCHANTMENT_COST_ITEM = BUILDER
            .comment("\n Item consumed instead of experience when enchanting. Use registry name like 'minecraft:diamond'. If empty or invalid, lapis lazuli is used.")
            .define("enchantmentCostItem", "minecraft:emerald");

    public static final ModConfigSpec.DoubleValue ENCHANTMENT_COST_MULTIPLIER = BUILDER
            .comment("\n Multiplier for the item cost based on the required enchantment level. (e.g., 30 levels * 0.1 = 3 items)")
            .defineInRange("enchantmentCostMultiplier", 1.5, 0.0, 100.0);

    public static final ModConfigSpec.ConfigValue<String> ENCHANTMENT_COOLDOWN_TYPE = BUILDER
            .comment("\n Type of cooldown for enchantment buttons. Options: 'current_level' (default), 'last_level'.")
            .define("enchantmentCooldownType", "current_level");

    public static final ModConfigSpec.ConfigValue<List<? extends Integer>> ENCHANTMENT_BASE_REQUIRED_LEVELS = BUILDER
            .comment("\n Initial player experience levels required for enchantment table buttons 1, 2 and 3. Player progression is stored separately per player.")
            .defineList("enchantmentBaseRequiredLevels", List.of(10, 15, 20), () -> 0, obj -> obj instanceof Integer integer && integer >= 0);

    public static final ModConfigSpec.DoubleValue ENCHANTMENT_REQUIRED_LEVEL_BIAS = BUILDER
            .comment(
                "\n Difficulty weight for the enchantment cooldown curve. Range: 0.0 to 1.0.",
                " Formula: ceil(buttonIndex x bias x 50 / sqrt(currentPlayerLevel))",
                " Uses a square-root curve so the cooldown stays meaningful at high levels (200+).",
                "  0.0 = minimum difficulty (always +1 level)",
                "  0.5 = balanced default",
                "  1.0 = maximum difficulty",
                " Example increments for button 3 (hardest):",
                "  Level  10: bias 0.1=+5,  bias 0.5=+24, bias 1.0=+48",
                "  Level  50: bias 0.1=+2,  bias 0.5=+11, bias 1.0=+21",
                "  Level 100: bias 0.1=+2,  bias 0.5=+8,  bias 1.0=+15",
                "  Level 200: bias 0.1=+1,  bias 0.5=+5,  bias 1.0=+11",
                "  Level 500: bias 0.1=+1,  bias 0.5=+3,  bias 1.0=+7",
                "  Level 1000: bias 0.1=+1, bias 0.5=+3,  bias 1.0=+5"
            )
            .defineInRange("enchantmentRequiredLevelBias", 0.25, 0.0, 1.0);

    public static final ModConfigSpec.BooleanValue GIVE_EXPERIENCE_EVERY_DAY = BUILDER
            .comment("\n If true, players will receive experience points every in-game day they survive without dying.")
            .define("giveExperienceEveryDay", false);

    public static final ModConfigSpec.IntValue GIVE_EXPERIENCE_EVERY_DAY_BASE = BUILDER
            .comment("\n Base experience points awarded to players each day they survive.")
            .defineInRange("giveExperienceEveryDayBase", 5, 0, 100000);

    public static final ModConfigSpec.DoubleValue GIVE_EXPERIENCE_EVERY_DAY_GROWTH = BUILDER
            .comment("\n Growth multiplier (percentage) for consecutive days survived. Example: 0.1 = 10% more experience per consecutive day survived.")
            .defineInRange("giveExperienceEveryDayGrowth", 0.1, 0.0, 100.0);

    public static final ModConfigSpec.BooleanValue AUTO_FISHING = BUILDER
            .comment("\n If true, players will automatically reel in and recast their fishing rod when a fish bites.")
            .define("autoFishing", false);

    public static final ModConfigSpec.BooleanValue AUTO_FISHING_RECAST = BUILDER
            .comment("\n If true, players will automatically recast their fishing rod after reeling in a fish (requires autoFishing to be enabled).")
            .define("autoFishingRecast", true);

    public static final ModConfigSpec.BooleanValue ANVIL_BYPASS_TOO_EXPENSIVE = BUILDER
            .comment("\n If true, the 'Too Expensive!' level cost cap (40 levels) in the anvil is disabled.")
            .define("anvilBypassTooExpensive", true);

    public static final ModConfigSpec.BooleanValue ANVIL_USE_ITEM_COST = BUILDER
            .comment("\n If true, anvil repairs, combinations, and renames will consume items instead of experience levels.")
            .define("anvilUseItemCost", false);

    public static final ModConfigSpec.ConfigValue<String> ANVIL_COST_ITEM = BUILDER
            .comment("\n Item consumed instead of experience when using the anvil (if anvilUseItemCost is true). Use registry name like 'minecraft:emerald'. If empty or invalid, emerald is used.")
            .define("anvilCostItem", "minecraft:emerald");

    public static final ModConfigSpec.DoubleValue ANVIL_ITEM_COST_MULTIPLIER = BUILDER
            .comment("\n Multiplier for the item cost based on the vanilla experience level cost. (e.g., 10 levels * 0.5 = 5 items). Minimum cost is always 1 item.")
            .defineInRange("anvilItemCostMultiplier", 0.5, 0.0, 100.0);

    public static final ModConfigSpec.BooleanValue ALLOW_MENDING_WITH_INFINITY = BUILDER
            .comment("\n If true, Mending and Infinity enchantments can be combined on the same item via the anvil.")
            .define("allowMendingWithInfinity", false);

    public static final ModConfigSpec.BooleanValue ANVIL_ENCHANTMENT_EXTRACTION = BUILDER
            .comment("\n If true, placing an enchanted item in the left slot and a blank book in the right slot of the anvil extracts the first enchantment into an enchanted book.")
            .define("anvilEnchantmentExtraction", true);

    public static final ModConfigSpec.BooleanValue ANVIL_ENCHANTMENT_EXTRACTION_DESTROY_SOURCE = BUILDER
            .comment("\n If true, the source item is destroyed when its last enchantment is extracted. If false, it is returned without enchantments.")
            .define("anvilEnchantmentExtractionDestroySource", true);

    static final ModConfigSpec SPEC = BUILDER.build();
}
