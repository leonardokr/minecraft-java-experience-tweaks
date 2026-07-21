package com.ziondev.experiencetweaks.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A sugar cane variant that aligns visually with the top surface of a bottom dirt or grass slab.
 * Extends {@link Block} directly so the model JSON render type ({@code cutout}) is correctly
 * applied. When {@link #BOTTOM_OFFSET} is {@code true}, the block shape is shifted 8 pixels
 * downward so the cane appears to grow directly from the slab top face rather than floating.
 */
public class DirtSlabSugarCaneBlock extends Block {

    /** Growth age, mirroring vanilla {@code SugarCaneBlock.AGE} (0-15). */
    public static final IntegerProperty AGE = BlockStateProperties.AGE_15;

    /** When {@code true}, the cane renders 8 pixels lower to align with a bottom slab surface. */
    public static final BooleanProperty BOTTOM_OFFSET = BooleanProperty.create("bottom_offset");

    private static final VoxelShape SHAPE_NORMAL = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);
    private static final VoxelShape SHAPE_OFFSET  = Block.box(2.0, -8.0, 2.0, 14.0, 8.0, 14.0);

    /** Max stack height before growth stops (matches vanilla sugar cane). */
    private static final int MAX_HEIGHT = 3;

    /**
     * Constructs a new sugar cane slab block.
     *
     * @param properties block behaviour properties
     */
    public DirtSlabSugarCaneBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(
            this.stateDefinition.any()
                .setValue(AGE, 0)
                .setValue(BOTTOM_OFFSET, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, BOTTOM_OFFSET);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState below = context.getLevel().getBlockState(context.getClickedPos().below());
        boolean isBottom = false;
        if (isDirtSlab(below)) {
            isBottom = shouldOffset(below);
        } else if (isSugarCane(below)) {
            isBottom = below.hasProperty(BOTTOM_OFFSET) && below.getValue(BOTTOM_OFFSET);
        }
        return this.defaultBlockState().setValue(BOTTOM_OFFSET, isBottom);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(BOTTOM_OFFSET) ? SHAPE_OFFSET : SHAPE_NORMAL;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        if (isSugarCane(below)) {
            return true;
        }
        if (isDirtSlab(below)) {
            BlockPos floorPos = pos.below();
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                FluidState fluid = level.getFluidState(floorPos.relative(dir));
                if (fluid.is(FluidTags.WATER)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected BlockState updateShape(
            BlockState state, LevelReader level, ScheduledTickAccess scheduledTick,
            BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState,
            RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        if (direction == Direction.DOWN && neighborState.hasProperty(BOTTOM_OFFSET)) {
            return state.setValue(BOTTOM_OFFSET, neighborState.getValue(BOTTOM_OFFSET));
        }
        return super.updateShape(state, level, scheduledTick, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.isEmptyBlock(pos.above())) {
            return;
        }
        int height = 1;
        while (isSugarCane(level.getBlockState(pos.below(height)))) {
            height++;
        }
        if (height >= MAX_HEIGHT) {
            return;
        }
        int age = state.getValue(AGE);
        if (age == 15) {
            boolean isBottom = state.getValue(BOTTOM_OFFSET);
            level.setBlock(pos.above(), defaultBlockState().setValue(BOTTOM_OFFSET, isBottom), 2);
            level.setBlock(pos, state.setValue(AGE, 0), 2);
        } else {
            level.setBlock(pos, state.setValue(AGE, age + 1), 2);
        }
    }

    /**
     * Returns {@code true} if the state is this block or vanilla {@link Blocks#SUGAR_CANE}.
     *
     * @param state the block state to check
     * @return {@code true} if the block is a sugar cane variant
     */
    private boolean isSugarCane(BlockState state) {
        return state.is(this) || state.is(Blocks.SUGAR_CANE);
    }

    /**
     * Returns {@code true} if the state is a bottom dirt or grass slab (cane should use offset).
     *
     * @param state the floor block state
     * @return {@code true} if the cane should use the offset model
     */
    public static boolean shouldOffset(BlockState state) {
        if (!isDirtSlab(state)) return false;
        if (!state.hasProperty(SlabBlock.TYPE)) return false;
        return state.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;
    }

    /**
     * Returns {@code true} if the state is a dirt or grass slab of any type.
     *
     * @param state the block state to check
     * @return {@code true} if the block is a dirt or grass slab
     */
    public static boolean isDirtSlab(BlockState state) {
        return state.getBlock() instanceof DirtSlabBlock || state.getBlock() instanceof GrassSlabBlock;
    }
}