package com.ziondev.experiencetweaks.network;

/**
 * Client-side cache storing raw server configuration settings received via network packets.
 * Contains no default fallback values; fallbacks are centralized in {@link com.ziondev.experiencetweaks.ModConfig}.
 */
public final class ClientServerConfigCache {

    private static Boolean anvilUseItemCost = null;
    private static String anvilCostItem = null;
    private static Double anvilItemCostMultiplier = null;
    private static Boolean anvilBypassTooExpensive = null;

    private ClientServerConfigCache() {}

    public static void update(boolean useItemCost, String costItem, double multiplier, boolean bypassTooExpensive) {
        anvilUseItemCost = useItemCost;
        anvilCostItem = costItem;
        anvilItemCostMultiplier = multiplier;
        anvilBypassTooExpensive = bypassTooExpensive;
    }

    public static Boolean getAnvilUseItemCost() {
        return anvilUseItemCost;
    }

    public static String getAnvilCostItem() {
        return anvilCostItem;
    }

    public static Double getAnvilItemCostMultiplier() {
        return anvilItemCostMultiplier;
    }

    public static Boolean getAnvilBypassTooExpensive() {
        return anvilBypassTooExpensive;
    }
}
