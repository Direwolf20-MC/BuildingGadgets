package com.direwolf20.buildinggadgets.common.blocks;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManager;
import com.direwolf20.buildinggadgets.common.config.SyncedConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(BuildingGadgets.MODID)
public class ModBlocks {

    @ObjectHolder("effectblock")
    public static EffectBlock effectBlock;
    @ObjectHolder("constructionblock")
    public static ConstructionBlock constructionBlock;
    @ObjectHolder("constructionblockpowder")
    public static ConstructionBlockPowder constructionBlockPowder;
    @ObjectHolder("templatemanager")
    public static TemplateManager templateManager;

    public static void initColorHandlers() {
        BlockColors blockColors = Minecraft.getInstance().getBlockColors();
        if (SyncedConfig.enablePaste) {constructionBlock.initColorHandler(blockColors);}
    }
}