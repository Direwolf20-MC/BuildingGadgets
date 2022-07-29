package com.direwolf20.buildinggadgets.common.entities;

import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = Reference.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class OurEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_REGISTER = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Reference.MODID);

    public static final RegistryObject<EntityType<ConstructionBlockEntity>> CONSTRUCTION_BLOCK_ENTITY = ENTITY_REGISTER.register("construction_block_entity", () -> EntityType.Builder.<ConstructionBlockEntity>of(ConstructionBlockEntity::new, MobCategory.MISC)
            .setTrackingRange(64)
            .setUpdateInterval(1)
            .setShouldReceiveVelocityUpdates(false)
            .setCustomClientFactory(((spawnEntity, world) -> new ConstructionBlockEntity(world)))
            .build(""));

    @SubscribeEvent
    public static void registerModels(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(CONSTRUCTION_BLOCK_ENTITY.get(), ConstructionBlockEntityRender::new);
    }
}
