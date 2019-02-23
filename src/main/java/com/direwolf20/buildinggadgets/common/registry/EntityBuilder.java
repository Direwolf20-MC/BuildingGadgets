package com.direwolf20.buildinggadgets.common.registry;

import com.direwolf20.buildinggadgets.common.entities.BlockBuildEntity;
import com.google.common.base.Preconditions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EntityType.Builder;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

import java.util.Objects;
import java.util.function.Function;

public class EntityBuilder<T extends Entity> extends RegistryObjectBuilder<EntityType<?>,Builder<T>> {
    private Class<T> entityClass;
    private IRenderFactory<? super T> renderFactory;
    public EntityBuilder(String registryName) {
        super(registryName);
    }

    public EntityBuilder(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    public EntityBuilder<T> factory(Function<Builder<T>, EntityType<?>> factory) {
        return (EntityBuilder<T>) super.factory(factory);
    }

    @Override
    public EntityBuilder<T> builder(Builder<T> builder) {
        return (EntityBuilder<T>) super.builder(builder);
    }

    public EntityBuilder<T> renderer(IRenderFactory<? super T> renderFactory) {
        this.renderFactory = Objects.requireNonNull(renderFactory);
        return this;
    }

    @Override
    public EntityType<?> construct() {
        Preconditions.checkState(renderFactory!=null,"Cannot construct an Entity without an Renderer!");
        EntityType<?> type = super.construct();
        @SuppressWarnings("unchecked") //I hope this is safe an people don't do things they shouldn't
        Class<T> clazz = (Class<T>)Objects.requireNonNull(type.getEntityClass());
        entityClass = clazz;
        return type;
    }

    public Class<T> getEntityClass() {
        Preconditions.checkState(entityClass!=null,"Cannot request Entity class before type has been constructed!");
        return entityClass;
    }

    public IRenderFactory<? super T> getRenderFactory() {
        Preconditions.checkState(renderFactory!=null,"Expected Renderer to be present before EntityType has been constructed!");
        return renderFactory;
    }
}
