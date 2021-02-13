package com.direwolf20.buildinggadgets.common.building.modes;

import com.direwolf20.buildinggadgets.api.BuildingGadgetsAPI;
import com.direwolf20.buildinggadgets.api.modes.IModeEntry;
import com.direwolf20.buildinggadgets.common.building.AbstractMode;
import com.direwolf20.buildinggadgets.common.building.BuildingContext;
import com.direwolf20.buildinggadgets.common.building.ModeEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class EmptyMode extends AbstractMode {
    public static final ResourceLocation name = new ResourceLocation(BuildingGadgetsAPI.MODID, "empty");
    private static final ModeEntry entry = new ModeEntry("empty", name);

    public EmptyMode() { super(false); }

    @Override
    public List<BlockPos> collect(BuildingContext context, PlayerEntity player, BlockPos start) {
        return new ArrayList<>();
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
