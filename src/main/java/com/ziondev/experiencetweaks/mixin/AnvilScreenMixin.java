package com.ziondev.experiencetweaks.mixin;

import com.ziondev.experiencetweaks.ModConfig;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AnvilScreen.class)
public abstract class AnvilScreenMixin {

    @ModifyConstant(method = "extractLabels", constant = @Constant(intValue = 40))
    private int experienceTweaks$modifyMaxCostLimit(int original) {
        return ModConfig.isAnvilBypassTooExpensive() ? Integer.MAX_VALUE : original;
    }

    @Redirect(
            method = "extractLabels",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/network/chat/MutableComponent;")
    )
    private MutableComponent experienceTweaks$redirectCostText(String key, Object[] args) {
        if ("container.repair.cost".equals(key) && ModConfig.isAnvilUseItemCost()) {
            int levelCost = (Integer) args[0];
            int itemCost = experienceTweaks$getAnvilItemCost(levelCost);
            ItemStack costItem = new ItemStack(ModConfig.getAnvilCostItem());
            return Component.translatable("experiencetweaks.anvil.cost_item", itemCost, costItem.getHoverName());
        }
        return Component.translatable(key, args);
    }

    @Unique
    private int experienceTweaks$getAnvilItemCost(int levelCost) {
        double multiplier = ModConfig.getAnvilItemCostMultiplier();
        if (multiplier <= 0.0D) {
            return 0;
        }
        return Math.max(1, (int) Math.ceil(levelCost * multiplier));
    }
}
