package com.ziondev.experiencetweaks.mixin;

import com.ziondev.experiencetweaks.ModConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Restores food points when a player clears effects.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    /**
     * Restores food points when effect clearing occurs.
     *
     * @param cir the callback info returnable
     */
    @Inject(method = "removeAllEffects", at = @At("HEAD"))
    private void experienceTweaks$onRemoveAllEffects(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof Player player && player.isAlive() && !player.level().isClientSide()) {
            int nutrition = ModConfig.getMilkBucketNutrition();
            if (nutrition > 0) {
                player.getFoodData().eat(nutrition, 0.2F);
            }
        }
    }
}
