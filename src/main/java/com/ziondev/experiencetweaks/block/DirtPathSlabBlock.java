package com.ziondev.experiencetweaks.block;

import com.ziondev.experiencetweaks.ExperienceTweaksMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DirtPathSlabBlock extends SlabBlock {

    private static final VoxelShape SHAPE_BOTTOM = Block.column(16.0, 0.0, 7.0);
    private static final VoxelShape SHAPE_TOP = Block.column(16.0, 8.0, 15.0);
    private static final VoxelShape SHAPE_DOUBLE = Block.column(16.0, 0.0, 15.0);

    public DirtPathSlabBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.defaultBlockState().setValue(TYPE, SlabType.BOTTOM).setValue(WATERLOGGED, false));
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch ((SlabType) state.getValue(TYPE)) {
            case TOP -> SHAPE_TOP;
            case BOTTOM -> SHAPE_BOTTOM;
            case DOUBLE -> SHAPE_DOUBLE;
        };
    }

    @SuppressWarnings("deprecation")
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState aboveState = level.getBlockState(pos.above());
        return !aboveState.isSolid() || aboveState.getBlock() instanceof FenceGateBlock;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
            Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        if (directionToNeighbour == Direction.UP && !state.canSurvive(level, pos)) {
            ticks.scheduleTick(pos, this, 1);
        }
        return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.setBlockAndUpdate(pos, ExperienceTweaksMod.DIRT_SLAB.get().defaultBlockState()
                .setValue(TYPE, state.getValue(TYPE))
                .setValue(WATERLOGGED, state.getValue(WATERLOGGED)));
    }
}
