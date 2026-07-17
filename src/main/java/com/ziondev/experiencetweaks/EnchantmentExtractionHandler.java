package com.ziondev.experiencetweaks;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;

/**
 * Handles enchantment extraction via the anvil.
 * <p>
 * When {@code anvilEnchantmentExtraction} is enabled, placing any enchanted
 * item in the left slot and a blank book in the right slot extracts the first
 * enchantment from the source item into an enchanted book.
 * <p>
 * The source item has that enchantment removed after the player takes the
 * result. If it had only one enchantment and {@code anvilEnchantmentExtractionDestroySource}
 * is enabled, the source item is destroyed; otherwise it is returned to the
 * left slot without that enchantment. This side effect is applied by
 * {@link com.ziondev.experiencetweaks.mixin.AnvilMenuMixin}, which has the
 * necessary access to the anvil's input slots.
 * <p>
 * The XP cost is equal to the extracted enchantment's anvil cost multiplied by
 * its level. When {@code anvilUseItemCost} is active, the existing anvil mixin
 * converts that level cost into an item cost automatically.
 */
@EventBusSubscriber(modid = ExperienceTweaksMod.MODID)
public class EnchantmentExtractionHandler {

    /**
     * Computes the enchanted-book output when the left input is an enchanted
     * item and the right input is a blank book.
     * <p>
     * Extracts the first enchantment in the item's enchantment list and places
     * it into an enchanted book. Sets the XP cost proportionally to the
     * enchantment's own anvil weight and level. Consumes exactly one blank book.
     *
     * @param event the anvil update event with both inputs and the mutable result
     */
    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        if (!ModConfig.isAnvilEnchantmentExtractionEnabled()) {
            return;
        }

        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();

        if (!right.is(Items.BOOK) || right.has(DataComponents.STORED_ENCHANTMENTS)) {
            return;
        }

        ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(left);
        if (enchantments.isEmpty()) {
            return;
        }

        var firstEntry = enchantments.entrySet().iterator().next();
        var enchantmentHolder = firstEntry.getKey();
        int enchantmentLevel = firstEntry.getIntValue();

        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        ItemEnchantments.Mutable storedEnchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        storedEnchantments.set(enchantmentHolder, enchantmentLevel);
        book.set(DataComponents.STORED_ENCHANTMENTS, storedEnchantments.toImmutable());

        int xpCost = Math.max(1, enchantmentHolder.value().getAnvilCost() * enchantmentLevel);
        event.setXpCost(xpCost);

        event.setMaterialCost(1);
        event.setOutput(book);
    }
}
