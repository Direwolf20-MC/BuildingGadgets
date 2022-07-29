package com.direwolf20.buildinggadgets.common.tileentities;

import com.direwolf20.buildinggadgets.common.blocks.OurBlocks;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class OurTileEntities {
    public static final DeferredRegister<BlockEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Reference.MODID);

    public static final RegistryObject<BlockEntityType<EffectBlockTileEntity>> EFFECT_BLOCK_TILE_ENTITY =
            TILE_ENTITIES.register("effect_block_tile", () -> BlockEntityType.Builder.of(EffectBlockTileEntity::new, OurBlocks.EFFECT_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<ConstructionBlockTileEntity>> CONSTRUCTION_BLOCK_TILE_ENTITY =
            TILE_ENTITIES.register("construction_tile", () -> BlockEntityType.Builder.of(ConstructionBlockTileEntity::new, OurBlocks.CONSTRUCTION_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<TemplateManagerTileEntity>> TEMPLATE_MANAGER_TILE_ENTITY =
            TILE_ENTITIES.register("template_manager_tile", () -> BlockEntityType.Builder.of(TemplateManagerTileEntity::new, OurBlocks.TEMPLATE_MANGER_BLOCK.get()).build(null));
}
