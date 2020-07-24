package com.direwolf20.buildinggadgets.common.blocks;

import com.direwolf20.buildinggadgets.common.items.OurItems;
import com.direwolf20.buildinggadgets.common.entities.tiles.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.entities.tiles.EffectBlockTileEntity;
import com.direwolf20.buildinggadgets.common.entities.tiles.TemplateManagerTileEntity;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import com.direwolf20.buildinggadgets.common.util.ref.Reference.BlockReference;
import com.direwolf20.buildinggadgets.common.util.ref.Reference.TileEntityReference;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
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

    /**
     * As the effect block is effectively air it needs to have a material just like Air.
     * We don't use Material.AIR as this is replaceable.
     */
    private static final Material EFFECT_BLOCK_MATERIAL = new Material.Builder(MaterialColor.AIR).notSolid().build();

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> registry = event.getRegistry();

        registry.register(new EffectBlock(Block.Properties.create(Material.IRON).hardnessAndResistance(20f).notSolid().noDrops()).setRegistryName(BlockReference.EFFECT_BLOCK_RL));
        registry.register(new ConstructionBlock(Block.Properties.create(Material.SAND).hardnessAndResistance(2f, 0f).harvestTool(ToolType.SHOVEL)).setRegistryName(BlockReference.CONSTRUCTION_BLOCK_RL));
        registry.register(new ConstructionBlockDense(Block.Properties.create(Material.ROCK).hardnessAndResistance(3f, 0f)).setRegistryName(BlockReference.CONSTRUCTION_BLOCK_DENSE_RL));
        registry.register(new ConstructionBlockPowder(Block.Properties.create(Material.SAND).hardnessAndResistance(10f)).setRegistryName(BlockReference.CONSTRUCTION_BLOCK_POWDER_RL));
        registry.register(new TemplateManager(Block.Properties.create(Material.ROCK).hardnessAndResistance(2f)).setRegistryName(BlockReference.TEMPLATE_MANAGER_RL));
    }

    @SubscribeEvent
    public static void registerItemBlocks(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();

        registry.register(new BlockItem(constructionBlock, OurItems.itemProperties()).setRegistryName(BlockReference.CONSTRUCTION_BLOCK_RL));
        registry.register(new BlockItem(constructionBlockDense, OurItems.itemProperties()).setRegistryName(BlockReference.CONSTRUCTION_BLOCK_DENSE_RL));
        registry.register(new BlockItem(constructionBlockPowder, OurItems.itemProperties()).setRegistryName(BlockReference.CONSTRUCTION_BLOCK_POWDER_RL));
        registry.register(new BlockItem(templateManger, OurItems.itemProperties()).setRegistryName(BlockReference.TEMPLATE_MANAGER_RL));
    }

    @SubscribeEvent
    public static void registerTiles(RegistryEvent.Register<TileEntityType<?>> event) {
        IForgeRegistry<TileEntityType<?>> registry = event.getRegistry();

        registry.register(TileEntityType.Builder.create(EffectBlockTileEntity::new, effectBlock).build(null).setRegistryName(TileEntityReference.EFFECT_BLOCK_TILE_RL));
        registry.register(TileEntityType.Builder.create(ConstructionBlockTileEntity::new, constructionBlock).build(null).setRegistryName(TileEntityReference.CONSTRUCTION_TILE_RL));
        registry.register(TileEntityType.Builder.create(TemplateManagerTileEntity::new, templateManger).build(null).setRegistryName(TileEntityReference.TEMPLATE_MANAGER_TILE_RL));
    }
}
