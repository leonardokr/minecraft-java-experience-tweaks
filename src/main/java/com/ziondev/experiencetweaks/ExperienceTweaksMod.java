package com.ziondev.experiencetweaks;

import com.ziondev.experiencetweaks.network.ClientEnchantLevelCache;
import com.ziondev.experiencetweaks.network.SyncEnchantLevelsPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.ziondev.experiencetweaks.block.DirtSlabBlock;
import com.ziondev.experiencetweaks.block.GrassSlabBlock;
import com.ziondev.experiencetweaks.block.DirtPathSlabBlock;
import com.ziondev.experiencetweaks.block.FarmlandSlabBlock;
import com.ziondev.experiencetweaks.block.FarmlandSlabCropBlock;
import com.ziondev.experiencetweaks.block.DirtSlabSugarCaneBlock;
import com.ziondev.experiencetweaks.block.SwitchRailBlock;

import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

@Mod(ExperienceTweaksMod.MODID)
public class ExperienceTweaksMod {
        public static final String MODID = "experiencetweaks";
        public static final Logger LOGGER = LogUtils.getLogger();

        public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
        public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

        public static final DeferredBlock<DirtSlabBlock> DIRT_SLAB = BLOCKS.registerBlock("dirt_slab",
                        DirtSlabBlock::new,
                        () -> BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.DIRT)
                                        .instrument(NoteBlockInstrument.BASEDRUM)
                                        .strength(0.5F)
                                        .sound(SoundType.GRAVEL));

        public static final DeferredBlock<GrassSlabBlock> GRASS_SLAB = BLOCKS.registerBlock("grass_slab",
                        GrassSlabBlock::new,
                        () -> BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.GRASS)
                                        .randomTicks()
                                        .strength(0.6F)
                                        .sound(SoundType.GRASS));

        public static final DeferredBlock<DirtPathSlabBlock> DIRT_PATH_SLAB = BLOCKS.registerBlock("dirt_path_slab",
                        DirtPathSlabBlock::new,
                        () -> BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.DIRT)
                                        .strength(0.65F)
                                        .sound(SoundType.GRAVEL));

        public static final DeferredBlock<FarmlandSlabBlock> FARMLAND_SLAB = BLOCKS.registerBlock("farmland_slab",
                        FarmlandSlabBlock::new,
                        () -> BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.DIRT)
                                        .randomTicks()
                                        .strength(0.6F)
                                        .sound(SoundType.GRAVEL));

        public static final DeferredItem<BlockItem> DIRT_SLAB_ITEM = ITEMS.registerSimpleBlockItem("dirt_slab",
                        DIRT_SLAB);
        public static final DeferredItem<BlockItem> GRASS_SLAB_ITEM = ITEMS.registerSimpleBlockItem("grass_slab",
                        GRASS_SLAB);
        public static final DeferredItem<BlockItem> DIRT_PATH_SLAB_ITEM = ITEMS.registerSimpleBlockItem(
                        "dirt_path_slab",
                        DIRT_PATH_SLAB);
        public static final DeferredItem<BlockItem> FARMLAND_SLAB_ITEM = ITEMS.registerSimpleBlockItem("farmland_slab",
                        FARMLAND_SLAB);

        public static final DeferredBlock<FarmlandSlabCropBlock> WHEAT_SLAB_CROP = BLOCKS.registerBlock(
                        "wheat_slab_crop",
                        props -> new FarmlandSlabCropBlock(props, Items.WHEAT_SEEDS),
                        () -> BlockBehaviour.Properties.of().noCollision().randomTicks().instabreak()
                                        .sound(SoundType.GRASS));
        public static final DeferredBlock<FarmlandSlabCropBlock> CARROTS_SLAB_CROP = BLOCKS.registerBlock(
                        "carrots_slab_crop",
                        props -> new FarmlandSlabCropBlock(props, Items.CARROT),
                        () -> BlockBehaviour.Properties.of().noCollision().randomTicks().instabreak()
                                        .sound(SoundType.GRASS));
        public static final DeferredBlock<FarmlandSlabCropBlock> POTATOES_SLAB_CROP = BLOCKS.registerBlock(
                        "potatoes_slab_crop",
                        props -> new FarmlandSlabCropBlock(props, Items.POTATO),
                        () -> BlockBehaviour.Properties.of().noCollision().randomTicks().instabreak()
                                        .sound(SoundType.GRASS));
        public static final DeferredBlock<FarmlandSlabCropBlock> BEETROOTS_SLAB_CROP = BLOCKS.registerBlock(
                        "beetroots_slab_crop",
                        props -> new FarmlandSlabCropBlock(props, Items.BEETROOT_SEEDS),
                        () -> BlockBehaviour.Properties.of().noCollision().randomTicks().instabreak()
                                        .sound(SoundType.GRASS));
        public static final DeferredBlock<DirtSlabSugarCaneBlock> SUGAR_CANE_SLAB = BLOCKS.registerBlock(
                        "sugar_cane_slab",
                        DirtSlabSugarCaneBlock::new,
                        () -> BlockBehaviour.Properties.of().noCollision().randomTicks().instabreak()
                                        .sound(SoundType.GRASS));
        public static final DeferredBlock<SwitchRailBlock> SWITCH_RAIL = BLOCKS.registerBlock(
                        "switch_rail",
                        SwitchRailBlock::new,
                        () -> BlockBehaviour.Properties.of().noCollision().strength(0.7F).sound(SoundType.METAL));
        public static final DeferredItem<BlockItem> SWITCH_RAIL_ITEM = ITEMS.registerSimpleBlockItem("switch_rail",
                        SWITCH_RAIL);

        public ExperienceTweaksMod(IEventBus modEventBus, ModContainer modContainer) {
                BLOCKS.register(modEventBus);
                ITEMS.register(modEventBus);
                modContainer.registerConfig(ModConfig.Type.COMMON, ServerConfig.SPEC);
                modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
                modEventBus.addListener(this::onConfigReload);
                modEventBus.addListener(this::onRegisterPayloads);
                modEventBus.addListener(this::addCreativeTabContents);
                LOGGER.info("Metalion's Experience Tweaks Mod initialized...");
        }

        private void addCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
                if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
                        event.accept(DIRT_SLAB_ITEM.get());
                        event.accept(GRASS_SLAB_ITEM.get());
                        event.accept(DIRT_PATH_SLAB_ITEM.get());
                        event.accept(FARMLAND_SLAB_ITEM.get());
                }
                if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
                        event.accept(SWITCH_RAIL_ITEM.get());
                }
        }

        private void onConfigReload(net.neoforged.fml.event.config.ModConfigEvent event) {
                LOGGER.info("Experience Tweaks [{}] config reloaded!", event.getConfig().getType());
        }

        private void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
                PayloadRegistrar registrar = event.registrar(MODID).optional();
                registrar.playToClient(
                                SyncEnchantLevelsPacket.TYPE,
                                SyncEnchantLevelsPacket.STREAM_CODEC,
                                (packet, ctx) -> ClientEnchantLevelCache.update(packet.requiredLevels()));

                registrar.playToClient(
                                com.ziondev.experiencetweaks.network.SyncServerConfigPacket.TYPE,
                                com.ziondev.experiencetweaks.network.SyncServerConfigPacket.STREAM_CODEC,
                                (packet, ctx) -> com.ziondev.experiencetweaks.network.ClientServerConfigCache.update(
                                                packet.anvilUseItemCost(),
                                                packet.anvilCostItem(),
                                                packet.anvilItemCostMultiplier(),
                                                packet.anvilBypassTooExpensive()));

                registrar.playToServer(
                                com.ziondev.experiencetweaks.network.SyncClientSettingsPacket.TYPE,
                                com.ziondev.experiencetweaks.network.SyncClientSettingsPacket.STREAM_CODEC,
                                (packet, ctx) -> {
                                        if (ctx.player() instanceof ServerPlayer player) {
                                                com.ziondev.experiencetweaks.network.ServerClientSettingsCache.update(
                                                                player.getUUID(),
                                                                packet.keepExperience(),
                                                                packet.directExperience(),
                                                                packet.giveExperienceEveryDay());
                                        }
                                });

                registrar.playToServer(
                                com.ziondev.experiencetweaks.network.UpdateServerConfigPacket.TYPE,
                                com.ziondev.experiencetweaks.network.UpdateServerConfigPacket.STREAM_CODEC,
                                (packet, ctx) -> {
                                        if (ctx.player() instanceof ServerPlayer player
                                                        && player.level().getServer() != null
                                                        && player.level().getServer().getPlayerList().isOp(
                                                                        new net.minecraft.server.players.NameAndId(
                                                                                        player.getGameProfile()))) {
                                                ServerConfig.GIVE_EXPERIENCE_EVERY_DAY_BASE
                                                                .set(packet.giveExperienceEveryDayBase());
                                                ServerConfig.GIVE_EXPERIENCE_EVERY_DAY_GROWTH
                                                                .set(packet.giveExperienceEveryDayGrowth());
                                                ServerConfig.ANVIL_BYPASS_TOO_EXPENSIVE
                                                                .set(packet.anvilBypassTooExpensive());
                                                ServerConfig.ANVIL_USE_ITEM_COST.set(packet.anvilUseItemCost());
                                                ServerConfig.ANVIL_COST_ITEM.set(packet.anvilCostItem());
                                                ServerConfig.ANVIL_ITEM_COST_MULTIPLIER
                                                                .set(packet.anvilItemCostMultiplier());
                                                ServerConfig.ALLOW_MENDING_WITH_INFINITY
                                                                .set(packet.allowMendingWithInfinity());
                                                ServerConfig.ANVIL_ENCHANTMENT_EXTRACTION
                                                                .set(packet.anvilEnchantmentExtraction());
                                                ServerConfig.ANVIL_ENCHANTMENT_EXTRACTION_DESTROY_SOURCE
                                                                .set(packet.anvilEnchantmentExtractionDestroySource());
                                                ServerConfig.ENCHANTMENT_COST_ITEM.set(packet.enchantmentCostItem());
                                                ServerConfig.ENCHANTMENT_COST_MULTIPLIER
                                                                .set(packet.enchantmentCostMultiplier());
                                                ServerConfig.ENCHANTMENT_COOLDOWN_TYPE
                                                                .set(packet.enchantmentCooldownType());
                                                ServerConfig.WATER_BELOW_HYDRATES_FARMLAND
                                                                .set(packet.waterBelowHydratesFarmland());
                                                ServerConfig.WATER_HYDRATION_RADIUS.set(packet.waterHydrationRadius());
                                                ServerConfig.MILK_BUCKET_NUTRITION.set(packet.milkBucketNutrition());
                                                ServerConfig.WANDERING_TRADER_UNLIMITED_TRADES.set(packet.wanderingTraderUnlimitedTrades());
                                                ServerConfig.VILLAGER_UNLIMITED_TRADES.set(packet.villagerUnlimitedTrades());
                                                ServerConfig.ALL_ARROWS_AFFECTED_BY_INFINITY.set(packet.allArrowsAffectedByInfinity());
                                                ServerConfig.SPEC.save();

                                                PacketDistributor.sendToAllPlayers(new com.ziondev.experiencetweaks.network.SyncServerConfigPacket(
                                                                packet.anvilUseItemCost(),
                                                                packet.anvilCostItem(),
                                                                packet.anvilItemCostMultiplier(),
                                                                packet.anvilBypassTooExpensive()));

                                                LOGGER.info("Server configuration updated by OP player {}",
                                                                player.getName().getString());
                                        }
                                });
        }

        /**
         * Returns the persistent {@link PlayerEnchantData} from the overworld saved
         * data store,
         * or {@code null} if the server is not running (e.g., on the client side).
         */
        public static PlayerEnchantData getEnchantData() {
                MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                if (server == null) {
                        return null;
                }
                return server.overworld().getDataStorage().computeIfAbsent(PlayerEnchantData.TYPE);
        }

        /**
         * Sends the player's current required levels to their client.
         * Should be called whenever the enchantment menu opens or after a successful
         * enchanting.
         */
        public static void syncEnchantLevels(ServerPlayer player) {
                PlayerEnchantData data = getEnchantData();
                if (data == null) {
                        return;
                }

                int currentLevel = player.experienceLevel;
                List<Integer> levels = new ArrayList<>();
                for (int b = 0; b < 3; b++) {
                        levels.add(data.getRequiredLevel(player.getUUID(), b, currentLevel));
                }

                PacketDistributor.sendToPlayer(player, new SyncEnchantLevelsPacket(levels));
        }
}
