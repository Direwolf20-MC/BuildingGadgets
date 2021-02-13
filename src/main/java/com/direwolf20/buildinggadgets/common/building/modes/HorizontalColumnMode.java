package com.direwolf20.buildinggadgets.common.building.modes;

import com.direwolf20.buildinggadgets.api.BuildingGadgetsAPI;
import com.direwolf20.buildinggadgets.api.modes.IModeEntry;
import com.direwolf20.buildinggadgets.common.building.AbstractMode;
import com.direwolf20.buildinggadgets.common.building.BuildingContext;
import com.direwolf20.buildinggadgets.common.building.ModeEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class HorizontalColumnMode extends AbstractMode {
    private static final ResourceLocation name = new ResourceLocation(BuildingGadgetsAPI.MODID, "horizontal_column");
    private static final ModeEntry entry = new ModeEntry("horizontal_column", name);

    public HorizontalColumnMode(boolean isExchanging) {
        super(isExchanging);
    }

    @Override
    public List<BlockPos> collect(BuildingContext context, PlayerEntity player, BlockPos start) {
        List<BlockPos> coordinates = new ArrayList<>();

        Direction side = XYZ.isAxisY(context.getHitSide()) ? player.getHorizontalFacing() : context.getHitSide().getOpposite();
        if( !isExchanging() ) {
            for (int i = 0; i < context.getRange(); i++)
                coordinates.add(XYZ.extendPosSingle(i, start, side, XYZ.fromFacing(side)));
        } else {
            side = side.rotateY();
            int halfRange = context.getRange() / 2;
            for (int i = -halfRange; i <= halfRange; i++)
                coordinates.add(XYZ.extendPosSingle(i, start, side, XYZ.fromFacing(side)));
        }

        return coordinates;
    }

    @Override
    public ResourceLocation identifier() {
        return name;
    }

    @Override
    public IModeEntry entry() {
        return entry;
    }
}

