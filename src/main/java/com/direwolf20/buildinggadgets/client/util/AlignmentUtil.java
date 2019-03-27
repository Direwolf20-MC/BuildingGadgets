package com.direwolf20.buildinggadgets.client.util;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.google.common.base.Preconditions;
import net.minecraft.util.ResourceLocation;

public class AlignmentUtil {

    /**
     * Size in pixels of an vanilla slot background
     */
    public static final int SLOT_SIZE = 18;

    /**
     * {@link ResourceLocation} of vanilla item slot background.
     */
    public static final ResourceLocation SLOT_BACKGROUND = new ResourceLocation(BuildingGadgets.MODID, "textures/gui/item_slot_background.png");

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
