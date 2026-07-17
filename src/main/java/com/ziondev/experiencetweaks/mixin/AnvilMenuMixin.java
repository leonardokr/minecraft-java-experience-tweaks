package com.ziondev.experiencetweaks.mixin;

import com.ziondev.experiencetweaks.ModConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Applies two configurable tweaks to the anvil:
 * <ul>
 *   <li><b>Bypass "Too Expensive"</b> — removes the 40-level cost cap that
 *       normally blocks operations when an item has been repaired or combined
 *       too many times. Controlled by {@code anvilBypassTooExpensive}.</li>
 *   <li><b>Item cost</b> — replaces the experience level cost with a
 *       configurable item (e.g. emeralds). The amount consumed is derived from
 *       the vanilla level cost multiplied by {@code anvilItemCostMultiplier}.
 *       Controlled by {@code anvilUseItemCost}.</li>
 * </ul>
 */
@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin {

    @Shadow @Final private DataSlot cost;

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
     * Consumes the configured cost item from the player's inventory when they
     * take the result from the anvil, replacing the vanilla experience level
     * deduction when {@code anvilUseItemCost} is enabled.
     * <p>
     * Players in creative mode are never charged. Has no effect when item cost
     * mode is disabled, in which case experience levels are deducted normally.
     *
     * @param player the player taking the result
     * @param levels the level delta that vanilla would have applied (negative)
     */
    @Redirect(
            method = "onTake",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;giveExperienceLevels(I)V")
    )
    private void experienceTweaks$redirectGiveExperienceLevels(Player player, int levels) {
        if (ModConfig.isAnvilUseItemCost()) {
            int levelCost = -levels; // levels is negative when the player takes the result
            int itemCost = experienceTweaks$getAnvilItemCost(levelCost);
            if (itemCost > 0 && !player.getAbilities().instabuild) {
                experienceTweaks$consumeItems(player, ModConfig.getAnvilCostItem(), itemCost);
                player.getInventory().setChanged();
                player.containerMenu.broadcastChanges();
            }
        } else {
            player.giveExperienceLevels(levels);
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
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    /**
     * Removes the specified number of the given item from the player's
     * inventory, consuming stacks in order from the first slot found.
     *
     * @param player the player whose inventory is modified
     * @param item   the item type to consume
     * @param amount the total number of items to remove
     */
    @Unique
    private void experienceTweaks$consumeItems(Player player, Item item, int amount) {
        int remaining = amount;
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) {
                int count = stack.getCount();
                if (count >= remaining) {
                    stack.shrink(remaining);
                    if (stack.isEmpty()) {
                        player.getInventory().setItem(i, ItemStack.EMPTY);
                    }
                    remaining = 0;
                } else {
                    remaining -= count;
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                }
            }
        }
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
    }
}
