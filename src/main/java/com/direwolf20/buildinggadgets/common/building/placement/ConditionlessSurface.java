package com.direwolf20.buildinggadgets.common.building.placement;

import com.direwolf20.buildinggadgets.common.building.IPlacementSequence;
import com.direwolf20.buildinggadgets.common.building.Region;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.AbstractIterator;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;
import java.util.Iterator;

public final class ConditionlessSurface implements IPlacementSequence {

    /**
     * @param world  Block access for searching reference
     * @param center Center of the region for reference
     * @param side   Facing to offset from the {@code center} to get to the center of the region to search
     */
    public static ConditionlessSurface create(IBlockAccess world, BlockPos center, EnumFacing side, int range, boolean fuzzy) {
        return new ConditionlessSurface(world, center, side, range, fuzzy);
    }

    private final IBlockAccess world;
    private final IBlockState selectedBase;
    private final Region searchingRegion;
    private final EnumFacing side;
    private final boolean fuzzy;

    @VisibleForTesting
    private ConditionlessSurface(IBlockAccess world, BlockPos center, EnumFacing side, int range, boolean fuzzy) {
        this.world = world;
        this.selectedBase = world.getBlockState(center);
        this.searchingRegion = Wall.clickedSide(center.offset(side), side, range).getBoundingBox();
        this.side = side;
        this.fuzzy = fuzzy;
    }

    private ConditionlessSurface(IBlockAccess world, IBlockState selectedBase, Region searchingRegion, EnumFacing side, boolean fuzzy) {
        this.world = world;
        this.selectedBase = selectedBase;
        this.searchingRegion = searchingRegion;
        this.side = side;
        this.fuzzy = fuzzy;
    }

    @Override
    public Region getBoundingBox() {
        return searchingRegion;
    }

    /**
     * {@inheritDoc}<br>
     * <b>inaccurate representation (case 2)</b>:
     */
    @Override
    public boolean mayContain(int x, int y, int z) {
        return searchingRegion.contains(x, y, z);
    }

    /**
     * @deprecated ConditionlessSurface should be immutable, so this is not needed
     */
    @Deprecated
    @Override
    public IPlacementSequence copy() {
        return new ConditionlessSurface(world, selectedBase, searchingRegion, side, fuzzy);
    }

    @Nonnull
    @Override
    public Iterator<BlockPos> iterator() {
        return new AbstractIterator<BlockPos>() {
            private final Iterator<BlockPos> it = searchingRegion.iterator();

            @Override
            protected BlockPos computeNext() {
                while (it.hasNext()) {
                    BlockPos pos = it.next();
                    IBlockState baseBlock = getBaseBlockFor(pos);
                    if ((fuzzy || baseBlock == selectedBase) && baseBlock.getMaterial() != Material.AIR) {
                        return pos;
                    }
                }
                return endOfData();
            }
        };
    }

    private IBlockState getBaseBlockFor(BlockPos pos) {
        return world.getBlockState(pos.offset(side.getOpposite()));
    }

}
