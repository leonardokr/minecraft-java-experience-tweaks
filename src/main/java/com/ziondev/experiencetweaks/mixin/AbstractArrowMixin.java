package com.ziondev.experiencetweaks.mixin;

import com.ziondev.experiencetweaks.ModConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin targeting AbstractArrow to track whether it was fired by a mob,
 * allowing it to be collectible by players based on configuration.
 */
@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin {

    @Shadow
    public AbstractArrow.Pickup pickup;

    @Unique
    private boolean experienceTweaks$firedByMob;

    @Shadow
    protected abstract ItemStack getPickupItem();

    /**
     * Checks if the shooter is a mob and records it.
     *
     * @param owner the shooter entity
     * @param ci    the callback info
     */
    @Inject(method = "setOwner", at = @At("RETURN"))
    private void experienceTweaks$setOwner(Entity owner, CallbackInfo ci) {
        if (owner instanceof net.minecraft.world.entity.Mob) {
            this.experienceTweaks$firedByMob = true;
        }
    }

    /**
     * Saves the fired-by-mob flag to the entity's data.
     *
     * @param output the save output destination
     * @param ci     the callback info
     */
    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void experienceTweaks$addAdditionalSaveData(ValueOutput output, CallbackInfo ci) {
        output.putBoolean("ExperienceTweaksFiredByMob", this.experienceTweaks$firedByMob);
    }

    /**
     * Reads the fired-by-mob flag from the entity's data.
     *
     * @param input the save input source
     * @param ci    the callback info
     */
    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void experienceTweaks$readAdditionalSaveData(ValueInput input, CallbackInfo ci) {
        this.experienceTweaks$firedByMob = input.getBooleanOr("ExperienceTweaksFiredByMob", false);
    }

    /**
     * Integrates with tryPickup to allow collecting mob-fired arrows if configuration is enabled.
     *
     * @param player the player trying to collect the arrow
     * @param cir    the callback info returnable
     */
    @Inject(method = "tryPickup", at = @At("HEAD"), cancellable = true)
    private void experienceTweaks$tryPickup(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (ModConfig.isMobArrowsCollectible(player) && this.experienceTweaks$firedByMob) {
            if (this.pickup == AbstractArrow.Pickup.DISALLOWED || this.pickup == AbstractArrow.Pickup.CREATIVE_ONLY) {
                boolean pickedUp = player.hasInfiniteMaterials() || player.getInventory().add(this.getPickupItem());
                cir.setReturnValue(pickedUp);
            }
        }
    }
}
