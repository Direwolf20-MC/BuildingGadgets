package com.direwolf20.buildinggadgets.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.state.Property;
import net.minecraft.util.text.StringTextComponent;

public class ChangeMaterialsScreen extends Screen {
    private int x;
    private int y;

    private ItemStack templateProvider;

    public ChangeMaterialsScreen(ItemStack tool) {
        super(new StringTextComponent(""));
        this.templateProvider = tool;

//        this.templateProvider.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).ifPresent(e ->
//            Minecraft.getInstance().world.getCapability(CapabilityTemplate.TEMPLATE_PROVIDER_CAPABILITY).ifPresent(template -> {
//                Template templateForKey = template.getTemplateForKey(e);
//                ImmutableMap<BlockPos, BlockData> map = templateForKey.getMap();
//                Set<BlockData> allBlocks = new HashSet<>(map.values());
//
//                for (BlockData block : allBlocks) {
//                    System.out.println(block.getState().getBlock().getTranslatedName().getString());
//                    System.out.println(block.getState().getProperties());
//                    for (Property<?> property : block.getState().getProperties()) {
//                        System.out.println(property.getAllowedValues());
//                        Object[] objects = property.getAllowedValues().toArray();
//                        for (Object object : objects) {
//                            System.out.println(getStateWithProperty(block.getState(), property, property.getValueClass().cast(object)));
//                        }
//                    }
//                }
//            }));
    }

    private <T extends Comparable<T>, V extends T> BlockState getStateWithProperty(BlockState state, Property<?> property, Comparable<?> value) {
        return state.with((Property<T>) property, (V) value);
    }

    @Override
    public void init() {
        super.init();

        this.x = width / 2;
        this.y = height / 2;

    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, partialTicks);

        drawCenteredString(matrices, this.font, "Hello", 0, this.x, this.y);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
