package com.ziondev.experiencetweaks.mixin;

import com.ziondev.experiencetweaks.ModConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to allow all arrow types to benefit from the Infinity enchantment
 * when the corresponding configuration option is enabled.
 */
@Mixin(ArrowItem.class)
public abstract class ArrowItemMixin {

    /**
     * Checks if the arrow should be treated as infinite when fired from an Infinity bow.
     *
     * @param ammo         the arrow stack
     * @param bow          the bow stack
     * @param livingEntity the entity firing the bow
     * @param cir          the callback info returnable
     */
    @Inject(method = "isInfinite", at = @At("HEAD"), cancellable = true)
    private void experienceTweaks$isInfinite(ItemStack ammo, ItemStack bow, LivingEntity livingEntity, CallbackInfoReturnable<Boolean> cir) {
        if (ModConfig.isAllArrowsAffectedByInfinity()) {
            boolean hasInfinity = livingEntity.level().registryAccess()
                    .get(Enchantments.INFINITY)
                    .map(bow::getEnchantmentLevel)
                    .orElse(0) > 0;
            if (hasInfinity) {
                cir.setReturnValue(true);
            }
        }
    }
}
