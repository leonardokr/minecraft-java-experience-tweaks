package com.ziondev.experiencetweaks.mixin;

import com.ziondev.experiencetweaks.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.FarmlandBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Injects into {@link FarmlandBlock#isNearWater} to allow configurable water search radius
 * and hydration from water located directly underneath vanilla farmland blocks.
 */
@Mixin(FarmlandBlock.class)
public abstract class FarmlandBlockMixin {

    /**
     * Checks for water using configured horizontal radius and optional below check.
     *
     * @param level the level reader
     * @param pos   the farmland position
     * @param cir   callback info returnable
     */
    @Inject(method = "isNearWater", at = @At("HEAD"), cancellable = true)
    private static void experienceTweaks$isNearWaterWithBelowCheck(
            LevelReader level,
            BlockPos pos,
            CallbackInfoReturnable<Boolean> cir
    ) {
        int radius = ModConfig.getWaterHydrationRadius();
        boolean checkBelow = ModConfig.isWaterBelowHydratesFarmlandEnabled();

        if (radius != 4 || checkBelow) {
            int minY = checkBelow ? -1 : 0;
            for (BlockPos testPos : BlockPos.betweenClosed(pos.offset(-radius, minY, -radius), pos.offset(radius, 1, radius))) {
                if (level.getFluidState(testPos).is(FluidTags.WATER)) {
                    cir.setReturnValue(true);
                    return;
                }
            }
            if (radius != 4 && !checkBelow) {
                cir.setReturnValue(false);
            }
        }
    }
}
