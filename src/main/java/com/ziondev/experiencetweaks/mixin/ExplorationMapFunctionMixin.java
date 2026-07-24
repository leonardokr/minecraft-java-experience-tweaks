package com.ziondev.experiencetweaks.mixin;

import com.ziondev.experiencetweaks.ModConfig;
import com.ziondev.experiencetweaks.ExperienceTweaksMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.*;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.ExplorationMapFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to intercept ExplorationMapFunction's map generation and prevent duplicates.
 */
@Mixin(ExplorationMapFunction.class)
public abstract class ExplorationMapFunctionMixin {

    @Shadow
    @Final
    private TagKey<Structure> destination;

    @Shadow
    @Final
    private Holder<MapDecorationType> mapDecoration;

    @Shadow
    @Final
    private byte zoom;

    @Shadow
    @Final
    private int searchRadius;

    @Shadow
    @Final
    private boolean skipKnownStructures;

    /**
     * Injects at the head of the map generation run to evaluate unmapped structures.
     *
     * @param itemStack the map item stack
     * @param context the loot context
     * @param cir the return callback info
     */
    @Inject(method = "run", at = @At("HEAD"), cancellable = true)
    private void experienceTweaks$neverRepeatTreasureMaps(ItemStack itemStack, LootContext context,
            CallbackInfoReturnable<ItemStack> cir) {
        if (!itemStack.is(Items.MAP)) {
            return;
        }

        net.minecraft.world.entity.Entity entity = context.getOptionalParameter(LootContextParams.THIS_ENTITY);
        boolean enabled = false;
        if (entity instanceof Player player) {
            enabled = ModConfig.isNeverRepeatTreasureMaps(player);
            ExperienceTweaksMod.LOGGER.debug("[ET-Debug] Found player: {}, enabled={}", player.getScoreboardName(), enabled);
        } else {
            enabled = ModConfig.isNeverRepeatTreasureMaps();
            ExperienceTweaksMod.LOGGER.debug("[ET-Debug] No player in context, fallback enabled={}", enabled);
        }

        if (!enabled) {
            ExperienceTweaksMod.LOGGER.debug("[ET-Debug] neverRepeatTreasureMaps option is disabled.");
            return;
        }

        ExperienceTweaksMod.LOGGER.debug("[ET-Debug] neverRepeatTreasureMaps option is enabled.");
        Vec3 lootPos = context.getOptionalParameter(LootContextParams.ORIGIN);
        if (lootPos != null) {
            ServerLevel level = context.getLevel();
            BlockPos originalOrigin = BlockPos.containing(lootPos);
            BlockPos nearestMapStructure = null;
            int attempts = 0;
            java.util.Set<BlockPos> avoidedLocations = new java.util.HashSet<>();
            experienceTweaks$collectMappedLocations(level, avoidedLocations, this.zoom);

            while (attempts < 24) {
                BlockPos searchOrigin = experienceTweaks$getSpiralOffset(originalOrigin, attempts);
                
                if (experienceTweaks$isNearAvoided(searchOrigin, avoidedLocations)) {
                    ExperienceTweaksMod.LOGGER.debug("[ET-Debug] Attempt {} skipped: Near already mapped structure.", attempts);
                    attempts++;
                    continue;
                }

                int dx = searchOrigin.getX() - originalOrigin.getX();
                int dz = searchOrigin.getZ() - originalOrigin.getZ();
                int maxOffset = Math.max(Math.abs(dx), Math.abs(dz));
                int currentRadius = this.searchRadius + (maxOffset / 16);
                
                nearestMapStructure = level.findNearestMapStructure(
                        this.destination, searchOrigin, currentRadius, this.skipKnownStructures);
                
                if (nearestMapStructure == null) {
                    ExperienceTweaksMod.LOGGER.debug("[ET-Debug] Attempt {} (radius {} chunks): No nearest map structure found.", attempts, currentRadius);
                    attempts++;
                    continue;
                }
                ExperienceTweaksMod.LOGGER.debug("[ET-Debug] Attempt {} (radius {} chunks): Found nearest structure at: {}", attempts, currentRadius, nearestMapStructure);

                if (experienceTweaks$isAlreadyMapped(level, nearestMapStructure, this.zoom)) {
                    avoidedLocations.add(nearestMapStructure);
                    ExperienceTweaksMod.LOGGER.debug("[ET-Debug] Structure at {} is already mapped. Trying next spiral offset...", nearestMapStructure);
                    attempts++;
                } else {
                    ExperienceTweaksMod.LOGGER.debug("[ET-Debug] Structure at {} is not mapped. Choosing this!", nearestMapStructure);
                    break;
                }
            }

            if (nearestMapStructure != null) {
                ItemStack map = MapItem.create(level, nearestMapStructure.getX(), nearestMapStructure.getZ(), this.zoom,
                        true, true);
                MapItem.renderBiomePreviewMap(level, map);
                MapItemSavedData.addTargetDecoration(map, nearestMapStructure, "+", this.mapDecoration);
                cir.setReturnValue(map);
                return;
            }
        }
    }

