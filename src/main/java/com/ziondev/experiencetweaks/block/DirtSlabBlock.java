package com.ziondev.experiencetweaks.block;

import com.ziondev.experiencetweaks.ExperienceTweaksMod;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import org.jspecify.annotations.Nullable;

public class DirtSlabBlock extends SlabBlock {

    public DirtSlabBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @SuppressWarnings("deprecation")
    @Override
    public @Nullable BlockState getToolModifiedState(BlockState state, UseOnContext context, ItemAbility itemAbility,
            boolean simulate) {
        BlockPos above = context.getClickedPos().above();
        BlockState aboveState = context.getLevel().getBlockState(above);

        if (itemAbility == ItemAbilities.SHOVEL_FLATTEN) {
            if (!aboveState.isSolid() || aboveState.getBlock() instanceof FenceGateBlock) {
                return ExperienceTweaksMod.DIRT_PATH_SLAB.get().defaultBlockState()
                        .setValue(TYPE, state.getValue(TYPE))
                        .setValue(WATERLOGGED, state.getValue(WATERLOGGED));
            }
        } else if (itemAbility == ItemAbilities.HOE_TILL) {
            if (!aboveState.isSolid()) {
                return ExperienceTweaksMod.FARMLAND_SLAB.get().defaultBlockState()
                        .setValue(TYPE, state.getValue(TYPE))
                        .setValue(WATERLOGGED, state.getValue(WATERLOGGED));
            }
        }
        return super.getToolModifiedState(state, context, itemAbility, simulate);
    }
}
