package com.direwolf20.buildinggadgets.common;

import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlock;
import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlockPowder;
import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.blocks.EffectBlock;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManager;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManagerTileEntity;
import com.direwolf20.buildinggadgets.common.config.SyncedConfig;
import com.direwolf20.buildinggadgets.common.entities.BlockBuildEntity;
import com.direwolf20.buildinggadgets.common.entities.ConstructionBlockEntity;
import com.direwolf20.buildinggadgets.common.items.pastes.ConstructionPasteContainer;
import com.direwolf20.buildinggadgets.common.items.pastes.ConstructionPasteContainerCreative;
import com.direwolf20.buildinggadgets.common.items.Template;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetBuilding;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetDestruction;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetExchanger;
import com.direwolf20.buildinggadgets.common.items.pastes.ConstructionPaste;
import com.direwolf20.buildinggadgets.common.items.pastes.RegularPasteContainerTypes;

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
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BuildingGadgets.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BuildingObjects {
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
    public static final GadgetBuilding gadgetBuilding       = new GadgetBuilding(itemBuilder(), "buildingtool");
    public static final GadgetCopyPaste gadgetCopyPaste     = new GadgetCopyPaste(itemBuilder(), "copypastetool");
    public static final GadgetDestruction gadgetDestruction = new GadgetDestruction(itemBuilder(), "destructiontool");
    public static final GadgetExchanger gadgetExchanger     = new GadgetExchanger(itemBuilder(), "exchangertool");

    // Building Items
    public static final Item constructionPaste  = new ConstructionPaste(itemBuilder()).setRegistryName(modId, "constructionpaste");
    public static final Item template           = new Template(itemBuilder().maxStackSize(1)).setRegistryName(modId, "template");

    // Construction Paste Containers
    public static final Item ConstructionPasteContainer = new ConstructionPasteContainer(itemBuilder(), RegularPasteContainerTypes.T1);
    public static final Item ConstructionPasteContainer2 = new ConstructionPasteContainer(itemBuilder(), RegularPasteContainerTypes.T2);
    public static final Item ConstructionPasteContainer3 = new ConstructionPasteContainer(itemBuilder(), RegularPasteContainerTypes.T3);
    public static final Item ConstructionPasteContainerCreative = new ConstructionPasteContainerCreative(itemBuilder());

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
                ConstructionPasteContainerCreative,

                template
        );

        // Item Blocks
        event.getRegistry().registerAll(
                new ItemBlock(constructionBlock, itemBuilder()).setRegistryName(constructionBlock.getRegistryName()),
                new ItemBlock(constructionBlockPowder, itemBuilder()).setRegistryName(constructionBlockPowder.getRegistryName()),
                new ItemBlock(templateManger, itemBuilder()).setRegistryName(templateManger.getRegistryName())
        );

        if (SyncedConfig.enableDestructionGadget)
            event.getRegistry().register(gadgetDestruction);
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(
                effectBlock,
                templateManger
        );

        if (SyncedConfig.enablePaste) {
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

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        for (ModSounds sound : ModSounds.values()) {
            event.getRegistry().register(sound.getSound());
        }
    }

    private static Item.Builder itemBuilder() {
        return new Item.Builder().group(creativeTab);
    }

    public static void initColorHandlers() { //TODO ItemBlock Creative Tabs
        BlockColors blockColors = BuildingGadgets.getInstance().getMinecraft().getBlockColors();
        if (SyncedConfig.enablePaste) ((ConstructionBlock) constructionBlock).initColorHandler(blockColors);
    }
}
