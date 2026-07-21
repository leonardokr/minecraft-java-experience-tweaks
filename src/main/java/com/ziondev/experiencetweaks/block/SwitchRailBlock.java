package com.ziondev.experiencetweaks.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.NonNull;

import java.util.EnumSet;
import java.util.Set;

/**
 * A toggleable switch rail block that toggles its curve direction on each redstone pulse (rising edge)
 * or when right-clicked by a player.
 * It automatically initializes into a curved junction shape when placed at intersections,
 * connects directly to active activator rails on switchable branches, and toggles when activated from a non-switchable stem.
 */
public class SwitchRailBlock extends RailBlock {

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE;

    /**
     * Constructs a new switch rail block.
     *
     * @param properties block behaviour properties
     */
    public SwitchRailBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }

    @Override
    public Property<RailShape> getShapeProperty() {
        return SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state != null) {
            return ensureJunctionShape(state, context.getLevel(), context.getClickedPos());
        }
        return state;
    }

    @Override
    protected @NonNull InteractionResult useWithoutItem(@NonNull BlockState state, Level level, @NonNull BlockPos pos, @NonNull Player player, @NonNull BlockHitResult hit) {
        if (!level.isClientSide()) {
            BlockState newState = toggleJunctionShape(state, level, pos);
            level.setBlock(pos, newState, 3);
            level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.5F, 0.8F);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void updateState(BlockState state, Level level, BlockPos pos, Block neighborBlock) {
        boolean hasSignal = level.hasNeighborSignal(pos) || level.hasNeighborSignal(pos.above());
        boolean isPowered = state.getValue(POWERED);

        if (hasSignal && !isPowered) {
            BlockState newState = state.setValue(POWERED, true);
            newState = toggleJunctionShape(newState, level, pos);
            level.setBlock(pos, newState, 3);
            level.updateNeighborsAt(pos.below(), this);
            if (isSlope(newState.getValue(SHAPE))) {
                level.updateNeighborsAt(pos.above(), this);
            }
        } else if (!hasSignal && isPowered) {
            BlockState newState = state.setValue(POWERED, false);
            level.setBlock(pos, newState, 3);
            level.updateNeighborsAt(pos.below(), this);
            if (isSlope(newState.getValue(SHAPE))) {
                level.updateNeighborsAt(pos.above(), this);
            }
        } else {
            BlockState newState = ensureJunctionShape(state, level, pos);
            if (newState != state) {
                level.setBlock(pos, newState, 3);
            }
        }
    }

    /**
     * Ensures that the rail initializes to an appropriate curved shape when placed at a junction.
     *
     * @param state current block state
     * @param level world level
     * @param pos   block position
     * @return block state with appropriate curve shape
     */
    private static BlockState ensureJunctionShape(BlockState state, Level level, BlockPos pos) {
        RailShape current = state.getValue(SHAPE);
        if (isSlope(current)) {
            return state;
        }

        Set<Direction> dirs = findConnectedRailDirections(level, pos);
        boolean hasNorth = dirs.contains(Direction.NORTH);
        boolean hasSouth = dirs.contains(Direction.SOUTH);
        boolean hasEast  = dirs.contains(Direction.EAST);
        boolean hasWest  = dirs.contains(Direction.WEST);

        if (hasSouth && hasEast && hasWest && !hasNorth) {
            if (current != RailShape.SOUTH_EAST && current != RailShape.SOUTH_WEST) {
                return state.setValue(SHAPE, RailShape.SOUTH_EAST);
            }
        } else if (hasNorth && hasEast && hasWest && !hasSouth) {
            if (current != RailShape.NORTH_EAST && current != RailShape.NORTH_WEST) {
                return state.setValue(SHAPE, RailShape.NORTH_EAST);
            }
        } else if (hasNorth && hasSouth && hasEast && !hasWest) {
            if (current != RailShape.NORTH_EAST && current != RailShape.SOUTH_EAST) {
                return state.setValue(SHAPE, RailShape.SOUTH_EAST);
            }
        } else if (hasNorth && hasSouth && hasWest && !hasEast) {
            if (current != RailShape.NORTH_WEST && current != RailShape.SOUTH_WEST) {
                return state.setValue(SHAPE, RailShape.SOUTH_WEST);
            }
        } else if (hasSouth && hasEast && !hasNorth && !hasWest) {
            return state.setValue(SHAPE, RailShape.SOUTH_EAST);
        } else if (hasSouth && hasWest && !hasNorth && !hasEast) {
            return state.setValue(SHAPE, RailShape.SOUTH_WEST);
        } else if (hasNorth && hasEast && !hasSouth && !hasWest) {
            return state.setValue(SHAPE, RailShape.NORTH_EAST);
        } else if (hasNorth && hasWest && !hasSouth && !hasEast) {
            return state.setValue(SHAPE, RailShape.NORTH_WEST);
        }

        return state;
    }

    /**
     * Toggles the curve direction between connecting junction branches.
     * Activator rails on switchable branches call the curve toward them,
     * while activator rails on the non-switchable stem toggle the curve.
     *
     * @param state current block state
     * @param level world level
     * @param pos   block position
     * @return block state with toggled curve shape
     */
    private static BlockState toggleJunctionShape(BlockState state, Level level, BlockPos pos) {
        RailShape current = state.getValue(SHAPE);
        if (isSlope(current)) {
            return state;
        }

        Set<Direction> dirs = findConnectedRailDirections(level, pos);
        boolean hasNorth = dirs.contains(Direction.NORTH);
        boolean hasSouth = dirs.contains(Direction.SOUTH);
        boolean hasEast  = dirs.contains(Direction.EAST);
        boolean hasWest  = dirs.contains(Direction.WEST);

        Direction sideActivatorDir = findAdjacentActiveActivatorRail(level, pos);
        if (sideActivatorDir != null) {
            if (hasSouth && hasEast && hasWest && !hasNorth) {
                if (sideActivatorDir == Direction.EAST) return state.setValue(SHAPE, RailShape.SOUTH_EAST);
                if (sideActivatorDir == Direction.WEST) return state.setValue(SHAPE, RailShape.SOUTH_WEST);
                if (sideActivatorDir == Direction.SOUTH) return state.setValue(SHAPE, current == RailShape.SOUTH_EAST ? RailShape.SOUTH_WEST : RailShape.SOUTH_EAST);
            }
            if (hasNorth && hasEast && hasWest && !hasSouth) {
                if (sideActivatorDir == Direction.EAST) return state.setValue(SHAPE, RailShape.NORTH_EAST);
                if (sideActivatorDir == Direction.WEST) return state.setValue(SHAPE, RailShape.NORTH_WEST);
                if (sideActivatorDir == Direction.NORTH) return state.setValue(SHAPE, current == RailShape.NORTH_EAST ? RailShape.NORTH_WEST : RailShape.NORTH_EAST);
            }
            if (hasNorth && hasSouth && hasEast && !hasWest) {
                if (sideActivatorDir == Direction.NORTH) return state.setValue(SHAPE, RailShape.NORTH_EAST);
                if (sideActivatorDir == Direction.SOUTH) return state.setValue(SHAPE, RailShape.SOUTH_EAST);
                if (sideActivatorDir == Direction.EAST) return state.setValue(SHAPE, current == RailShape.SOUTH_EAST ? RailShape.NORTH_EAST : RailShape.SOUTH_EAST);
            }
            if (hasNorth && hasSouth && hasWest && !hasEast) {
                if (sideActivatorDir == Direction.NORTH) return state.setValue(SHAPE, RailShape.NORTH_WEST);
                if (sideActivatorDir == Direction.SOUTH) return state.setValue(SHAPE, RailShape.SOUTH_WEST);
                if (sideActivatorDir == Direction.WEST) return state.setValue(SHAPE, current == RailShape.SOUTH_WEST ? RailShape.NORTH_WEST : RailShape.SOUTH_WEST);
            }

            RailShape targetShape = getCurveTowardsDirection(sideActivatorDir);
            if (targetShape != null) {
                return state.setValue(SHAPE, targetShape);
            }
        }

        if (hasSouth && hasEast && hasWest && !hasNorth) {
            return state.setValue(SHAPE, current == RailShape.SOUTH_EAST ? RailShape.SOUTH_WEST : RailShape.SOUTH_EAST);
        }
        if (hasNorth && hasEast && hasWest && !hasSouth) {
            return state.setValue(SHAPE, current == RailShape.NORTH_EAST ? RailShape.NORTH_WEST : RailShape.NORTH_EAST);
        }
        if (hasNorth && hasSouth && hasEast && !hasWest) {
            return state.setValue(SHAPE, current == RailShape.SOUTH_EAST ? RailShape.NORTH_EAST : RailShape.SOUTH_EAST);
        }
        if (hasNorth && hasSouth && hasWest && !hasEast) {
            return state.setValue(SHAPE, current == RailShape.SOUTH_WEST ? RailShape.NORTH_WEST : RailShape.SOUTH_WEST);
        }

        RailShape next = switch (current) {
            case SOUTH_EAST  -> RailShape.SOUTH_WEST;
            case SOUTH_WEST  -> RailShape.NORTH_WEST;
            case NORTH_WEST  -> RailShape.NORTH_EAST;
            case NORTH_EAST  -> RailShape.SOUTH_EAST;
            case NORTH_SOUTH -> RailShape.SOUTH_EAST;
            case EAST_WEST   -> RailShape.SOUTH_EAST;
            default -> current;
        };
        return state.setValue(SHAPE, next);
    }

    /**
     * Finds adjacent horizontal directions containing connecting rail blocks.
     *
     * @param level world level
     * @param pos   block position
     * @return set of directions with adjacent rails
     */
    private static Set<Direction> findConnectedRailDirections(Level level, BlockPos pos) {
        Set<Direction> dirs = EnumSet.noneOf(Direction.class);
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos sidePos = pos.relative(dir);
            BlockState sideState = level.getBlockState(sidePos);
            if (sideState.getBlock() instanceof BaseRailBlock) {
                dirs.add(dir);
                continue;
            }
            BlockState aboveState = level.getBlockState(sidePos.above());
            if (aboveState.getBlock() instanceof BaseRailBlock) {
                dirs.add(dir);
                continue;
            }
            BlockState belowState = level.getBlockState(sidePos.below());
            if (belowState.getBlock() instanceof BaseRailBlock) {
                dirs.add(dir);
            }
        }
        return dirs;
    }

    /**
     * Returns whether the given rail shape represents an ascending slope.
     *
     * @param shape the rail shape
     * @return {@code true} if the shape is ascending
     */
    private static boolean isSlope(RailShape shape) {
        return shape == RailShape.ASCENDING_EAST || shape == RailShape.ASCENDING_WEST
                || shape == RailShape.ASCENDING_NORTH || shape == RailShape.ASCENDING_SOUTH;
    }

    /**
     * Checks adjacent horizontal positions for an active activator or detector rail.
     *
     * @param level world level
     * @param pos   block position
     * @return direction of the active rail, or {@code null} if none found
     */
    private static Direction findAdjacentActiveActivatorRail(Level level, BlockPos pos) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos sidePos = pos.relative(dir);
            BlockState sideState = level.getBlockState(sidePos);
            if (sideState.getBlock() instanceof net.minecraft.world.level.block.PoweredRailBlock
                    || sideState.getBlock() instanceof net.minecraft.world.level.block.DetectorRailBlock) {
                if (sideState.hasProperty(BlockStateProperties.POWERED) && sideState.getValue(BlockStateProperties.POWERED)) {
                    return dir;
                }
            }
        }
        return null;
    }

    /**
     * Returns a curved rail shape that turns toward the specified direction.
     *
     * @param dir target direction
     * @return corresponding curved RailShape, or {@code null}
     */
    private static RailShape getCurveTowardsDirection(Direction dir) {
        return switch (dir) {
            case NORTH -> RailShape.NORTH_WEST;
            case SOUTH -> RailShape.SOUTH_EAST;
            case EAST  -> RailShape.SOUTH_EAST;
            case WEST  -> RailShape.NORTH_WEST;
            default    -> null;
        };
    }
}
