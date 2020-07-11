package com.direwolf20.buildinggadgets.common.construction.modes;

import com.direwolf20.buildinggadgets.common.construction.ModeUseContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.List;

public class EmptyMode extends Mode {
    public EmptyMode(String name) {
        super(name, false);
    }

    @Override
    List<BlockPos> collect(ModeUseContext context, PlayerEntity player, BlockPos start) {
        return Collections.emptyList();
    }

    @Override
    public boolean validator(PlayerEntity player, BlockPos pos, ModeUseContext context) {
        return true;
    }
}
