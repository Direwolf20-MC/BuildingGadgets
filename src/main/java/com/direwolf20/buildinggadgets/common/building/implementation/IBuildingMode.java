package com.direwolf20.buildinggadgets.common.building.implementation;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

/**
 * Represents a mode that can be used by some gadget,
 */
public interface IBuildingMode {

    //TODO move to iterator system
    List<BlockPos> computeCoordinates(World world, EntityPlayer player, BlockPos hit, EnumFacing sideHit, ItemStack tool);

    //TODO move to IBuildMode so that these can be applied
//    /**
//     * Registry name used for mapping.
//     */
//    //TODO implement mode registry system
//    ResourceLocation getRegistryName();
//
//    /**
//     * Translation key that vanilla's {@link I18n} can recognize.
//     */
//    String getTranslationKey();
//
//    /**
//     * Locale translated from {@code I18n.format(getTranslationKey()}.
//     * <p>Implementations can override this method to use formatting features.</p>
//     */
//    default String getLocale() {
//        return I18n.format(getTranslationKey());
//    }

}
