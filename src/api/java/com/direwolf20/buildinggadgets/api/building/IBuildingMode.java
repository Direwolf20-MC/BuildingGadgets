package com.direwolf20.buildinggadgets.api.building;

import com.direwolf20.buildinggadgets.api.capability.CapabilityBlockProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;

import java.util.function.BiPredicate;

/**
 * Represents a mode that can be used for building by some gadget.
 */
public interface IBuildingMode {

    /**
     * Iterator that supplies raw coordinates that haven't been filtered yet.
     */
    IPositionPlacementSequence computeCoordinates(EntityPlayer player, BlockPos hit, EnumFacing sideHit, ItemStack tool);

    /**
     * <p>Get the the block provider that can be accessed by using ItemStack capability system.</p>
     */
    default IBlockProvider getBlockProvider(ItemStack tool) {
        LazyOptional<IBlockProvider> capability = tool.getCapability(CapabilityBlockProvider.BLOCK_PROVIDER, null);
        return capability.orElse(CapabilityBlockProvider.getDefaultAirProvider());
    }

    BiPredicate<BlockPos, IBlockState> createValidatorFor(World world, ItemStack tool, EntityPlayer player, BlockPos initial);

    /**
     * @see Context#getPositionSequence()
     */
    default Context createExecutionContext(EntityPlayer player, BlockPos hit, EnumFacing sideHit, ItemStack tool) {
        return new Context(computeCoordinates(player, hit, sideHit, tool), getBlockProvider(tool), this::createValidatorFor);
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
