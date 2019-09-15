package com.direwolf20.buildinggadgets.common.registry;

import com.direwolf20.buildinggadgets.common.entities.ConstructionBlockEntity;
import com.direwolf20.buildinggadgets.common.entities.ConstructionBlockEntityRender;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import com.direwolf20.buildinggadgets.common.util.ref.Reference.EntityReference;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ObjectHolder;

/**
 * @implNote This class does not require a builder like {@link OurItems} or {@link OurBlocks}
 *           as we only have a single entity so far with no plans to change this any time
 *           soon.
 */
@EventBusSubscriber(modid = Reference.MODID, bus = Bus.MOD)
public class OurEntities {

    @ObjectHolder(EntityReference.CONSTRUCTION_BLOCK_ENTITY)
    public static EntityType<ConstructionBlockEntity> CONSTRUCTION_BLOCK;

    /**
     * Our only Entity is the one used to show the animation of a block
     * being placed or removed.
     */
    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityType<?>> event) {
        event.getRegistry().register(
                EntityType.Builder.<ConstructionBlockEntity>create(ConstructionBlockEntity::new, EntityClassification.MISC)
                        .setTrackingRange(64)
                        .setUpdateInterval(1)
                        .setShouldReceiveVelocityUpdates(false)
                        .setCustomClientFactory(((spawnEntity, world) -> new ConstructionBlockEntity(CONSTRUCTION_BLOCK, world)))
                        .build("")
                        .setRegistryName(EntityReference.CONSTRUCTION_BLOCK_ENTITY_RL)
        );
    }

    /**
     * Called from the runWhenOn(Dist.CLIENT...) method somewhere else.
     * This is a client side only render.
     */
    public static void registerModels() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(event -> RenderingRegistry.registerEntityRenderingHandler(ConstructionBlockEntity.class, ConstructionBlockEntityRender::new));
    }
}
