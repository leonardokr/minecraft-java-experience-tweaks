package com.ziondev.experiencetweaks;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Defines client-side configuration specifications for personal player preferences such as
 * keeping experience on death, direct experience pickup, daily experience rewards, and auto-fishing.
 */
public class ClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    /** If true, player keeps experience level, total experience, and progress after death. */
    public static final ModConfigSpec.BooleanValue KEEP_EXPERIENCE = BUILDER
            .comment("\n If true, your experience points and levels are kept intact after death.")
            .define("keepExperience", true);

    /** If true, experience points will be inserted directly into the player instead of dropping as orbs. */
    public static final ModConfigSpec.BooleanValue DIRECT_EXPERIENCE = BUILDER
            .comment("\n If true, experience points will be inserted directly into the player instead of dropping as orbs.")
            .define("directExperience", true);

    /** If true, players will receive experience points every in-game day they survive without dying. */
    public static final ModConfigSpec.BooleanValue GIVE_EXPERIENCE_EVERY_DAY = BUILDER
            .comment("\n If true, players will receive experience points every in-game day they survive without dying.")
            .define("giveExperienceEveryDay", true);

    /** If true, players will automatically reel in their fishing rod when a fish bites. */
    public static final ModConfigSpec.BooleanValue AUTO_FISHING = BUILDER
            .comment("\n If true, players will automatically reel in their fishing rod when a fish bites.")
            .define("autoFishing", true);

    /** If true, players will automatically recast their fishing rod after reeling in a fish. */
    public static final ModConfigSpec.BooleanValue AUTO_FISHING_RECAST = BUILDER
            .comment("\n If true, players will automatically recast their fishing rod after reeling in a fish (requires autoFishing to be enabled).")
            .define("autoFishingRecast", true);

    /** If true, the Riptide enchantment can be used anywhere, regardless of weather or water. */
    public static final ModConfigSpec.BooleanValue RIPTIDE_ANYWHERE = BUILDER
            .comment("\n If true, the Riptide enchantment can be used anywhere, regardless of weather or water.")
            .define("riptideAnywhere", false);

    /** If true, arrows shot by mobs can be collected by players. */
    public static final ModConfigSpec.BooleanValue MOB_ARROWS_COLLECTIBLE = BUILDER
            .comment("\n If true, arrows shot by mobs can be collected by players.")
            .define("mobArrowsCollectible", false);

    /** If true, treasure maps and exploration maps will never point to already mapped structures. */
    public static final ModConfigSpec.BooleanValue NEVER_REPEAT_TREASURE_MAPS = BUILDER
            .comment("\n If true, treasure maps and exploration maps will never point to already mapped structures.")
            .define("neverRepeatTreasureMaps", false);

    /** If true, enables debug logging of configuration changes and network packets. */
    public static final ModConfigSpec.BooleanValue ENABLE_DEBUG_MODE = BUILDER
            .comment("\n If true, enables debug logging of configuration changes and network packets.")
            .define("enableDebugMode", false);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
