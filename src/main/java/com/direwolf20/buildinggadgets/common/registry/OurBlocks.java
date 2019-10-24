package com.direwolf20.buildinggadgets.common.registry;

import com.direwolf20.buildinggadgets.client.renderer.ChargingStationTER;
import com.direwolf20.buildinggadgets.client.renderer.EffectBlockTER;
import com.direwolf20.buildinggadgets.common.blocks.*;
import com.direwolf20.buildinggadgets.common.items.ChargingStationItem;
import com.direwolf20.buildinggadgets.common.tiles.ChargingStationTileEntity;
import com.direwolf20.buildinggadgets.common.tiles.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.tiles.EffectBlockTileEntity;
import com.direwolf20.buildinggadgets.common.tiles.TemplateManagerTileEntity;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import com.direwolf20.buildinggadgets.common.util.ref.Reference.BlockReference;
import com.direwolf20.buildinggadgets.common.util.ref.Reference.TileEntityReference;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@EventBusSubscriber(modid = Reference.MODID, bus = Bus.MOD)
public final class OurBlocks {
    private OurBlocks() {}

    // ugly Object holder code below
    @ObjectHolder(BlockReference.EFFECT_BLOCK)
    public static EffectBlock effectBlock;

    @ObjectHolder(BlockReference.CONSTRUCTION_BLOCK)
    public static ConstructionBlock constructionBlock;

    @ObjectHolder(BlockReference.CONSTRUCTION_BLOCK_DENSE)
    public static ConstructionBlockDense constructionBlockDense;

    @ObjectHolder(BlockReference.CONSTRUCTION_BLOCK_POWDER)
    public static ConstructionBlockPowder constructionBlockPowder;

    @ObjectHolder(BlockReference.TEMPLATE_MANAGER)
    public static TemplateManager templateManger;

    @ObjectHolder(BlockReference.CHARGING_STATION)
    public static ChargingStationBlock chargingStation;

    /**
     * As the effect block is effectively air it needs to have a material just like Air.
     * We don't use Material.AIR as this is replaceable.
     */
    private static final Material EFFECT_BLOCK_MATERIAL = new Material.Builder(MaterialColor.AIR).notSolid().build();

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> registry = event.getRegistry();

        registry.register(new EffectBlock(Block.Properties.create(EFFECT_BLOCK_MATERIAL).hardnessAndResistance(20f)).setRegistryName(BlockReference.EFFECT_BLOCK_RL));
        registry.register(new ConstructionBlock(Block.Properties.create(Material.SAND).hardnessAndResistance(2f, 0f).harvestTool(ToolType.SHOVEL)).setRegistryName(BlockReference.CONSTRUCTION_BLOCK_RL));
        registry.register(new ConstructionBlockDense(Block.Properties.create(Material.ROCK).hardnessAndResistance(3f, 0f)).setRegistryName(BlockReference.CONSTRUCTION_BLOCK_DENSE_RL));
        registry.register(new ConstructionBlockPowder(Block.Properties.create(Material.SAND).hardnessAndResistance(10f)).setRegistryName(BlockReference.CONSTRUCTION_BLOCK_POWDER_RL));
        registry.register(new TemplateManager(Block.Properties.create(Material.ROCK).hardnessAndResistance(2f)).setRegistryName(BlockReference.TEMPLATE_MANAGER_RL));
        registry.register(new ChargingStationBlock(Block.Properties.create(Material.ROCK).hardnessAndResistance(2f)).setRegistryName(BlockReference.CHARGING_STATION_RL));
    }

    @SubscribeEvent
    public static void registerItemBlocks(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();

        registry.register(new BlockItem(constructionBlock, OurItems.itemProperties()).setRegistryName(BlockReference.CONSTRUCTION_BLOCK_RL));
        registry.register(new BlockItem(constructionBlockDense, OurItems.itemProperties()).setRegistryName(BlockReference.CONSTRUCTION_BLOCK_DENSE_RL));
        registry.register(new BlockItem(constructionBlockPowder, OurItems.itemProperties()).setRegistryName(BlockReference.CONSTRUCTION_BLOCK_POWDER_RL));
        registry.register(new BlockItem(templateManger, OurItems.itemProperties()).setRegistryName(BlockReference.TEMPLATE_MANAGER_RL));
        registry.register(new ChargingStationItem(OurItems.itemProperties()).setRegistryName(BlockReference.CHARGING_STATION_RL));
    }

    @SubscribeEvent
    public static void registerTiles(RegistryEvent.Register<TileEntityType<?>> event) {
        IForgeRegistry<TileEntityType<?>> registry = event.getRegistry();

        registry.register(TileEntityType.Builder.create(EffectBlockTileEntity::new, effectBlock).build(null).setRegistryName(TileEntityReference.EFFECT_BLOCK_TILE_RL));
        registry.register(TileEntityType.Builder.create(ConstructionBlockTileEntity::new, constructionBlock).build(null).setRegistryName(TileEntityReference.CONSTRUCTION_TILE_RL));
        registry.register(TileEntityType.Builder.create(TemplateManagerTileEntity::new, templateManger).build(null).setRegistryName(TileEntityReference.TEMPLATE_MANAGER_TILE_RL));
        registry.register(TileEntityType.Builder.create(ChargingStationTileEntity::new, chargingStation).build(null).setRegistryName(TileEntityReference.CHARGING_STATION_TILE_RL));
    }

    public static final class OurTileEntities {
        private OurTileEntities() {}

        @ObjectHolder(TileEntityReference.CONSTRUCTION_TILE)
        public static TileEntityType<?> CONSTRUCTION_BLOCK_TYPE;

        @ObjectHolder(TileEntityReference.TEMPLATE_MANAGER_TILE)
        public static TileEntityType<?> TEMPLATE_MANAGER_TYPE;

        @ObjectHolder(TileEntityReference.CHARGING_STATION_TILE)
        public static TileEntityType<?> CHARGING_STATION_TYPE;

        @ObjectHolder(TileEntityReference.EFFECT_BLOCK_TILE)
        public static TileEntityType<?> EFFECT_BLOCK_TYPE;

        /**
         * Called from {@link RegistryHandler} as this is required to be loaded
         * only on the client side.
         */
        @OnlyIn(Dist.CLIENT)
        static void registerRenderers() {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(event -> {
                ClientRegistry.bindTileEntitySpecialRenderer(EffectBlockTileEntity.class, new EffectBlockTER());
                ClientRegistry.bindTileEntitySpecialRenderer(ChargingStationTileEntity.class, new ChargingStationTER());
            });
        }
    }
}
