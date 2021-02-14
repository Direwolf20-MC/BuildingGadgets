package com.direwolf20.buildinggadgets.common.building.modes;

import com.direwolf20.buildinggadgets.api.BuildingGadgetsAPI;
import com.direwolf20.buildinggadgets.api.modes.IModeUiEntry;
import com.direwolf20.buildinggadgets.common.building.AbstractMode;
import com.direwolf20.buildinggadgets.common.building.BuildingActionContext;
import com.direwolf20.buildinggadgets.common.building.BuildingContext;
import com.direwolf20.buildinggadgets.common.building.ModeUiEntry;
import com.direwolf20.buildinggadgets.common.tainted.building.Region;
import com.direwolf20.buildinggadgets.common.tainted.building.placement.ConnectedSurface;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SurfaceMode extends AbstractMode {
    public static final ResourceLocation name = new ResourceLocation(BuildingGadgetsAPI.MODID, "surface");
    private static final ModeUiEntry entry = new ModeUiEntry("surface", name);

    public SurfaceMode(boolean isExchanging) { super(isExchanging); }

    @Override
    public List<BlockPos> collect(BuildingContext context, PlayerEntity player, BlockPos start) {
        int bound = context.getRange() / 2;

        // Grow the area. getXOffset will invert some math for us
        Region area = new Region(start).expand(
                bound * (1 - Math.abs(context.getHitSide().getXOffset())),
                bound * (1 - Math.abs(context.getHitSide().getYOffset())),
                bound * (1 - Math.abs(context.getHitSide().getZOffset()))
        );

        if (!context.isConnected()) {
            return area.stream().map(BlockPos::toImmutable).collect(Collectors.toList());
        }

        List<BlockPos> coords = new ArrayList<>();

        ConnectedSurface.create(area, context.getWorld(), pos -> isExchanging() ? pos : pos.offset(context.getHitSide().getOpposite()), start, context.getHitSide().getOpposite(), context.getRange(), context.isFuzzy())
                .spliterator()
                .forEachRemaining(coords::add);

        return coords;
    }

    @Override
    public boolean validate(BuildingActionContext actionContext) {
        // Do our default checks, then do our more complex fuzzy aware checks.
        boolean topRow = super.validate(actionContext);
        if( this.isExchanging() )
            return topRow;

        final BuildingContext context = actionContext.getContext();
        BlockState startState = context.getWorldState(context.getStartPos());
        if( context.isFuzzy() )
            return topRow && !context.getWorld().isAirBlock(actionContext.getCurrentPos().offset(context.getHitSide().getOpposite()));

        return topRow && context.getWorld().getBlockState(actionContext.getCurrentPos().offset(context.getHitSide().getOpposite())) == startState;
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
