package com.ziondev.experiencetweaks.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.minecraft.client.color.block.BlockTintSources;
import com.ziondev.experiencetweaks.ExperienceTweaksMod;

import java.util.List;

/**
 * Registers client-side block tint sources for custom blocks such as grass slabs and sugar cane slabs.
 */
@EventBusSubscriber(modid = ExperienceTweaksMod.MODID, value = Dist.CLIENT)
public class ClientColors {

    /**
     * Registers color tint sources for blocks that respond to biome colors.
     *
     * @param event the block tint sources registration event
     */
    @SubscribeEvent
    public static void registerBlockTintSources(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(List.of(BlockTintSources.grassBlock()), ExperienceTweaksMod.GRASS_SLAB.get());
        event.register(List.of(BlockTintSources.foliage()), ExperienceTweaksMod.SUGAR_CANE_SLAB.get());
    }
}
