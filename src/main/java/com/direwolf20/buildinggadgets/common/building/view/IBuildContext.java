package com.direwolf20.buildinggadgets.common.building.view;

import com.direwolf20.buildinggadgets.common.template.Template;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Context providing additional information in which a given {@link IBuildView} is executed.
 * Besides the {@link IWorld} provided by this {@code IBuildContext} all information is optional, even though users should provide as much information as possible.
 */
public interface IBuildContext {
    /**
     * @return An {@link IWorld} in which an {@link Template} might be build.
     */
    World getWorld();

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
