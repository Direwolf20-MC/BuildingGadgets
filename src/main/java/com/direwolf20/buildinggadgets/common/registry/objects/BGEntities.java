package com.direwolf20.buildinggadgets.common.registry.objects;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.entities.BlockBuildEntity;
import com.direwolf20.buildinggadgets.common.entities.ConstructionBlockEntity;
import com.direwolf20.buildinggadgets.common.registry.RegistryContainer;
import com.direwolf20.buildinggadgets.common.registry.RegistryObjectBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EntityType.Builder;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(BuildingGadgets.MODID)
@EventBusSubscriber(modid = BuildingGadgets.MODID, bus = Bus.MOD)
public class BGEntities {
    private static final RegistryContainer<EntityType<?>, RegistryObjectBuilder<EntityType<?>, Builder<?>>> container = new RegistryContainer<>();

    @ObjectHolder("build_block_entity")
    public static EntityType<?> BUILD_BLOCK;
    @ObjectHolder("construction_block_entity")
    public static EntityType<?> CONSTRUCTION_BLOCK;

    public static void init() {
        container.add(new RegistryObjectBuilder<EntityType<?>, Builder<?>>(new ResourceLocation(BuildingGadgets.MODID, "build_block_entity"))
                .builder(Builder.create(BlockBuildEntity.class, BlockBuildEntity::new).tracker(64, 1, true))
                .factory(b -> b.build("")));
        container.add(new RegistryObjectBuilder<EntityType<?>, Builder<?>>(new ResourceLocation(BuildingGadgets.MODID, "construction_block_entity"))
                .builder(Builder.create(ConstructionBlockEntity.class, ConstructionBlockEntity::new).tracker(64, 1, true))
                .factory(b -> b.build("")));
    }

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityType<?>> event) {
        container.register(event);
    }

    public static void cleanup() {
        container.clear();
    }
}
