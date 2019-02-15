package com.direwolf20.buildinggadgets.common.registry.objects;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlock;
import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlockPowder;
import com.direwolf20.buildinggadgets.common.blocks.EffectBlock;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManager;
import com.direwolf20.buildinggadgets.common.registry.BlockBuilder;
import com.direwolf20.buildinggadgets.common.registry.BlockRegistryContainer;
import net.minecraft.block.Block;
import net.minecraft.block.Block.Builder;
import net.minecraft.block.material.Material;
import net.minecraftforge.registries.ObjectHolder;

import static com.direwolf20.buildinggadgets.common.registry.objects.BuildingObjects.EFFECT_BLOCK_MATERIAL;

@ObjectHolder(BuildingGadgets.MODID)
public final class BGBlocks {
    private BGBlocks() {}

    private static final BlockRegistryContainer blocks = new BlockRegistryContainer();
    // Blocks
    @ObjectHolder("effect_block")
    public static Block effectBlock;
    @ObjectHolder("construction/block")
    public static Block constructionBlock;
    @ObjectHolder("construction/block/powder")
    public static Block constructionBlockPowder;
    @ObjectHolder("template_manager")
    public static Block templateManger;

    public static void init() {
        blocks.add(new BlockBuilder(EffectBlock.REGISTRY_NAME).builder(Builder.create(EFFECT_BLOCK_MATERIAL).hardnessAndResistance(20f)));
        blocks.add(new BlockBuilder(ConstructionBlock.REGISTRY_NAME).builder(Builder.create(Material.ROCK).hardnessAndResistance(0.5f,0f)));
        blocks.add(new BlockBuilder(ConstructionBlockPowder.REGISTRY_NAME).builder(Builder.create(Material.SAND).hardnessAndResistance(20f)));
        blocks.add(new BlockBuilder(TemplateManager.REGISTRY_NAME).builder(Builder.create(Material.ROCK).hardnessAndResistance(2f)));
    }
}
