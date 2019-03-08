package com.direwolf20.buildinggadgets.api.template;

import com.direwolf20.buildinggadgets.api.abstraction.IUniqueItem;
import com.google.common.collect.Multiset;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public interface ITemplate extends Iterable<PlacementTarget> {
    public Stream<PlacementTarget> stream();

    @Nullable
    public ITemplateTransaction startTransaction();

    public boolean translateTo(BlockPos pos);

    public Multiset<IUniqueItem> getRequiredItems();
}
