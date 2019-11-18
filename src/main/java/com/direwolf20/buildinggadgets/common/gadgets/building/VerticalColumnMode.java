package com.direwolf20.buildinggadgets.common.gadgets.building;

import com.direwolf20.buildinggadgets.common.gadgets.GadgetBuilding;
import com.direwolf20.buildinggadgets.common.gadgets.GadgetExchanger;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class VerticalColumnMode extends AbstractMode {
    public VerticalColumnMode(boolean isExchanging) {
        super(isExchanging);
    }

    @Override
    List<BlockPos> collect(EntityPlayer player, BlockPos playerPos, EnumFacing side, int range, BlockPos start) {
        List<BlockPos> coordinates = new ArrayList<>();

        // Exchanger... Come back to this
//        if (sideHit == EnumFacing.UP || sideHit == EnumFacing.DOWN) {
//            for (int x = boundZ * -1; x <= boundZ; x++) {
//                for (int z = boundX * -1; z <= boundX; z++) {
//                        coordinates.add(new BlockPos(startBlock.getX() - x, startBlock.getY(), startBlock.getZ() + z));
//                }
//            }
//        } else {
//            for (int y = bound; y >= bound * -1; y--) {
//                pos = new BlockPos(startBlock.getX(), startBlock.getY() - y, startBlock.getZ());
//                if (isReplaceable(world, pos, currentBlock, setBlock, fuzzyMode)) {
//                    coordinates.add(new BlockPos(startBlock.getX(), startBlock.getY() - y, startBlock.getZ()));
//                }
//            }
//        }

        // If up or down, full height from start block
        if( XYZ.isAxisY(side) ) {
            for (int i = 0; i < range; i++)
                coordinates.add(XYZ.extendPosSingle(i, start, side, XYZ.Y));

        // Else, half and half
        } else {
            int halfRange = range / 2;
            for (int i = -halfRange; i <= halfRange; i++)
                coordinates.add(XYZ.extendPosSingle(i, start, side, XYZ.Y));
        }

        return coordinates;
    }

    /**
     * We need to modify where the offset is for this mode as when looking at any
     * face that isn't up or down, we need to push the offset back into the block
     * and ignore placeOnTop as this mode does the action by default.
     */
    @Override
    public BlockPos withOffset(BlockPos pos, EnumFacing side, boolean placeOnTop) {
        return XYZ.isAxisY(side) ? super.withOffset(pos, side, placeOnTop) : pos;
    }
}

