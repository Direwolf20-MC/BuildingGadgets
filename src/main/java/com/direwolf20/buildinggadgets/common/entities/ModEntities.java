package com.direwolf20.buildinggadgets.common.entities;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
//import net.minecraftforge.fml.common.registry.EntityRegistry;

public class ModEntities {
    public static final EntityType<BlockBuildEntity> BLOCK_BUILD = EntityType.register("build_block", EntityType.Builder.create(BlockBuildEntity.class, BlockBuildEntity::new));
    public static final EntityType<ConstructionBlockEntity> CONSTRUCTION_BLOCK = EntityType.register("construction_block", EntityType.Builder.create(ConstructionBlockEntity.class, ConstructionBlockEntity::new));


    public static void init() {
        int id = 1;
//        EntityRegistry.registerModEntity(new ResourceLocation(BuildingGadgets.MODID, "BlockBuildEntity"), BlockBuildEntity.class, "LaserGunEntity", id++, BuildingGadgets.instance, 64, 1, true);
//        EntityRegistry.registerModEntity(new ResourceLocation(BuildingGadgets.MODID, "ConstructionBlockEntity"), ConstructionBlockEntity.class, "ConstructionBlockEntity", id++, BuildingGadgets.instance, 64, 1, true);

    }

    public static void initModels() {
        RenderingRegistry.registerEntityRenderingHandler(BlockBuildEntity.class, new BlockBuildEntityRender.Factory());
        RenderingRegistry.registerEntityRenderingHandler(ConstructionBlockEntity.class, new ConstructionBlockEntityRender.Factory());
    }
}
