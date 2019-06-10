package com.direwolf20.buildinggadgets.common.registry.objects;

import com.direwolf20.buildinggadgets.common.entities.BlockBuildEntity;
import com.direwolf20.buildinggadgets.common.entities.BlockBuildEntityRender;
import com.direwolf20.buildinggadgets.common.entities.ConstructionBlockEntity;
import com.direwolf20.buildinggadgets.common.entities.ConstructionBlockEntityRender;
import com.direwolf20.buildinggadgets.common.registry.entity.EntityBuilder;
import com.direwolf20.buildinggadgets.common.registry.entity.EntityRegistryContainer;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import com.direwolf20.buildinggadgets.common.util.ref.Reference.EntityReference;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EntityType.Builder;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Reference.MODID)
@EventBusSubscriber(modid = Reference.MODID, bus = Bus.MOD)
public class BGEntities {
    private static final EntityRegistryContainer container = new EntityRegistryContainer();

    @ObjectHolder(EntityReference.BUILD_BLOCK_ENTITY)
    public static EntityType<?> BUILD_BLOCK;
    @ObjectHolder(EntityReference.CONSTRUCTION_BLOCK_ENTITY)
    public static EntityType<?> CONSTRUCTION_BLOCK;

    public static void init() {
        container.add(new EntityBuilder<BlockBuildEntity>(EntityReference.BUILD_BLOCK_ENTITY_RL)
                .builder(Builder.<BlockBuildEntity>create(BlockBuildEntity::new, EntityClassification.MISC)
                        .setTrackingRange(64)
                        .setUpdateInterval(1)
                        .setShouldReceiveVelocityUpdates(false))
                .renderer(BlockBuildEntityRender::new)
                .factory(b -> b.build("")));
        container.add(new EntityBuilder<ConstructionBlockEntity>(EntityReference.CONSTRUCTION_BLOCK_ENTITY_RL)
                .builder(Builder.<ConstructionBlockEntity>create(ConstructionBlockEntity::new, EntityClassification.MISC)
                        .setTrackingRange(64)
                        .setUpdateInterval(1)
                        .setShouldReceiveVelocityUpdates(false))
                .renderer(ConstructionBlockEntityRender::new)
                .factory(b -> b.build("")));
    }

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityType<?>> event) {
        container.register(event);
    }

    static void cleanup() {
        container.clear();
    }

    static void clientInit() {
        container.clientInit();
    }
}
