package com.ziondev.experiencetweaks.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor mixin for internal fields and synched entity data of {@link FishingHook}.
 */
@Mixin(FishingHook.class)
public interface FishingHookAccessor {

    @Accessor("nibble")
    int getNibble();

    @Accessor("biting")
    boolean isBiting();

    @Accessor("DATA_BITING")
    static EntityDataAccessor<Boolean> getDataBiting() {
        throw new AssertionError();
    }
}
