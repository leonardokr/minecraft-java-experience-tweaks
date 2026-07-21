package com.ziondev.experiencetweaks;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

/**
 * Restores food points (hunger level) when a player drinks milk from a bucket,
 * bottle, or any modded milk container item.
 */
@EventBusSubscriber(modid = ExperienceTweaksMod.MODID)
public class MilkBucketNutritionHandler {

    /**
     * Listens for item consumption finishing and restores configured food points to
     * the player
     * if the consumed item represents milk (vanilla or modded).
     *
     * @param event the item use finish event
     */
    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        ItemStack stack = event.getItem();
        if (!isMilkItem(stack)) {
            return;
        }

        if (event.getEntity() instanceof Player player && !player.level().isClientSide()) {
            int nutrition = ModConfig.getMilkBucketNutrition();
            if (nutrition > 0) {
                player.getFoodData().eat(nutrition, 0.2F);
            }
        }
    }

    /**
     * Determines whether the given item stack represents milk, checking vanilla
     * items,
     * item registry paths, and item tags for milk definitions across mods.
     *
     * @param stack the item stack to check
     * @return {@code true} if the item represents milk
     */
    @SuppressWarnings("deprecation")
    private static boolean isMilkItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        if (stack.is(Items.MILK_BUCKET)) {
            return true;
        }

        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id != null) {
            String path = id.getPath().toLowerCase();
            if (path.contains("milk")) {
                return true;
            }
        }

        return stack.getItem().builtInRegistryHolder().tags()
                .anyMatch(tag -> tag.location().getPath().toLowerCase().contains("milk"));
    }
}
