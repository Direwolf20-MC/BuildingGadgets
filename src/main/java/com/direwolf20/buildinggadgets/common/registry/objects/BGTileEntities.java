package com.direwolf20.buildinggadgets.common.registry.objects;

import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManagerTileEntity;
import com.direwolf20.buildinggadgets.common.registry.tile.TileEntityBuilder;
import com.direwolf20.buildinggadgets.common.registry.tile.TileEntityRegistryContainer;
import com.direwolf20.buildinggadgets.common.utils.ref.Reference;
import com.direwolf20.buildinggadgets.common.utils.ref.Reference.TileEntityReference;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.tileentity.TileEntityType.Builder;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Reference.MODID)
@EventBusSubscriber(modid = Reference.MODID, bus = Bus.MOD)
public class BGTileEntities {
    private static final TileEntityRegistryContainer container = new TileEntityRegistryContainer();

    @ObjectHolder(TileEntityReference.CONSTRUCTION_TILE)
    public static TileEntityType<?> CONSTRUCTION_BLOCK_TYPE;
    @ObjectHolder(TileEntityReference.TEMPLATE_MANAGER_TILE)
    public static TileEntityType<?> TEMPLATE_MANAGER_TYPE;

    static void init() {
        container.add(new TileEntityBuilder<>(TileEntityReference.CONSTRUCTION_TILE_RL)
                .builder(Builder.create(ConstructionBlockTileEntity::new))
                .factory((b) -> b.build(null)));
        container.add(new TileEntityBuilder<>(TileEntityReference.TEMPLATE_MANAGER_TILE_RL)
                .builder(Builder.create(TemplateManagerTileEntity::new))
                .factory((b) -> b.build(null)));
    }

    @SubscribeEvent
    public static void registerTiles(RegistryEvent.Register<TileEntityType<?>> event) {
        container.register(event);
    }

    static void clientInit() {
        container.clientInit();
    }
    static void cleanup() {
        container.clear();
    }
}
