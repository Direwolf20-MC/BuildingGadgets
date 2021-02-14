package com.direwolf20.buildinggadgets.common.building.modes;

import com.direwolf20.buildinggadgets.api.BuildingGadgetsAPI;
import com.direwolf20.buildinggadgets.api.modes.IModeUiEntry;
import com.direwolf20.buildinggadgets.common.building.AbstractMode;
import com.direwolf20.buildinggadgets.common.building.BuildingContext;
import com.direwolf20.buildinggadgets.common.building.ModeUiEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class StairMode extends AbstractMode {
    private static final ResourceLocation name = new ResourceLocation(BuildingGadgetsAPI.MODID, "stairs");
    private static final ModeUiEntry entry = new ModeUiEntry("stairs", name);

    public StairMode() { super(false); }

    @Override
    public List<BlockPos> collect(BuildingContext context, PlayerEntity player, BlockPos start) {
        List<BlockPos> coordinates = new ArrayList<>();

        Direction side = context.getHitSide();
        if (XYZ.isAxisY(side))
            side = player.getHorizontalFacing().getOpposite();

        XYZ facingXYZ = XYZ.fromFacing(side);
        for( int i = 0; i < context.getRange(); i ++ ) {
            // Check to see if we should build up or down from the player
            int tmp = start.getY() > player.getPosY() + 1 ? (i + 1) * -1 : i;

            if( facingXYZ == XYZ.X )
                coordinates.add(new BlockPos(start.getX() + (tmp * (side == Direction.EAST ? -1 : 1)), start.getY() + tmp, start.getZ()));

            if( facingXYZ == XYZ.Z )
                coordinates.add(new BlockPos(start.getX(), start.getY() + tmp, start.getZ() + (tmp * (side == Direction.SOUTH ? -1 : 1))));
        }

        return coordinates;
    }

    @Override
    public BlockPos withOffset(BlockPos pos, Direction side, boolean placeOnTop) {
        // Is top / bottom? Do as normal. Not? then place on top or inside :D
        return XYZ.isAxisY(side) ? super.withOffset(pos, side, placeOnTop) : (placeOnTop ? pos.offset(Direction.UP) : pos);
    }

    @Override
    public ResourceLocation getRegistryName() {
        return name;
    }

    @Override
    public IModeUiEntry getUiEntry() {
        return entry;
    }
}
