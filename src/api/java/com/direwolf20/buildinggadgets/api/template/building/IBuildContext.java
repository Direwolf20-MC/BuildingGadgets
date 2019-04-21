package com.direwolf20.buildinggadgets.api.template.building;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.IWorld;

import javax.annotation.Nullable;

/**
 * Context providing additional information in which a given {@link com.direwolf20.buildinggadgets.api.template.ITemplate} is viewed as an {@link ITemplateView}.
 * Besides the {@link IWorld} provided by this {@code IBuildContext} all information is optional, even though users should provide as much information as possible.
 */
public interface IBuildContext {
    /**
     * @return An {@link IWorld} in which an {@link com.direwolf20.buildinggadgets.api.template.ITemplate} might be build.
     */
    IWorld getWorld();

    /**
     * @return The {@link EntityPlayer} who would perform a build. This is optional and therefore may be null.
     */
    @Nullable
    EntityPlayer getBuildingPlayer();
}
