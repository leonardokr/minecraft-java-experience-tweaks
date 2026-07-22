package com.ziondev.experiencetweaks.mixin;

import com.ziondev.experiencetweaks.ModConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TridentItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Mixin to allow the Riptide enchantment to be used anywhere,
 * regardless of the current weather conditions or if the player is in water.
 */
@Mixin(TridentItem.class)
public abstract class TridentItemMixin {

    /**
     * Redirects the weather/water check in use and releaseUsing methods of TridentItem.
     *
     * @param player the player using the trident
     * @return {@code true} if the player should be allowed to use Riptide
     */
    @Redirect(
            method = {"use", "releaseUsing"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isInWaterOrRain()Z")
    )
    private boolean experienceTweaks$isInWaterOrRain(Player player) {
        return ModConfig.isRiptideAnywhere(player) || player.isInWaterOrRain();
    }
}
