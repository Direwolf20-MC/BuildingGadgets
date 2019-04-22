package com.direwolf20.buildinggadgets.common.registry.tile;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.registry.ClientConstructContainer;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class TileEntityRegistryContainer extends ClientConstructContainer<TileEntityType<?>, TileEntityBuilder<?>> {
    @Override
    public void clientInit() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerTERs);
    }

    private void registerTERs(FMLClientSetupEvent event) {
        BuildingGadgets.LOG.info("Registering {} TileEntityRenderer's", Reference.MODID);
        int count = 0;
        for (TileEntityBuilder<?> builder : getBuilders()) {
            if (builder.hasRenderer()) {
                builder.registerRenderer();
                ++ count;
            }
        }
        BuildingGadgets.LOG.info("Finished registering {} {} TileEntityRenderer's", count, Reference.MODID);
    }
}
