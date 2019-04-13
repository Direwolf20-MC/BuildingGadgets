package com.direwolf20.buildinggadgets.client.utils;

import com.google.common.base.Preconditions;

public final class AlignmentUtil {

    private AlignmentUtil() {
    }

    /**
     * @return Left x value if the box is be aligned to the right.
     */
    public static int getXForAlignedRight(int width, int rightX) {
        return rightX - width;
    }

    /**
     * @return Left x value if the box is aligned in the middle.
     */
    public static int getXForAlignedCenter(int width, int leftX, int rightX) {
        Preconditions.checkArgument(leftX < rightX);
        return leftX + (rightX - leftX) / 2 - width / 2;
    }

    /**
     * @return Top y value if the box is aligned to the bottom.
     */
    public static int getYForAlignedBottom(int height, int bottomY) {
        return bottomY - height;
    }

    /**
     * @return Top y value if the box is aligned in the center.
     */
    public static int getYForAlignedCenter(int height, int topY, int bottomY) {
        Preconditions.checkArgument(bottomY > topY);
        return topY + (bottomY - topY) / 2 - height / 2;
    }

    /**
     * @param x      x coordinate of the point
     * @param y      y coordinate of the point
     * @param bx     left x coordinate of the box
     * @param by     top x coordinate of the box
     * @param width  width of the box
     * @param height height of the box
     * @return Whether the point is inside the box or not.
     * @implSpec Top left coordinate will be tested exclusively ({@code >} and {@code <}), and bottom right coordinates
     * will be tested inclusively ({@code >=} and {@code <=}).
     */
    public static boolean isPointInBox(int x, int y, int bx, int by, int width, int height) {
        return x > bx &&
                y > by &&
                x <= bx + width &&
                y <= by + height;
    }

}