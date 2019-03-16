package com.direwolf20.buildinggadgets.api.template.building;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.IWorld;

import javax.annotation.Nullable;

public interface IBuildContext {
    @Nullable
    IWorld getWorld();

    @Nullable
    EntityPlayer getBuildingPlayer();
}
