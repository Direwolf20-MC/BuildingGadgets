package com.direwolf20.buildinggadgets.api.template;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public interface ITemplate extends Iterable<PlacementTarget>, INBTSerializable<NBTTagCompound> {
    public Stream<PlacementTarget> stream();

    @Nullable
    public ITemplateTransaction startTransaction();

    public void translateTo(BlockPos pos);
}
