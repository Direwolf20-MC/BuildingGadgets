package com.direwolf20.buildinggadgets.common.building.modes;

import com.direwolf20.buildinggadgets.common.building.BlockData;
import com.direwolf20.buildinggadgets.common.building.IBlockProvider;
import com.direwolf20.buildinggadgets.common.building.placement.IPositionPlacementSequence;
import com.direwolf20.buildinggadgets.common.building.view.BuildContext;
import com.direwolf20.buildinggadgets.common.building.view.SimpleBuildSequence;
import com.direwolf20.buildinggadgets.common.capability.CapabilityBlockProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;
import java.util.function.BiPredicate;

/**
 * Represents a mode that can be used for building by some gadget.
 */
public interface IBuildingMode {

    /**
     * Iterator that supplies raw coordinates that haven't been filtered yet.
     *
     * @param player      target player
     * @param hit         BlockPos hit
     * @param sideHit     Side Hit
     * @param tool        Current Gadget
     *
     * @return {@link IPositionPlacementSequence}
     */
    IPositionPlacementSequence computeCoordinates(PlayerEntity player, BlockPos hit, Direction sideHit, ItemStack tool);

    /**
     * <p>Get the the block provider that can be accessed by using ItemStack capability system.</p>
     * @param tool      Current gadget
     *
     * @return {@link IBlockProvider}
     */
    default IBlockProvider getBlockProvider(ItemStack tool) {
        LazyOptional<IBlockProvider> capability = tool.getCapability(CapabilityBlockProvider.BLOCK_PROVIDER, null);
        return capability.orElse(CapabilityBlockProvider.getDefaultAirProvider());
    }

    BiPredicate<BlockPos, BlockData> createValidatorFor(IWorld world, ItemStack tool, PlayerEntity player, BlockPos initial);

    /**
     * @see SimpleBuildSequence#getPositionSequence()
     *
     * @param player    target player
     * @param hit       BlockPos hit
     * @param sideHit   Side Hit
     * @param tool      Current Gadget
     * @param initial   Initial BlockPos
     *
     * @return {@link BuildContext}
     */
    default SimpleBuildSequence createExecutionContext(PlayerEntity player, BlockPos hit, Direction sideHit, ItemStack tool, @Nullable BlockPos initial) {
        return new SimpleBuildSequence(computeCoordinates(player, hit, sideHit, tool), getBlockProvider(tool), this::createValidatorFor,
                BuildContext.builder().buildingPlayer(player).usedStack(tool).build(), initial);
    }

    /**
     * Registry name used for mapping.
     *
     * @return {@link ResourceLocation}
     */
    //TODO implement mode registry system
    ResourceLocation getRegistryName();

    /**
     * A localized user-readable textual representation of this {@code IBuildingMode}
     * <p>Implementations should override this method and use formatting features.</p>
     *
     * @return {@link String}
     */
    String getLocalizedName();

}
