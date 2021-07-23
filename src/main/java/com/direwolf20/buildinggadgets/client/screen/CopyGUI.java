package com.direwolf20.buildinggadgets.client.screen;

import com.direwolf20.buildinggadgets.client.screen.components.GuiIncrementer;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.items.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.packets.PacketCopyCoords;
import com.direwolf20.buildinggadgets.common.tainted.building.Region;
import com.direwolf20.buildinggadgets.common.util.lang.GuiTranslation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CopyGUI extends Screen {
    private GuiIncrementer startX, startY, startZ, endX, endY, endZ;

    private boolean absoluteCoords = Config.GENERAL.absoluteCoordDefault.get() && Config.GENERAL.allowAbsoluteCoords.get();

    private int x;
    private int y;

    private ItemStack copyPasteTool;
    private BlockPos startPos;
    private BlockPos endPos;

    private List<GuiIncrementer> fields = new ArrayList<>();

    public CopyGUI(ItemStack tool) {
        super(new TextComponent(""));
        this.copyPasteTool = tool;
    }

    @Override
    public void init() {
        super.init();

        this.fields.clear();

        this.x = width / 2;
        this.y = height / 2;

        Region reg = GadgetCopyPaste.getSelectedRegion(copyPasteTool).orElse(Region.singleZero());
        startPos = reg.getMin();
        endPos = reg.getMax();

        int incrementerWidth = GuiIncrementer.WIDTH + (GuiIncrementer.WIDTH / 2);

        fields.add(startX = new GuiIncrementer(x - incrementerWidth - 35, y - 40));
        fields.add(startY = new GuiIncrementer(x - GuiIncrementer.WIDTH / 2, y - 40));
        fields.add(startZ = new GuiIncrementer(x + (GuiIncrementer.WIDTH / 2) + 35, y - 40));
        fields.add(endX = new GuiIncrementer(x - incrementerWidth - 35, y - 15));
        fields.add(endY = new GuiIncrementer(x - GuiIncrementer.WIDTH / 2, y - 15));
        fields.add(endZ = new GuiIncrementer(x + (GuiIncrementer.WIDTH / 2) + 35, y - 15));
        fields.forEach(this::addWidget);

        updateTextFields();

        List<AbstractButton> buttons = new ArrayList<AbstractButton>() {{
            add(new CenteredButton(y + 20, 50, GuiTranslation.SINGLE_CONFIRM.componentTranslation(), (button) -> {
                if (absoluteCoords) {
                    startPos = new BlockPos(startX.getValue(), startY.getValue(), startZ.getValue());
                    endPos = new BlockPos(endX.getValue(), endY.getValue(), endZ.getValue());
                } else {
                    startPos = new BlockPos(startPos.getX() + startX.getValue(), startPos.getY() + startY.getValue(), startPos.getZ() + startZ.getValue());
                    endPos = new BlockPos(startPos.getX() + endX.getValue(), startPos.getY() + endY.getValue(), startPos.getZ() + endZ.getValue());
                }
                PacketHandler.sendToServer(new PacketCopyCoords(startPos, endPos));
            }));
            add(new CenteredButton(y + 20, 50, GuiTranslation.SINGLE_CLOSE.componentTranslation(), (button) -> onClose()));
            add(new CenteredButton(y + 20, 50, GuiTranslation.SINGLE_CLEAR.componentTranslation(), (button) -> {
                PacketHandler.sendToServer(new PacketCopyCoords(BlockPos.ZERO, BlockPos.ZERO));
                onClose();
            }));

            if( Config.GENERAL.allowAbsoluteCoords.get() ) {
                add(new CenteredButton(y + 20, 120, GuiTranslation.COPY_BUTTON_ABSOLUTE.componentTranslation(), (button) -> {
                    coordsModeSwitch();
                    updateTextFields();
                }));
            }
        }};

        CenteredButton.centerButtonList(buttons, x);
        buttons.forEach(this::addWidget);
    }

    private void drawFieldLabel(PoseStack matrices, String name, int x, int y) {
        font.drawShadow(matrices, name, this.x + x, this.y + y, 0xFFFFFF);
    }

    private void coordsModeSwitch() {
        absoluteCoords = !absoluteCoords;
    }

    private void updateTextFields() {
        if (absoluteCoords) {
            BlockPos start = startX.getValue() != 0 ? new BlockPos(startPos.getX() + startX.getValue(), startPos.getY() + startY.getValue(), startPos.getZ() + startZ.getValue()) : startPos;
            BlockPos end = endX.getValue() != 0 ? new BlockPos(startPos.getX() + endX.getValue(), startPos.getY() + endY.getValue(), startPos.getZ() + endZ.getValue()) : endPos;

            startX.setValue(start.getX());
            startY.setValue(start.getY());
            startZ.setValue(start.getZ());
            endX.setValue(end.getX());
            endY.setValue(end.getY());
            endZ.setValue(end.getZ());
        } else {
            startX.setValue(startX.getValue() != 0 ? startX.getValue() - startPos.getX() : 0);
            startY.setValue(startY.getValue() != 0 ? startY.getValue() - startPos.getY() : 0);
            startZ.setValue(startZ.getValue() != 0 ? startZ.getValue() - startPos.getZ() : 0);
            endX.setValue(endX.getValue() != 0 ? endX.getValue() - startPos.getX() : endPos.getX() - startPos.getX());
            endY.setValue(endY.getValue() != 0 ? endY.getValue() - startPos.getY() : endPos.getY() - startPos.getY());
            endZ.setValue(endZ.getValue() != 0 ? endZ.getValue() - startPos.getZ() : endPos.getZ() - startPos.getZ());
        }
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
        drawFieldLabel(matrices, GuiTranslation.FIELD_START.format() + " X", -175, -36);
        drawFieldLabel(matrices, "Y", -45, -36);
        drawFieldLabel(matrices, "Z", 55, -36);
        drawFieldLabel(matrices, GuiTranslation.FIELD_END.format() + " X", 8 - 175, -11);
        drawFieldLabel(matrices, "Y", -45, -11);
        drawFieldLabel(matrices, "Z", 55, -11);

        drawCenteredString(matrices, Minecraft.getInstance().font, I18n.get(GuiTranslation.COPY_LABEL_HEADING.getTranslationKey()), this.x, this.y - 80, 0xFFFFFF);
        drawCenteredString(matrices, Minecraft.getInstance().font, I18n.get(GuiTranslation.COPY_LABEL_SUBHEADING.getTranslationKey()), this.x, this.y - 68, 0xFFFFFF);

        super.render(matrices, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean keyPressed(int mouseX, int mouseY, int __unused) {
        fields.forEach(button -> button.keyPressed(mouseX, mouseY, __unused));
        return super.keyPressed(mouseX, mouseY, __unused);
    }

    @Override
    public boolean charTyped(char charTyped, int __unused) {
        fields.forEach(button -> button.charTyped(charTyped, __unused));
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    static class CenteredButton extends Button {
        CenteredButton(int y, int width, Component text, OnPress onPress) {
            super(0, y, width, 20, text, onPress);
        }

        static void centerButtonList(List<AbstractButton> buttons, int startX) {
            int collectiveWidth = buttons.stream().mapToInt(AbstractButton::getWidth).sum() + (buttons.size() - 1) * 5;

            int nextX = startX - collectiveWidth / 2;
            for(AbstractButton button : buttons) {
                button.x = nextX;
                nextX += button.getWidth() + 5;
            }
        }
    }
}