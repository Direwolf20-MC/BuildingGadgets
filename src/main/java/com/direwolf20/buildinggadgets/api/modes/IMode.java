package com.direwolf20.buildinggadgets.api.modes;

import com.direwolf20.buildinggadgets.common.building.AbstractMode;
import com.direwolf20.buildinggadgets.common.building.BuildingContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.util.List;

/**
 * This represents the purest sense of a "Mode". We actually use an Abstract implementation to base all of our
 * native modes {@link AbstractMode} which serves us well. Feel free to use it over this interface if you do
 * not need complete control.
 */
public interface IMode {
    /**
     * A list of blocks that are expected have to been pre-sorted pre-filters and pre-collected.
     * You can use the {@link AbstractMode} for more information on how we use this method.
     */
    List<BlockPos> getCollection(BuildingContext context, PlayerEntity player);

    /**
     * Used for identification NOT translation
     */
    ResourceLocation identifier();

    /**
     * The modes active interface entry
     */
    IModeEntry entry();
}
