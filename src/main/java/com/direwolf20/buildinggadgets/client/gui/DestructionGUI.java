/**
 * Parts of this class were adapted from code written by TTerrag for the Chisel mod: https://github.com/Chisel-Team/Chisel
 * Chisel is Open Source and distributed under GNU GPL v2
 */

package com.direwolf20.buildinggadgets.client.gui;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetDestruction;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.packets.PacketDestructionGUI;
import com.direwolf20.buildinggadgets.common.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

public class DestructionGUI extends GuiScreen {

    private GuiTextField left;
    private GuiTextField right;
    private GuiTextField up;
    private GuiTextField down;
    private GuiTextField depth;

    private int guiLeft = 15;
    private int guiTop = 50;

    private ItemStack destructionTool;

    private static final ResourceLocation background = new ResourceLocation(BuildingGadgets.MODID, "textures/gui/testcontainer.png");

    DestructionGUI(ItemStack tool) {
        super();
        this.destructionTool = tool;
    }

    @Override
    public void initGui() {
        super.initGui();

        left = new GuiTextField(0, this.fontRenderer, this.guiLeft + 80, this.guiTop + 60, 40, this.fontRenderer.FONT_HEIGHT);
        left.setMaxStringLength(50);
        left.setVisible(true);

        right = new GuiTextField(1, this.fontRenderer, this.guiLeft + 320, this.guiTop + 60, 40, this.fontRenderer.FONT_HEIGHT);
        right.setMaxStringLength(50);
        right.setVisible(true);

        up = new GuiTextField(2, this.fontRenderer, this.guiLeft + 200, this.guiTop + 30, 40, this.fontRenderer.FONT_HEIGHT);
        up.setMaxStringLength(50);
        up.setVisible(true);


        down = new GuiTextField(3, this.fontRenderer, this.guiLeft + 200, this.guiTop + 90, 40, this.fontRenderer.FONT_HEIGHT);
        down.setMaxStringLength(50);
        down.setVisible(true);

        depth = new GuiTextField(4, this.fontRenderer, this.guiLeft + 200, this.guiTop + 60, 40, this.fontRenderer.FONT_HEIGHT);
        depth.setMaxStringLength(50);
        depth.setVisible(true);

        nullCheckTextBoxes();

        addButton(new GuiButtonAction(guiLeft + 145, guiTop + 125, 40, 20, I18n.format(GuiMod.getLangKeyButton("destruction", "confirm")), () -> {
            nullCheckTextBoxes();
            if (sizeCheckBoxes()) {
                PacketHandler.sendToServer(new PacketDestructionGUI(Integer.parseInt(left.getText()), Integer.parseInt(right.getText()), Integer.parseInt(up.getText()), Integer.parseInt(down.getText()), Integer.parseInt(depth.getText())));
                mc.displayGuiScreen(null);
            } else {
                Minecraft.getInstance().player.sendStatusMessage(new TextComponentString(TextFormatting.RED + new TextComponentTranslation("message.gadget.destroysizeerror").getUnformattedComponentText()), true);
            }
        }));
        addButton(new GuiButtonAction(guiLeft + 245, guiTop + 125, 40, 20, I18n.format(GuiMod.getLangKeyButton("destruction", "cancel")), () -> close()));
        addButton(new DireButton(guiLeft + 65, guiTop + 59, 10, 10, "-", () -> fieldChange(left, -1)));
        addButton(new DireButton(guiLeft + 125, guiTop + 59, 10, 10, "+", () -> fieldChange(left, 1)));
        addButton(new DireButton(guiLeft + 305, guiTop + 59, 10, 10, "-", () -> fieldChange(right, -1)));
        addButton(new DireButton(guiLeft + 365, guiTop + 59, 10, 10, "+", () -> fieldChange(right, 1)));
        addButton(new DireButton(guiLeft + 185, guiTop + 29, 10, 10, "-", () -> fieldChange(up, -1)));
        addButton(new DireButton(guiLeft + 245, guiTop + 29, 10, 10, "+", () -> fieldChange(up, 1)));
        addButton(new DireButton(guiLeft + 185, guiTop + 89, 10, 10, "-", () -> fieldChange(down, -1)));
        addButton(new DireButton(guiLeft + 245, guiTop + 89, 10, 10, "+", () -> fieldChange(down, 1)));
        addButton(new DireButton(guiLeft + 185, guiTop + 59, 10, 10, "-", () -> fieldChange(depth, -1)));
        addButton(new DireButton(guiLeft + 245, guiTop + 59, 10, 10, "+", () -> fieldChange(depth, 1)));
    }

