package com.direwolf20.buildinggadgets.common.building.modes;

import com.direwolf20.buildinggadgets.api.BuildingGadgetsAPI;
import com.direwolf20.buildinggadgets.api.modes.IModeUiEntry;
import com.direwolf20.buildinggadgets.common.building.AbstractMode;
import com.direwolf20.buildinggadgets.common.building.BuildingContext;
import com.direwolf20.buildinggadgets.common.building.ModeUiEntry;
import com.direwolf20.buildinggadgets.common.config.Config;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class BuildToMeMode extends AbstractMode {
    public static final ResourceLocation name = new ResourceLocation(BuildingGadgetsAPI.MODID, "build_to_me_mode");
    private static final ModeUiEntry entry = new ModeUiEntry("build_to_me", name);

    public BuildToMeMode() { super(false); }

    @Override
    public List<BlockPos> collect(BuildingContext context, PlayerEntity player, BlockPos start) {
        List<BlockPos> coordinates = new ArrayList<>();

        XYZ facingXYZ = XYZ.fromFacing(context.getHitSide());

        int startCoord = XYZ.posToXYZ(start, facingXYZ);
        int playerCoord = XYZ.posToXYZ(player.getPosition(), facingXYZ);

        // Clamp the value to the max range of the gadgets raytrace
        double difference = Math.max(0, Math.min(Config.GENERAL.rayTraceRange.get(), Math.abs(startCoord - playerCoord)));
        for( int i = 0; i < difference; i ++)
            coordinates.add(XYZ.extendPosSingle(i, start, context.getHitSide(), facingXYZ));

        return coordinates;
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
