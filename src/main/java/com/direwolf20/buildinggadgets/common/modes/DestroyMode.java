package com.direwolf20.buildinggadgets.common.modes;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.List;

public class DestroyMode extends Mode {
    public DestroyMode() {
        super("destroy", false);
    }

    @Override
    List<BlockPos> collect(ModeUseContext context, PlayerEntity player, BlockPos start) {
        return Collections.emptyList();
    }

    @Override
    public boolean validator(PlayerEntity player, BlockPos pos, ModeUseContext context) {
        return super.validator(player, pos, context);
    }
}
