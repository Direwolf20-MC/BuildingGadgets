package com.direwolf20.buildinggadgets.common.blocks;

import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.block.Block;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class OurBlocks {
    private OurBlocks() {}

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Reference.MODID);

    public static final RegistryObject<Block> EFFECT_BLOCK = BLOCKS.register("effect_block", EffectBlock::new);
    public static final RegistryObject<Block> CONSTRUCTION_BLOCK = BLOCKS.register("construction_block", ConstructionBlock::new);
    public static final RegistryObject<Block> CONSTRUCTION_DENSE_BLOCK = BLOCKS.register("construction_block_dense", ConstructionBlockDense::new);
    public static final RegistryObject<Block> CONSTRUCTION_POWDER_BLOCK = BLOCKS.register("construction_block_powder", ConstructionBlockPowder::new);
    public static final RegistryObject<Block> TEMPLATE_MANGER_BLOCK = BLOCKS.register("template_manager", TemplateManager::new);
}
