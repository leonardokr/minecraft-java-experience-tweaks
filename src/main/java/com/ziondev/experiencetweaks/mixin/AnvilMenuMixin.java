package com.ziondev.experiencetweaks.mixin;

import com.ziondev.experiencetweaks.ModConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Applies two configurable tweaks to the anvil:
 * <ul>
 * <li><b>Bypass "Too Expensive"</b> — removes the 40-level cost cap that
 * normally blocks operations when an item has been repaired or combined
 * too many times. Controlled by {@code anvilBypassTooExpensive}.</li>
 * <li><b>Item cost</b> — replaces the experience level cost with a
 * configurable item (e.g. emeralds). The amount consumed is derived from
 * the vanilla level cost multiplied by {@code anvilItemCostMultiplier}.
 * Controlled by {@code anvilUseItemCost}.</li>
 * </ul>
 */
@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin {

    @Shadow
    @Final
    private DataSlot cost;

    /**
     * Captures the left input item at the start of {@code onTake} so it can be
     * used at TAIL after vanilla has already cleared the input slots.
     */
    @Unique
    private ItemStack experienceTweaks$extractionLeftSnapshot = null;

    /**
     * Captures the level cost before vanilla zeroes it during {@code onTake}.
     */
    @Unique
    private int experienceTweaks$takenLevelCost = 0;

    /**
     * Captures state at the very start of {@code onTake}, before vanilla clears
     * input slots or zeroes the cost.
     */
    @Inject(method = "onTake", at = @At("HEAD"))
    private void experienceTweaks$captureOnTakeState(Player player, ItemStack carried, CallbackInfo ci) {
        experienceTweaks$takenLevelCost = this.cost.get();

        if (!ModConfig.isAnvilEnchantmentExtractionEnabled()) {
            experienceTweaks$extractionLeftSnapshot = null;
            return;
        }
        Container slots = ((ItemCombinerMenuAccessor) this).experienceTweaks$getInputSlots();
        ItemStack right = slots.getItem(1);
        if (right.is(Items.BOOK) && !right.has(DataComponents.STORED_ENCHANTMENTS)
                && carried.is(Items.ENCHANTED_BOOK)) {
            experienceTweaks$extractionLeftSnapshot = slots.getItem(0).copy();
        } else {
            experienceTweaks$extractionLeftSnapshot = null;
        }
    }

    /**
     * Removes the 40-level "Too Expensive" cap from the anvil when
     * {@code anvilBypassTooExpensive} is enabled, allowing any operation
     * regardless of how many times the item has been modified.
     *
     * @param original the vanilla cap value (40)
     * @return {@link Integer#MAX_VALUE} if the cap is bypassed, otherwise the
     *         original value unchanged
     */
    @ModifyConstant(method = "createResultInternal", constant = @Constant(intValue = 40))
    private int experienceTweaks$modifyMaxCostLimit(int original) {
        return ModConfig.isAnvilBypassTooExpensive() ? Integer.MAX_VALUE : original;
    }

    /**
     * Determines whether the player is allowed to take the result from the anvil
     * when {@code anvilUseItemCost} is enabled.
     * <p>
     * Instead of checking experience levels, verifies that the player holds
     * enough of the configured cost item in their inventory. Players in creative
     * mode are always allowed. Has no effect when item cost mode is disabled.
     *
     * @param player the player attempting to take the result
     * @param active whether the result slot is active
     * @param cir    callback used to override the return value
     */
    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    private void experienceTweaks$mayPickup(Player player, boolean active, CallbackInfoReturnable<Boolean> cir) {
        if (ModConfig.isAnvilUseItemCost()) {
            int levelCost = this.cost.get();
            if (levelCost <= 0) {
                cir.setReturnValue(false);
                return;
            }
            if (player.getAbilities().instabuild) {
                cir.setReturnValue(true);
                return;
            }
            int itemCost = experienceTweaks$getAnvilItemCost(levelCost);
            cir.setReturnValue(experienceTweaks$countItems(player, ModConfig.getAnvilCostItem()) >= itemCost);
        }
    }

    /**
     * Zeroes the XP argument passed to {@code giveExperienceLevels} inside
     * {@code onTake} when item cost mode is active, suppressing the vanilla XP
     * deduction without replacing the call entirely.
     * <p>
     * Using {@link ModifyArg} instead of {@code @Redirect} ensures compatibility
     * with other mods that also inject into {@code AnvilMenu.onTake}.
     *
     * @param levels the vanilla XP delta (negative, representing cost)
     * @return {@code 0} when item cost is active, otherwise the original value
     */
    @ModifyArg(method = "onTake", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;giveExperienceLevels(I)V"), index = 0)
    private int experienceTweaks$zeroOutXpCost(int levels) {
        if (ModConfig.isAnvilUseItemCost() && levels < 0) {
            return 0;
        }
        return levels;
    }

    /**
     * Consumes the configured cost items from the player's inventory after the
     * anvil result has been taken, when item cost mode is active.
     * <p>
     * Runs at {@code TAIL} so it executes after vanilla has already cleared the
     * input slots, and after the XP arg has been zeroed by
     * {@link #experienceTweaks$zeroOutXpCost}.
     *
     * @param player  the player taking the result
     * @param carried the item that was taken
     * @param ci      callback info (unused)
     */
    @Inject(method = "onTake", at = @At("TAIL"))
    private void experienceTweaks$consumeItemsOnTake(Player player, ItemStack carried, CallbackInfo ci) {
        int levelCost = this.experienceTweaks$takenLevelCost;
        this.experienceTweaks$takenLevelCost = 0;

        if (!ModConfig.isAnvilUseItemCost() || player.getAbilities().instabuild) {
            return;
        }
        if (levelCost <= 0) {
            return;
        }
        int itemCost = experienceTweaks$getAnvilItemCost(levelCost);
        if (itemCost > 0) {
            experienceTweaks$consumeItems(player, ModConfig.getAnvilCostItem(), itemCost);
        }
    }

    /**
     * Converts a vanilla experience level cost into the equivalent item cost
     * using the configured multiplier.
     * <p>
     * The result is always at least 1 when the multiplier is greater than zero,
     * and exactly 0 when the multiplier is zero or negative (free operation).
     *
     * @param levelCost the vanilla experience level cost for the operation
     * @return the number of items to consume
     */
    @Unique
    private int experienceTweaks$getAnvilItemCost(int levelCost) {
        double multiplier = ModConfig.getAnvilItemCostMultiplier();
        if (multiplier <= 0.0D) {
            return 0;
        }
        return Math.max(1, (int) Math.ceil(levelCost * multiplier));
    }

    /**
     * Returns the total count of the given item across all slots in the
     * player's inventory.
     *
     * @param player the player whose inventory is searched
     * @param item   the item type to count
     * @return total number of matching items found
     */
    @Unique
    private int experienceTweaks$countItems(Player player, Item item) {
        return player.getInventory().clearOrCountMatchingItems(stack -> stack.is(item), 0, player.inventoryMenu.getCraftSlots());
    }

    /**
     * Removes the specified number of the given item from the player's
     * inventory using Minecraft's native container clearOrCountMatchingItems method.
     *
     * @param player the player whose inventory is modified
     * @param item   the item type to consume
     * @param amount the total number of items to remove
     */
    @Unique
    private void experienceTweaks$consumeItems(Player player, Item item, int amount) {
        player.getInventory().clearOrCountMatchingItems(stack -> stack.is(item), amount, player.inventoryMenu.getCraftSlots());
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
    }

    /**
     * Applies the source-item side effect after the player takes an enchantment
     * extraction result from the anvil.
     * <p>
     * Removes the extracted enchantment from the left input slot. If the source
     * item ends up with no enchantments and
     * {@code anvilEnchantmentExtractionDestroySource}
     * is enabled, the slot is cleared; otherwise the stripped item is returned.
     * <p>
     * This hook runs before vanilla clears the input slots in {@code onTake},
     * allowing us to place the modified item back before vanilla would remove it.
     *
     * @param player  the player taking the result
     * @param carried the item that was taken
     * @param ci      callback info (unused)
     */
    @Inject(method = "onTake", at = @At("TAIL"))
    private void experienceTweaks$applyExtractionSideEffect(Player player, ItemStack carried, CallbackInfo ci) {
        if (!ModConfig.isAnvilEnchantmentExtractionEnabled()) {
            return;
        }

        if (!carried.is(Items.ENCHANTED_BOOK)) {
            return;
        }

        ItemStack leftSnapshot = experienceTweaks$extractionLeftSnapshot;
        experienceTweaks$extractionLeftSnapshot = null;

        if (leftSnapshot == null || leftSnapshot.isEmpty()) {
            return;
        }

        ItemEnchantments storedEnchantments = carried.getOrDefault(DataComponents.STORED_ENCHANTMENTS,
                ItemEnchantments.EMPTY);
        if (storedEnchantments.isEmpty()) {
            return;
        }
        var extractedHolder = storedEnchantments.entrySet().iterator().next().getKey();

        ItemEnchantments sourceEnchantments = EnchantmentHelper.getEnchantmentsForCrafting(leftSnapshot);
        if (sourceEnchantments.getLevel(extractedHolder) == 0) {
            return;
        }

        ItemStack stripped = leftSnapshot.copy();
        ItemEnchantments.Mutable remaining = new ItemEnchantments.Mutable(sourceEnchantments);
        remaining.set(extractedHolder, 0);
        EnchantmentHelper.setEnchantments(stripped, remaining.toImmutable());

        boolean noEnchantmentsLeft = remaining.toImmutable().isEmpty();
        boolean destroySource = ModConfig.isAnvilEnchantmentExtractionDestroySource();

        if (!noEnchantmentsLeft || !destroySource) {
            Container inputSlots = ((ItemCombinerMenuAccessor) this).experienceTweaks$getInputSlots();
            inputSlots.setItem(0, stripped);
        }
    }
}
