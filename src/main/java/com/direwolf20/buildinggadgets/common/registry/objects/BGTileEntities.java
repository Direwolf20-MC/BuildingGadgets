package com.direwolf20.buildinggadgets.common.registry.objects;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManagerTileEntity;
import com.direwolf20.buildinggadgets.common.registry.RegistryContainer;
import com.direwolf20.buildinggadgets.common.registry.RegistryObjectBuilder;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.tileentity.TileEntityType.Builder;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(BuildingGadgets.MODID)
@EventBusSubscriber(modid = BuildingGadgets.MODID, bus = Bus.MOD)
public class BGTileEntities {
    private static final RegistryContainer<TileEntityType<?>, RegistryObjectBuilder<TileEntityType<?>, Builder<?>>> container = new RegistryContainer<>();
    public static TileEntityType<?> CONSTRUCTION_BLOCK_TYPE;
    public static TileEntityType<?> TEMPLATE_MANAGER_TYPE;

    public static void init() {
        container.add(new RegistryObjectBuilder<TileEntityType<?>, Builder<?>>(new ResourceLocation(BuildingGadgets.MODID,"construction_tile"))
                .builder(Builder.create(ConstructionBlockTileEntity::new))
                .factory((b) -> b.build(null)));
        container.add(new RegistryObjectBuilder<TileEntityType<?>, Builder<?>>(new ResourceLocation(BuildingGadgets.MODID,"template_manager_tile"))
                .builder(Builder.create(TemplateManagerTileEntity::new))
                .factory((b) -> b.build(null)));
    }

    @SubscribeEvent
    public static void registerTiles(RegistryEvent.Register<TileEntityType<?>> event) {
        container.register(event);
    }
}
