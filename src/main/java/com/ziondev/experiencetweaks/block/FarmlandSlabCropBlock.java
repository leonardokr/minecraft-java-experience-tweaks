package com.ziondev.experiencetweaks.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A crop block variant that renders correctly when placed on a bottom
 * {@link FarmlandSlabBlock}.
 * <p>
 * Vanilla crops render at Y=0 of their block space, which creates a visual gap
 * when the farmland slab below only reaches Y=7. This class adds a
 * {@link #BOTTOM_OFFSET} boolean property: when {@code true}, all age shapes
 * are shifted down by 8 pixels so the crop base aligns with the top of the
 * bottom slab. When {@code false} (top slab or double slab), the vanilla shapes
 * are used unchanged.
 * <p>
 * Instances are registered internally and never exposed as items. The
 * {@link com.ziondev.experiencetweaks.mixin.CropPlacementMixin} redirects seed
 * placement to these blocks whenever the target farmland is a bottom slab.
 */
public class FarmlandSlabCropBlock extends CropBlock {

    /**
     * When {@code true}, the crop renders 8 pixels lower than normal, aligning
     * its base with the top of a bottom farmland slab.
     */
    public static final BooleanProperty BOTTOM_OFFSET = BooleanProperty.create("bottom_offset");

    /**
     * Shapes for each age stage when placed on a bottom slab.
     * All shapes start at Y=-8 (one half-block below the block's own origin)
     * to align with the top surface of the farmland slab below.
     */
    private static final VoxelShape[] OFFSET_SHAPES = {
            Block.box(0.0, -8.0, 0.0, 16.0, -6.0, 16.0), // age 0
            Block.box(0.0, -8.0, 0.0, 16.0, -4.0, 16.0), // age 1
            Block.box(0.0, -8.0, 0.0, 16.0, -2.0, 16.0), // age 2
            Block.box(0.0, -8.0, 0.0, 16.0, 0.0, 16.0), // age 3
            Block.box(0.0, -8.0, 0.0, 16.0, 2.0, 16.0), // age 4
            Block.box(0.0, -8.0, 0.0, 16.0, 4.0, 16.0), // age 5
            Block.box(0.0, -8.0, 0.0, 16.0, 6.0, 16.0), // age 6
            Block.box(0.0, -8.0, 0.0, 16.0, 8.0, 16.0), // age 7
    };

    private final ItemLike seedItem;

    /**
     * @param properties block behaviour properties (copied from vanilla crop)
     * @param seedItem   the seed item that produces this crop when used
     */
    public FarmlandSlabCropBlock(BlockBehaviour.Properties properties, ItemLike seedItem) {
        super(properties);
        this.seedItem = seedItem;
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(AGE, 0)
                        .setValue(BOTTOM_OFFSET, false));
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return seedItem;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, BOTTOM_OFFSET);
    }

    /**
     * Returns the visual shape for the current age, shifted down by 8px when
     * {@link #BOTTOM_OFFSET} is {@code true}.
     */
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(BOTTOM_OFFSET)) {
            return OFFSET_SHAPES[state.getValue(AGE)];
        }
        return super.getShape(state, level, pos, context);
    }

    @Override
    public BlockState getStateForAge(int age) {
        return this.defaultBlockState().setValue(AGE, age);
    }

    /**
     * Preserves {@link #BOTTOM_OFFSET} when the crop advances to the next age
     * during a random tick. The vanilla implementation calls
     * {@link #getStateForAge(int)} which would reset {@code BOTTOM_OFFSET} to
     * {@code false}.
     */
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getRawBrightness(pos, 0) >= 9) {
            int age = getAge(state);
            if (age < getMaxAge()) {
                float growthSpeed = getGrowthSpeed(state, level, pos);
                if (net.neoforged.neoforge.common.CommonHooks.canCropGrow(level, pos, state,
                        random.nextInt((int) (25.0F / growthSpeed) + 1) == 0)) {
                    BlockState nextState = state.setValue(AGE, age + 1);
                    level.setBlock(pos, nextState, Block.UPDATE_CLIENTS);
                    net.neoforged.neoforge.common.CommonHooks.fireCropGrowPost(level, pos, state);
                }
            }
        }
    }

    /**
     * The crop can only be placed on a {@link FarmlandSlabBlock}. It does not
     * survive on regular farmland — use the vanilla crop there.
     */
    @Override
    protected boolean mayPlaceOn(BlockState floor, BlockGetter level, BlockPos pos) {
        return floor.getBlock() instanceof FarmlandSlabBlock;
    }

    /**
     * Determines whether the crop is on a bottom slab and should be rendered
     * with the downward offset.
     *
     * @param state the block state of the farmland below
     * @return {@code true} if the slab type is BOTTOM
     */
    public static boolean shouldOffset(BlockState state) {
        if (state.getBlock() instanceof FarmlandSlabBlock && state.hasProperty(SlabBlock.TYPE)) {
            return state.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;
        }
        return false;
    }
}
