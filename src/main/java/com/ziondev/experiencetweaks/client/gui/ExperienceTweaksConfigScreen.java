package com.ziondev.experiencetweaks.client.gui;

import com.ziondev.experiencetweaks.ClientConfig;
import com.ziondev.experiencetweaks.ModConfig;
import com.ziondev.experiencetweaks.ServerConfig;
import com.ziondev.experiencetweaks.network.UpdateServerConfigPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import net.minecraft.client.gui.screens.ConfirmScreen;
import java.util.List;

/**
 * In-game configuration GUI screen providing two tabs: "Client" (personal
 * options for all players)
 * and "Server" (world mechanics and reward amounts accessible only to OP
 * players or singleplayer hosts).
 */
public class ExperienceTweaksConfigScreen extends Screen {

    private final Screen lastScreen;
    private ConfigOptionList optionList;

    private Button clientTabButton;
    private Button serverTabButton;

    private boolean activeTabIsServer = false;
    private boolean isPlayerOp = false;

    private boolean clientKeepExperience;
    private boolean clientDirectExperience;
    private boolean clientGiveExperienceEveryDay;
    private boolean clientAutoFishing;
    private boolean clientAutoFishingRecast;
    private boolean clientRiptideAnywhere;

    private String serverGiveExperienceEveryDayBase;
    private String serverGiveExperienceEveryDayGrowth;
    private boolean serverAnvilBypassTooExpensive;
    private boolean serverAnvilUseItemCost;
    private String serverAnvilCostItem;
    private String serverAnvilItemCostMultiplier;
    private String serverAnvilDurabilityMultiplier;
    private boolean serverAllowMendingWithInfinity;
    private boolean serverAnvilEnchantmentExtraction;
    private boolean serverAnvilEnchantmentExtractionDestroySource;
    private String serverEnchantmentCostItem;
    private String serverEnchantmentCostMultiplier;
    private String serverEnchantmentCooldownType;
    private boolean serverWaterBelowHydratesFarmland;
    private String serverWaterHydrationRadius;
    private String serverMilkBucketNutrition;
    private boolean serverWanderingTraderUnlimitedTrades;
    private boolean serverVillagerUnlimitedTrades;
    private boolean serverAllArrowsAffectedByInfinity;
    private boolean serverAllowMultipleDamageEnchantments;
    private boolean serverAllowMultipleProtectionEnchantments;
    private boolean serverAllowPiercingWithMultishot;
    private boolean serverAllowMultipleTridentEnchantments;

    /**
     * Constructs a new configuration GUI screen.
     *
     * @param lastScreen the previous screen to return to when closing, or
     *                   {@code null}
     */
    public ExperienceTweaksConfigScreen(Screen lastScreen) {
        super(Component.translatable("experiencetweaks.gui.config.title"));
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        super.init();

        this.isPlayerOp = this.minecraft != null && (this.minecraft.isSingleplayer() ||
                (this.minecraft.player != null && this.minecraft.player.connection != null));

        loadCurrentValues();

        int buttonWidth = 100;
        int tabY = 22;

        this.clientTabButton = Button.builder(Component.translatable("experiencetweaks.gui.config.tab.client"), btn -> {
            this.activeTabIsServer = false;
            updateTabStates();
            rebuildOptionList();
        }).bounds(this.width / 2 - buttonWidth - 5, tabY, buttonWidth, 20).build();

        Component serverTabTitle = this.isPlayerOp
                ? Component.translatable("experiencetweaks.gui.config.tab.server")
                : Component.translatable("experiencetweaks.gui.config.tab.server_locked");

        this.serverTabButton = Button.builder(serverTabTitle, btn -> {
            if (this.isPlayerOp) {
                this.activeTabIsServer = true;
                updateTabStates();
                rebuildOptionList();
            }
        }).bounds(this.width / 2 + 5, tabY, buttonWidth, 20).build();

        if (!this.isPlayerOp) {
            this.serverTabButton.active = false;
        }

        this.addRenderableWidget(this.clientTabButton);
        this.addRenderableWidget(this.serverTabButton);

        this.optionList = new ConfigOptionList(this.minecraft, this.width, this.height - 82, 48, 24);
        this.addRenderableWidget(this.optionList);

        int bottomY = this.height - 28;
        this.addRenderableWidget(Button.builder(Component.translatable("experiencetweaks.gui.config.save"), btn -> {
            saveAndClose();
        }).bounds(this.width / 2 - 125, bottomY, 120, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("experiencetweaks.gui.config.cancel"), btn -> {
            onClose();
        }).bounds(this.width / 2 + 5, bottomY, 120, 20).build());

        updateTabStates();
        rebuildOptionList();
    }

