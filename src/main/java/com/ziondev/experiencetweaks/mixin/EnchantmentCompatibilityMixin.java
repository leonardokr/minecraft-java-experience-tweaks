package com.ziondev.experiencetweaks.mixin;

import com.ziondev.experiencetweaks.ModConfig;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Removes the mutual exclusivity between the Mending and Infinity enchantments
 * when the {@code allowMendingWithInfinity} config option is enabled.
 * <p>
 * By default, Mending and Infinity cannot be applied to the same item.
 * With this option active, both enchantments can coexist on a bow,
 * allowing it to repair itself from experience orbs while also having
 * infinite arrows.
 */
@Mixin(Enchantment.class)
public abstract class EnchantmentCompatibilityMixin {

    /**
     * Marks Mending and Infinity as compatible with each other when
     * {@code allowMendingWithInfinity} is enabled, allowing them to be
     * combined on the same item via the anvil.
     * <p>
     * Has no effect when the config option is disabled.
     */
    @Inject(method = "areCompatible", at = @At("HEAD"), cancellable = true)
    private static void experienceTweaks$areCompatible(
            Holder<Enchantment> enchantment,
            Holder<Enchantment> other,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (ModConfig.isAllowMultipleDamageEnchantments()
                && experienceTweaks$isDamageEnchantment(enchantment)
                && experienceTweaks$isDamageEnchantment(other)) {
            cir.setReturnValue(true);
            return;
        }

        if (!ModConfig.isAllowMendingWithInfinity()) {
            return;
        }

        boolean firstIsMending   = enchantment.is(Enchantments.MENDING);
        boolean firstIsInfinity  = enchantment.is(Enchantments.INFINITY);
        boolean secondIsMending  = other.is(Enchantments.MENDING);
        boolean secondIsInfinity = other.is(Enchantments.INFINITY);

        if ((firstIsMending && secondIsInfinity) || (firstIsInfinity && secondIsMending)) {
            cir.setReturnValue(true);
        }
    }

    private static boolean experienceTweaks$isDamageEnchantment(Holder<Enchantment> enchantment) {
        return enchantment.is(Enchantments.SHARPNESS)
                || enchantment.is(Enchantments.SMITE)
                || enchantment.is(Enchantments.BANE_OF_ARTHROPODS);
    }
}
