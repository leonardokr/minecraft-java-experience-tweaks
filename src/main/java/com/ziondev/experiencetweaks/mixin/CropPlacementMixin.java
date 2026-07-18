package com.ziondev.experiencetweaks.mixin;

import com.ziondev.experiencetweaks.ExperienceTweaksMod;
import com.ziondev.experiencetweaks.block.FarmlandSlabBlock;
import com.ziondev.experiencetweaks.block.FarmlandSlabCropBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Redirects crop placement so that seeds planted on a bottom
 * {@link FarmlandSlabBlock} produce an offset {@link FarmlandSlabCropBlock}
 * instead of the vanilla crop block.
 * <p>
 * The offset variant renders its shapes 8 pixels lower than normal, aligning
 * the crop base with the top surface of the bottom slab (Y=7) rather than
 * floating at the top of the full block space (Y=16).
 * <p>
 * Only bottom slabs trigger the redirect. Top slabs and double slabs use the
 * vanilla crop because the crop visually sits at the correct height there.
 */
@Mixin(value = Block.class, priority = 900)
public abstract class CropPlacementMixin {

    /**
     * Intercepts {@link Block#getStateForPlacement} for crop blocks. When the
     * block below the placement position is a bottom farmland slab, returns the
     * corresponding {@link FarmlandSlabCropBlock} state with
     * {@link FarmlandSlabCropBlock#BOTTOM_OFFSET} set to {@code true}.
     *
     * @param ctx the placement context
     * @param cir callback used to override the returned block state
     */
    @Inject(method = "getStateForPlacement", at = @At("HEAD"), cancellable = true)
    private void experienceTweaks$redirectCropToSlabVariant(
            BlockPlaceContext ctx,
            CallbackInfoReturnable<BlockState> cir
    ) {
        Block self = (Block) (Object) this;
        if (!(self instanceof CropBlock)) {
            return;
        }

        BlockPos pos = ctx.getClickedPos();
        BlockState below = ctx.getLevel().getBlockState(pos.below());

        if (!(below.getBlock() instanceof FarmlandSlabBlock)) {
            return;
        }

        boolean isBottom = FarmlandSlabCropBlock.shouldOffset(below);

        FarmlandSlabCropBlock slabCrop = experienceTweaks$getSlabCrop(self);
        if (slabCrop == null) {
            return;
        }

        cir.setReturnValue(
                slabCrop.defaultBlockState()
                        .setValue(FarmlandSlabCropBlock.AGE, 0)
                        .setValue(FarmlandSlabCropBlock.BOTTOM_OFFSET, isBottom)
        );
    }

    /**
     * Maps a vanilla crop block to its farmland-slab variant.
     *
     * @param vanilla the vanilla crop block
     * @return the corresponding {@link FarmlandSlabCropBlock}, or {@code null}
     *         if there is no registered slab variant
     */
    private static FarmlandSlabCropBlock experienceTweaks$getSlabCrop(Block vanilla) {
        if (vanilla == Blocks.WHEAT)     return ExperienceTweaksMod.WHEAT_SLAB_CROP.get();
        if (vanilla == Blocks.CARROTS)   return ExperienceTweaksMod.CARROTS_SLAB_CROP.get();
        if (vanilla == Blocks.POTATOES)  return ExperienceTweaksMod.POTATOES_SLAB_CROP.get();
        if (vanilla == Blocks.BEETROOTS) return ExperienceTweaksMod.BEETROOTS_SLAB_CROP.get();
        return null;
    }
}
