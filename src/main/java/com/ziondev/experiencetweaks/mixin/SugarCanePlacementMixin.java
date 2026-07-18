package com.ziondev.experiencetweaks.mixin;

import com.ziondev.experiencetweaks.ExperienceTweaksMod;
import com.ziondev.experiencetweaks.block.DirtSlabSugarCaneBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Redirects sugar cane placement so that cane planted on a dirt or grass slab produces a
 * {@link DirtSlabSugarCaneBlock} with the correct visual offset instead of the vanilla block.
 * Only activates for vanilla {@link SugarCaneBlock}; the mod's own variant is excluded
 * to prevent infinite recursion.
 */
@Mixin(value = Block.class, priority = 800)
public abstract class SugarCanePlacementMixin {

    /**
     * Intercepts {@link Block#getStateForPlacement} for vanilla sugar cane.
     * When the floor is a dirt or grass slab, returns the slab-aware variant with
     * {@link DirtSlabSugarCaneBlock#BOTTOM_OFFSET} set appropriately.
     *
     * @param ctx the placement context
     * @param cir callback used to override the returned block state
     */
    @Inject(method = "getStateForPlacement", at = @At("HEAD"), cancellable = true)
    private void experienceTweaks$redirectSugarCaneToSlabVariant(
            BlockPlaceContext ctx,
            CallbackInfoReturnable<BlockState> cir
    ) {
        Block self = (Block) (Object) this;
        if (!(self instanceof SugarCaneBlock) || self instanceof DirtSlabSugarCaneBlock) {
            return;
        }

        BlockPos pos = ctx.getClickedPos();
        BlockState below = ctx.getLevel().getBlockState(pos.below());

        if (!DirtSlabSugarCaneBlock.isDirtSlab(below)) {
            return;
        }

        boolean isBottom = DirtSlabSugarCaneBlock.shouldOffset(below);
        cir.setReturnValue(
                ExperienceTweaksMod.SUGAR_CANE_SLAB.get().defaultBlockState()
                        .setValue(DirtSlabSugarCaneBlock.BOTTOM_OFFSET, isBottom)
        );
    }
}