package com.direwolf20.buildinggadgets.common.building;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.BiPredicate;

/**
 * Represents a mode that can be used by some gadget,
 */
public interface IBuildingMode {

    /**
     * Iterator that supplies raw coordinates that hasn't been filtered yet.
     */
    IPlacementSequence computeCoordinates(EntityPlayer player, BlockPos hit, EnumFacing sideHit, ItemStack tool);

    /**
     * <p>Get the the block provider that can be accessed by using ItemStack capability system.</p>
     */
    default IBlockProvider getBlockProvider(ItemStack tool) {
        IBlockProvider capability = tool.getCapability(CapabilityBlockProvider.BLOCK_PROVIDER, null);
        if (capability != null) {
            return capability;
        }
        return CapabilityBlockProvider.DEFAULT_AIR_PROVIDER;
    }

    BiPredicate<BlockPos, IBlockState> createValidatorFor(World world);

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
     * Translation key that vanilla's {@link I18n} can recognize.
     */
    default String getTranslationKey() {
        return "modes." + getRegistryName();
    }

    /**
     * Locale translated from {@code I18n.format(getTranslationKey()}.
     * <p>Implementations may override this method to use formatting features.</p>
     */
    default String getLocalized() {
        return I18n.format(getTranslationKey());
    }

}
