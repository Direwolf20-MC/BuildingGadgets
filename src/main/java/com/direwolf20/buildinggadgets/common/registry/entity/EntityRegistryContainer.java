package com.direwolf20.buildinggadgets.common.registry.entity;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.registry.ClientConstructContainer;
import com.direwolf20.buildinggadgets.common.utils.ref.Reference;
import net.minecraft.entity.EntityType;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;

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
        BuildingGadgets.LOG.info("Registering {} EntityRenderer's", Reference.MODID);
        for (EntityBuilder<?> builder:getBuilders()) {
            builder.registerRenderer();
        }
        BuildingGadgets.LOG.info("Finished registering {} {} EntityRenderer's", getBuilders().size(), Reference.MODID);
    }
}
