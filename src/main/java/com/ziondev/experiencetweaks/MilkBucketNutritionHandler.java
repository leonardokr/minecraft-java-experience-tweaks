package com.ziondev.experiencetweaks;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

/**
 * Restores food points (hunger level) when a player drinks a milk bucket.
 */
@EventBusSubscriber(modid = ExperienceTweaksMod.MODID)
public class MilkBucketNutritionHandler {

    /**
     * Listens for item consumption finishing and restores configured food points to the player
     * if the item consumed is a milk bucket.
     *
     * @param event the item use finish event
     */
    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (!event.getItem().is(Items.MILK_BUCKET)) {
            return;
        }

        if (event.getEntity() instanceof Player player && !player.level().isClientSide()) {
            int nutrition = ModConfig.getMilkBucketNutrition();
            if (nutrition > 0) {
                player.getFoodData().eat(nutrition, 0.2F);
            }
        }
    }
}
