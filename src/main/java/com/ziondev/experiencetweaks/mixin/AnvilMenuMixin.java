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

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin {

    @Shadow
    @Final
    private DataSlot cost;

    @ModifyConstant(method = "createResultInternal", constant = @Constant(intValue = 40))
    private int experienceTweaks$modifyMaxCostLimit(int original) {
        return ModConfig.isAnvilBypassTooExpensive() ? Integer.MAX_VALUE : original;
    }

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

    @Redirect(
            method = "onTake",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;giveExperienceLevels(I)V")
    )
    private void experienceTweaks$redirectGiveExperienceLevels(Player player, int levels) {
        if (ModConfig.isAnvilUseItemCost()) {
            int levelCost = -levels;
            int itemCost = experienceTweaks$getAnvilItemCost(levelCost);
            if (itemCost > 0 && !player.getAbilities().instabuild) {
                experienceTweaks$consumeItems(player, ModConfig.getAnvilCostItem(), itemCost);
            }
        } else {
            player.giveExperienceLevels(levels);
        }
    }

    @Unique
    private int experienceTweaks$getAnvilItemCost(int levelCost) {
        double multiplier = ModConfig.getAnvilItemCostMultiplier();
        if (multiplier <= 0.0D) {
            return 0;
        }
        return Math.max(1, (int) Math.ceil(levelCost * multiplier));
    }

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

    @Unique
    private void experienceTweaks$consumeItems(Player player, Item item, int amount) {
        int remaining = amount;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) {
                int count = stack.getCount();
                if (count >= remaining) {
                    stack.shrink(remaining);
                    if (stack.isEmpty()) {
                        player.getInventory().setItem(i, ItemStack.EMPTY);
                    }
                    break;
                } else {
                    remaining -= count;
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                }
            }
        }
    }
}
