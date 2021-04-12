package com.direwolf20.buildinggadgets.common.construction.modes;

import com.direwolf20.buildinggadgets.common.construction.ModeUseContext;
import com.direwolf20.buildinggadgets.common.construction.XYZ;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class CircleMode extends Mode {
    private final boolean filled;

    public CircleMode(boolean isExchanging, boolean filled) {
        super(filled ? "filled_circle" : "circle_outline", isExchanging);
        this.filled = filled;
    }

    @Override
    List<BlockPos> collect(ModeUseContext context, PlayerEntity player, BlockPos start) {
        List<BlockPos> coordinates = new ArrayList<>();
        Direction direction = context.getHitSide();

        int x = context.getRange(); // radius
        int y = 0;
        int correction = 1 - x;

        while(x >= y) {
            if (this.filled) {
                for (int i = -x; i <= x; i++) {
                    coordinates.add(this.getBlockPosition(start, i, y, direction));
                    if (y != 0)
                        coordinates.add(this.getBlockPosition(start, i, -y, direction));
                }
                if (x != y) { // if x == u this loop is the same as the one above.
                    for (int i = -y; i <= y; i++) {
                        coordinates.add(this.getBlockPosition(start, i, x, direction));
                        if (x != 0)
                            coordinates.add(this.getBlockPosition(start, i, -x, direction));
                    }
                }
            } else {
                coordinates.add(this.getBlockPosition(start, x, y, direction));
                coordinates.add(this.getBlockPosition(start, -x, y, direction));
                coordinates.add(this.getBlockPosition(start, -y, -x, direction));
                coordinates.add(this.getBlockPosition(start, x, -y, direction));
                if (x != y) { // if x == y these four are the same as below.
                    coordinates.add(this.getBlockPosition(start, y, x, direction));
                    coordinates.add(this.getBlockPosition(start, -y, x, direction));
                    coordinates.add(this.getBlockPosition(start, -x, -y, direction));
                    coordinates.add(this.getBlockPosition(start, y, -x, direction));
                }
            }
            y++;
            x -= correction < 0 ? 0 : 1;
            correction += 2 * (correction < 0 ? (y + 1) :  (y -x + 1));
        }

        return coordinates;
    }

    /**
     * Get the position to place a block based on directionality
     */
    private BlockPos getBlockPosition(BlockPos start, int directionalX, int directionalY, Direction direction) {
        if (XYZ.isAxisY(direction))
            return start.offset(directionalX, 0, directionalY);
        if (XYZ.isAxisZ(direction))
            return start.offset(directionalX, directionalY, 0);
        return start.offset(0, directionalX, directionalY);
    }
}