    /**
     * Loads current configuration values into temporary GUI memory fields.
     */
    private void loadCurrentValues() {
        this.clientKeepExperience = ModConfig.isKeepExperienceEnabled();
        this.clientDirectExperience = ModConfig.isDirectExperience();
        this.clientGiveExperienceEveryDay = ModConfig.isGiveExperienceEveryDayEnabled();
        this.clientAutoFishing = ModConfig.isAutoFishingEnabled();
        this.clientAutoFishingRecast = ModConfig.isAutoFishingRecastEnabled();
        this.clientRiptideAnywhere = ModConfig.isRiptideAnywhere();

        this.serverGiveExperienceEveryDayBase = String.valueOf(ModConfig.getGiveExperienceEveryDayBase());
        this.serverGiveExperienceEveryDayGrowth = String.valueOf(ModConfig.getGiveExperienceEveryDayGrowth());
        this.serverAnvilBypassTooExpensive = ModConfig.isAnvilBypassTooExpensive();
        this.serverAnvilUseItemCost = ModConfig.isAnvilUseItemCost();
        this.serverAnvilCostItem = ServerConfig.ANVIL_COST_ITEM.get();
        this.serverAnvilItemCostMultiplier = String.valueOf(ModConfig.getAnvilItemCostMultiplier());
        this.serverAnvilDurabilityMultiplier = String.valueOf(ModConfig.getAnvilDurabilityMultiplier());
        this.serverAllowMendingWithInfinity = ModConfig.isAllowMendingWithInfinity();
        this.serverAnvilEnchantmentExtraction = ModConfig.isAnvilEnchantmentExtractionEnabled();
        this.serverAnvilEnchantmentExtractionDestroySource = ModConfig.isAnvilEnchantmentExtractionDestroySource();
        this.serverEnchantmentCostItem = ServerConfig.ENCHANTMENT_COST_ITEM.get();
        this.serverEnchantmentCostMultiplier = String.valueOf(ModConfig.getEnchantmentCostMultiplier());
        this.serverEnchantmentCooldownType = ModConfig.getEnchantmentCooldownType();
        this.serverWaterBelowHydratesFarmland = ModConfig.isWaterBelowHydratesFarmlandEnabled();
        this.serverWaterHydrationRadius = String.valueOf(ModConfig.getWaterHydrationRadius());
        this.serverMilkBucketNutrition = String.valueOf(ModConfig.getMilkBucketNutrition());
        this.serverWanderingTraderUnlimitedTrades = ModConfig.isWanderingTraderUnlimitedTrades();
        this.serverVillagerUnlimitedTrades = ModConfig.isVillagerUnlimitedTrades();
        this.serverAllArrowsAffectedByInfinity = ModConfig.isAllArrowsAffectedByInfinity();
        this.serverAllowMultipleDamageEnchantments = ModConfig.isAllowMultipleDamageEnchantments();
        this.serverAllowMultipleProtectionEnchantments = ModConfig.isAllowMultipleProtectionEnchantments();
        this.serverAllowPiercingWithMultishot = ModConfig.isAllowPiercingWithMultishot();
        this.serverAllowMultipleTridentEnchantments = ModConfig.isAllowMultipleTridentEnchantments();
    }

    /**
     * Updates active visual states of client and server tab buttons.
     */
    private void updateTabStates() {
        this.clientTabButton.active = this.activeTabIsServer;
        this.serverTabButton.active = !this.activeTabIsServer && this.isPlayerOp;
    }

