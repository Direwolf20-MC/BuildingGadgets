package com.direwolf20.buildinggadgets.common.registry.objects;

import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlock;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.utils.ref.Reference;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

//@Mod.EventBusSubscriber(modid = BuildingGadgets.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BuildingObjects {

    // Creative tab
    public static ItemGroup creativeTab = new ItemGroup(Reference.MODID){
        @Override
        public ItemStack createIcon() {
            return new ItemStack(BGItems.gadgetBuilding);
        }
    };

    // Materials
    public static final Material EFFECT_BLOCK_MATERIAL = new Material.Builder(MaterialColor.AIR).notSolid().build();


    public static void initColorHandlers() { //TODO ItemBlock Creative Tabs
        BlockColors blockColors = Minecraft.getInstance().getBlockColors();
        //TODO non conditional registry
        if (Config.GENERAL.enablePaste.get()) ((ConstructionBlock) BGBlocks.constructionBlock).initColorHandler(blockColors);
    }

    public static void init() {
        BGBlocks.init();
        BGItems.init();
        BGEntities.init();
        BGTileEntities.init();
        DistExecutor.runWhenOn(Dist.CLIENT,() -> BGEntities::clientInit);
    }

    public static void cleanup() {
        BGBlocks.cleanup();
        BGItems.cleanup();
        BGEntities.cleanup();
        BGTileEntities.cleanup();
    }
}
