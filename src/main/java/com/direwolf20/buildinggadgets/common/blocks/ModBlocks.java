package com.direwolf20.buildinggadgets.common.blocks;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManager;
import com.direwolf20.buildinggadgets.common.config.SyncedConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@GameRegistry.ObjectHolder(BuildingGadgets.MODID)
public class ModBlocks {

    @GameRegistry.ObjectHolder("effectblock")
    public static EffectBlock effectBlock;
    @GameRegistry.ObjectHolder("constructionblock")
    public static ConstructionBlock constructionBlock;
    @GameRegistry.ObjectHolder("constructionblock_dense")
    public static ConstructionBlockDense constructionBlockDense;
    @GameRegistry.ObjectHolder("constructionblockpowder")
    public static ConstructionBlockPowder constructionBlockPowder;
    @GameRegistry.ObjectHolder("templatemanager")
    public static TemplateManager templateManager;

    @SideOnly(Side.CLIENT)
    public static void initColorHandlers() {
        BlockColors blockColors = Minecraft.getMinecraft().getBlockColors();
        if (SyncedConfig.enablePaste) {constructionBlock.initColorHandler(blockColors);}
    }
}