    /**
     * Rebuilds entries in the scrollable option list depending on the selected tab.
     */
    private void rebuildOptionList() {
        if (this.optionList == null) {
            return;
        }
        this.optionList.clearEntries();

        if (!this.activeTabIsServer) {
            this.optionList.addEntry(new BooleanOptionEntry(
                    Component.translatable("experiencetweaks.gui.config.keep_experience"),
                    this.clientKeepExperience,
                    val -> this.clientKeepExperience = val,
                    List.of(Component.translatable("experiencetweaks.gui.config.keep_experience.tooltip"))));
            this.optionList.addEntry(new BooleanOptionEntry(
                    Component.translatable("experiencetweaks.gui.config.direct_experience"),
                    this.clientDirectExperience,
                    val -> this.clientDirectExperience = val,
                    List.of(Component.translatable("experiencetweaks.gui.config.direct_experience.tooltip"))));
            this.optionList.addEntry(new BooleanOptionEntry(
                    Component.translatable("experiencetweaks.gui.config.give_experience_every_day"),
                    this.clientGiveExperienceEveryDay,
                    val -> this.clientGiveExperienceEveryDay = val,
                    List.of(Component.translatable("experiencetweaks.gui.config.give_experience_every_day.tooltip"))));
            this.optionList.addEntry(new BooleanOptionEntry(
                    Component.translatable("experiencetweaks.gui.config.auto_fishing"),
                    this.clientAutoFishing,
                    val -> {
                        this.clientAutoFishing = val;
                        if (!val) {
                            this.clientAutoFishingRecast = false;
                        }
                        rebuildOptionList();
                    },
                    List.of(Component.translatable("experiencetweaks.gui.config.auto_fishing.tooltip"))));
            if (this.clientAutoFishing) {
                this.optionList.addEntry(new BooleanOptionEntry(
                        Component.translatable("experiencetweaks.gui.config.auto_fishing_recast"),
                        this.clientAutoFishingRecast,
                        val -> this.clientAutoFishingRecast = val,
                        List.of(Component.translatable("experiencetweaks.gui.config.auto_fishing_recast.tooltip"))));
            }
            this.optionList.addEntry(new BooleanOptionEntry(
                    Component.translatable("experiencetweaks.gui.config.riptide_anywhere"),
                    this.clientRiptideAnywhere,
                    val -> this.clientRiptideAnywhere = val,
                    List.of(Component.translatable("experiencetweaks.gui.config.riptide_anywhere.tooltip"))));
        } else {
            this.optionList.addEntry(new StringOptionEntry(
                    Component.translatable("experiencetweaks.gui.config.give_experience_every_day_base"),
                    this.serverGiveExperienceEveryDayBase,
                    val -> this.serverGiveExperienceEveryDayBase = val,
                    List.of(Component.translatable("experiencetweaks.gui.config.give_experience_every_day_base.tooltip"))));
            this.optionList.addEntry(new StringOptionEntry(
                    Component.translatable("experiencetweaks.gui.config.give_experience_every_day_growth"),
                    this.serverGiveExperienceEveryDayGrowth,
                    val -> this.serverGiveExperienceEveryDayGrowth = val,
                    List.of(Component.translatable("experiencetweaks.gui.config.give_experience_every_day_growth.tooltip"))));
            this.optionList.addEntry(new BooleanOptionEntry(
                    Component.translatable("experiencetweaks.gui.config.anvil_bypass_too_expensive"),
                    this.serverAnvilBypassTooExpensive,
                    val -> this.serverAnvilBypassTooExpensive = val,
                    List.of(Component.translatable("experiencetweaks.gui.config.anvil_bypass_too_expensive.tooltip"))));
            this.optionList.addEntry(new BooleanOptionEntry(
                    Component.translatable("experiencetweaks.gui.config.anvil_use_item_cost"),
                    this.serverAnvilUseItemCost,
                    val -> this.serverAnvilUseItemCost = val,
                    List.of(Component.translatable("experiencetweaks.gui.config.anvil_use_item_cost.tooltip"))));
            this.optionList.addEntry(new StringOptionEntry(
                    Component.translatable("experiencetweaks.gui.config.anvil_cost_item"),
                    this.serverAnvilCostItem,
                    val -> this.serverAnvilCostItem = val,
                    List.of(Component.translatable("experiencetweaks.gui.config.anvil_cost_item.tooltip"))));
            this.optionList.addEntry(new StringOptionEntry(
                    Component.translatable("experiencetweaks.gui.config.anvil_item_cost_multiplier"),
                    this.serverAnvilItemCostMultiplier,
                    val -> this.serverAnvilItemCostMultiplier = val,
                    List.of(Component.translatable("experiencetweaks.gui.config.anvil_item_cost_multiplier.tooltip"))));
            this.optionList.addEntry(new StringOptionEntry(
                    Component.translatable("experiencetweaks.gui.config.anvil_durability_multiplier"),
                    this.serverAnvilDurabilityMultiplier,
                    val -> this.serverAnvilDurabilityMultiplier = val,
                    List.of(Component.translatable("experiencetweaks.gui.config.anvil_durability_multiplier.tooltip"))));
            this.optionList.addEntry(new BooleanOptionEntry(
                    Component.translatable("experiencetweaks.gui.config.allow_mending_with_infinity"),
                    this.serverAllowMendingWithInfinity,
                    val -> this.serverAllowMendingWithInfinity = val,
                    List.of(Component.translatable("experiencetweaks.gui.config.allow_mending_with_infinity.tooltip"))));
            this.optionList.addEntry(new BooleanOptionEntry(
                    Component.translatable("experiencetweaks.gui.config.anvil_enchantment_extraction"),
                    this.serverAnvilEnchantmentExtraction,
                    val -> this.serverAnvilEnchantmentExtraction = val,
                    List.of(Component.translatable("experiencetweaks.gui.config.anvil_enchantment_extraction.tooltip"))));
            this.optionList.addEntry(new BooleanOptionEntry(
                    Component.translatable("experiencetweaks.gui.config.anvil_enchantment_extraction_destroy_source"),
                    this.serverAnvilEnchantmentExtractionDestroySource,
                    val -> this.serverAnvilEnchantmentExtractionDestroySource = val,
                    List.of(Component.translatable("experiencetweaks.gui.config.anvil_enchantment_extraction_destroy_source.tooltip"))));
            this.optionList.addEntry(new StringOptionEntry(
                    Component.translatable("experiencetweaks.gui.config.enchantment_cost_item"),
                    this.serverEnchantmentCostItem,
                    val -> this.serverEnchantmentCostItem = val,
                    List.of(Component.translatable("experiencetweaks.gui.config.enchantment_cost_item.tooltip"))));
            this.optionList.addEntry(new StringOptionEntry(
                    Component.translatable("experiencetweaks.gui.config.enchantment_cost_multiplier"),
                    this.serverEnchantmentCostMultiplier,
                    val -> this.serverEnchantmentCostMultiplier = val,
                    List.of(Component.translatable("experiencetweaks.gui.config.enchantment_cost_multiplier.tooltip"))));

            List<String> coolKeys = List.of("current_level", "last_level");
            List<Component> coolNames = List.of(
                    Component.translatable("experiencetweaks.gui.config.enchantment_cooldown_type.current_level"),
                    Component.translatable("experiencetweaks.gui.config.enchantment_cooldown_type.last_level")
            );
            this.optionList.addEntry(new SelectionOptionEntry(
                    Component.translatable("experiencetweaks.gui.config.enchantment_cooldown_type"),
                    coolKeys,
                    coolNames,
                    this.serverEnchantmentCooldownType,
                    val -> this.serverEnchantmentCooldownType = val,
                    List.of(Component.translatable("experiencetweaks.gui.config.enchantment_cooldown_type.tooltip"))));

            this.optionList.addEntry(new BooleanOptionEntry(
                    Component.translatable("experiencetweaks.gui.config.water_below_hydrates_farmland"),
                    this.serverWaterBelowHydratesFarmland,
                    val -> this.serverWaterBelowHydratesFarmland = val,
                    List.of(Component.translatable("experiencetweaks.gui.config.water_below_hydrates_farmland.tooltip"))));
            this.optionList.addEntry(new StringOptionEntry(
                    Component.translatable("experiencetweaks.gui.config.water_hydration_radius"),
                    this.serverWaterHydrationRadius,
                    val -> this.serverWaterHydrationRadius = val,
                    List.of(Component.translatable("experiencetweaks.gui.config.water_hydration_radius.tooltip"))));
            this.optionList.addEntry(new StringOptionEntry(
                    Component.translatable("experiencetweaks.gui.config.milk_bucket_nutrition"),
                    this.serverMilkBucketNutrition,
                    val -> this.serverMilkBucketNutrition = val,
                    List.of(Component.translatable("experiencetweaks.gui.config.milk_bucket_nutrition.tooltip"))));
            this.optionList.addEntry(new BooleanOptionEntry(
                    Component.translatable("experiencetweaks.gui.config.wandering_trader_unlimited_trades"),
                    this.serverWanderingTraderUnlimitedTrades,
                    val -> this.serverWanderingTraderUnlimitedTrades = val,
                    List.of(Component.translatable("experiencetweaks.gui.config.wandering_trader_unlimited_trades.tooltip"))));
            this.optionList.addEntry(new BooleanOptionEntry(
                    Component.translatable("experiencetweaks.gui.config.villager_unlimited_trades"),
                    this.serverVillagerUnlimitedTrades,
                    val -> this.serverVillagerUnlimitedTrades = val,
                    List.of(Component.translatable("experiencetweaks.gui.config.villager_unlimited_trades.tooltip"))));
            this.optionList.addEntry(new BooleanOptionEntry(
                    Component.translatable("experiencetweaks.gui.config.all_arrows_affected_by_infinity"),
                    this.serverAllArrowsAffectedByInfinity,
                    val -> this.serverAllArrowsAffectedByInfinity = val,
                    List.of(Component.translatable("experiencetweaks.gui.config.all_arrows_affected_by_infinity.tooltip"))));
            this.optionList.addEntry(new BooleanOptionEntry(
                    Component.translatable("experiencetweaks.gui.config.allow_multiple_damage_enchantments"),
                    this.serverAllowMultipleDamageEnchantments,
                    val -> this.serverAllowMultipleDamageEnchantments = val,
                    List.of(Component.translatable("experiencetweaks.gui.config.allow_multiple_damage_enchantments.tooltip"))));
            this.optionList.addEntry(new BooleanOptionEntry(
                    Component.translatable("experiencetweaks.gui.config.allow_multiple_protection_enchantments"),
                    this.serverAllowMultipleProtectionEnchantments,
                    val -> this.serverAllowMultipleProtectionEnchantments = val,
                    List.of(Component.translatable("experiencetweaks.gui.config.allow_multiple_protection_enchantments.tooltip"))));
            this.optionList.addEntry(new BooleanOptionEntry(
                    Component.translatable("experiencetweaks.gui.config.allow_piercing_with_multishot"),
                    this.serverAllowPiercingWithMultishot,
                    val -> this.serverAllowPiercingWithMultishot = val,
                    List.of(Component.translatable("experiencetweaks.gui.config.allow_piercing_with_multishot.tooltip"))));
            this.optionList.addEntry(new BooleanOptionEntry(
                    Component.translatable("experiencetweaks.gui.config.allow_multiple_trident_enchantments"),
                    this.serverAllowMultipleTridentEnchantments,
                    val -> this.serverAllowMultipleTridentEnchantments = val,
                    List.of(Component.translatable("experiencetweaks.gui.config.allow_multiple_trident_enchantments.tooltip"))));
            this.optionList.addEntry(new ButtonOptionEntry(
                    Component.translatable("experiencetweaks.gui.config.unlock_all_recipes"),
                    Component.translatable("experiencetweaks.gui.config.unlock"),
                    btn -> {
                        if (this.minecraft != null) {
                            this.minecraft.setScreen(new ConfirmScreen(
                                    confirmed -> {
                                        if (confirmed && this.minecraft.getConnection() != null) {
                                            this.minecraft.getConnection().send(
                                                    new net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket(
                                                            new com.ziondev.experiencetweaks.network.UnlockAllRecipesPacket()));
                                        }
                                        this.minecraft.setScreen(this);
                                    },
                                    Component.translatable(
                                            "experiencetweaks.gui.config.unlock_all_recipes.confirm.title"),
                                    Component.translatable(
                                            "experiencetweaks.gui.config.unlock_all_recipes.confirm.message")));
                        }
                    },
                    List.of(Component.translatable("experiencetweaks.gui.config.unlock_all_recipes.tooltip"))));
        }
    }

