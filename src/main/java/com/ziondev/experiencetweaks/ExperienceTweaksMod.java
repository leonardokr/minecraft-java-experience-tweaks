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
                    .sound(SoundType.GRAVEL)
    );

    public static final DeferredBlock<GrassSlabBlock> GRASS_SLAB = BLOCKS.registerBlock("grass_slab",
            GrassSlabBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.GRASS)
                    .randomTicks()
                    .strength(0.6F)
                    .sound(SoundType.GRASS)
    );

    public static final DeferredBlock<DirtPathSlabBlock> DIRT_PATH_SLAB = BLOCKS.registerBlock("dirt_path_slab",
            DirtPathSlabBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIRT)
                    .strength(0.65F)
                    .sound(SoundType.GRAVEL)
    );

    public static final DeferredBlock<FarmlandSlabBlock> FARMLAND_SLAB = BLOCKS.registerBlock("farmland_slab",
            FarmlandSlabBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIRT)
                    .randomTicks()
                    .strength(0.6F)
                    .sound(SoundType.GRAVEL)
    );

    public ExperienceTweaksMod(IEventBus modEventBus, ModContainer modContainer) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        modEventBus.addListener(this::onConfigReload);
        modEventBus.addListener(this::onRegisterPayloads);
        modEventBus.addListener(this::addCreativeTabContents);
        LOGGER.info("Metalion's Experience Tweaks Mod initialized...");
    }

    private void addCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            event.accept(DIRT_SLAB);
            event.accept(GRASS_SLAB);
            event.accept(DIRT_PATH_SLAB);
            event.accept(FARMLAND_SLAB);
        }
    }

    private void onConfigReload(net.neoforged.fml.event.config.ModConfigEvent event) {
        if (event.getConfig().getType() == ModConfig.Type.COMMON) {
            LOGGER.info("Experience Tweaks config reloaded!");
        }
    }

    private void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MODID);
        registrar.playToClient(
                SyncEnchantLevelsPacket.TYPE,
                SyncEnchantLevelsPacket.STREAM_CODEC,
                (packet, ctx) -> ClientEnchantLevelCache.update(packet.requiredLevels())
        );
    }

    /**
     * Returns the persistent {@link PlayerEnchantData} from the overworld saved data store,
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
     * Should be called whenever the enchantment menu opens or after a successful enchanting.
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
