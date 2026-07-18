package com.ziondev.experiencetweaks.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.ItemCombinerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes the {@code inputSlots} container from {@link ItemCombinerMenu} so
 * that mixins targeting its subclasses (such as {@link net.minecraft.world.inventory.AnvilMenu})
 * can read and write the input slots directly.
 */
@Mixin(ItemCombinerMenu.class)
public interface ItemCombinerMenuAccessor {

    /**
     * Returns the container holding the two input slots of the combiner menu.
     *
     * @return the input slots container
     */
    @Accessor("inputSlots")
    Container experienceTweaks$getInputSlots();
}