    private void fieldChange(GuiTextField textField, int amount) {
        nullCheckTextBoxes();
        if (GuiScreen.isShiftKeyDown()) amount = amount * 10;
        try {
            int i = Integer.valueOf(textField.getText());
            i = i + amount;
            if (i < 0) i = 0;
            if (i > 16) i = 16;
            textField.setText(String.valueOf(i));
        } catch (NumberFormatException t) {
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        mc.getTextureManager().bindTexture(background);

        this.left.drawTextField(mouseX, mouseY, partialTicks);
        this.right.drawTextField(mouseX, mouseY, partialTicks);
        this.up.drawTextField(mouseX, mouseY, partialTicks);
        this.down.drawTextField(mouseX, mouseY, partialTicks);
        this.depth.drawTextField(mouseX, mouseY, partialTicks);

        drawFieldLable("left", 35, 60);
        drawFieldLable("right", 278, 60);
        drawFieldLable("up", 170, 30);
        drawFieldLable("down", 158, 90);
        drawFieldLable("depth", 155, 60);

        super.render(mouseX, mouseY, partialTicks);
    }

    private void drawFieldLable(String name, int x, int y) {
        fontRenderer.drawStringWithShadow(I18n.format(GuiMod.getLangKeyField("destruction", name)), this.guiLeft + x, this.guiTop + y, 0xFFFFFF);
    }

    private void nullCheckTextBoxes() {
        if (left.getText().isEmpty()) {
            left.setText(String.valueOf(GadgetDestruction.getToolValue(destructionTool, "left")));
        }
        if (right.getText().isEmpty()) {
            right.setText(String.valueOf(GadgetDestruction.getToolValue(destructionTool, "right")));
        }
        if (up.getText().isEmpty()) {
            up.setText(String.valueOf(GadgetDestruction.getToolValue(destructionTool, "up")));
        }
        if (down.getText().isEmpty()) {
            down.setText(String.valueOf(GadgetDestruction.getToolValue(destructionTool, "down")));
        }
        if (depth.getText().isEmpty()) {
            depth.setText(String.valueOf(GadgetDestruction.getToolValue(destructionTool, "depth")));
        }
    }

    private boolean sizeCheckBoxes() {
        if( !Utils.isStringNumeric(left.getText()) || !Utils.isStringNumeric(right.getText()) || !Utils.isStringNumeric(up.getText()) || !Utils.isStringNumeric(down.getText()) || !Utils.isStringNumeric(depth.getText()) )
            return false;

        if (Integer.parseInt(left.getText()) + Integer.parseInt(right.getText()) > 16) return false;
        if (Integer.parseInt(up.getText()) + Integer.parseInt(down.getText()) > 16) return false;
        if (Integer.parseInt(depth.getText()) > 16) return false;
        if (Integer.parseInt(left.getText()) < 0) return false;
        if (Integer.parseInt(right.getText()) < 0) return false;
        if (Integer.parseInt(up.getText()) < 0) return false;
        if (Integer.parseInt(down.getText()) < 0) return false;
        if (Integer.parseInt(depth.getText()) < 0) return false;

        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (mouseButton == 1) {
            if (this.left.mouseClicked(mouseX, mouseY, 0)) {
                left.setText("");
            } else if (this.right.mouseClicked(mouseX, mouseY, 0)) {
                right.setText("");
            } else if (this.up.mouseClicked(mouseX, mouseY, 0)) {
                up.setText("");
            } else if (this.down.mouseClicked(mouseX, mouseY, 0)) {
                down.setText("");
            } else if (this.depth.mouseClicked(mouseX, mouseY, 0)) {
                depth.setText("");
            } else {
                super.mouseClicked(mouseX, mouseY, mouseButton);
            }
        } else {
            if (this.left.mouseClicked(mouseX, mouseY, mouseButton)) {
                left.setFocused(true);
            } else if (this.right.mouseClicked(mouseX, mouseY, mouseButton)) {
                right.setFocused(true);
            } else if (this.up.mouseClicked(mouseX, mouseY, mouseButton)) {
                up.setFocused(true);
            } else if (this.down.mouseClicked(mouseX, mouseY, mouseButton)) {
                down.setFocused(true);
            } else if (this.depth.mouseClicked(mouseX, mouseY, mouseButton)) {
                depth.setFocused(true);
            } else {
                super.mouseClicked(mouseX, mouseY, mouseButton);
            }
        }

        return true;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
