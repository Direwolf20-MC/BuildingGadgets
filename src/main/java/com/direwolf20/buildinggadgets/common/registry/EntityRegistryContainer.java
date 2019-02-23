package com.direwolf20.buildinggadgets.common.registry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class EntityRegistryContainer extends ClientConstructContainer<EntityType<?>,EntityBuilder<?>> {
    @Override
    public void clientInit() {
        MinecraftForge.EVENT_BUS.addListener(this::registerModels);
    }

    @Override
    public void add(EntityBuilder builder) {
        super.add(builder);
    }

    protected void registerModels(ModelRegistryEvent event) {
        for (EntityBuilder<?> builder:getBuilders()) {
            registerRenderer(builder.getEntityClass(),builder.getRenderFactory());
        }
    }

    @SuppressWarnings("unchecked") //should be fine, as the builder should have appropriate types...
    private <T extends Entity> void registerRenderer(Class<?> clazz, IRenderFactory<?> fac) {
        RenderingRegistry.registerEntityRenderingHandler((Class<T>)clazz,(IRenderFactory<? super T>)fac);
    }
}
