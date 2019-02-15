package com.direwolf20.buildinggadgets.common.registry;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class BlockRegistryContainer extends RegistryContainer<Block,BlockBuilder>{
    public BlockRegistryContainer() {
        super();
    }

    @SubscribeEvent
    public void registerItemBlocks(RegistryEvent.Register<Item> event) {
        BuildingGadgets.LOG.debug("Registering Buildinggadgets item blocks");
        for (BlockBuilder builder:getBuilders()) {
            event.getRegistry().register(builder.createItemFromBlock());
        }
        BuildingGadgets.LOG.debug("Finished Registering {} Buildinggadgets item blocks",getBuilders().size());
    }
}
