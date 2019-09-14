package com.direwolf20.buildinggadgets.common.registry;

import com.direwolf20.buildinggadgets.common.blocks.EffectBlock;
import net.minecraft.block.Block;

import java.util.function.Function;

public class OurBlocks implements IRegistryComponent {

    /**
     * Something to note: the factory will not work without the Builder being initialized
     * with the empty <>. We're not 100% sure why the generics is breaking the Function
     * interface but it does so this is what we've done to fix it :cry: java why...
     */
    public static void setup() {
        new Builder<>().setFactory(EffectBlock::new);
    }

    private static final class Builder<T extends Block> {
        private Function<Block.Properties, Block> factory;

        public Builder setFactory(Function<Block.Properties, Block> factory) {
            this.factory = factory;
            return this;
        }
    }
}
