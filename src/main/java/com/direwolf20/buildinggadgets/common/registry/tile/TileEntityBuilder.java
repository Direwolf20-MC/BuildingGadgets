package com.direwolf20.buildinggadgets.common.registry.tile;

import com.direwolf20.buildinggadgets.common.registry.RegistryObjectBuilder;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import java.util.Objects;
import java.util.function.Function;

public final class TileEntityBuilder<T extends TileEntity> extends RegistryObjectBuilder<TileEntityType<?>, TileEntityType.Builder<T>> {
    private Class<T> tileClass;
    private TileEntityRenderer<? super T> renderer;

    public TileEntityBuilder(String registryName) {
        super(registryName);
    }

    public TileEntityBuilder(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    public TileEntityBuilder<T> factory(Function<TileEntityType.Builder<T>, TileEntityType<?>> factory) {
        return (TileEntityBuilder<T>) super.factory(factory);
    }

    @Override
    public TileEntityBuilder<T> builder(TileEntityType.Builder<T> builder) {
        return (TileEntityBuilder<T>) super.builder(builder);
    }

    public TileEntityBuilder<T> renderer(Class<T> clazz, TileEntityRenderer<? super T> renderer) {
        this.tileClass = Objects.requireNonNull(clazz);
        this.renderer = Objects.requireNonNull(renderer);
        return this;
    }

    private Class<T> getTileClass() {
        return tileClass;
    }

    private TileEntityRenderer<? super T> getRenderer() {
        return renderer;
    }

    boolean hasRenderer() {
        return getRenderer() != null;
    }

    void registerRenderer() {
        ClientRegistry.bindTileEntitySpecialRenderer(getTileClass(), getRenderer());
    }
}
