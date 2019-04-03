package com.direwolf20.buildinggadgets.client.utils;

import com.google.common.base.Preconditions;

public final class AlignmentUtil {

    private AlignmentUtil() { }

    /**
     * @return Left x value if the box is be aligned to the right
     */
    public static int getXForAlignedRight(int width, int rightX) {
        return rightX - width;
    }

    /**
     * @return Left x value if the box is aligned in the middle
     */
    public static int getXForAlignedCenter(int width, int leftX, int rightX) {
        Preconditions.checkArgument(leftX < rightX);
        return leftX + (rightX - leftX) / 2 - width / 2;
    }

    /**
     * @return Top y value if the box is aligned to the bottom
     */
    public static int getYForAlignedBottom(int height, int bottomY) {
        return bottomY - height;
    }

    /**
     * @return Top y value if the box is aligned in the center
     */
    public static int getYForAlignedCenter(int height, int topY, int bottomY) {
        Preconditions.checkArgument(bottomY > topY);
        return topY + (bottomY - topY) / 2 - height / 2;
    }

}