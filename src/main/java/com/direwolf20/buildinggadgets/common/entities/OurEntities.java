package com.direwolf20.buildinggadgets.common.entities;

import com.direwolf20.buildinggadgets.common.blocks.OurBlocks;
import com.direwolf20.buildinggadgets.common.items.OurItems;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import com.direwolf20.buildinggadgets.common.util.ref.Reference.EntityReference;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

/**
 * @implNote This class does not require a builder like {@link OurItems} or {@link OurBlocks}
 *           as we only have a single entity so far with no plans to change this any time
 *           soon.
 */
@EventBusSubscriber(modid = Reference.MODID, bus = Bus.MOD)
public class OurEntities {
    private OurEntities() {}

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
                        .setCustomClientFactory(((spawnEntity, world) -> new ConstructionBlockEntity(ConstructionBlockEntity.TYPE, world)))
                        .build("")
                        .setRegistryName(EntityReference.CONSTRUCTION_BLOCK_ENTITY_RL)
        );
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent evt) {
        RenderingRegistry.registerEntityRenderingHandler(ConstructionBlockEntity.TYPE, ConstructionBlockEntityRender::new);
    }
}
