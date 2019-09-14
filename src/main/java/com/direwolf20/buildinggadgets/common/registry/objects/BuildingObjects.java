package com.direwolf20.buildinggadgets.common.registry.objects;

import com.direwolf20.buildinggadgets.common.registry.OurBlocks;
import com.direwolf20.buildinggadgets.common.registry.OurEntities;
import com.direwolf20.buildinggadgets.common.registry.OurItems;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class BuildingObjects {

    // Creative tab
    public static ItemGroup creativeTab = new ItemGroup(Reference.MODID){
        @Override
        public ItemStack createIcon() {
            ItemStack stack = new ItemStack(OurItems.gadgetBuilding);
            stack.getOrCreateTag().putByte(NBTKeys.CREATIVE_MARKER, (byte) 0);
            return stack;
        }
    };

    // Materials
    public static final Material EFFECT_BLOCK_MATERIAL = new Material.Builder(MaterialColor.AIR).notSolid().build();


    public static void initColorHandlers(BlockColors colors) {
        OurBlocks.constructionBlock.initColorHandler(colors);
    }

    public static void init() {
        OurItems.setup();
        BGContainers.init();

        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            OurEntities.registerModels();
            OurBlocks.OurTileEntities.registerRenderers();
        });
    }

    public static void clientSetup() {
        BGContainers.clientSetup();
        initColorHandlers(Minecraft.getInstance().getBlockColors());
    }

    public static void cleanup() {
        BGContainers.cleanup();
    }
}
