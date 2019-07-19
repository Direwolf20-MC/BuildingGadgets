package com.direwolf20.buildinggadgets.api.building.view;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IWorld;

import javax.annotation.Nullable;

/**
 * Context providing additional information in which a given {@link ITemplateView} is executed.
 * Besides the {@link IWorld} provided by this {@code IBuildContext} all information is optional, even though users should provide as much information as possible.
 */
public interface IBuildContext {
    /**
     * @return An {@link IWorld} in which an {@link com.direwolf20.buildinggadgets.api.template.ITemplate} might be build.
     */
    IWorld getWorld();

    /**
     * @return The {@link PlayerEntity} who would perform a build. This is optional and therefore may be null.
     */
    @Nullable
    PlayerEntity getBuildingPlayer();

    /**
     * @return The {@link net.minecraft.item.Item} used in this Build in the Form of an {@link ItemStack}
     */
    ItemStack getUsedStack();
}
