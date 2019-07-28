/**
 * Parts of this class were adapted from code written by TTerrag for the Chisel mod: https://github.com/Chisel-Team/Chisel
 * Chisel is Open Source and distributed under GNU GPL v2
 */

package com.direwolf20.buildinggadgets.client.gui;

import com.direwolf20.buildinggadgets.client.gui.components.GuiIncrementer;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.packets.PacketCopyCoords;
import com.direwolf20.buildinggadgets.common.registry.objects.BGItems;
import com.direwolf20.buildinggadgets.common.util.lang.GuiTranslation;
import com.direwolf20.buildinggadgets.common.util.lang.ITranslationProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.List;

public class CopyGUI extends Screen {
    private GuiIncrementer startX, startY, startZ, endX, endY, endZ;

    private boolean absoluteCoords = Config.GENERAL.absoluteCoordDefault.get();

    private int x;
    private int y;

    private ItemStack copyPasteTool;
    private BlockPos startPos;
    private BlockPos endPos;

    private List<GuiIncrementer> fields = new ArrayList<>();

    public CopyGUI(ItemStack tool) {
        super(new StringTextComponent(""));
        this.copyPasteTool = tool;
    }

    @Override
    public void init() {
        super.init();

        this.fields.clear();

        this.x = width / 2;
        this.y = height / 2;

        startPos = BGItems.gadgetCopyPaste.getStartPos(copyPasteTool);
        endPos = BGItems.gadgetCopyPaste.getEndPos(copyPasteTool);

        if (startPos == null) startPos = new BlockPos(0, 0, 0);
        if (endPos == null) endPos = new BlockPos(0, 0, 0);

        int incrementerWidth = GuiIncrementer.WIDTH + (GuiIncrementer.WIDTH / 2);

        fields.add(startX = new GuiIncrementer(x - incrementerWidth - 35, y - 40));
        fields.add(startY = new GuiIncrementer(x - GuiIncrementer.WIDTH / 2, y - 40));
        fields.add(startZ = new GuiIncrementer(x + (GuiIncrementer.WIDTH / 2) + 35, y - 40));
        fields.add(endX = new GuiIncrementer(x - incrementerWidth - 35, y - 15));
        fields.add(endY = new GuiIncrementer(x - GuiIncrementer.WIDTH / 2, y - 15));
        fields.add(endZ = new GuiIncrementer(x + (GuiIncrementer.WIDTH / 2) + 35, y - 15));
        fields.forEach(this::addButton);

        updateTextFields();

        List<AbstractButton> buttons = new ArrayList<AbstractButton>() {{
            add(new CenteredButton(y + 20, 50, GuiTranslation.SINGLE_CONFIRM, (button) -> {
                if (absoluteCoords) {
                    startPos = new BlockPos(startX.getValue(), startY.getValue(), startZ.getValue());
                    endPos = new BlockPos(endX.getValue(), endY.getValue(), endZ.getValue());
                } else {
                    startPos = new BlockPos(startPos.getX() + startX.getValue(), startPos.getY() + startY.getValue(), startPos.getZ() + startZ.getValue());
                    endPos = new BlockPos(startPos.getX() + endX.getValue(), startPos.getY() + endY.getValue(), startPos.getZ() + endZ.getValue());
                }
                PacketHandler.sendToServer(new PacketCopyCoords(startPos, endPos));
            }));
            add(new CenteredButton(y + 20, 50, GuiTranslation.SINGLE_CLOSE, (button) -> onClose()));
            add(new CenteredButton(y + 20, 50, GuiTranslation.SINGLE_CLEAR, (button) -> {
                PacketHandler.sendToServer(new PacketCopyCoords(BlockPos.ZERO, BlockPos.ZERO));
                onClose();
            }));
            add(new CenteredButton(y + 20, 120, GuiTranslation.COPY_BUTTON_ABSOLUTE, (button) -> {
                coordsModeSwitch();
                updateTextFields();
            }));
        }};

        CenteredButton.centerButtonList(buttons, x);
        buttons.forEach(this::addButton);
    }

    private void drawFieldLabel(String name, int x, int y) {
        font.drawStringWithShadow(name, this.x + x, this.y + y, 0xFFFFFF);
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
    public void render(int mouseX, int mouseY, float partialTicks) {
        drawFieldLabel("Start X", 0 - 175, -36);
        drawFieldLabel("Y", -45, -36);
        drawFieldLabel("Z", 55, -36);
        drawFieldLabel("End X", 8 - 175, -11);
        drawFieldLabel("Y", -45, -11);
        drawFieldLabel("Z", 55, -11);

        drawCenteredString(Minecraft.getInstance().fontRenderer, I18n.format(GuiTranslation.COPY_LABEL_HEADING.getTranslationKey()), this.x, this.y - 80, 0xFFFFFF);
        drawCenteredString(Minecraft.getInstance().fontRenderer, I18n.format(GuiTranslation.COPY_LABEL_SUBHEADING.getTranslationKey()), this.x, this.y - 68, 0xFFFFFF);

        super.render(mouseX, mouseY, partialTicks);
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

    public static class CenteredButton extends Button {
        public CenteredButton(int y, int width, ITranslationProvider text, IPressable onPress) {
            super(0, y, width, 20, I18n.format(text.getTranslationKey()), onPress);
        }

        protected static void centerButtonList(List<AbstractButton> buttons, int startX) {
            int collectiveWidth = buttons.stream().mapToInt(AbstractButton::getWidth).sum() + (buttons.size() - 1) * 5;

            int nextX = startX - collectiveWidth / 2;
            for(AbstractButton button : buttons) {
                button.x = nextX;
                nextX += button.getWidth() + 5;
            }
        }
    }
}