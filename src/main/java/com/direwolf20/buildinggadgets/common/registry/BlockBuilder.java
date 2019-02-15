package com.direwolf20.buildinggadgets.common.registry;

import net.minecraft.block.Block;
import net.minecraft.block.Block.Builder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

public class BlockBuilder extends RegistryObjectBuilder<Block,Block.Builder> {
    private Block block;
    private BiFunction<Block, Item.Builder, Item> itemBlockFactory;
    private Item.Builder itemBuilder;

    public static BlockBuilder create(String registryName) {
        return new BlockBuilder(registryName);
    }

    public static BlockBuilder create(ResourceLocation registryName) {
        return new BlockBuilder(registryName);
    }

    public BlockBuilder(String registryName) {
        super(registryName);
        this.itemBlockFactory = ItemBlock::new;
        this.itemBuilder = new Item.Builder();
    }

    public BlockBuilder(ResourceLocation registryName) {
        super(registryName);
        this.itemBlockFactory = ItemBlock::new;
        this.itemBuilder = new Item.Builder();
    }

    public BlockBuilder item(Item.Builder itemBuilder, BiFunction<Block, Item.Builder, Item> itemBlockFactory) {
        this.itemBuilder = Objects.requireNonNull(itemBuilder);
        this.itemBlockFactory = Objects.requireNonNull(itemBlockFactory);
        return this;
    }

    public BlockBuilder item(Item.Builder itemBuilder) {
        return item(itemBuilder,itemBlockFactory);
    }

    @Override
    public BlockBuilder factory(Function<Builder, Block> factory) {
        return (BlockBuilder) super.factory(factory);
    }

    @Override
    public BlockBuilder builder(Builder builder) {
        return (BlockBuilder) super.builder(builder);
    }

    @Override
    public Block construct() {
        block =  super.construct();
        return block;
    }

    public Item createItemFromBlock() {
        return itemBlockFactory.apply(block,itemBuilder).setRegistryName(getRegistryName());
    }

}
