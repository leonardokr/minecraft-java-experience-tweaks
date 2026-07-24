package com.ziondev.experiencetweaks.network;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side cache storing client settings received from individual players.
 */
public final class ServerClientSettingsCache {

    private static final Map<UUID, Boolean> KEEP_EXPERIENCE = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> DIRECT_EXPERIENCE = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> GIVE_EXPERIENCE_EVERY_DAY = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> RIPTIDE_ANYWHERE = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> MOB_ARROWS_COLLECTIBLE = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> NEVER_REPEAT_TREASURE_MAPS = new ConcurrentHashMap<>();

    private ServerClientSettingsCache() {}

    /**
     * Updates the cached settings for a player.
     *
     * @param playerUuid the UUID of the player
     * @param keepExperience whether to keep experience on death
     * @param directExperience whether experience is picked up directly
     * @param giveExperienceEveryDay whether daily experience rewards are active
     * @param riptideAnywhere whether Riptide can be used anywhere
     * @param mobArrowsCollectible whether arrows shot by mobs are collectible
     * @param neverRepeatTreasureMaps whether maps should not repeat structures
     */
    public static void update(UUID playerUuid, boolean keepExperience, boolean directExperience, boolean giveExperienceEveryDay, boolean riptideAnywhere, boolean mobArrowsCollectible, boolean neverRepeatTreasureMaps) {
        KEEP_EXPERIENCE.put(playerUuid, keepExperience);
        DIRECT_EXPERIENCE.put(playerUuid, directExperience);
        GIVE_EXPERIENCE_EVERY_DAY.put(playerUuid, giveExperienceEveryDay);
        RIPTIDE_ANYWHERE.put(playerUuid, riptideAnywhere);
        MOB_ARROWS_COLLECTIBLE.put(playerUuid, mobArrowsCollectible);
        NEVER_REPEAT_TREASURE_MAPS.put(playerUuid, neverRepeatTreasureMaps);
    }

    /**
     * Removes settings for a player when they disconnect.
     *
     * @param playerUuid the UUID of the player
     */
    public static void remove(UUID playerUuid) {
        KEEP_EXPERIENCE.remove(playerUuid);
        DIRECT_EXPERIENCE.remove(playerUuid);
        GIVE_EXPERIENCE_EVERY_DAY.remove(playerUuid);
        RIPTIDE_ANYWHERE.remove(playerUuid);
        MOB_ARROWS_COLLECTIBLE.remove(playerUuid);
        NEVER_REPEAT_TREASURE_MAPS.remove(playerUuid);
    }

    /**
     * Gets the keep experience setting for a player.
     *
     * @param playerUuid the UUID of the player
     * @return the keep experience setting, or null if uncached
     */
    public static Boolean getKeepExperience(UUID playerUuid) {
        return KEEP_EXPERIENCE.get(playerUuid);
    }

    /**
     * Gets the direct experience setting for a player.
     *
     * @param playerUuid the UUID of the player
     * @return the direct experience setting, or null if uncached
     */
    public static Boolean getDirectExperience(UUID playerUuid) {
        return DIRECT_EXPERIENCE.get(playerUuid);
    }

    /**
     * Gets the daily experience reward setting for a player.
     *
     * @param playerUuid the UUID of the player
     * @return the daily experience reward setting, or null if uncached
     */
    public static Boolean getGiveExperienceEveryDay(UUID playerUuid) {
        return GIVE_EXPERIENCE_EVERY_DAY.get(playerUuid);
    }

    /**
     * Gets the riptide anywhere setting for a player.
     *
     * @param playerUuid the UUID of the player
     * @return the riptide anywhere setting, or null if uncached
     */
    public static Boolean getRiptideAnywhere(UUID playerUuid) {
        return RIPTIDE_ANYWHERE.get(playerUuid);
    }

    /**
     * Gets the mob arrows collectible setting for a player.
     *
     * @param playerUuid the UUID of the player
     * @return the mob arrows collectible setting, or null if uncached
     */
    public static Boolean getMobArrowsCollectible(UUID playerUuid) {
        return MOB_ARROWS_COLLECTIBLE.get(playerUuid);
    }

    /**
     * Gets the never repeat treasure maps setting for a player.
     *
     * @param playerUuid the UUID of the player
     * @return the never repeat treasure maps setting, or null if uncached
     */
    public static Boolean getNeverRepeatTreasureMaps(UUID playerUuid) {
        return NEVER_REPEAT_TREASURE_MAPS.get(playerUuid);
    }
}
