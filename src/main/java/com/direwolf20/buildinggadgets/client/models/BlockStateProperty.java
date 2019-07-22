package com.direwolf20.buildinggadgets.client.models;

import com.direwolf20.buildinggadgets.common.util.blocks.BlockStateWrapper;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.state.Property;

import java.util.Collection;
import java.util.Optional;

public class BlockStateProperty extends Property<BlockStateWrapper> {
    private final Block theBlock;
    private ImmutableList<BlockStateWrapper> validValues = null;

    public BlockStateProperty(String name, Block block) {
        super(name, BlockStateWrapper.class);
        this.theBlock = block;
    }

    @Override
    public Collection<BlockStateWrapper> getAllowedValues() {
        if (validValues == null) {
            validValues = getBlock().getStateContainer().getValidStates()
                    .stream().map(BlockStateWrapper::new).collect(ImmutableList.toImmutableList());
        }
        return validValues;
    }

    public Block getBlock() {
        return theBlock;
    }

    @Override
    public Optional<BlockStateWrapper> parseValue(String value) {
        return Optional.empty();
    }

    /**
     * Get the name for the given value.
     *
     * @param value
     */
    @Override
    public String getName(BlockStateWrapper value) {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlockStateProperty)) return false;
        if (!super.equals(o)) return false;

        BlockStateProperty that = (BlockStateProperty) o;

        return theBlock.equals(that.theBlock);
    }

    @Override
    public int computeHashCode() {
        return 31 * super.computeHashCode() + theBlock.hashCode();
    }

    @Override
    public String toString() {
        return "BlockStateProperty{" +
                "block=" + theBlock.getRegistryName() +
                '}';
    }
}
