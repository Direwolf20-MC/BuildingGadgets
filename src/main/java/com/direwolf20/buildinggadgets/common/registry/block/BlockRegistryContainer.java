package com.direwolf20.buildinggadgets.common.registry.block;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.registry.RegistryContainer;
import com.direwolf20.buildinggadgets.common.registry.block.tile.TileEntityBuilder;
import com.direwolf20.buildinggadgets.common.registry.block.tile.TileEntityRegistryContainer;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import com.google.common.base.Preconditions;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;

public class BlockRegistryContainer extends RegistryContainer<Block, BlockBuilder> {
    private final TileEntityRegistryContainer tileContainer;

    public BlockRegistryContainer(TileEntityRegistryContainer tileContainer) {
        this.tileContainer = tileContainer;
    }

    @Override
    public void add(BlockBuilder builder) {
        super.add(builder);
        if (builder.getTileEntityBuilder() != null) {
            tileContainer.add(builder.getTileEntityBuilder());
        } else if (builder.getTileEntityId() != null) {
            TileEntityBuilder<?> tileEntityBuilder = tileContainer.getBuilderWithId(builder.getTileEntityId());
            Preconditions.checkArgument(tileEntityBuilder != null, "Attempted to add Block with TileEntity without constructing a TileEntityType!");
            builder.withTileEntity(tileEntityBuilder);
        }
    }

    public void registerItemBlocks(RegistryEvent.Register<Item> event) {
        BuildingGadgets.LOG.debug("Registering {} ItemBlocks", Reference.MODID);
        for (BlockBuilder builder:getBuilders()) {
            if (builder.hasItem())
                event.getRegistry().register(builder.createItemFromBlock());
        }
        BuildingGadgets.LOG.debug("Finished Registering {} {} ItemBlock's", getBuilders().size(), Reference.MODID);
    }
}
