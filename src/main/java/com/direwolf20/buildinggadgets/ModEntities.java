package com.direwolf20.buildinggadgets;

import com.direwolf20.buildinggadgets.Entities.BlockBuildEntity;
import com.direwolf20.buildinggadgets.Entities.BlockBuildEntityRender;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;

public class ModEntities {
    public static void init() {
        int id = 1;
        EntityRegistry.registerModEntity(new ResourceLocation("buildinggadgets:BlockBuildEntity"),BlockBuildEntity.class,"LaserGunEntity",id++,BuildingGadgets.instance,64,1,true);
        //EntityRegistry.registerModEntity(new ResourceLocation("diregoo:LaserBlastEntity"),LaserBlastEntity.class,"LaserBlastEntity",id++,DireGoo.instance,64,1,true);

    }

    public static void initModels() {
        RenderingRegistry.registerEntityRenderingHandler(BlockBuildEntity.class, new BlockBuildEntityRender.Factory());
        //RenderingRegistry.registerEntityRenderingHandler(LaserBlastEntity.class, new LaserBlastEntityRender.Factory());
    }
}
