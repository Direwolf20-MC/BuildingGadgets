package com.direwolf20.buildinggadgets.common.tools;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;

import java.util.BitSet;

public class PlanerBlockPosSet {
//
//    private final BitSet delegate;
//    private final Axis ignoredAxis;
//
//    public PlanerBlockPosSet(Axis ignoredAxis) {
//        this.delegate = new BitSet();
//        this.ignoredAxis = ignoredAxis;
//    }
//
//    public boolean mark(BlockPos pos) {
//        int planer = toPlaner(pos);
//        delegate.set(planer);
//        return false;
//    }
//
//    @VisibleForTesting
//    private int toPlaner(BlockPos relative) {
//        switch (ignoredAxis) {
//            case X:
//                return relative.getY() * region.getZSize() + relative.getZ();
//            case Y:
//                return relative.getX() * region.getZSize() + relative.getZ();
//            case Z:
//                return relative.getY() * region.getXSize() + relative.getX();
//        }
//        throw new IllegalArgumentException();
//    }

}
