package com.direwolf20.buildinggadgets.common.registry.block.tile;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.registry.ClientConstructContainer;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TileEntityRegistryContainer extends ClientConstructContainer<TileEntityType<?>, TileEntityBuilder<?>> {
    private Map<ResourceLocation, TileEntityBuilder<?>> reverseMapping;

    public TileEntityRegistryContainer() {
        this.reverseMapping = new HashMap<>();
    }

    @Override
    public void clientInit() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerTERs);
    }

    @Override
    public void add(TileEntityBuilder<?> builder) {
        super.add(builder);
        reverseMapping.put(builder.getRegistryName(), builder);
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

    @Nullable
    public TileEntityBuilder<?> getBuilderWithId(ResourceLocation id) {
        return reverseMapping.get(Objects.requireNonNull(id));
    }

    @Override
    public void clear() {
        super.clear();
        this.reverseMapping.clear();
    }
}
