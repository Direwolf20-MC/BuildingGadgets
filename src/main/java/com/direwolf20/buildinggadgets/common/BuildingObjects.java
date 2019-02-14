package com.direwolf20.buildinggadgets.common;

import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlock;
import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlockPowder;
import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.blocks.EffectBlock;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManager;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManagerTileEntity;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.entities.BlockBuildEntity;
import com.direwolf20.buildinggadgets.common.entities.ConstructionBlockEntity;
import com.direwolf20.buildinggadgets.common.items.ConstructionPaste;
import com.direwolf20.buildinggadgets.common.items.ConstructionPasteContainer;
import com.direwolf20.buildinggadgets.common.items.Template;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetBuilding;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetDestruction;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetExchanger;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BuildingGadgets.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BuildingObjects {

    private static int[] containerAmounts = new int[]{512, 2048, 8192};
    private static final String modId = BuildingGadgets.MODID;

    // Types
    public static final EntityType<?> BLOCK_BUILD = EntityType.Builder.create(BlockBuildEntity.class, BlockBuildEntity::new).build("").setRegistryName(modId, "build_block");
    public static final EntityType<?> CONSTRUCTION_BLOCK = EntityType.Builder.create(ConstructionBlockEntity.class, ConstructionBlockEntity::new).build("").setRegistryName(modId, "construction_block");

    public static final TileEntityType<?> TEMPLATE_MANAGER_TYPE = TileEntityType.Builder.create(TemplateManagerTileEntity::new).build(null).setRegistryName(modId, "template_manager_tile");
    public static final TileEntityType<?> CONSTRUCTION_BLOCK_TYPE = TileEntityType.Builder.create(ConstructionBlockTileEntity::new).build(null).setRegistryName(modId, "construction_tile");

    // Creative tab
    private static ItemGroup creativeTab = new ItemGroup(BuildingGadgets.MODID){
        @Override
        public ItemStack createIcon() {
            return new ItemStack(gadgetBuilding);
        }
    };

    // Materials
    public static final Material EFFECT_BLOCK_MATERIAL = new Material.Builder(MapColor.AIR).notSolid().build();

    // Gadgets
    public static final Item gadgetBuilding     = new GadgetBuilding(itemBuilder().maxStackSize(1)).setRegistryName(modId, "buildingtool");
    public static final Item gadgetCopyPaste    = new GadgetCopyPaste(itemBuilder().maxStackSize(1)).setRegistryName(modId, "copypastetool");
    public static final Item gadgetDestruction  = new GadgetDestruction(itemBuilder().maxStackSize(1)).setRegistryName(modId, "destructiontool");
    public static final Item gadgetExchanger    = new GadgetExchanger(itemBuilder().maxStackSize(1)).setRegistryName(modId, "exchangertool");

    // Building Items
    public static final Item constructionPaste  = new ConstructionPaste(itemBuilder()).setRegistryName(modId, "constructionpaste");
    public static final Item template           = new Template(itemBuilder().maxStackSize(1)).setRegistryName(modId, "template");

    // Construction Paste Containers
    public static final Item ConstructionPasteContainer = new ConstructionPasteContainer(itemBuilder(), containerAmounts[0]).setRegistryName(modId, "constructionpastecontainer");
    public static final Item ConstructionPasteContainer2 = new ConstructionPasteContainer(itemBuilder(), containerAmounts[1]).setRegistryName(modId, "constructionpastecontainer2");
    public static final Item ConstructionPasteContainer3 = new ConstructionPasteContainer(itemBuilder(), containerAmounts[2]).setRegistryName(modId, "constructionpastecontainer3");

    // Blocks
    public static final Block effectBlock                = new EffectBlock(Block.Builder.create(EFFECT_BLOCK_MATERIAL).hardnessAndResistance(20f)).setRegistryName(modId, "effect_block");
    public static final Block constructionBlock          = new ConstructionBlock(Block.Builder.create(Material.ROCK).hardnessAndResistance(2.0f)).setRegistryName(modId, "construction_block");
    public static final Block constructionBlockPowder    = new ConstructionBlockPowder(Block.Builder.create(Material.SAND).hardnessAndResistance(0.5f, 0f)).setRegistryName(modId, "construction_block_powder");
    public static final Block templateManger             = new TemplateManager(Block.Builder.create(Material.ROCK).hardnessAndResistance(2f)).setRegistryName(modId, "templatemanager");

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().registerAll(
                // Gadgets
                gadgetBuilding,
                gadgetCopyPaste,
                gadgetExchanger,

                // Building Items
                constructionPaste,
                ConstructionPasteContainer,
                ConstructionPasteContainer2,
                ConstructionPasteContainer3,

                template
        );

        // Item Blocks
        event.getRegistry().registerAll(
                new ItemBlock(constructionBlock, itemBuilder()).setRegistryName(constructionBlock.getRegistryName()),
                new ItemBlock(constructionBlockPowder, itemBuilder()).setRegistryName(constructionBlockPowder.getRegistryName()),
                new ItemBlock(templateManger, itemBuilder()).setRegistryName(templateManger.getRegistryName())
        );

        //TODO non conditional registration
        if (Config.GENERAL.enableDestructionGadget.get()) {
            event.getRegistry().register(gadgetDestruction);
        }
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(
                effectBlock,
                templateManger
        );

        //TODO non conditional registration
        if (Config.GENERAL.enablePaste.get()) {
            event.getRegistry().registerAll(
                    constructionBlock,
                    constructionBlockPowder
            );
        }
    }

    @SubscribeEvent
    public static void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event) {
        event.getRegistry().registerAll(
                TEMPLATE_MANAGER_TYPE,
                CONSTRUCTION_BLOCK_TYPE
        );
    }

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityType<?>> event) {
// @todo: reimplement @since 1.13.x
// Taken from ModEntities before removal
//        EntityRegistry.registerModEntity(new ResourceLocation(BuildingGadgets.MODID, "BlockBuildEntity"), BlockBuildEntity.class, "LaserGunEntity", id++, BuildingGadgets.instance, 64, 1, true);
//        EntityRegistry.registerModEntity(new ResourceLocation(BuildingGadgets.MODID, "ConstructionBlockEntity"), ConstructionBlockEntity.class, "ConstructionBlockEntity", id++, BuildingGadgets.instance, 64, 1, true);


        event.getRegistry().registerAll(
                BLOCK_BUILD,
                CONSTRUCTION_BLOCK
        );
    }

    private static Item.Builder itemBuilder() {
        return new Item.Builder().group(creativeTab);
    }

    public static void initColorHandlers() { //TODO ItemBlock Creative Tabs
        BlockColors blockColors = BuildingGadgets.getInstance().getMinecraft().getBlockColors();
        //TODO non conditional registration
        if (Config.GENERAL.enablePaste.get()) ((ConstructionBlock) constructionBlock).initColorHandler(blockColors);
    }
}
