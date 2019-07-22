package com.direwolf20.buildinggadgets.api.building.modes;

import com.direwolf20.buildinggadgets.api.building.BlockData;
import com.direwolf20.buildinggadgets.api.building.IBlockProvider;
import com.direwolf20.buildinggadgets.api.building.placement.IPositionPlacementSequence;
import com.direwolf20.buildinggadgets.api.building.view.SimpleBuildContext;
import com.direwolf20.buildinggadgets.api.building.view.SimpleBuildView;
import com.direwolf20.buildinggadgets.api.capability.CapabilityBlockProvider;
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
     */
    IPositionPlacementSequence computeCoordinates(PlayerEntity player, BlockPos hit, Direction sideHit, ItemStack tool);

    /**
     * <p>Get the the block provider that can be accessed by using ItemStack capability system.</p>
     */
    default IBlockProvider getBlockProvider(ItemStack tool) {
        LazyOptional<IBlockProvider> capability = tool.getCapability(CapabilityBlockProvider.BLOCK_PROVIDER, null);
        return capability.orElse(CapabilityBlockProvider.getDefaultAirProvider());
    }

    BiPredicate<BlockPos, BlockData> createValidatorFor(IWorld world, ItemStack tool, PlayerEntity player, BlockPos initial);

    /**
     * @see SimpleBuildView#getPositionSequence()
     */
    default SimpleBuildView createExecutionContext(PlayerEntity player, BlockPos hit, Direction sideHit, ItemStack tool, @Nullable BlockPos initial) {
        return new SimpleBuildView(computeCoordinates(player, hit, sideHit, tool), getBlockProvider(tool), this::createValidatorFor,
                SimpleBuildContext.builder().buildingPlayer(player).usedStack(tool).build(), initial);
    }

    /**
     * Registry name used for mapping.
     */
    //TODO implement mode registry system
    ResourceLocation getRegistryName();

    /**
     * A localized user-readable textual representation of this {@code IBuildingMode}
     * <p>Implementations should override this method and use formatting features.</p>
     */
    String getLocalizedName();

}