    /**
     * Calculates the offset in a spiral layout for a given attempt.
     *
     * @param originalOrigin the starting center point of the search
     * @param attempt the current search attempt number
     * @return the shifted BlockPos coordinates
     */
    @Unique
    private BlockPos experienceTweaks$getSpiralOffset(BlockPos originalOrigin, int attempt) {
        if (attempt == 0) {
            return originalOrigin;
        }
        int ring = (attempt - 1) / 8 + 1;
        int direction = (attempt - 1) % 8;
        int distance = ring * 512;
        int x = 0;
        int z = 0;
        switch (direction) {
            case 0:
                z = distance;
                break;
            case 1:
                x = distance;
                break;
            case 2:
                z = -distance;
                break;
            case 3:
                x = -distance;
                break;
            case 4:
                x = distance;
                z = distance;
                break;
            case 5:
                x = distance;
                z = -distance;
                break;
            case 6:
                x = -distance;
                z = -distance;
                break;
            case 7:
                x = -distance;
                z = distance;
                break;
            default:
                break;
        }
        return originalOrigin.offset(x, 0, z);
    }

    /**
     * Collects all target locations and centers of existing maps to seed the avoided coordinates set.
     *
     * @param level the server level context
     * @param locations the set to populate with avoided coordinates
     * @param zoom the map zoom level
     */
    @SuppressWarnings("deprecation")
    @Unique
    private void experienceTweaks$collectMappedLocations(ServerLevel level, java.util.Set<BlockPos> locations, byte zoom) {
        int id = 0;
        int consecutiveNulls = 0;
        
        while (consecutiveNulls < 200) {
            MapItemSavedData mapData = level.getMapData(new MapId(id));
            if (mapData == null) {
                consecutiveNulls++;
                id++;
                continue;
            }
            consecutiveNulls = 0;

            if (mapData.dimension == level.dimension() && mapData.scale == zoom) {
                locations.add(new BlockPos(mapData.centerX, 64, mapData.centerZ));
            }

            for (MapDecoration decoration : mapData.getDecorations()) {
                if (decoration.type().is(MapDecorationTypes.TARGET_X) ||
                        decoration.type().is(MapDecorationTypes.TARGET_POINT) ||
                        decoration.type().is(MapDecorationTypes.OCEAN_MONUMENT) ||
                        decoration.type().is(MapDecorationTypes.WOODLAND_MANSION) ||
                        decoration.type().is(MapDecorationTypes.TRIAL_CHAMBERS)) {

                    int scaling = 1 << mapData.scale;
                    double decX = mapData.centerX + (decoration.x() * scaling) / 2.0;
                    double decZ = mapData.centerZ + (decoration.y() * scaling) / 2.0;
                    locations.add(new BlockPos((int) decX, 64, (int) decZ));
                }
            }
            id++;
        }
    }

    /**
     * Checks if the candidate coordinates are near any structure in the avoided coordinates set.
     *
     * @param pos the coordinates to test
     * @param avoided the set of avoided positions
     * @return {@code true} if the coordinates are near an avoided structure
     */
    @Unique
    private boolean experienceTweaks$isNearAvoided(BlockPos pos, java.util.Set<BlockPos> avoided) {
        for (BlockPos avoid : avoided) {
            double dx = pos.getX() - avoid.getX();
            double dz = pos.getZ() - avoid.getZ();
            if (dx * dx + dz * dz < 400.0 * 400.0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a candidate structure location has already been mapped.
     *
     * @param level the server level context
     * @param pos the structure coordinates
     * @param zoom the map zoom level
     * @return {@code true} if the coordinates are already mapped
     */
    @SuppressWarnings("deprecation")
    @Unique
    private boolean experienceTweaks$isAlreadyMapped(ServerLevel level, BlockPos pos, byte zoom) {
        int size = 128 * (1 << zoom);
        int areaX = net.minecraft.util.Mth.floor((pos.getX() + 64.0) / size);
        int areaZ = net.minecraft.util.Mth.floor((pos.getZ() + 64.0) / size);
        int candidateCenterX = areaX * size + size / 2 - 64;
        int candidateCenterZ = areaZ * size + size / 2 - 64;

        int id = 0;
        int consecutiveNulls = 0;
        
        while (consecutiveNulls < 200) {
            MapItemSavedData mapData = level.getMapData(new MapId(id));
            if (mapData == null) {
                consecutiveNulls++;
                id++;
                continue;
            }
            consecutiveNulls = 0;

            if (mapData.dimension == level.dimension() && mapData.scale == zoom) {
                if (mapData.centerX == candidateCenterX && mapData.centerZ == candidateCenterZ) {
                    ExperienceTweaksMod.LOGGER.debug("[ET-Debug] -> Map ID {} matched by center coords!", id);
                    return true;
                }
            }

            for (MapDecoration decoration : mapData.getDecorations()) {
                if (decoration.type().is(MapDecorationTypes.TARGET_X) ||
                        decoration.type().is(MapDecorationTypes.TARGET_POINT) ||
                        decoration.type().is(MapDecorationTypes.OCEAN_MONUMENT) ||
                        decoration.type().is(MapDecorationTypes.WOODLAND_MANSION) ||
                        decoration.type().is(MapDecorationTypes.TRIAL_CHAMBERS)) {

                    int scaling = 1 << mapData.scale;
                    double decX = mapData.centerX + (decoration.x() * scaling) / 2.0;
                    double decZ = mapData.centerZ + (decoration.y() * scaling) / 2.0;

                    if (Math.abs(decX - pos.getX()) < 16.0 && Math.abs(decZ - pos.getZ()) < 16.0) {
                        ExperienceTweaksMod.LOGGER.debug("[ET-Debug] -> Map ID {} matched by decoration!", id);
                        return true;
                    }
                }
            }
            id++;
        }
        return false;
    }
}
