package com.direwolf20.buildinggadgets.common.registry.objects;

import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.fml.DistExecutor;

public class BuildingObjects {

    // Creative tab
    public static ItemGroup creativeTab = new ItemGroup(Reference.MODID){
        @Override
        public ItemStack createIcon() {
            ItemStack stack = new ItemStack(BGItems.gadgetBuilding);
            stack.getOrCreateTag().putByte(NBTKeys.CREATIVE_MARKER, (byte) 0);
            return stack;
        }
    };

    // Materials
    public static final Material EFFECT_BLOCK_MATERIAL = new Material.Builder(MaterialColor.AIR).notSolid().build();


    public static void initColorHandlers(ColorHandlerEvent.Block event) { //TODO ItemBlock Creative Tabs
        BlockColors blockColors = event.getBlockColors();
        //TODO non conditional registry
        BGBlocks.constructionBlock.initColorHandler(blockColors);
    }

    public static void init() {
        BGBlocks.init();
        BGItems.init();
        BGEntities.init();
        BGBlocks.BGTileEntities.init();
        BGContainers.init();
        DistExecutor.runWhenOn(Dist.CLIENT, () -> BuildingObjects::clientInit);
    }

    public static void cleanup() {
        BGBlocks.cleanup();
        BGItems.cleanup();
        BGEntities.cleanup();
        BGBlocks.BGTileEntities.cleanup();
        BGContainers.cleanup();
    }

    private static void clientInit() {
        BGEntities.clientInit();
        BGBlocks.BGTileEntities.clientInit();
    }
}
