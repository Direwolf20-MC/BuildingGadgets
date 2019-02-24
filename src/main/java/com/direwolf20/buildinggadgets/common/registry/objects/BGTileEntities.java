package com.direwolf20.buildinggadgets.common.registry.objects;

import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManagerTileEntity;
import com.direwolf20.buildinggadgets.common.registry.RegistryContainer;
import com.direwolf20.buildinggadgets.common.registry.RegistryObjectBuilder;
import com.direwolf20.buildinggadgets.common.utils.Reference;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.tileentity.TileEntityType.Builder;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Reference.MODID)
@EventBusSubscriber(modid = Reference.MODID, bus = Bus.MOD)
public class BGTileEntities {
    private static final RegistryContainer<TileEntityType<?>, RegistryObjectBuilder<TileEntityType<?>, Builder<?>>> container = new RegistryContainer<>();

    @ObjectHolder("construction_tile")
    public static TileEntityType<?> CONSTRUCTION_BLOCK_TYPE;
    @ObjectHolder("template_manager_tile")
    public static TileEntityType<?> TEMPLATE_MANAGER_TYPE;

    static void init() {
        container.add(new RegistryObjectBuilder<TileEntityType<?>, Builder<?>>(new ResourceLocation(Reference.MODID,"construction_tile"))
                .builder(Builder.create(ConstructionBlockTileEntity::new))
                .factory((b) -> b.build(null)));
        container.add(new RegistryObjectBuilder<TileEntityType<?>, Builder<?>>(new ResourceLocation(Reference.MODID, "template_manager_tile"))
                .builder(Builder.create(TemplateManagerTileEntity::new))
                .factory((b) -> b.build(null)));
    }

    @SubscribeEvent
    public static void registerTiles(RegistryEvent.Register<TileEntityType<?>> event) {
        container.register(event);
    }

    static void cleanup() {
        container.clear();
    }
}