    /**
     * Saves all modified client and server options and closes the screen.
     */
    private void saveAndClose() {
        ClientConfig.KEEP_EXPERIENCE.set(this.clientKeepExperience);
        ClientConfig.DIRECT_EXPERIENCE.set(this.clientDirectExperience);
        ClientConfig.GIVE_EXPERIENCE_EVERY_DAY.set(this.clientGiveExperienceEveryDay);
        ClientConfig.AUTO_FISHING.set(this.clientAutoFishing);
        ClientConfig.AUTO_FISHING_RECAST.set(this.clientAutoFishingRecast);
        ClientConfig.RIPTIDE_ANYWHERE.set(this.clientRiptideAnywhere);
        ClientConfig.SPEC.save();

        if (this.minecraft != null && this.minecraft.getConnection() != null) {
            this.minecraft.getConnection()
                    .send(new net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket(
                            new com.ziondev.experiencetweaks.network.SyncClientSettingsPacket(
                                    this.clientKeepExperience,
                                    this.clientDirectExperience,
                                    this.clientGiveExperienceEveryDay,
                                    this.clientRiptideAnywhere)));
        }

        if (this.isPlayerOp) {
            int dailyBase = parseIntOrDefault(this.serverGiveExperienceEveryDayBase, 5);
            double dailyGrowth = parseDoubleOrDefault(this.serverGiveExperienceEveryDayGrowth, 0.1);
            double anvilMultiplier = parseDoubleOrDefault(this.serverAnvilItemCostMultiplier, 0.5);
            double enchMultiplier = parseDoubleOrDefault(this.serverEnchantmentCostMultiplier, 1.5);
            int waterRadius = parseIntOrDefault(this.serverWaterHydrationRadius, 4);
            int milkNutrition = parseIntOrDefault(this.serverMilkBucketNutrition, 2);

            int durability = parseIntOrDefault(this.serverAnvilDurabilityMultiplier, 100);
            if (durability < 1) durability = 1;
            if (durability > 1000) durability = 1000;

            ServerConfig.GIVE_EXPERIENCE_EVERY_DAY_BASE.set(dailyBase);
            ServerConfig.GIVE_EXPERIENCE_EVERY_DAY_GROWTH.set(dailyGrowth);
            ServerConfig.ANVIL_BYPASS_TOO_EXPENSIVE.set(this.serverAnvilBypassTooExpensive);
            ServerConfig.ANVIL_USE_ITEM_COST.set(this.serverAnvilUseItemCost);
            ServerConfig.ANVIL_COST_ITEM.set(this.serverAnvilCostItem.trim());
            ServerConfig.ANVIL_ITEM_COST_MULTIPLIER.set(anvilMultiplier);
            ServerConfig.ANVIL_DURABILITY_MULTIPLIER.set(durability);
            ServerConfig.ALLOW_MENDING_WITH_INFINITY.set(this.serverAllowMendingWithInfinity);
            ServerConfig.ANVIL_ENCHANTMENT_EXTRACTION.set(this.serverAnvilEnchantmentExtraction);
            ServerConfig.ANVIL_ENCHANTMENT_EXTRACTION_DESTROY_SOURCE
                    .set(this.serverAnvilEnchantmentExtractionDestroySource);
            ServerConfig.ENCHANTMENT_COST_ITEM.set(this.serverEnchantmentCostItem.trim());
            ServerConfig.ENCHANTMENT_COST_MULTIPLIER.set(enchMultiplier);
            ServerConfig.ENCHANTMENT_COOLDOWN_TYPE.set(this.serverEnchantmentCooldownType.trim());
            ServerConfig.WATER_BELOW_HYDRATES_FARMLAND.set(this.serverWaterBelowHydratesFarmland);
            ServerConfig.WATER_HYDRATION_RADIUS.set(waterRadius);
            ServerConfig.MILK_BUCKET_NUTRITION.set(milkNutrition);
            ServerConfig.WANDERING_TRADER_UNLIMITED_TRADES.set(this.serverWanderingTraderUnlimitedTrades);
            ServerConfig.VILLAGER_UNLIMITED_TRADES.set(this.serverVillagerUnlimitedTrades);
            ServerConfig.ALL_ARROWS_AFFECTED_BY_INFINITY.set(this.serverAllArrowsAffectedByInfinity);
            ServerConfig.ALLOW_MULTIPLE_DAMAGE_ENCHANTMENTS.set(this.serverAllowMultipleDamageEnchantments);
            ServerConfig.ALLOW_MULTIPLE_PROTECTION_ENCHANTMENTS.set(this.serverAllowMultipleProtectionEnchantments);
            ServerConfig.ALLOW_PIERCING_WITH_MULTISHOT.set(this.serverAllowPiercingWithMultishot);
            ServerConfig.ALLOW_MULTIPLE_TRIDENT_ENCHANTMENTS.set(this.serverAllowMultipleTridentEnchantments);
            ServerConfig.SPEC.save();

            if (this.minecraft != null && this.minecraft.getConnection() != null && !this.minecraft.isSingleplayer()) {
                this.minecraft.getConnection()
                        .send(new net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket(
                                new UpdateServerConfigPacket(
                                        dailyBase,
                                        dailyGrowth,
                                        this.serverAnvilBypassTooExpensive,
                                        this.serverAnvilUseItemCost,
                                        this.serverAnvilCostItem.trim(),
                                        anvilMultiplier,
                                        this.serverAllowMendingWithInfinity,
                                        this.serverAnvilEnchantmentExtraction,
                                        this.serverAnvilEnchantmentExtractionDestroySource,
                                        this.serverEnchantmentCostItem.trim(),
                                        enchMultiplier,
                                        this.serverEnchantmentCooldownType.trim(),
                                        this.serverWaterBelowHydratesFarmland,
                                        waterRadius,
                                        milkNutrition,
                                        this.serverWanderingTraderUnlimitedTrades,
                                        this.serverVillagerUnlimitedTrades,
                                        this.serverAllArrowsAffectedByInfinity,
                                        this.serverAllowMultipleDamageEnchantments,
                                        this.serverAllowMultipleProtectionEnchantments,
                                        this.serverAllowPiercingWithMultishot,
                                        this.serverAllowMultipleTridentEnchantments,
                                        durability)));
            }
        }

        onClose();
    }

