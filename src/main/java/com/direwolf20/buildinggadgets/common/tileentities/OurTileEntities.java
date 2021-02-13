package com.direwolf20.buildinggadgets.common.tileentities;

import com.direwolf20.buildinggadgets.api.BuildingGadgetsAPI;
import com.direwolf20.buildinggadgets.common.blocks.OurBlocks;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class OurTileEntities {
    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, BuildingGadgetsAPI.MODID);

    public static final RegistryObject<TileEntityType<EffectBlockTileEntity>> EFFECT_BLOCK_TILE_ENTITY =
            TILE_ENTITIES.register("effect_block_tile", () -> TileEntityType.Builder.create(EffectBlockTileEntity::new, OurBlocks.EFFECT_BLOCK.get()).build(null));
    public static final RegistryObject<TileEntityType<ConstructionBlockTileEntity>> CONSTRUCTION_BLOCK_TILE_ENTITY =
            TILE_ENTITIES.register("construction_tile", () -> TileEntityType.Builder.create(ConstructionBlockTileEntity::new, OurBlocks.CONSTRUCTION_BLOCK.get()).build(null));
    public static final RegistryObject<TileEntityType<TemplateManagerTileEntity>> TEMPLATE_MANAGER_TILE_ENTITY =
            TILE_ENTITIES.register("template_manager_tile", () -> TileEntityType.Builder.create(TemplateManagerTileEntity::new, OurBlocks.TEMPLATE_MANGER_BLOCK.get()).build(null));
}
