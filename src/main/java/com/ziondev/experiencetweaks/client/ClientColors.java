package com.ziondev.experiencetweaks.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.minecraft.client.color.block.BlockTintSources;
import com.ziondev.experiencetweaks.ExperienceTweaksMod;

import java.util.List;

@EventBusSubscriber(modid = ExperienceTweaksMod.MODID, value = Dist.CLIENT)
public class ClientColors {

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(List.of(BlockTintSources.grassBlock()), ExperienceTweaksMod.GRASS_SLAB.get());
    }
}
