package com.ziondev.experiencetweaks.block;

import com.ziondev.experiencetweaks.ExperienceTweaksMod;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SnowyBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.lighting.LightEngine;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class GrassSlabBlock extends SlabBlock implements BonemealableBlock {

    public static final BooleanProperty SNOWY = BlockStateProperties.SNOWY;

    public GrassSlabBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(TYPE, net.minecraft.world.level.block.state.properties.SlabType.BOTTOM)
                .setValue(WATERLOGGED, false).setValue(SNOWY, false));
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(TYPE, WATERLOGGED, SNOWY);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
            Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        BlockState updated = super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos,
                neighbourState, random);
        if (directionToNeighbour == Direction.UP) {
            return updated.setValue(SNOWY, neighbourState.is(BlockTags.SNOW));
        }
        return updated;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null)
            return null;
        BlockState aboveState = context.getLevel().getBlockState(context.getClickedPos().above());
        return state.setValue(SNOWY, aboveState.is(BlockTags.SNOW));
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    private static boolean canStayAlive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos above = pos.above();
        BlockState aboveState = level.getBlockState(above);
        if (aboveState.is(Blocks.SNOW) && aboveState.getValue(SnowLayerBlock.LAYERS) == 1) {
            return true;
        } else if (aboveState.getFluidState().isFull()) {
            return false;
        } else {
            int lightBlockInto = LightEngine.getLightBlockInto(state, aboveState, Direction.UP,
                    aboveState.getLightDampening());
            return lightBlockInto < 15;
        }
    }

    private static boolean canPropagate(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos above = pos.above();
        return canStayAlive(state, level, pos) && !level.getFluidState(above).is(FluidTags.WATER);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!canStayAlive(state, level, pos)) {
            if (!level.isAreaLoaded(pos, 1))
                return;
            level.setBlockAndUpdate(pos, ExperienceTweaksMod.DIRT_SLAB.get().defaultBlockState()
                    .setValue(TYPE, state.getValue(TYPE))
                    .setValue(WATERLOGGED, state.getValue(WATERLOGGED)));
        } else {
            if (!level.isAreaLoaded(pos, 3))
                return;
            if (level.getMaxLocalRawBrightness(pos.above()) >= 9) {
                BlockState defaultGrassSlabState = this.defaultBlockState();
                for (int i = 0; i < 4; i++) {
                    BlockPos testPos = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
                    BlockState neighborState = level.getBlockState(testPos);

                    if (neighborState.is(Blocks.DIRT)) {
                        BlockState defaultGrassState = Blocks.GRASS_BLOCK.defaultBlockState();
                        if (canPropagate(defaultGrassState, level, testPos)) {
                            level.setBlockAndUpdate(testPos, defaultGrassState.setValue(SnowyBlock.SNOWY,
                                    level.getBlockState(testPos.above()).is(BlockTags.SNOW)));
                        }
                    } else if (neighborState.is(ExperienceTweaksMod.DIRT_SLAB.get())) {
                        if (canPropagate(defaultGrassSlabState, level, testPos)) {
                            level.setBlockAndUpdate(testPos, defaultGrassSlabState
                                    .setValue(TYPE, neighborState.getValue(TYPE))
                                    .setValue(WATERLOGGED, neighborState.getValue(WATERLOGGED))
                                    .setValue(SNOWY, level.getBlockState(testPos.above()).is(BlockTags.SNOW)));
                        }
                    }
                }
            }
        }
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

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return level.getBlockState(pos.above()).isAir() && level.isInsideBuildHeight(pos.above());
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        BlockPos above = pos.above();
        BlockState grass = Blocks.SHORT_GRASS.defaultBlockState();
        Optional<Holder.Reference<PlacedFeature>> grassFeature = level.registryAccess()
                .lookupOrThrow(Registries.PLACED_FEATURE)
                .get(VegetationPlacements.GRASS_BONEMEAL);

        label48: for (int j = 0; j < 128; j++) {
            BlockPos testPos = above;

            for (int i = 0; i < j / 16; i++) {
                testPos = testPos.offset(random.nextInt(3) - 1, (random.nextInt(3) - 1) * random.nextInt(3) / 2,
                        random.nextInt(3) - 1);
                if (!level.getBlockState(testPos.below()).is(this)
                        || level.getBlockState(testPos).isCollisionShapeFullBlock(level, testPos)) {
                    continue label48;
                }
            }

            BlockState testState = level.getBlockState(testPos);
            if (testState.is(grass.getBlock()) && random.nextInt(10) == 0) {
                BonemealableBlock bonemealableBlock = (BonemealableBlock) grass.getBlock();
                if (bonemealableBlock.isValidBonemealTarget(level, testPos, testState)) {
                    bonemealableBlock.performBonemeal(level, random, testPos, testState);
                }
            }

            if (testState.isAir() && !level.isOutsideBuildHeight(testPos)) {
                if (random.nextInt(8) == 0) {
                    List<ConfiguredFeature<?, ?>> features = level.getBiome(testPos).value().getGenerationSettings()
                            .getBoneMealFeatures();
                    if (!features.isEmpty()) {
                        ConfiguredFeature<?, ?> placementFeature = Util.getRandom(features, random);
                        placementFeature.place(level, level.getChunkSource().getGenerator(), random, testPos);
                    }
                } else if (grassFeature.isPresent()) {
                    grassFeature.get().value().place(level, level.getChunkSource().getGenerator(), random, testPos);
                }
            }
        }
    }

    @Override
    public BonemealableBlock.Type getType() {
        return Type.NEIGHBOR_SPREADER;
    }
}
