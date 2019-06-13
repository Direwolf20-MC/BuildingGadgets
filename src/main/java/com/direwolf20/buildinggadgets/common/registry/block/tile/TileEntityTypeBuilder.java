package com.direwolf20.buildinggadgets.common.registry.block.tile;

import com.direwolf20.buildinggadgets.common.util.UnnamedCompat;
import com.mojang.datafixers.types.Type;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

public final class TileEntityTypeBuilder<T extends TileEntity> {
    private Supplier<T> factory;
    private Set<ResourceLocation> validBlocks;
    private Type<?> dataFixer;

    public TileEntityTypeBuilder(Supplier<T> factory) {
        this.factory = Objects.requireNonNull(factory);
        this.validBlocks = new HashSet<>();
    }

    public TileEntityTypeBuilder<T> addBlocks(ResourceLocation... location) {
        this.validBlocks.addAll(Arrays.asList(location));
        return this;
    }

    public TileEntityTypeBuilder<T> dataFixer(Type<?> dataFixer) {
        this.dataFixer = Objects.requireNonNull(dataFixer);
        return this;
    }

    public TileEntityType<T> build() {
        Block[] resolvedBlocks = new Block[validBlocks.size()];
        int i = 0;
        for (ResourceLocation location : validBlocks) {
            resolvedBlocks[i] = ForgeRegistries.BLOCKS.getValue(location);
            ++ i;
        }
        return UnnamedCompat.TileEntityType.builder(factory, resolvedBlocks).build(dataFixer);
    }
}
