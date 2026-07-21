package com.ziondev.experiencetweaks.block;

import com.ziondev.experiencetweaks.ExperienceTweaksMod;
import com.ziondev.experiencetweaks.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.minecraft.util.TriState;
import org.jspecify.annotations.Nullable;

/**
 * A farmland slab block that supports crops and moisture state transitions.
 */
public class FarmlandSlabBlock extends SlabBlock {

    public static final IntegerProperty MOISTURE = BlockStateProperties.MOISTURE;
    private static final VoxelShape SHAPE_BOTTOM = Block.column(16.0, 0.0, 7.0);
    private static final VoxelShape SHAPE_TOP = Block.column(16.0, 8.0, 15.0);
    private static final VoxelShape SHAPE_DOUBLE = Block.column(16.0, 0.0, 15.0);

    /**
     * Constructs a new farmland slab block.
     *
     * @param properties block behaviour properties
     */
    public FarmlandSlabBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(TYPE, SlabType.BOTTOM).setValue(WATERLOGGED, false)
                .setValue(MOISTURE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TYPE, WATERLOGGED, MOISTURE);
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
        turnToDirt(state, level, pos);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int moisture = state.getValue(MOISTURE);
        if (!isNearWater(level, pos) && !level.isRainingAt(pos.above())) {
            if (moisture > 0) {
                level.setBlock(pos, state.setValue(MOISTURE, moisture - 1), 2);
            } else if (!shouldMaintainFarmland(level, pos)) {
                turnToDirt(state, level, pos);
            }
        } else if (moisture < 7) {
            level.setBlock(pos, state.setValue(MOISTURE, 7), 2);
        }
    }

    /**
     * Converts the farmland slab block state to a regular dirt slab.
     *
     * @param state the farmland slab state
     * @param level the server level
     * @param pos   the block position
     */
    private static void turnToDirt(BlockState state, ServerLevel level, BlockPos pos) {
        level.setBlockAndUpdate(pos, ExperienceTweaksMod.DIRT_SLAB.get().defaultBlockState()
                .setValue(TYPE, state.getValue(TYPE))
                .setValue(WATERLOGGED, state.getValue(WATERLOGGED)));
    }

    /**
     * Returns {@code true} if water is nearby to hydrate the farmland slab.
     * Checks configurable horizontal radius from {@link ModConfig#getWaterHydrationRadius()}
     * and vertical range of 0 to 1 (or -1 to 1 if {@link ModConfig#isWaterBelowHydratesFarmlandEnabled()} is active).
     *
     * @param level the level reader
     * @param pos   the farmland slab position
     * @return {@code true} if water is nearby
     */
    private static boolean isNearWater(LevelReader level, BlockPos pos) {
        int radius = ModConfig.getWaterHydrationRadius();
        int minY = ModConfig.isWaterBelowHydratesFarmlandEnabled() ? -1 : 0;
        for (BlockPos testPos : BlockPos.betweenClosed(pos.offset(-radius, minY, -radius), pos.offset(radius, 1, radius))) {
            if (level.getFluidState(testPos).is(FluidTags.WATER)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the block above maintains farmland (e.g. crops).
     *
     * @param level the level reader
     * @param pos   the farmland slab position
     * @return {@code true} if farmland should be maintained
     */
    private static boolean shouldMaintainFarmland(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos.above()).is(BlockTags.MAINTAINS_FARMLAND);
    }

    @Override
    public TriState canSustainPlant(BlockState state, BlockGetter level, BlockPos pos, Direction direction,
            BlockState plantState) {
        if (plantState.is(BlockTags.CROPS) || plantState.is(BlockTags.GROWS_CROPS)) {
            return TriState.TRUE;
        }
        return super.canSustainPlant(state, level, pos, direction, plantState);
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
        }
        return super.getToolModifiedState(state, context, itemAbility, simulate);
    }
}