    private static int parseIntOrDefault(String text, int defaultValue) {
        try {
            return Integer.parseInt(text.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private static double parseDoubleOrDefault(String text, double defaultValue) {
        try {
            return Double.parseDouble(text.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.lastScreen);
        }
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.text(this.font, this.title, (this.width - this.font.width(this.title)) / 2, 8, 0xFFFFFFFF);
    }

    /**
     * Scrollable option list containing entries for configuration settings.
     */
    private static class ConfigOptionList extends ContainerObjectSelectionList<OptionEntry> {

        public ConfigOptionList(Minecraft minecraft, int width, int height, int y, int itemHeight) {
            super(minecraft, width, height, y, itemHeight);
        }

        @Override
        public int addEntry(@NonNull OptionEntry entry) {
            return super.addEntry(entry);
        }

        public void clearEntries() {
            super.clearEntries();
        }

        @Override
        public int getRowWidth() {
            return 320;
        }
    }

    /**
     * Abstract base class for scroll list option entries.
     */
    private static abstract class OptionEntry extends ContainerObjectSelectionList.Entry<OptionEntry> {
    }

    /**
     * Option entry row displaying a label and a boolean toggle button.
     */
    private static class BooleanOptionEntry extends OptionEntry {
        private final Component label;
        private boolean value;
        private final java.util.function.Consumer<Boolean> consumer;
        private final Button button;
        private final List<Component> tooltip;

        public BooleanOptionEntry(Component label, boolean initialValue,
                java.util.function.Consumer<Boolean> consumer, List<Component> tooltip) {
            this.label = label;
            this.value = initialValue;
            this.consumer = consumer;
            this.tooltip = tooltip;
            this.button = Button.builder(getButtonText(initialValue), btn -> {
                this.value = !this.value;
                btn.setMessage(getButtonText(this.value));
                this.consumer.accept(this.value);
            }).bounds(0, 0, 80, 20).build();
        }

        private static Component getButtonText(boolean val) {
            return val
                    ? Component.translatable("experiencetweaks.gui.config.on")
                    : Component.translatable("experiencetweaks.gui.config.off");
        }

        @Override
        public void extractContent(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean isHovered,
                float partialTick) {
            int maxWidth = this.getWidth() - 90;
            List<net.minecraft.util.FormattedCharSequence> lines = Minecraft.getInstance().font.split(this.label, maxWidth);
            int yOffset = lines.size() > 1 ? 2 : 6;
            for (int i = 0; i < lines.size() && i < 2; i++) {
                graphics.text(Minecraft.getInstance().font, lines.get(i), this.getX() + 5, this.getY() + yOffset + (i * 9), 0xFFFFFFFF);
            }

            this.button.setX(this.getX() + this.getWidth() - 85);
            this.button.setY(this.getY());
            this.button.extractRenderState(graphics, mouseX, mouseY, partialTick);

            if (isHovered && this.tooltip != null) {
                graphics.setComponentTooltipForNextFrame(Minecraft.getInstance().font, this.tooltip, mouseX, mouseY);
            }
        }

        @Override
        public @NonNull List<? extends GuiEventListener> children() {
            return List.of(this.button);
        }

        @Override
        public @NonNull List<? extends NarratableEntry> narratables() {
            return List.of(this.button);
        }
    }

    /**
     * Option entry row displaying a label and a text edit box for string or numeric
     * values.
     */
    private static class StringOptionEntry extends OptionEntry {
        private final Component label;
        private final EditBox editBox;
        private final java.util.function.Consumer<String> consumer;
        private final List<Component> tooltip;

        public StringOptionEntry(Component label, String initialValue, java.util.function.Consumer<String> consumer, List<Component> tooltip) {
            this.label = label;
            this.consumer = consumer;
            this.tooltip = tooltip;
            this.editBox = new EditBox(Minecraft.getInstance().font, 0, 0, 100, 18, label);
            this.editBox.setValue(initialValue);
            this.editBox.setResponder(this.consumer::accept);
        }

        @Override
        public void extractContent(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean isHovered,
                float partialTick) {
            int maxWidth = this.getWidth() - 110;
            List<net.minecraft.util.FormattedCharSequence> lines = Minecraft.getInstance().font.split(this.label, maxWidth);
            int yOffset = lines.size() > 1 ? 2 : 6;
            for (int i = 0; i < lines.size() && i < 2; i++) {
                graphics.text(Minecraft.getInstance().font, lines.get(i), this.getX() + 5, this.getY() + yOffset + (i * 9), 0xFFFFFFFF);
            }

            this.editBox.setX(this.getX() + this.getWidth() - 105);
            this.editBox.setY(this.getY() + 1);
            this.editBox.extractRenderState(graphics, mouseX, mouseY, partialTick);

            if (isHovered && this.tooltip != null) {
                graphics.setComponentTooltipForNextFrame(Minecraft.getInstance().font, this.tooltip, mouseX, mouseY);
            }
        }

        @Override
        public @NonNull List<? extends GuiEventListener> children() {
            return List.of(this.editBox);
        }

        @Override
        public @NonNull List<? extends NarratableEntry> narratables() {
            return List.of(this.editBox);
        }
    }

    /**
     * Option entry row displaying a label and a button that triggers a custom
     * action.
     */
    private static class ButtonOptionEntry extends OptionEntry {
        private final Component label;
        private final Button button;
        private final List<Component> tooltip;

        public ButtonOptionEntry(Component label, Component buttonLabel, Button.OnPress onPress, List<Component> tooltip) {
            this.label = label;
            this.button = Button.builder(buttonLabel, onPress).bounds(0, 0, 100, 20).build();
            this.tooltip = tooltip;
        }

        @Override
        public void extractContent(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean isHovered,
                float partialTick) {
            int maxWidth = this.getWidth() - 110;
            List<net.minecraft.util.FormattedCharSequence> lines = Minecraft.getInstance().font.split(this.label, maxWidth);
            int yOffset = lines.size() > 1 ? 2 : 6;
            for (int i = 0; i < lines.size() && i < 2; i++) {
                graphics.text(Minecraft.getInstance().font, lines.get(i), this.getX() + 5, this.getY() + yOffset + (i * 9), 0xFFFFFFFF);
            }

            this.button.setX(this.getX() + this.getWidth() - 105);
            this.button.setY(this.getY());
            this.button.extractRenderState(graphics, mouseX, mouseY, partialTick);

            if (isHovered && this.tooltip != null) {
                graphics.setComponentTooltipForNextFrame(Minecraft.getInstance().font, this.tooltip, mouseX, mouseY);
            }
        }

        @Override
        public @NonNull List<? extends GuiEventListener> children() {
            return List.of(this.button);
        }

        @Override
        public @NonNull List<? extends NarratableEntry> narratables() {
            return List.of(this.button);
        }
    }

    /**
     * Option entry row displaying a label and a selection cycle button.
     */
    private static class SelectionOptionEntry extends OptionEntry {
        private final Component label;
        private final List<String> values;
        private final List<Component> friendlyNames;
        private int currentIndex;
        private final java.util.function.Consumer<String> consumer;
        private final Button button;
        private final List<Component> tooltip;

        public SelectionOptionEntry(Component label, List<String> values, List<Component> friendlyNames,
                String initialValue, java.util.function.Consumer<String> consumer, List<Component> tooltip) {
            this.label = label;
            this.values = values;
            this.friendlyNames = friendlyNames;
            this.consumer = consumer;
            this.tooltip = tooltip;

            int index = values.indexOf(initialValue);
            this.currentIndex = index != -1 ? index : 0;

            this.button = Button.builder(friendlyNames.get(this.currentIndex), btn -> {
                this.currentIndex = (this.currentIndex + 1) % this.values.size();
                btn.setMessage(this.friendlyNames.get(this.currentIndex));
                this.consumer.accept(this.values.get(this.currentIndex));
            }).bounds(0, 0, 100, 20).build();
        }

        @Override
        public void extractContent(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean isHovered,
                float partialTick) {
            int maxWidth = this.getWidth() - 110;
            List<net.minecraft.util.FormattedCharSequence> lines = Minecraft.getInstance().font.split(this.label, maxWidth);
            int yOffset = lines.size() > 1 ? 2 : 6;
            for (int i = 0; i < lines.size() && i < 2; i++) {
                graphics.text(Minecraft.getInstance().font, lines.get(i), this.getX() + 5, this.getY() + yOffset + (i * 9), 0xFFFFFFFF);
            }

            this.button.setX(this.getX() + this.getWidth() - 105);
            this.button.setY(this.getY());
            this.button.extractRenderState(graphics, mouseX, mouseY, partialTick);

            if (isHovered && this.tooltip != null) {
                graphics.setComponentTooltipForNextFrame(Minecraft.getInstance().font, this.tooltip, mouseX, mouseY);
            }
        }

        @Override
        public @NonNull List<? extends GuiEventListener> children() {
            return List.of(this.button);
        }

        @Override
        public @NonNull List<? extends NarratableEntry> narratables() {
            return List.of(this.button);
        }
    }
}
