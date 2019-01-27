package com.direwolf20.buildinggadgets.common.blocks;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManager;
import com.direwolf20.buildinggadgets.common.config.SyncedConfig;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(BuildingGadgets.MODID)
public class ModBlocks {
    public static final Material EFFECT_BLOCK_MATERIAL = new Material.Builder(MapColor.AIR).notSolid().build();
    @ObjectHolder("effect_block")
    public static EffectBlock effectBlock;
    @ObjectHolder("construction_block")
    public static ConstructionBlock constructionBlock;
    @ObjectHolder("construction_block_powder")
    public static ConstructionBlockPowder constructionBlockPowder;
    @ObjectHolder("template_manager")
    public static TemplateManager templateManager;

    public static void initColorHandlers() { //TODO ItemBlock Creative Tabs
        BlockColors blockColors = BuildingGadgets.getInstance().getMinecraft().getBlockColors();
        if (SyncedConfig.enablePaste) constructionBlock.initColorHandler(blockColors);
    }


}