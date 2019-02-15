package com.direwolf20.buildinggadgets.common.registry.objects;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlock;
import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManagerTileEntity;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.entities.BlockBuildEntity;
import com.direwolf20.buildinggadgets.common.entities.ConstructionBlockEntity;
import com.direwolf20.buildinggadgets.common.registry.RegistryContainer;
import com.direwolf20.buildinggadgets.common.registry.RegistryObjectBuilder;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
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
    private static final RegistryContainer<Item, RegistryObjectBuilder<Item,Item.Builder>> tiles = new RegistryContainer<>();
    private static final RegistryContainer<Item, RegistryObjectBuilder<Item,Item.Builder>> entities = new RegistryContainer<>();
    // Types
    public static final EntityType<?> BLOCK_BUILD = EntityType.Builder.create(BlockBuildEntity.class, BlockBuildEntity::new).build("").setRegistryName(modId, "build_block");
    public static final EntityType<?> CONSTRUCTION_BLOCK = EntityType.Builder.create(ConstructionBlockEntity.class, ConstructionBlockEntity::new).build("").setRegistryName(modId, "construction_block");

    public static final TileEntityType<?> TEMPLATE_MANAGER_TYPE = TileEntityType.Builder.create(TemplateManagerTileEntity::new).build(null).setRegistryName(modId, "template_manager_tile");
    public static final TileEntityType<?> CONSTRUCTION_BLOCK_TYPE = TileEntityType.Builder.create(ConstructionBlockTileEntity::new).build(null).setRegistryName(modId, "construction_tile");

    // Creative tab
    public static ItemGroup creativeTab = new ItemGroup(BuildingGadgets.MODID){
        @Override
        public ItemStack createIcon() {
            return new ItemStack(BGItems.gadgetBuilding);
        }
    };

    // Materials
    public static final Material EFFECT_BLOCK_MATERIAL = new Material.Builder(MapColor.AIR).notSolid().build();


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
        for (BGSounds sound : BGSounds.values()) {
            event.getRegistry().register(sound.getSound());
        }
    }

    public static void initColorHandlers() { //TODO ItemBlock Creative Tabs
        BlockColors blockColors = BuildingGadgets.getInstance().getMinecraft().getBlockColors();
        //TODO non conditional registry
        if (Config.GENERAL.enablePaste.get()) ((ConstructionBlock) BGBlocks.constructionBlock).initColorHandler(blockColors);
    }

    public static void init() {
        BGBlocks.init();
        BGItems.init();
    }
}
