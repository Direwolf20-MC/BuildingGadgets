package com.direwolf20.buildinggadgets.common.registry.block;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.registry.RegistryContainer;
import com.direwolf20.buildinggadgets.common.utils.ref.Reference;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;

public class BlockRegistryContainer extends RegistryContainer<Block, BlockBuilder> {
    public void registerItemBlocks(RegistryEvent.Register<Item> event) {
        BuildingGadgets.LOG.debug("Registering {} item blocks", Reference.MODID);
        for (BlockBuilder builder:getBuilders()) {
            event.getRegistry().register(builder.createItemFromBlock());
        }
        BuildingGadgets.LOG.debug("Finished Registering {} {} ItemBlock's", getBuilders().size(), Reference.MODID);
    }
}
