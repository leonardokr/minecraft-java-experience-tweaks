package com.ziondev.experiencetweaks.mixin;

import com.ziondev.experiencetweaks.ExperienceTweaksMod;
import com.ziondev.experiencetweaks.block.GrassSlabBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SpreadingSnowyBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpreadingSnowyBlock.class)
public class SpreadingSnowyBlockMixin {

    @Inject(method = "randomTick", at = @At("HEAD"))
    private void experienceTweaks$onRandomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (state.is(Blocks.GRASS_BLOCK)) {
            if (level.getMaxLocalRawBrightness(pos.above()) >= 9) {
                for (int i = 0; i < 4; i++) {
                    BlockPos testPos = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
                    BlockState targetState = level.getBlockState(testPos);
                    if (targetState.is(ExperienceTweaksMod.DIRT_SLAB.get())) {
                        BlockPos aboveTest = testPos.above();
                        BlockState aboveState = level.getBlockState(aboveTest);
                        if (!aboveState.getFluidState().isFull()) {
                            int lightBlockInto = net.minecraft.world.level.lighting.LightEngine.getLightBlockInto(targetState, aboveState, net.minecraft.core.Direction.UP, aboveState.getLightDampening());
                            if (lightBlockInto < 15 && !level.getFluidState(aboveTest).is(FluidTags.WATER)) {
                                BlockState grassSlabState = ExperienceTweaksMod.GRASS_SLAB.get().defaultBlockState()
                                        .setValue(SlabBlock.TYPE, targetState.getValue(SlabBlock.TYPE))
                                        .setValue(SlabBlock.WATERLOGGED, targetState.getValue(SlabBlock.WATERLOGGED))
                                        .setValue(GrassSlabBlock.SNOWY, aboveState.is(BlockTags.SNOW));
                                level.setBlockAndUpdate(testPos, grassSlabState);
                            }
                        }
                    }
                }
            }
        }
    }
}
